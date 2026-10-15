/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { MergedRegion } from '../types/wire.js';

/**
 * A 1-based cell coordinate.
 *
 * - `col` is 1-based: column A is 1, column B is 2, ..., column Z is 26,
 *   column AA is 27, column ZZ is 702, column AAA is 703.
 * - `row` is 1-based: row 1 is 1.
 *
 * The 1-based convention matches POI's `CellAddress` API and the
 * server-side `Spreadsheet` public API. Conversions to / from POI's
 * column-letter notation are below.
 */
export interface CellCoord {
  /** 1-based column index. */
  col: number;
  /** 1-based row index. */
  row: number;
}

/**
 * Parse a single cell reference (e.g. `"A1"`, `"BC23"`, `"a1"`) into a
 * `CellCoord`. Tolerates lower-case; the parser uppercases internally.
 *
 * Behavior contract:
 *
 * - Empty / whitespace-only / non-cell-ref input → `{ col: 0, row: 0 }`
 *   (no exception). Caller is responsible for validating that the input
 *   IS a cell reference (use `isCellRef` first) — this function is
 *   permissive on shape.
 * - Multi-letter column prefixes (up to 3 letters) are accumulated in
 *   base-26 with no leading-zero handling: A=1, Z=26, AA=27, AZ=52,
 *   BA=53, ..., ZZ=702, AAA=703 — the standard Excel column-letter
 *   convention.
 *
 * Pure function — no side effects.
 *
 * @param cellRef Cell reference text (e.g. `"AA42"`).
 * @returns The parsed `CellCoord`, or `{ col: 0, row: 0 }` on
 *          unparseable input.
 */
export function parseSingleCell(cellRef: string | null): CellCoord {
  if (cellRef === null) {
    return { col: 0, row: 0 };
  }

  // Java splits twice: once by digits (to extract the letter prefix)
  // and once by letters (to extract the number suffix). We do the
  // same to preserve the exact behavior on edge cases like "A1B2"
  // (which the original parser maps to col=A, row=2).
  const upperRef = cellRef.toUpperCase();
  const splitByDigits = upperRef.split(/[0-9]/);
  const splitByLetters = cellRef.split(/[A-Za-z]/);

  const letterPart = splitByDigits.length > 0 ? splitByDigits[0] : '';
  const digitPart = splitByLetters.length > 0 ? splitByLetters[splitByLetters.length - 1] : '';

  const row = digitPart.length > 0 ? Number.parseInt(digitPart, 10) : 0;

  let col = 0;
  for (let i = 0; i < letterPart.length; i++) {
    const code = letterPart.charCodeAt(i);
    let charNum = 0;
    if (code >= 65 && code <= 90) {
      // 'A'..'Z'
      charNum = code - 64;
    }
    // The Java version also matches 'a'..'z' here, but at this
    // point we've already uppercased — keep the structure so the
    // intent is documented.
    col = col * 26 + charNum;
  }

  return { col, row };
}

/**
 * Format a 1-based column index into Excel column letters.
 * Inverse of the `parseSingleCell` column-letter loop.
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

/**
 * Parse a cell-reference token into a `MergedRegion` (single-cell,
 * range, or sheet-qualified). Returns `null` when the token refers to a
 * non-active sheet (caller's responsibility to provide the active
 * sheet name).
 *
 * Behavior contract:
 *
 * - Sheet-prefixed ref (`"Sheet2!A1"`): only parsed if the sheet
 *   prefix matches `activeSheetName`; otherwise returns `null`.
 * - Range ref (`"A1:B3"`): both endpoints parsed; corners populated
 *   directly without reordering (caller normalizes if needed).
 * - Single-cell ref (`"C5"`): col1/col2 collapse to the parsed col;
 *   row1/row2 collapse to the parsed row.
 *
 * Pure function — no side effects.
 *
 * @param ref               Cell-reference token (e.g. `"A1"`,
 *                          `"A1:B3"`, `"Sheet2!C5"`).
 * @param activeSheetName   Active sheet's name; refs not on this sheet
 *                          return `null`.
 * @returns A `MergedRegion` with `id=0` describing the addressed range,
 *          or `null` when the ref is on another sheet.
 */
export function parseCellRefRange(ref: string, activeSheetName: string): MergedRegion | null {
  if (ref.includes('!')) {
    // Sheet-qualified ref.
    const parts = ref.split('!');
    const sheetname = parts[0];
    if (activeSheetName !== sheetname) {
      return null;
    }
    return parseCellRefRange(parts[1], activeSheetName);
  } else if (ref.includes(':')) {
    // Range ref.
    const refs = ref.split(':');
    const c1 = parseSingleCell(refs[0]);
    const c2 = parseSingleCell(refs[1]);
    return {
      id: 0,
      col1: c1.col,
      col2: c2.col,
      row1: c1.row,
      row2: c2.row
    };
  } else {
    // Single-cell ref.
    const cc = parseSingleCell(ref);
    return {
      id: 0,
      col1: cc.col,
      col2: cc.col,
      row1: cc.row,
      row2: cc.row
    };
  }
}
