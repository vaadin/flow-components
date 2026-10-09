/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Wire-format data transfer objects exchanged between the server-side
 * Spreadsheet and this client. These interfaces mirror the JSON shape the
 * server emits via `Serializer.serialize(...)`, pinned by
 * `DtoWireFormatTest` on the server side.
 *
 * IMPORTANT: the field names and types here MUST match the server's wire
 * format. See `vaadin-spreadsheet-flow/src/test/java/com/
 * vaadin/flow/component/spreadsheet/DtoWireFormatTest.java` for the
 * authoritative server-side snapshots.
 *
 * Field-name conventions follow the server (camelCase, alphabetically
 * ordered on the wire — Jackson default — though TypeScript-side reads
 * are not order-sensitive).
 */

/**
 * A single spreadsheet cell — the most-trafficked wire DTO.
 *
 * Server-side shape: `com.vaadin.flow.component.spreadsheet.client.CellData`.
 * The `value` field carries `@JsonInclude` so it's always emitted (even
 * when `null`); other fields use NON_DEFAULT inclusion, so type-default
 * values are omitted from the wire.
 */
export interface CellData {
  /** 1-based row index. */
  row: number;
  /** 1-based column index. */
  col: number;
  /** Display value. ALWAYS present in the wire format (even null). */
  value: string | null;
  /** Formula source text, when the cell holds a formula. */
  formulaValue?: string;
  /** Raw underlying value (locale-independent). */
  originalValue?: string;
  /** CSS class index for this cell's style. Default `"cs0"`. */
  cellStyle: string;
  /** Override text color (hex RGB, no leading `#`). */
  textColor?: string;
  /** When true, the cell can't be edited regardless of selection. */
  locked: boolean;
  /** Hint to the renderer that this cell needs a width measurement. */
  needsMeasure: boolean;
  /**
   * Width in pixels to draw this cell's text at when it is too wide for its
   * column, absent when it fits. It covers the cell's own column and the
   * empty ones the text runs into - only its own when it has nowhere to go -
   * and what still does not fit ends in an ellipsis.
   *
   * The server settles how far, because it is the side that knows what the
   * neighbouring cells hold. Absent rather than `0` on the wire: the
   * serializer drops default values.
   */
  overflowWidth?: number;
  /** True if the cell's number format includes `%`. */
  isPercentage: boolean;
}

/**
 * A merged-region range on the sheet.
 *
 * Server-side shape: `com.vaadin.flow.component.spreadsheet.client.MergedRegion`.
 * `id` is positive for POI-tracked regions; the algorithm in
 * `MergedRegionUtil.findIncreasingSelection` constructs transient
 * regions with id 0.
 */
export interface MergedRegion {
  id: number;
  /** Left column, 1-based, inclusive. */
  col1: number;
  /** Right column, 1-based, inclusive. */
  col2: number;
  /** Top row, 1-based, inclusive. */
  row1: number;
  /** Bottom row, 1-based, inclusive. */
  row2: number;
}

/**
 * A floating overlay on the sheet (image, chart, or embedded component).
 *
 * Server-side shape: `com.vaadin.flow.component.spreadsheet.client.OverlayInfo`.
 * `type` is the Java enum's `name()` value — Jackson serializes enums as
 * their constant name. The TS-side compares against the literal strings.
 */
export interface OverlayInfo {
  type: 'IMAGE' | 'COMPONENT';
  col: number;
  row: number;
  width: number;
  height: number;
  dx: number;
  dy: number;
}

/**
 * Context-menu action details emitted from server to client.
 *
 * Server-side shape: `com.vaadin.flow.component.spreadsheet.client.
 * SpreadsheetActionDetails`. Note `type` is `int` on the server but
 * `string` on the GWT client side; the wire is a JSON number so the new
 * TS client reads it as `number` (preserving the server's typing).
 */
export interface SpreadsheetActionDetails {
  caption: string;
  /** 0 = cell, 1 = row, 2 = column. */
  type: number;
  key: string;
  /** Vaadin Flow node ID of the icon child, 0 if no icon. */
  iconNodeId: number;
}

/**
 * Per-popup-button state.
 *
 * Server-side shape: `com.vaadin.flow.component.spreadsheet.shared.
 * PopupButtonState`. The GWT-side mirror extends `AbstractComponentState`
 * (Vaadin-8 base) for legacy reasons; the wire-format Serializer's
 * NON_DEFAULT inclusion guarantees the server never emits those legacy
 * fields, so the TS-side interface only declares the server's own POJO
 * shape.
 */
export interface PopupButtonState {
  /** 1-based column index of the anchored cell. */
  col: number;
  /** 1-based row index. */
  row: number;
  active: boolean;
  headerHidden: boolean;
  popupHeight: string | null;
  popupWidth: string | null;
  sheet: string;
}

/**
 * One grouping (outline) entry — a row or column collapse/expand
 * affordance.
 *
 * Server-side shape: `com.vaadin.flow.component.spreadsheet.shared.
 * GroupingData`.
 */
export interface GroupingData {
  startIndex: number;
  endIndex: number;
  level: number;
  /** Unique within the grouping list for collapse/expand identity. */
  uniqueIndex: number;
  collapsed: boolean;
}
