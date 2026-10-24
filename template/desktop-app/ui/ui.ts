/**
 * ui.ts — controller logic for ui.xhtml.
 *
 * The TS/XHTML layer is the *interface* declaration. It lowers to the same
 * Lua widget calls as scripts/panels.lua; compute stays in C++
 * (nxs.desktop.compute / nxs.desktop.training) and higher-level logic in
 * python/helpers.py.
 *
 * Bindings:
 *   model.greeting            ← AppController.refresh()
 *   training.hours            ← TrainingEngine::hours()      (5 digits, %05d)
 *   training.generation       ← TrainingEngine::generation()
 *   training.best             ← TrainingEngine::best()
 *   training.status           ← TrainingEngine::status()
 */
import { NexusPage, native, state } from "@nexus/runtime";

export class AppPage extends NexusPage {
  greeting = native<string>("model.greeting");

  trainingHours = native<number>("training.hours");
  trainingGeneration = native<number>("training.generation");
  trainingBest = native<number>("training.best");
  trainingStatus = native<string>("training.status");

  trainingAction = state<string>("Start");

  toggleTraining(): void {
    this.invoke("nxs.training_toggle");
  }

  resetTraining(): void {
    this.invoke("nxs.training_reset");
  }

  addSine(): void {
    this.invoke("nxs.add_expression", "sin(x)");
  }

  addDamped(): void {
    this.invoke("nxs.add_expression", "exp(-0.1*x)*sin(2*x)");
  }
}
