/**
 * Nexus Framework — builtin DSL components.
 *
 * One concrete class per builtin XHTML tag (see `tags.ts`), all deriving from
 * the abstract {@link Component} base in `core.ts`. Each class fixes its
 * attribute and callback types so editors can autocomplete exactly what the
 * underlying Dear ImGui / ImPlot / imnodes widget supports.
 *
 * @example
 * ```ts
 * const win = new Window({
 *   attributes: { title: "Hello Nexus" },
 *   children: [
 *     new Text({ attributes: { value: "Counter demo" } }),
 *     new Button({ attributes: { label: "Click me" } })
 *       .on("onClick", () => console.log("clicked!")),
 *   ],
 * });
 * ```
 */

import { ComponentTag } from "./tags";
import {
  Callback,
  Color,
  CommonCallbacks,
  Component,
  Vec2,
} from "./core";

/* ------------------------------------------------------------------ */
/* Top-level containers                                               */
/* ------------------------------------------------------------------ */

export interface WindowAttributes {
  /** Title bar text (also the default ImGui window ID). */
  title: string;
  /** Whether the window shows a close button; bound two-way. */
  closable?: boolean;
  /** Initial position in screen pixels. */
  position?: Vec2;
  /** Prevent user resizing — `ImGuiWindowFlags_NoResize`. */
  fixedSize?: boolean;
  /** Hide the title bar — `ImGuiWindowFlags_NoTitleBar`. */
  noTitleBar?: boolean;
  /** Auto-fit the window to its content each frame. */
  autoResize?: boolean;
}

export interface WindowCallbacks extends CommonCallbacks {
  /** Fired when the user clicks the window's close button. */
  onClose?: Callback;
}

/**
 * Top-level application window. ImGui: `Begin`/`End`.
 * XHTML: `<window title="..." closable="true">…</window>`.
 */
export class Window extends Component<WindowAttributes, WindowCallbacks> {
  static readonly tag = ComponentTag.Window;
  readonly tag = ComponentTag.Window;
}

export interface PanelAttributes {
  /** Draw a 1px border around the region. */
  border?: boolean;
  /** Enable horizontal scrolling (vertical is always available). */
  horizontalScroll?: boolean;
}

/**
 * Scrollable child region inside a window. ImGui: `BeginChild`/`EndChild`.
 * XHTML: `<panel border="true">…</panel>`. Size comes from `style.size`.
 */
export class Panel extends Component<PanelAttributes> {
  static readonly tag = ComponentTag.Panel;
  readonly tag = ComponentTag.Panel;
}

/* ------------------------------------------------------------------ */
/* Basic content                                                      */
/* ------------------------------------------------------------------ */

export interface TextAttributes {
  /** The string to display. */
  value: string;
  /** Wrap at the available width — `TextWrapped`. */
  wrap?: boolean;
  /** Render in the muted "disabled" color — `TextDisabled`. */
  muted?: boolean;
}

/**
 * Static text label. ImGui: `TextUnformatted` / `TextWrapped`.
 * XHTML: `<text value="Hello" wrap="true"/>` (or tag body as the value).
 */
export class Text extends Component<TextAttributes> {
  static readonly tag = ComponentTag.Text;
  readonly tag = ComponentTag.Text;
}

export interface ButtonAttributes {
  /** Button caption. */
  label: string;
  /** Render as a compact "small" button — `SmallButton`. */
  small?: boolean;
}

export interface ButtonCallbacks extends CommonCallbacks {
  /** Fired on click release, matching ImGui's `Button()` return. */
  onClick?: Callback;
}

/**
 * Push button. ImGui: `Button` / `SmallButton`.
 * XHTML: `<button label="OK"/>`.
 */
export class Button extends Component<ButtonAttributes, ButtonCallbacks> {
  static readonly tag = ComponentTag.Button;
  readonly tag = ComponentTag.Button;
}

export interface ImageAttributes {
  /** Asset path or texture key registered with the runtime. */
  src: string;
  /** Optional tint multiplier. */
  tint?: Color;
}

/**
 * Textured image. ImGui: `Image`. Size comes from `style.size`
 * (defaults to the texture's native resolution).
 * XHTML: `<image src="assets/logo.png"/>`.
 */
export class Image extends Component<ImageAttributes> {
  static readonly tag = ComponentTag.Image;
  readonly tag = ComponentTag.Image;
}

export interface ProgressBarAttributes {
  /** Progress in the 0–1 range. */
  value: number;
  /** Text overlay; defaults to a percentage. */
  overlay?: string;
}

/**
 * Determinate progress bar. ImGui: `ProgressBar`.
 * XHTML: `<progress-bar value="0.4" overlay="40%"/>`.
 */
export class ProgressBar extends Component<ProgressBarAttributes> {
  static readonly tag = ComponentTag.ProgressBar;
  readonly tag = ComponentTag.ProgressBar;
}

/**
 * Horizontal rule. ImGui: `Separator`. XHTML: `<separator/>`.
 * Takes no attributes.
 */
export class Separator extends Component {
  static readonly tag = ComponentTag.Separator;
  readonly tag = ComponentTag.Separator;
}

export interface SpacerAttributes {
  /** Empty space to reserve; defaults to one item-spacing unit. */
  size?: Vec2;
}

/**
 * Fixed empty space. ImGui: `Dummy` (sized) / `Spacing` (default).
 * XHTML: `<spacer size="0,12"/>`.
 */
export class Spacer extends Component<SpacerAttributes> {
  static readonly tag = ComponentTag.Spacer;
  readonly tag = ComponentTag.Spacer;
}

/* ------------------------------------------------------------------ */
/* Value inputs                                                       */
/* ------------------------------------------------------------------ */

/** Callbacks for widgets that edit a single value of type `T`. */
export interface ValueCallbacks<T> extends CommonCallbacks {
  /** Fired every frame the value changes while editing. */
  onChange?: Callback<[value: T]>;
  /** Fired once when editing is committed — `IsItemDeactivatedAfterEdit`. */
  onCommit?: Callback<[value: T]>;
}

export interface CheckboxAttributes {
  /** Label rendered next to the box. */
  label: string;
  /** Current checked state (two-way bound). */
  checked?: boolean;
}

/**
 * Boolean checkbox. ImGui: `Checkbox`.
 * XHTML: `<checkbox label="Enable" checked="true"/>`.
 */
export class Checkbox extends Component<
  CheckboxAttributes,
  ValueCallbacks<boolean>
> {
  static readonly tag = ComponentTag.Checkbox;
  readonly tag = ComponentTag.Checkbox;
}

export interface RadioGroupAttributes {
  /** Value of the currently selected child `RadioButton`. */
  value?: string;
  /** Lay options out horizontally instead of stacked. */
  horizontal?: boolean;
}

/**
 * Exclusive-choice container for {@link RadioButton} children.
 * ImGui: a shared-state series of `RadioButton` calls.
 * XHTML: `<radio-group value="b"><radio value="a" label="A"/>…</radio-group>`.
 */
export class RadioGroup extends Component<
  RadioGroupAttributes,
  ValueCallbacks<string>
> {
  static readonly tag = ComponentTag.RadioGroup;
  readonly tag = ComponentTag.RadioGroup;
}

export interface RadioButtonAttributes {
  /** Value reported to the parent group when selected. */
  value: string;
  /** Visible caption. */
  label: string;
}

/**
 * Single option inside a {@link RadioGroup}. ImGui: `RadioButton`.
 * XHTML: `<radio value="a" label="Option A"/>`.
 */
export class RadioButton extends Component<RadioButtonAttributes> {
  static readonly tag = ComponentTag.RadioButton;
  readonly tag = ComponentTag.RadioButton;
}

export interface SliderAttributes {
  /** Label rendered next to the slider. */
  label: string;
  /** `"int"` → `SliderInt`, `"float"` → `SliderFloat`. Default `"float"`. */
  type?: "int" | "float";
  /** Current value (two-way bound). */
  value?: number;
  min: number;
  max: number;
  /** printf-style display format, e.g. `"%.2f"`. */
  format?: string;
}

/**
 * Numeric slider. ImGui: `SliderInt` / `SliderFloat`.
 * XHTML: `<slider label="Volume" type="float" min="0" max="1"/>`.
 */
export class Slider extends Component<SliderAttributes, ValueCallbacks<number>> {
  static readonly tag = ComponentTag.Slider;
  readonly tag = ComponentTag.Slider;
}

export interface DropdownAttributes {
  /** Label rendered next to the combo. */
  label: string;
  /** Selectable options. */
  options: string[];
  /** Index of the selected option (two-way bound). */
  selected?: number;
}

/** Callbacks for list-style selectors reporting `(index, option)`. */
export interface SelectCallbacks extends CommonCallbacks {
  /** Fired when the selection changes. */
  onChange?: Callback<[index: number, option: string]>;
}

/**
 * Single-select dropdown. ImGui: `BeginCombo` + `Selectable` loop.
 * XHTML: `<dropdown label="Theme" options="Dark,Light" selected="0"/>`.
 */
export class Dropdown extends Component<DropdownAttributes, SelectCallbacks> {
  static readonly tag = ComponentTag.Dropdown;
  readonly tag = ComponentTag.Dropdown;
}

export interface ListBoxAttributes {
  /** Label rendered next to the list. */
  label: string;
  /** Selectable options. */
  options: string[];
  /** Index of the selected option (two-way bound). */
  selected?: number;
  /** Visible rows before scrolling; default 4–7 per ImGui heuristics. */
  visibleRows?: number;
}

/**
 * Always-visible selectable list. ImGui: `BeginListBox` + `Selectable` loop.
 * XHTML: `<listbox label="Files" options="a.txt,b.txt"/>`.
 */
export class ListBox extends Component<ListBoxAttributes, SelectCallbacks> {
  static readonly tag = ComponentTag.ListBox;
  readonly tag = ComponentTag.ListBox;
}

export interface InputTextAttributes {
  /** Label rendered next to the field. */
  label: string;
  /** Current text (two-way bound). */
  value?: string;
  /** Grey placeholder hint — `InputTextWithHint`. */
  placeholder?: string;
  /** Multiline editor — `InputTextMultiline`. */
  multiline?: boolean;
  /** Mask characters — `ImGuiInputTextFlags_Password`. */
  password?: boolean;
  /** Maximum buffer length in bytes. Default 256. */
  maxLength?: number;
}

export interface InputTextCallbacks extends ValueCallbacks<string> {
  /** Fired when Enter is pressed — `ImGuiInputTextFlags_EnterReturnsTrue`. */
  onSubmit?: Callback<[value: string]>;
}

/**
 * Text field. ImGui: `InputText` / `InputTextMultiline` / `InputTextWithHint`.
 * XHTML: `<input-text label="Name" placeholder="Jane"/>`.
 */
export class InputText extends Component<InputTextAttributes, InputTextCallbacks> {
  static readonly tag = ComponentTag.InputText;
  readonly tag = ComponentTag.InputText;
}

export interface InputNumberAttributes {
  /** Label rendered next to the field. */
  label: string;
  /** `"int"` → `InputInt`, `"float"` → `InputFloat`. Default `"float"`. */
  type?: "int" | "float";
  /** Current value (two-way bound). */
  value?: number;
  /** Increment for the +/- step buttons. */
  step?: number;
  min?: number;
  max?: number;
}

/**
 * Numeric field with step buttons. ImGui: `InputInt` / `InputFloat`.
 * XHTML: `<input-number label="Count" type="int" step="1"/>`.
 */
export class InputNumber extends Component<
  InputNumberAttributes,
  ValueCallbacks<number>
> {
  static readonly tag = ComponentTag.InputNumber;
  readonly tag = ComponentTag.InputNumber;
}

export interface ColorPickerAttributes {
  /** Label rendered next to the swatch. */
  label: string;
  /** Current color (two-way bound). */
  value?: Color;
  /** Show the full picker inline instead of a swatch — `ColorPicker4`. */
  inline?: boolean;
  /** Include an alpha channel. Default `true`. */
  alpha?: boolean;
}

/**
 * RGBA color editor. ImGui: `ColorEdit4` (swatch) / `ColorPicker4` (inline).
 * XHTML: `<color-picker label="Tint" value="#ff8800ff"/>`.
 */
export class ColorPicker extends Component<
  ColorPickerAttributes,
  ValueCallbacks<Color>
> {
  static readonly tag = ComponentTag.ColorPicker;
  readonly tag = ComponentTag.ColorPicker;
}

/* ------------------------------------------------------------------ */
/* Structure & navigation                                             */
/* ------------------------------------------------------------------ */

export interface TabBarAttributes {
  /** Allow drag-reordering of tabs — `ImGuiTabBarFlags_Reorderable`. */
  reorderable?: boolean;
}

export interface TabBarCallbacks extends CommonCallbacks {
  /** Fired when the active tab changes; payload is the tab's `id`. */
  onChange?: Callback<[tabId: string]>;
}

/**
 * Container of {@link Tab} pages. ImGui: `BeginTabBar`/`EndTabBar`.
 * XHTML: `<tab-bar><tab label="One">…</tab></tab-bar>`.
 */
export class TabBar extends Component<TabBarAttributes, TabBarCallbacks> {
  static readonly tag = ComponentTag.TabBar;
  readonly tag = ComponentTag.TabBar;
}

export interface TabAttributes {
  /** Tab caption. */
  label: string;
  /** Show a close button; fires `onClose`. */
  closable?: boolean;
}

export interface TabCallbacks extends CommonCallbacks {
  /** Fired when a closable tab's close button is clicked. */
  onClose?: Callback;
}

/**
 * One page of a {@link TabBar}. ImGui: `BeginTabItem`/`EndTabItem`.
 * Children render only while the tab is selected.
 */
export class Tab extends Component<TabAttributes, TabCallbacks> {
  static readonly tag = ComponentTag.Tab;
  readonly tag = ComponentTag.Tab;
}

/**
 * Root container of a tree view; children must be {@link TreeNode}s.
 * XHTML: `<tree><tree-node label="src">…</tree-node></tree>`.
 */
export class Tree extends Component {
  static readonly tag = ComponentTag.Tree;
  readonly tag = ComponentTag.Tree;
}

export interface TreeNodeAttributes {
  /** Node caption. */
  label: string;
  /** Start expanded — `ImGuiTreeNodeFlags_DefaultOpen`. */
  open?: boolean;
  /** Render as a selectable leaf (no arrow) — `ImGuiTreeNodeFlags_Leaf`. */
  leaf?: boolean;
}

export interface TreeNodeCallbacks extends CommonCallbacks {
  /** Fired when the node is expanded or collapsed. */
  onToggle?: Callback<[open: boolean]>;
  /** Fired when the node label is clicked. */
  onSelect?: Callback;
}

/**
 * Expandable tree node. ImGui: `TreeNodeEx`/`TreePop`.
 * Nest more `TreeNode`s as children to build the hierarchy.
 */
export class TreeNode extends Component<TreeNodeAttributes, TreeNodeCallbacks> {
  static readonly tag = ComponentTag.TreeNode;
  readonly tag = ComponentTag.TreeNode;
}

export interface CollapsingHeaderAttributes {
  /** Header caption. */
  label: string;
  /** Start expanded. */
  open?: boolean;
}

/**
 * Full-width collapsible section. ImGui: `CollapsingHeader`.
 * Children render only while expanded.
 * XHTML: `<collapsing-header label="Advanced">…</collapsing-header>`.
 */
export class CollapsingHeader extends Component<CollapsingHeaderAttributes> {
  static readonly tag = ComponentTag.CollapsingHeader;
  readonly tag = ComponentTag.CollapsingHeader;
}

export interface TableAttributes {
  /** Column headers; also fixes the column count. */
  columns: string[];
  /** Draw row/column borders — `ImGuiTableFlags_Borders`. */
  borders?: boolean;
  /** Alternate row background — `ImGuiTableFlags_RowBg`. */
  striped?: boolean;
  /** Allow user column resizing — `ImGuiTableFlags_Resizable`. */
  resizable?: boolean;
  /** Enable click-to-sort headers; fires `onSort`. */
  sortable?: boolean;
}

export interface TableCallbacks extends CommonCallbacks {
  /** Fired when the user changes the sort column/direction. */
  onSort?: Callback<[column: number, ascending: boolean]>;
}

/**
 * Multi-column table; children must be {@link TableRow}s.
 * ImGui: `BeginTable`/`EndTable` with `TableSetupColumn` per header.
 * XHTML: `<table columns="Name,Size"><tr><td>…</td></tr></table>`.
 */
export class Table extends Component<TableAttributes, TableCallbacks> {
  static readonly tag = ComponentTag.Table;
  readonly tag = ComponentTag.Table;
}

/** One row of a {@link Table}. ImGui: `TableNextRow`. Children must be {@link TableCell}s. */
export class TableRow extends Component {
  static readonly tag = ComponentTag.TableRow;
  readonly tag = ComponentTag.TableRow;
}

/**
 * One cell of a {@link TableRow}. ImGui: `TableSetColumnIndex`.
 * Any component can be nested inside a cell.
 */
export class TableCell extends Component {
  static readonly tag = ComponentTag.TableCell;
  readonly tag = ComponentTag.TableCell;
}

/* ------------------------------------------------------------------ */
/* Menus, overlays, popups                                            */
/* ------------------------------------------------------------------ */

export interface MenuBarAttributes {
  /** Attach to the application frame instead of the parent window — `BeginMainMenuBar`. */
  main?: boolean;
}

/**
 * Menu strip; children must be {@link Menu}s.
 * ImGui: `BeginMenuBar` / `BeginMainMenuBar`.
 * XHTML: `<menu-bar><menu label="File">…</menu></menu-bar>`.
 */
export class MenuBar extends Component<MenuBarAttributes> {
  static readonly tag = ComponentTag.MenuBar;
  readonly tag = ComponentTag.MenuBar;
}

export interface MenuAttributes {
  /** Menu caption. */
  label: string;
}

/**
 * Submenu holding {@link MenuItem}s or nested `Menu`s. ImGui: `BeginMenu`.
 */
export class Menu extends Component<MenuAttributes> {
  static readonly tag = ComponentTag.Menu;
  readonly tag = ComponentTag.Menu;
}

export interface MenuItemAttributes {
  /** Item caption. */
  label: string;
  /** Right-aligned shortcut hint, e.g. `"Ctrl+S"` (display only). */
  shortcut?: string;
  /** Render a checkmark; toggled on click (two-way bound). */
  checked?: boolean;
}

export interface MenuItemCallbacks extends CommonCallbacks {
  /** Fired when the item is activated. */
  onClick?: Callback;
}

/**
 * Leaf menu entry. ImGui: `MenuItem`.
 * XHTML: `<menu-item label="Save" shortcut="Ctrl+S"/>`.
 */
export class MenuItem extends Component<MenuItemAttributes, MenuItemCallbacks> {
  static readonly tag = ComponentTag.MenuItem;
  readonly tag = ComponentTag.MenuItem;
}

export interface TooltipAttributes {
  /** Convenience plain-text content; use children for rich tooltips. */
  text?: string;
}

/**
 * Hover hint shown when the pointer rests on the *previous sibling*.
 * ImGui: `BeginTooltip` guarded by `IsItemHovered`.
 * XHTML: `<button label="?"/><tooltip text="Help!"/>`.
 */
export class Tooltip extends Component<TooltipAttributes> {
  static readonly tag = ComponentTag.Tooltip;
  readonly tag = ComponentTag.Tooltip;
}

export interface ModalAttributes {
  /** Title bar text. */
  title: string;
  /** Visibility (two-way bound); set `true` to open. */
  open?: boolean;
  /** Show a close button in the title bar. */
  closable?: boolean;
}

export interface ModalCallbacks extends CommonCallbacks {
  /** Fired when the modal is dismissed (close button or `CloseCurrentPopup`). */
  onClose?: Callback;
}

/**
 * Blocking dialog that dims and locks the rest of the UI.
 * ImGui: `OpenPopup` + `BeginPopupModal`.
 * XHTML: `<modal title="Confirm" open="false">…</modal>`.
 */
export class Modal extends Component<ModalAttributes, ModalCallbacks> {
  static readonly tag = ComponentTag.Modal;
  readonly tag = ComponentTag.Modal;
}

export interface PopupAttributes {
  /** Visibility (two-way bound). */
  open?: boolean;
  /** Open automatically on right-click of the previous sibling — `BeginPopupContextItem`. */
  contextMenu?: boolean;
}

export interface PopupCallbacks extends CommonCallbacks {
  /** Fired when the popup closes (click-away or programmatic). */
  onClose?: Callback;
}

/**
 * Non-blocking floating popup, dismissed by clicking elsewhere.
 * ImGui: `BeginPopup` / `BeginPopupContextItem`.
 */
export class Popup extends Component<PopupAttributes, PopupCallbacks> {
  static readonly tag = ComponentTag.Popup;
  readonly tag = ComponentTag.Popup;
}

/* ------------------------------------------------------------------ */
/* Layout containers                                                  */
/* ------------------------------------------------------------------ */

/**
 * Invisible logical grouping: the whole group behaves as one item for
 * hover/tooltip/size queries. ImGui: `BeginGroup`/`EndGroup`.
 */
export class Group extends Component {
  static readonly tag = ComponentTag.Group;
  readonly tag = ComponentTag.Group;
}

export interface RowAttributes {
  /** Horizontal gap between children in pixels; default item spacing. */
  gap?: number;
}

/**
 * Horizontal layout: children are placed on one line.
 * ImGui: `SameLine` emitted between children.
 * XHTML: `<row gap="8"><button label="A"/><button label="B"/></row>`.
 */
export class Row extends Component<RowAttributes> {
  static readonly tag = ComponentTag.Row;
  readonly tag = ComponentTag.Row;
}

export interface ColumnAttributes {
  /** Vertical gap between children in pixels; default item spacing. */
  gap?: number;
}

/**
 * Vertical layout (ImGui's natural flow), useful inside a {@link Row}
 * or for setting a uniform gap. XHTML: `<column gap="4">…</column>`.
 */
export class Column extends Component<ColumnAttributes> {
  static readonly tag = ComponentTag.Column;
  readonly tag = ComponentTag.Column;
}

/* ------------------------------------------------------------------ */
/* Plotting (ImPlot)                                                  */
/* ------------------------------------------------------------------ */

export interface PlotAttributes {
  /** Plot title displayed above the axes. */
  title: string;
  xLabel?: string;
  yLabel?: string;
  /** Show the series legend. Default `true`. */
  legend?: boolean;
  /** Auto-fit axes to the data each frame — `ImPlotAxisFlags_AutoFit`. */
  autoFit?: boolean;
}

/**
 * Chart surface; children must be plot series ({@link PlotLine},
 * {@link PlotScatter}, {@link PlotBars}). ImPlot: `BeginPlot`/`EndPlot`.
 * XHTML: `<plot title="FPS"><plot-line label="frame" …/></plot>`.
 */
export class Plot extends Component<PlotAttributes> {
  static readonly tag = ComponentTag.Plot;
  readonly tag = ComponentTag.Plot;
}

/** Attributes shared by all plot series. */
export interface PlotSeriesAttributes {
  /** Series name shown in the legend. */
  label: string;
  /** X values; when omitted, indices `0..n-1` are used. */
  x?: number[];
  /** Y values. */
  y: number[];
  /** Series color; auto-assigned from the ImPlot colormap when omitted. */
  color?: Color;
}

/** Line series inside a {@link Plot}. ImPlot: `PlotLine`. */
export class PlotLine extends Component<PlotSeriesAttributes> {
  static readonly tag = ComponentTag.PlotLine;
  readonly tag = ComponentTag.PlotLine;
}

/** Scatter series inside a {@link Plot}. ImPlot: `PlotScatter`. */
export class PlotScatter extends Component<PlotSeriesAttributes> {
  static readonly tag = ComponentTag.PlotScatter;
  readonly tag = ComponentTag.PlotScatter;
}

export interface PlotBarsAttributes extends PlotSeriesAttributes {
  /** Bar width in plot units. Default `0.67`. */
  barWidth?: number;
  /** Draw bars horizontally. */
  horizontal?: boolean;
}

/** Bar series inside a {@link Plot}. ImPlot: `PlotBars`. */
export class PlotBars extends Component<PlotBarsAttributes> {
  static readonly tag = ComponentTag.PlotBars;
  readonly tag = ComponentTag.PlotBars;
}

/* ------------------------------------------------------------------ */
/* Advanced surfaces                                                  */
/* ------------------------------------------------------------------ */

/**
 * Minimal immediate-mode draw API handed to {@link CanvasCallbacks.onDraw}.
 * Backed by `ImDrawList`; coordinates are local to the canvas origin.
 */
export interface DrawContext {
  line(from: Vec2, to: Vec2, color: Color, thickness?: number): void;
  rect(min: Vec2, max: Vec2, color: Color, filled?: boolean): void;
  circle(center: Vec2, radius: number, color: Color, filled?: boolean): void;
  text(position: Vec2, text: string, color: Color): void;
}

export interface CanvasCallbacks extends CommonCallbacks {
  /** Called every frame to paint the canvas. */
  onDraw?: Callback<[ctx: DrawContext]>;
  /** Fired on click, with the position local to the canvas. */
  onClick?: Callback<[position: Vec2]>;
}

/**
 * Custom drawing surface. ImGui: `InvisibleButton` (for input) +
 * `GetWindowDrawList` primitives. Size comes from `style.size`.
 * XHTML: `<canvas id="viz"/>` with callbacks attached from script.
 */
export class Canvas extends Component<Record<never, never>, CanvasCallbacks> {
  static readonly tag = ComponentTag.Canvas;
  readonly tag = ComponentTag.Canvas;
}

/** A node in the {@link NodeEditor} graph. */
export interface GraphNode {
  id: number;
  title: string;
  position?: Vec2;
  /** Input pin labels (imnodes input attributes). */
  inputs?: string[];
  /** Output pin labels (imnodes output attributes). */
  outputs?: string[];
}

/** A link between two pins in the {@link NodeEditor} graph. */
export interface GraphLink {
  id: number;
  /** Pin ID the link starts from (an output pin). */
  from: number;
  /** Pin ID the link ends at (an input pin). */
  to: number;
}

export interface NodeEditorAttributes {
  nodes: GraphNode[];
  links: GraphLink[];
  /** Show the navigation minimap. */
  minimap?: boolean;
}

export interface NodeEditorCallbacks extends CommonCallbacks {
  /** Fired when the user drags a new link between two pins. */
  onConnect?: Callback<[from: number, to: number]>;
  /** Fired when a link is detached or deleted. */
  onDisconnect?: Callback<[linkId: number]>;
  /** Fired when a node stops being dragged, with its new position. */
  onNodeMove?: Callback<[nodeId: number, position: Vec2]>;
  /** Fired when the node selection changes. */
  onSelect?: Callback<[nodeIds: number[]]>;
}

/**
 * Node graph editor. imnodes: `BeginNodeEditor` with `BeginNode` /
 * `Link` calls generated from {@link NodeEditorAttributes.nodes} and
 * {@link NodeEditorAttributes.links}.
 * XHTML: `<node-editor id="graph"/>` with data supplied from script.
 */
export class NodeEditor extends Component<
  NodeEditorAttributes,
  NodeEditorCallbacks
> {
  static readonly tag = ComponentTag.NodeEditor;
  readonly tag = ComponentTag.NodeEditor;
}
