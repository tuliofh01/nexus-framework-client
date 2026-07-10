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
