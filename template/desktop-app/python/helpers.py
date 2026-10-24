"""Higher-level logic helpers for {{projectName}}.

Architecture split
------------------
    C++   (nxs.desktop.compute / nxs.desktop.training)
          numerical kernels: MLP forward, fitness, mutation, crossover,
          Wilson intervals, series statistics, NXSW .bin checkpoints.
    Python (this module)
          higher-level concerns: acquiring/normalising market data, deciding
          the study schema, and persisting/aggregating rows in SQLite.
    Lua    widgets and panels (scripts/panels.lua) via the `nxs.*` API.
    TS/XHTML  declarative interface (ui/ui.xhtml + ui/ui.ts).

This module therefore contains no training math — only data preparation and
the SQLite "study mode" registry.
"""

from __future__ import annotations

import json
import os
import sqlite3
import struct
from datetime import datetime, timezone

import numpy as np

MODELS_DIR = "models"
DATA_CANDIDATES = (
    "data/market.csv",
    "data/btcusdt_1d.csv",
    "market.csv",
)
STUDY_DB = os.path.join("data", "study.db")
DATASET_PATH = os.path.join(MODELS_DIR, "dataset.bin")

_RETURNS: np.ndarray | None = None


# --------------------------------------------------------------------------- #
# Data acquisition (higher-level policy)
# --------------------------------------------------------------------------- #
def _load_returns() -> np.ndarray:
    """Cached log-returns (see :func:`_load_returns_uncached`)."""
    global _RETURNS
    if _RETURNS is None:
        _RETURNS = _load_returns_uncached()
    return _RETURNS


def _load_returns_uncached() -> np.ndarray:
    """Load log-returns from a CSV if present, else synthesize a regime series."""
    for path in DATA_CANDIDATES:
        if not os.path.exists(path):
            continue
        try:
            raw = np.genfromtxt(path, delimiter=",", names=True)
            names = [n.lower() for n in raw.dtype.names or ()]
            for key in ("close", "price", "adj_close", "adjclose"):
                if key in names:
                    close = np.asarray(raw[key], dtype=np.float64)
                    close = close[np.isfinite(close) & (close > 0)]
                    if close.size > 128:
                        return np.diff(np.log(close))
        except Exception:
            continue

    # Deterministic fallback so a fresh app still trains out of the box.
    rng = np.random.default_rng(42)
    n = 2048
    regime = np.zeros(n, dtype=int)
    for i in range(1, n):
        regime[i] = regime[i - 1] ^ (rng.random() < 0.02)
    vol = np.where(regime == 0, 0.010, 0.030)
    drift = np.where(regime == 0, 0.0004, -0.0012)
    return drift + vol * rng.standard_normal(n)


def prepare_dataset(path: str = DATASET_PATH, window: int = 8) -> str:
    """Build the NXSD feature dataset consumed by the C++ training engine.

    Layout (little-endian):
        magic  : b"NXSD"
        n      : uint32   rows
        d      : uint32   features per row
        x      : float32  n*d, row-major, z-scored per column
        y      : float32  n     (next log-return)
    """
    returns = _load_returns().astype(np.float64)
    if returns.size < window + 64:
        returns = np.concatenate([returns, np.zeros(window + 64)])

    rows = returns.size - window - 1
    x = np.empty((rows, window), dtype=np.float32)
    y = np.empty(rows, dtype=np.float32)
    for i in range(rows):
        x[i] = returns[i:i + window]
        y[i] = returns[i + window]

    mean = x.mean(axis=0, keepdims=True)
    sd = x.std(axis=0, keepdims=True) + 1e-9
    x = (x - mean) / sd

    os.makedirs(os.path.dirname(path) or ".", exist_ok=True)
    with open(path, "wb") as fh:
        fh.write(b"NXSD")
        fh.write(struct.pack("<II", rows, window))
        fh.write(x.astype(np.float32).tobytes(order="C"))
        fh.write(y.astype(np.float32).tobytes(order="C"))
    return path


def _asset_label() -> str:
    for path in DATA_CANDIDATES:
        if os.path.exists(path):
            return os.path.splitext(os.path.basename(path))[0].upper()
    return "SYNTHETIC"


# --------------------------------------------------------------------------- #
# Study mode: SQLite registry (persistence + aggregation only)
# --------------------------------------------------------------------------- #
# C++ computes the statistics and the Wilson interval; Python owns the schema
# and the durable record so estimates improve across sessions.
_STUDY_SCHEMA = """
CREATE TABLE IF NOT EXISTS study_runs (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    ts             TEXT    NOT NULL,
    asset          TEXT    NOT NULL,
    timeframe      TEXT    NOT NULL,
    n_samples      INTEGER NOT NULL,
    mean_return    REAL,
    vol            REAL,
    sharpe         REAL,
    max_drawdown   REAL,
    hit_rate       REAL,
    skew           REAL,
    kurtosis       REAL,
    generation     INTEGER,
    best_fitness   REAL,
    success_prob   REAL,
    success_lo     REAL,
    success_hi     REAL,
    method         TEXT,
    features_json  TEXT
);
CREATE INDEX IF NOT EXISTS idx_study_asset ON study_runs(asset);
CREATE INDEX IF NOT EXISTS idx_study_ts ON study_runs(ts);
"""


def _ensure_study_db() -> None:
    os.makedirs(os.path.dirname(STUDY_DB), exist_ok=True)
    con = sqlite3.connect(STUDY_DB)
    try:
        con.executescript(_STUDY_SCHEMA)
        con.commit()
    finally:
        con.close()


def study_persist(asset: str, timeframe: str, n: int, mean: float, vol: float,
                  sharpe: float, max_dd: float, hit_rate: float, skew: float,
                  kurtosis: float, generation: int, best: float,
                  success_prob: float, lo: float, hi: float) -> bool:
    """Persist one C++-computed study row."""
    _ensure_study_db()
    features = {
        "n": int(n), "mean": mean, "vol": vol, "sharpe": sharpe,
        "max_dd": max_dd, "hit_rate": hit_rate, "skew": skew,
        "kurtosis": kurtosis,
    }
    ts = datetime.now(timezone.utc).isoformat(timespec="seconds")
    con = sqlite3.connect(STUDY_DB)
    try:
        con.execute(
            """INSERT INTO study_runs
               (ts, asset, timeframe, n_samples, mean_return, vol, sharpe,
                max_drawdown, hit_rate, skew, kurtosis, generation,
                best_fitness, success_prob, success_lo, success_hi, method,
                features_json)
               VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
            (ts, asset, timeframe, int(n), mean, vol, sharpe, max_dd,
             hit_rate, skew, kurtosis, int(generation), best, success_prob,
             lo, hi, "wilson-cpp", json.dumps(features, sort_keys=True)),
        )
        con.commit()
        return True
    except Exception:
        return False
    finally:
        con.close()


def study_estimate(asset: str | None = None, limit: int = 200) -> dict:
    """Aggregate recent study rows into a sample-weighted success estimate."""
    _ensure_study_db()
    con = sqlite3.connect(STUDY_DB)
    try:
        if asset:
            rows = con.execute(
                "SELECT success_prob, success_lo, success_hi, n_samples "
                "FROM study_runs WHERE asset=? ORDER BY id DESC LIMIT ?",
                (asset, limit)).fetchall()
        else:
            rows = con.execute(
                "SELECT success_prob, success_lo, success_hi, n_samples "
                "FROM study_runs ORDER BY id DESC LIMIT ?", (limit,)).fetchall()
    finally:
        con.close()

    if not rows:
        return {"asset": asset or "*", "samples": 0, "p_success": 0.5,
                "ci95": [0.0, 1.0], "method": "prior", "runs": 0}

    weights = np.array([max(1, r[3]) for r in rows], dtype=np.float64)
    probs = np.array([r[0] for r in rows], dtype=np.float64)
    los = np.array([r[1] for r in rows], dtype=np.float64)
    his = np.array([r[2] for r in rows], dtype=np.float64)
    w = weights / weights.sum()
    return {
        "asset": asset or "*",
        "samples": int(weights.sum()),
        "p_success": float((probs * w).sum()),
        "ci95": [float((los * w).sum()), float((his * w).sum())],
        "method": "weighted-wilson-cpp",
        "runs": len(rows),
    }


def study_snapshot(asset: str | None = None) -> str:
    """JSON snapshot for widgets/tests."""
    return json.dumps(study_estimate(asset), sort_keys=True)


def greeting(name: str) -> str:
    return f"Hello, {name}!"
