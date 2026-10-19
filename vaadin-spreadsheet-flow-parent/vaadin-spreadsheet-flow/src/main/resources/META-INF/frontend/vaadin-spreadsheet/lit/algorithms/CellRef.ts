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
 * A 1-based cell coordinate.
 *
 * - `col` is 1-based: column A is 1, column B is 2, ..., column Z is 26,
 *   column AA is 27, column ZZ is 702, column AAA is 703.
 * - `row` is 1-based: row 1 is 1.
 *
 * The 1-based convention matches POI's `CellAddress` API and the
 * server-side `Spreadsheet` public API. Conversion to POI's column-letter
 * notation is below.
 */
export interface CellCoord {
  /** 1-based column index. */
  col: number;
  /** 1-based row index. */
  row: number;
}

/**
 * Format a 1-based column index into Excel column letters.
 *
 * Examples: 1 → "A", 26 → "Z", 27 → "AA", 702 → "ZZ", 703 → "AAA".
 *
 * Used when emitting cell refs into the address bar, named ranges, and
 * formula tokens.
 *
 * @param col 1-based column index. Values ≤ 0 return `""`.
 */
export function columnIndexToLetters(col: number): string {
  if (col <= 0) {
    return '';
  }
  let letters = '';
  let n = col;
  while (n > 0) {
    // Map 1..26 to A..Z; subtract 1 because the base-26 system is
    // "bijective" (no zero digit).
    const remainder = (n - 1) % 26;
    letters = String.fromCharCode(65 + remainder) + letters;
    n = Math.trunc((n - 1) / 26);
  }
  return letters;
}

/**
 * Format a `CellCoord` into the canonical `"A1"` address.
 *
 * @param coord The 1-based cell coordinate.
 * @returns The cell address (e.g. `"AA42"`). Empty string if
 *          `col <= 0` AND `row <= 0`.
 */
export function cellCoordToAddress(coord: CellCoord): string {
  return columnIndexToLetters(coord.col) + (coord.row > 0 ? coord.row : '');
}
