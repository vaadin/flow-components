/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Typing a number straight into a percentage-formatted cell.
 *
 * Someone typing `5` into a cell that reads `19%` means five percent. Without
 * help they get five hundred: the editor sends `5`, the sheet stores 5, and a
 * percentage format draws 5 as `500.00%`. So the editor opens on `5%`, with
 * the caret before the sign, and the next keystroke still lands on the
 * number.
 *
 * Both halves of that matter. The `%` alone would put the caret after it and
 * turn `53` into `5%3`.
 */

/** What the cell holds, as far as this decision is concerned. */
export interface PercentageCell {
  /** The cell's displayed text. */
  value?: string | null;
  /** Whether the cell's number format carries a `%`. */
  isPercentage?: boolean;
}

/**
 * Keys that begin a number. A sign counts: `-` then `5` has to end up as
 * `-5%`, which works because the caret sits before the sign character.
 */
const NUMBER_START = new Set(['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '+']);

/**
 * The value to open the cell editor on when {@code key} is typed into
 * {@code cell}.
 *
 * @param key the printable character that started the edit
 * @param cell the cell being typed into, or `undefined` when it is empty
 * @returns the key, with a percent sign when the cell is a percentage one
 */
export function initialEditValue(key: string, cell: PercentageCell | undefined): string {
  if (!NUMBER_START.has(key) || !isPercentageCell(cell)) {
    return key;
  }
  return `${key}%`;
}

/**
 * Where the caret belongs in a freshly opened editor: before a trailing
 * percent sign, so what is typed next joins the number, and at the end
 * otherwise.
 *
 * @param value the editor's starting value
 * @returns the caret index
 */
export function caretPosition(value: string): number {
  return value.endsWith('%') ? value.length - 1 : value.length;
}

function isPercentageCell(cell: PercentageCell | undefined): boolean {
  if (cell === undefined) {
    return false;
  }
  return cell.isPercentage === true || (cell.value ?? '').endsWith('%');
}
