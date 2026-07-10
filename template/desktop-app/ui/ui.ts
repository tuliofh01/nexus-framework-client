/**
 * ui.ts — controller logic for ui.xhtml.
 *
 * Compiled with ui.xhtml into Lua panel definitions executed through sol2.
 */
import { NexusPage, native, state } from "@nexus/runtime";

export class AppPage extends NexusPage {
  counter = state<number>(0);
  greeting = native<string>("model.greeting");

  increment(): void {
    this.invoke("nxs.increment");
  }

  decrement(): void {
    this.invoke("nxs.decrement");
  }

  reset(): void {
    this.invoke("nxs.reset");
  }
}
