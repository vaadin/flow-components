/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Cell-key + header-key codec — the {@code "col1 row1"} wire format
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
 *
 * The header-key format ({@code "rh<N>"} for row headers,
 * {@code "ch<N>"} for column headers) is a sibling contract.
 *
 * Implementation note: character-code scanners (rather than regex)
 * for parse + classify operations. These run on every selection
 * change + every paint of every visible cell, so allocation-free
 * scanning matters.
 */

/**
 * Format a 1-based (col, row) coordinate as the wire-format cell key:
 * {@code "col1 row1"}. Used as a cache key + CSS class prefix.
 */
export function toKey(col: number, row: number): string {
    return 'col' + col + ' row' + row;
}

/**
 * CSS-selector variant of {@link toKey}: {@code ".col1.row1"}. Used to
 * select a cell's rendered element by coordinate.
 */
export function toCssKey(col: number, row: number): string {
    return '.col' + col + '.row' + row;
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

/**
 * Return values for {@link headerType}. Numeric constants rather than
 * a TS enum, because callers compare against bare numbers in a few
 * places.
 */
export const HEADER_NONE = 0;
export const HEADER_ROW = 1;
export const HEADER_COL = 2;

/**
 * Classify a key string as a row header, column header, or cell.
 *
 * Naming convention:
 *  - Row header keys: {@code "rh<N>"} (e.g. {@code "rh5"} = 5th row header)
 *  - Column header keys: {@code "ch<N>"} (e.g. {@code "ch3"} = 3rd column header)
 *  - Cell keys: {@code "col<C> row<R>"} (the {@link toKey} format)
 *
 * The classifier checks the first two characters for the "rh" /
 * "ch" prefix. Cell keys start with "col" so they correctly classify
 * as HEADER_NONE — the second char 'o' breaks the "rh"/"ch" match.
 * The same logic distinguishes "row5" (which would be the "row"
 * portion of a cell key in isolation, not a row header — row headers
 * are always written "rh<N>") from "rh5".
 */
export function headerType(str: string): number {
    if (!str || str.length < 2) {
        return HEADER_NONE;
    }
    const first = str.charAt(0);
    if (first === 'r' && str.charAt(1) === 'h') {
        return HEADER_ROW;
    }
    if (first === 'c' && str.charAt(1) === 'h') {
        return HEADER_COL;
    }
    return HEADER_NONE;
}

/**
 * Extract the integer index from a header key.
 *
 * Expects the form {@code "rh<N>"} or {@code "ch<N>"} — a two-char
 * prefix followed by the index digits. Scans the digits after the
 * prefix; non-digit characters in the middle of the run stop the
 * scan.
 *
 * Defensive: returns 0 for empty / unrecognized input. Callers
 * either know the input is a header key (via {@link headerType}) or
 * handle the 0 return as "not a valid index".
 */
export function parseHeaderIndex(str: string): number {
    if (!str || str.length < 3) {
        return 0;
    }
    let index = 0;
    let i = 2; // skip the two-char prefix
    while (i < str.length) {
        const code = str.charCodeAt(i);
        if (code > 47 && code < 58) {
            index = index * 10 + code - 48;
        } else {
            break;
        }
        i++;
    }
    return index;
}
