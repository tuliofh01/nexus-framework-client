/**
 * ui.ts — controller logic for ui.xhtml, written in the web-developer-
 * familiar TypeScript layer of the Nexus DSL.
 *
 * This file is illustrative boilerplate: the Nexus toolchain compiles it
 * (together with ui.xhtml) into Lua panel definitions executed through
 * sol2 — it is never run by Node or a browser. The `@nexus/runtime`
 * import below resolves against the framework's ambient type stubs.
 */
import { NexusPage, native, state } from "@nexus/runtime";

/** Shape of one active curve as exposed by the C++ model (PlotSeries). */
interface Curve {
  id: string;
  label: string;
  visible: boolean;
  color: [number, number, number, number];
  xs: Float64Array; // zero-copy view over the native sample buffer
  ys: Float64Array;
}

export class PlotterPage extends NexusPage {
  // `state()` members are two-way bound to the XHTML `bind=` attributes.
  pendingFunction = state<string>("sine");
  sampleCount = state<number>(512);
  logScaleY = state<boolean>(false);
  showGrid = state<boolean>(true);

  // `native()` members are read-only projections of C++ model state;
  // they map to FunctionRegistry::available() / ::active().
  availableFunctions = native<string[]>("registry.available");
  activeCurves = native<Curve[]>("registry.active");

  /** <button label="Add"> handler — mirrors PlotController::addFunction. */
  addPending(): void {
    this.invoke("nxs.add_function", this.pendingFunction.value);
  }

  /** Per-row remove button. */
  remove(id: string): void {
    this.invoke("nxs.remove_function", id);
  }

  /** Slider on-change — re-samples every active curve through Python. */
  resample(): void {
    this.invoke("nxs.set_samples", this.sampleCount.value);
  }
}
