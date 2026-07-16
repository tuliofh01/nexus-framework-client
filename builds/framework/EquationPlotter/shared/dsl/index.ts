/**
 * Nexus Framework — DSL public entry point.
 *
 * Re-exports the tag enum, the abstract `Component` base and its supporting
 * types, and every builtin component class. Import from here:
 *
 * ```ts
 * import { Window, Button, ComponentTag } from "../shared/dsl";
 * ```
 */
export { ComponentTag } from "./tags";
export * from "./core";
export * from "./components";
