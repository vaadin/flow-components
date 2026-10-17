/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { GroupingData, MergedRegion, OverlayInfo } from '../types/wire.js';
import { coerceArray, coerceNumberArray, coerceObject, coerceSet, coerceSizes } from '../wire/Coerce.js';

/**
 * What the server mirrors onto the element, as the client reads it: each
 * property by its name on the wire, how its value is read in, and what it is
 * until the server sends one.
 *
 * The model is replaced whole on each change and never changed in place, and
 * a value the change does not touch is carried over as it is: the parts tell
 * a changed value from an unchanged one by identity alone. It holds what the
 * server sent, and what the client changed itself until the server answers.
 */

interface SheetProperty<W, V> {
  /** Reads the value as the server sends it. */
  readonly coerce: (value: W) => V;
  /** The value until the server sends one, new for each sheet. */
  readonly initial: () => V;
}

/** A value the server sends as it is. */
function plain<V>(initial: V): SheetProperty<V, V> {
  return { coerce: (value) => value, initial: () => initial };
}

/**
 * A collection, which the server sends as it is or as JSON text: empty until
 * it is sent, as it is read when the server clears it.
 */
function collection<W, V>(coerce: (value: W | null) => V): SheetProperty<W, V> {
  return { coerce, initial: () => coerce(null) };
}

export const SHEET_PROPERTIES = {
  /** How far past the screen cells are drawn, in pixels. */
  rowBufferSize: plain(200),
  columnBufferSize: plain(200),

  rows: plain(0),
  cols: plain(0),
  /** The height of a row the server gives none, in points. */
  defRowH: plain(15),
  /** The width of a column the server gives none, in pixels. */
  defColW: plain(64),
  /** Column widths in pixels. */
  colW: collection(coerceSizes),
  /*
   * Sets, not arrays: they are asked about one row or column at a time
   * inside loops over rows, and on a filtered table with tens of thousands
   * of hidden rows an array lookup made each arrow key take 200 ms.
   */
  hiddenColumnIndexes: collection(coerceSet<number>),
  hiddenRowIndexes: collection(coerceSet<number>),
  lockedColumnIndexes: collection(coerceSet<number>),
  lockedRowIndexes: collection(coerceSet<number>),

  /**
   * Where each sheet was last scrolled to, by visible sheet, as the server
   * keeps it: a sheet shown again goes back there.
   */
  verticalScrollPositions: collection(coerceNumberArray),
  horizontalScrollPositions: collection(coerceNumberArray),

  rowIndexToStyleIndex: collection(coerceObject<number>),
  columnIndexToStyleIndex: collection(coerceObject<number>),
  shiftedCellBorderStyles: collection(coerceArray<string>),
  conditionalFormattingStyles: collection(coerceObject<string>),

  colGroupingData: collection(coerceArray<GroupingData>),
  rowGroupingData: collection(coerceArray<GroupingData>),
  colGroupingMax: plain(0),
  rowGroupingMax: plain(0),
  colGroupingInversed: plain(false),
  rowGroupingInversed: plain(false),

  sheetProtected: plain(false),
  mergedRegions: collection(coerceArray<MergedRegion>),
  cellKeysToEditorIdMap: collection(coerceObject<string>),
  componentIDtoCellKeysMap: collection(coerceObject<string>),
  cellComments: collection(coerceObject<string>),
  cellCommentAuthors: collection(coerceObject<string>),
  visibleCellComments: collection(coerceArray<string>),
  invalidFormulaCells: collection(coerceSet<string>),
  overlays: collection(coerceObject<OverlayInfo>),
  showCustomEditorOnFocus: plain(false),
  /** Whether the server asks a factory for a component or an editor for each cell it loads. */
  hasComponentFactory: plain(false),
  /*
   * The message a cell with a formula the workbook cannot make sense of
   * shows on hover. Held here rather than waited for: the server only
   * pushes it when an application replaces the default, and until then a
   * null message means the cell explains nothing at all.
   */
  invalidFormulaErrorMessage: plain<string | null>('Invalid formula'),
  /** The frozen rows and columns. */
  verticalSplitPosition: plain(0),
  horizontalSplitPosition: plain(0),
  hyperlinksTooltips: collection(coerceObject<string>)
};

type Properties = typeof SHEET_PROPERTIES;

/** A property in the table, by its name on the wire. */
export type SheetPropertyName = keyof Properties;

/** A property's value as the server sends it. */
export type SheetPropertyValue<K extends SheetPropertyName> = Parameters<Properties[K]['coerce']>[0];

/** Each property's value as the client reads it. */
export type SheetModel = { readonly [K in SheetPropertyName]: ReturnType<Properties[K]['initial']> };

/** The model of a sheet the server has sent nothing of yet. */
export function initialSheetModel(): SheetModel {
  const model: Record<string, unknown> = {};
  for (const [name, property] of Object.entries(SHEET_PROPERTIES)) {
    model[name] = property.initial();
  }
  return Object.freeze(model) as SheetModel;
}

/** The model with a property as the server sends it, read in. */
export function withProperty<K extends SheetPropertyName>(
  model: SheetModel,
  name: K,
  value: SheetPropertyValue<K>
): SheetModel {
  const property = SHEET_PROPERTIES[name] as unknown as SheetProperty<SheetPropertyValue<K>, SheetModel[K]>;
  return withValues(model, { [name]: property.coerce(value) });
}

/** The model with values already read in, the others carried over as they are. */
export function withValues(model: SheetModel, values: Partial<SheetModel>): SheetModel {
  return Object.freeze({ ...model, ...values });
}
