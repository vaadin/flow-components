/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Cell-key codec — the {@code "col1 row1"} wire format
 * used throughout the spreadsheet client as both a cache key and a
 * CSS-class identifier (each rendered cell carries
 * {@code class="col1 row1"}).
 *
 * The format is a WIRE CONTRACT — the server's outgoing payloads use
 * the same encoding (see {@code SpreadsheetUtil.toKey} on the server
 * side), and the TestBench element classes locate cells via the
 * {@code .col<C>.row<R>} CSS selector. Changing the format breaks
 * compatibility with both directions; this module preserves it
 * exactly.
 */

/**
 * Format a 1-based (col, row) coordinate as the wire-format cell key:
 * {@code "col1 row1"}. Used as a cache key + CSS class prefix.
 */
export function toKey(col: number, row: number): string {
  return 'col' + col + ' row' + row;
}

/** Parsed cell-key pair returned by {@link parseColRow}. */
export interface ParsedCellKey {
  col: number;
  row: number;
}

/**
 * Inverse of {@link toKey}: parses {@code "col1 row1"} (or any string
 * that contains a column-digit run, a space, then a row-digit run)
 * back to its (col, row) coordinates.
 *
 * Cells render with a CSS class list like
 * {@code "col5 row3 cell-range cell-selected"} — the cell-key prefix
 * is followed by additional classes that describe selection +
 * formatting state. The parser stops after the second space, which
 * delimits the row-digit run from the rest. This is the contract
 * with the CSS-class-list-as-cell-key convention: the parser
 * decodes the prefix and ignores trailing classes.
 *
 * Returns {col: 0, row: 0} for any input where digits were never
 * accumulated for the corresponding axis — callers treat (0, 0) as
 * "not a cell key".
 */
export function parseColRow(str: string): ParsedCellKey {
  let i = 0;
  let flags = 0;
  let c = 0;
  let r = 0;
  while (i < str.length) {
    const code = str.charCodeAt(i);
    if (code === 32) {
      flags++;
    } else if (code > 47 && code < 58) {
      if (flags === 0) {
        c = c * 10 + code - 48;
      } else {
        r = r * 10 + code - 48;
      }
    }
    if (flags === 2) {
      break;
    }
    i++;
  }
  return { col: c, row: r };
}
