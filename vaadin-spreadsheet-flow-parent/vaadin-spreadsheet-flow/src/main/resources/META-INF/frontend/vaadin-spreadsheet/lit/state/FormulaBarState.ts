/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { FormulaBarRenderState } from '../elements/SpreadsheetFormulaBar.js';
import { type SelectionState, selectionSizeLabel } from './SelectionState.js';
import { cellCoordToAddress } from '../algorithms/CellRef.js';
import { formulaReferences } from '../algorithms/FormulaPointing.js';

/** What the formula bar shows besides what the selection and the formula being written say. */
export interface FormulaBarInputs {
  /**
   * What the server named the selection - a named range, a sheet-qualified
   * reference - or null, for the address of its cell. A selection the client
   * makes has none until the server answers.
   */
  readonly selectionName: string | null;
  /** What the formula field shows while nobody types in it: the cell's value, or the cell editor's as typed. */
  readonly fieldText: string;
  /** The named ranges of the workbook, offered beside the address field. */
  readonly namedRanges: ReadonlyArray<string>;
}

/** The formula bar of a sheet nothing has been shown in yet. */
export const NO_FORMULA_BAR_INPUTS: FormulaBarInputs = {
  selectionName: null,
  fieldText: '',
  namedRanges: []
};

/**
 * What the address field shows. While a range is being grown - Shift held,
 * or a drag - its size, "3R x 2C", as Excel's name box counts it out. Then
 * the name the server gave the selection, and otherwise the address of its
 * active cell.
 */
export function addressFieldText(selection: SelectionState, growing: boolean, selectionName: string | null): string {
  if (growing) {
    return selectionSizeLabel(selection.activeRange);
  }
  return selectionName ?? cellCoordToAddress(selection.activeCell);
}

/** What the formula bar draws. */
export function formulaBarRenderState(input: {
  readonly bar: FormulaBarInputs;
  readonly selection: SelectionState;
  /** Whether a range is being grown; see {@link addressFieldText}. */
  readonly growing: boolean;
  /** The formula being written, coloured by its references; null while none is. */
  readonly draft: { readonly text: string } | null;
  /** The sheet shown, which a reference without a sheet of its own is on. */
  readonly sheetName: string;
}): FormulaBarRenderState {
  const { bar, draft } = input;
  return {
    address: addressFieldText(input.selection, input.growing, bar.selectionName),
    formula: bar.fieldText,
    namedRanges: bar.namedRanges,
    highlights:
      draft === null
        ? null
        : {
            text: draft.text,
            references: formulaReferences(draft.text, input.sheetName).map((ref) => ({
              start: ref.start,
              end: ref.end,
              color: ref.color
            }))
          }
  };
}
