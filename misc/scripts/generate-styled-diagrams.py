#!/usr/bin/env python3
"""Backward-compat shim — use misc/scripts/generate-diagrams/generate-styled-diagrams.py"""
from pathlib import Path
import runpy

target = Path(__file__).resolve().parent / "generate-diagrams" / "generate-styled-diagrams.py"
runpy.run_path(str(target), run_name="__main__")
