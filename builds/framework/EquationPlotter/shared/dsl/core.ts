/**
 * Nexus Framework — DSL core.
 *
 * Defines the abstract {@link Component} base class shared by every builtin
 * widget in `components.ts`, plus the common style, callback, and constructor
 * option types. Nothing in this file renders anything by itself — components
 * form a tree that the native runtime walks each frame, emitting the
 * corresponding Dear ImGui calls and routing events back to the callbacks.
 */

import { ComponentTag } from "./tags";

/** An RGBA color. Accepts `#rrggbb`, `#rrggbbaa`, or a `[r, g, b, a]` tuple in 0–1 range. */
export type Color = string | [number, number, number, number];

/** A 2D size or position. `-1` means "fill available space" where supported. */
export type Vec2 = [number, number];

/**
 * Style properties common to all components. These map onto ImGui style
 * pushes (`PushStyleVar` / `PushStyleColor`) and item sizing calls that the
 * runtime scopes around the component while it is rendered.
 */
export interface StyleProps {
  /** Preferred size in pixels; `-1` on an axis fills the available space. */
  size?: Vec2;
  /** Item width for value widgets — `ImGui::SetNextItemWidth`. */
  width?: number;
  /** Foreground/text color — `ImGuiCol_Text`. */
  color?: Color;
  /** Background color — widget-appropriate `ImGuiCol_*` (e.g. `FrameBg`, `Button`). */
  background?: Color;
  /** Corner rounding in pixels — `ImGuiStyleVar_FrameRounding`. */
  rounding?: number;
  /** Inner padding — `ImGuiStyleVar_FramePadding` / `WindowPadding`. */
  padding?: Vec2;
  /** Spacing applied after this item — `ImGuiStyleVar_ItemSpacing`. */
  spacing?: Vec2;
  /** Font name registered with the runtime's font atlas. */
  font?: string;
  /** Skip rendering entirely when `false` (element also loses its layout slot). */
  visible?: boolean;
  /** Render greyed-out and ignore input — `ImGui::BeginDisabled`. */
  disabled?: boolean;
}

/**
 * A DSL event callback. The first argument is always the component instance
 * that fired the event; event-specific payloads follow (see each component's
 * callback interface).
 */
export type Callback<Args extends unknown[] = []> = (
  self: Component,
  ...args: Args
) => void;

/**
 * Lifecycle and pointer callbacks every component may receive. Concrete
 * components extend this with their own typed events (e.g. `Button.onClick`
 * carries no payload, `Slider.onChange` carries the new number).
 */
export interface CommonCallbacks {
  /** Pointer entered the item's bounding box — `ImGui::IsItemHovered`. */
  onHover?: Callback;
  /** Item gained keyboard/nav focus — `ImGui::IsItemFocused`. */
  onFocus?: Callback;
  /** Item lost keyboard/nav focus. */
  onBlur?: Callback;
  /** Called once per frame right before the component is emitted. */
  onRender?: Callback;
  /** Called once per frame after input handling; good for animations/polling. */
  onUpdate?: Callback<[deltaSeconds: number]>;
}

/**
 * Constructor options accepted by every component.
 *
 * @typeParam A - the component's attribute shape.
 * @typeParam C - the component's callback shape.
 */
export interface ComponentOptions<
  A extends object = Record<never, never>,
  C extends CommonCallbacks = CommonCallbacks,
> {
  /** Stable identifier; doubles as the ImGui ID scope (`###id`). Auto-generated if omitted. */
  id?: string;
  /** Component-specific attributes (mirrors the XHTML attributes of the tag). */
  attributes?: A;
  /** Event / lifecycle callbacks. */
  callbacks?: C;
  /** Visual style overrides. */
  style?: StyleProps;
  /** Initial children, appended in order. */
  children?: Component[];
}

let autoIdCounter = 0;

/**
 * Abstract base of every builtin DSL component.
 *
 * A `Component` owns:
 * - a {@link tag} identifying its XHTML element and native widget,
 * - a stable {@link id} used for the ImGui ID stack and event routing,
 * - a parent/children tree (see {@link add} / {@link remove}),
 * - {@link style} props and typed {@link attributes},
 * - typed {@link callbacks}, also attachable fluently via {@link on}.
 *
 * Subclasses fix the `A`/`C` type parameters to expose exactly the attributes
 * and events their ImGui counterpart supports.
 */
export abstract class Component<
  A extends object = Record<never, never>,
  C extends CommonCallbacks = CommonCallbacks,
> {
  /** XHTML DSL tag this component serializes to. */
  abstract readonly tag: ComponentTag;

  /** Unique, stable identifier (ImGui ID scope / event routing key). */
  readonly id: string;

  /** Parent in the UI tree, or `null` for roots. Managed by {@link add}/{@link remove}. */
  parent: Component | null = null;

  /** Ordered children; render order matches array order. */
  readonly children: Component[] = [];

  /** Visual style overrides applied while this component renders. */
  style: StyleProps;

  /** Component-specific attributes (mirrors the tag's XHTML attributes). */
  attributes: A;

  /** Registered event and lifecycle callbacks. */
  callbacks: C;

  constructor(options: ComponentOptions<A, C> = {}) {
    this.id = options.id ?? `nxs-${++autoIdCounter}`;
    this.style = options.style ?? {};
    this.attributes = options.attributes ?? ({} as A);
    this.callbacks = options.callbacks ?? ({} as C);
    if (options.children) this.add(...options.children);
  }

  /** Appends children (re-parenting them if needed). Returns `this` for chaining. */
  add(...children: Component[]): this {
    for (const child of children) {
      child.parent?.remove(child);
      child.parent = this as unknown as Component;
      this.children.push(child);
    }
    return this;
  }

  /** Detaches a direct child. Returns `true` if the child was found. */
  remove(child: Component): boolean {
    const index = this.children.indexOf(child);
    if (index < 0) return false;
    this.children.splice(index, 1);
    child.parent = null;
    return true;
  }

  /** Registers (or replaces) a callback fluently: `button.on("onClick", fn)`. */
  on<K extends keyof C>(event: K, fn: NonNullable<C[K]>): this {
    this.callbacks[event] = fn;
    return this;
  }
}
