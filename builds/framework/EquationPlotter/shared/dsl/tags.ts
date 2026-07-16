/**
 * Nexus Framework — DSL component tags.
 *
 * Every builtin component class in `components.ts` maps to exactly one XHTML
 * DSL tag listed here. The tag string is what appears in `ui.xhtml` documents
 * and what the native runtime resolves to a Dear ImGui / ImPlot / imnodes
 * draw call. Keep this enum in sync with the C++ tag registry.
 */
export enum ComponentTag {
  /** Top-level OS-decorated window — `ImGui::Begin`/`End`. */
  Window = "window",
  /** Scrollable child region with optional border — `ImGui::BeginChild`. */
  Panel = "panel",
  /** Static text label — `ImGui::TextUnformatted` / `TextWrapped`. */
  Text = "text",
  /** Clickable push button — `ImGui::Button`. */
  Button = "button",
  /** Boolean checkbox — `ImGui::Checkbox`. */
  Checkbox = "checkbox",
  /** Exclusive-choice group of `ImGui::RadioButton`s. */
  RadioGroup = "radio-group",
  /** Single option inside a `RadioGroup` — `ImGui::RadioButton`. */
  RadioButton = "radio",
  /** Numeric slider — `ImGui::SliderInt` / `SliderFloat` (per `type`). */
  Slider = "slider",
  /** Single-select dropdown — `ImGui::BeginCombo`/`Selectable`. */
  Dropdown = "dropdown",
  /** Always-visible selectable list — `ImGui::BeginListBox`. */
  ListBox = "listbox",
  /** Text field — `ImGui::InputText` / `InputTextMultiline`. */
  InputText = "input-text",
  /** Numeric field with step buttons — `ImGui::InputInt` / `InputFloat`. */
  InputNumber = "input-number",
  /** RGBA color editor — `ImGui::ColorEdit4` / `ColorPicker4`. */
  ColorPicker = "color-picker",
  /** Textured quad from an asset — `ImGui::Image`. */
  Image = "image",
  /** Determinate progress bar — `ImGui::ProgressBar`. */
  ProgressBar = "progress-bar",
  /** Horizontal rule — `ImGui::Separator`. */
  Separator = "separator",
  /** Fixed empty space — `ImGui::Dummy` / `Spacing`. */
  Spacer = "spacer",
  /** Container for tabs — `ImGui::BeginTabBar`. */
  TabBar = "tab-bar",
  /** One page of a `TabBar` — `ImGui::BeginTabItem`. */
  Tab = "tab",
  /** Root of a hierarchical tree view. */
  Tree = "tree",
  /** Expandable node — `ImGui::TreeNodeEx`. */
  TreeNode = "tree-node",
  /** Multi-column table — `ImGui::BeginTable`. */
  Table = "table",
  /** One row of a `Table` — `ImGui::TableNextRow`. */
  TableRow = "tr",
  /** One cell of a `TableRow` — `ImGui::TableSetColumnIndex`. */
  TableCell = "td",
  /** Window-level menu strip — `ImGui::BeginMenuBar` / `BeginMainMenuBar`. */
  MenuBar = "menu-bar",
  /** Submenu inside a `MenuBar` or another `Menu` — `ImGui::BeginMenu`. */
  Menu = "menu",
  /** Leaf entry of a menu — `ImGui::MenuItem`. */
  MenuItem = "menu-item",
  /** Hover hint attached to its parent — `ImGui::SetTooltip`/`BeginTooltip`. */
  Tooltip = "tooltip",
  /** Blocking dialog — `ImGui::BeginPopupModal`. */
  Modal = "modal",
  /** Non-blocking floating popup — `ImGui::BeginPopup`. */
  Popup = "popup",
  /** Chart surface hosting plot series — `ImPlot::BeginPlot`. */
  Plot = "plot",
  /** Line series inside a `Plot` — `ImPlot::PlotLine`. */
  PlotLine = "plot-line",
  /** Scatter series inside a `Plot` — `ImPlot::PlotScatter`. */
  PlotScatter = "plot-scatter",
  /** Bar series inside a `Plot` — `ImPlot::PlotBars`. */
  PlotBars = "plot-bars",
  /** Collapsible section header — `ImGui::CollapsingHeader`. */
  CollapsingHeader = "collapsing-header",
  /** Logical grouping, no visual — `ImGui::BeginGroup`. */
  Group = "group",
  /** Lays children out horizontally — `ImGui::SameLine` between children. */
  Row = "row",
  /** Lays children out vertically (ImGui's default flow). */
  Column = "column",
  /** Custom draw surface — `ImGui::GetWindowDrawList` primitives. */
  Canvas = "canvas",
  /** Node graph editor — imnodes `BeginNodeEditor`. */
  NodeEditor = "node-editor",
}
