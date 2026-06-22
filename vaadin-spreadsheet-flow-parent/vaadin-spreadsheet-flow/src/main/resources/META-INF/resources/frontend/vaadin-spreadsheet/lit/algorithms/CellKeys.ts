/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Pure-algorithm port of the GWT client's cell-key + header-key
 * codec — the "col1 row1" wire format used as a cache-key + CSS-class
 * identifier throughout the spreadsheet client.
 *
 * Ported from:
 *  - `SheetWidget.toKey(int, int)` / `toCssKey(int, int)` (lines 4578-4584)
 *  - `SheetJsniUtil.parseColRow(String)` (lines 20-47)
 *  - `SheetJsniUtil.isHeader(String)` (lines 58-76)
 *  - `SheetJsniUtil.parseHeaderIndex(String)` (lines 79-93)
 *
 * The GWT-side JSNI versions are hand-optimized character-code
 * scanners (avoiding regex allocation in hot paths). The TS ports
 * use the same approach for predictable performance and byte-
 * equivalent behavior on the same inputs.
 */

/**
 * Format a 1-based (col, row) coordinate as the wire-format cell key:
 * {@code "col1 row1"}. Used as both a cache key in the GWT client and
 * as the CSS class prefix (cells render with {@code class="col1 row1"}).
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
 * Mirrors {@code SheetJsniUtil.parseColRow(String)} verbatim — a
 * character-code scanner that:
 *  1. Accumulates digit characters (code 48-57) into the column,
 *     then into the row after seeing a space (code 32).
 *  2. Stops after the SECOND space (`flags === 2`). Anything after
 *     the second space is ignored — e.g. extra CSS classes in
 *     {@code "col1 row1 something-else"}.
 *  3. Treats non-digit, non-space characters as separators that
 *     don't advance the flag count and don't accumulate into the
 *     current integer (so {@code "col1 row1"} parses to (1, 1)
 *     because the `c`, `o`, `l`, `r`, `o`, `w` are silently skipped).
 *
 * Returns {col: 0, row: 0} for any input where digits were never
 * accumulated for one axis — matching the GWT behavior (the parser
 * fields default to 0 and only get assigned if digits were seen).
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
 * Enum-like return values for {@link headerType}. Matches the GWT
 * {@code isHeader} contract: {@code 1} for a row header, {@code 2}
 * for a column header, {@code 0} for a non-header (e.g. a cell key).
 *
 * Defined as numeric constants rather than a TS enum to mirror the
 * GWT integer return type byte-equivalent.
 */
export const HEADER_NONE = 0;
export const HEADER_ROW = 1;
export const HEADER_COL = 2;

/**
 * Classify a key string as a row header, column header, or cell.
 *
 * Mirrors {@code SheetJsniUtil.isHeader(String)} byte-equivalent.
 * The GWT version only inspects characters [0] and [1]:
 *  - "rh..." → row header → 1
 *  - "ch..." → col header → 2
 *  - anything else → 0
 *
 * Note that "row5" returns 0 (NOT a header) because str[1] is 'o',
 * not 'h'. The naming convention differs between cell keys
 * ("col1 row1") and header keys ("ch1" / "rh1") — the leading 'c'
 * in "col1" matches but the second char 'o' breaks the predicate
 * to 0. Preserved for byte-equivalence; the upstream relies on the
 * naming separation.
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
 * Extract the trailing integer from a key string by accumulating
 * digit characters wherever they appear. Used for header keys
 * ({@code "rh5"} → 5, {@code "ch12"} → 12) and any string that
 * embeds a single index.
 *
 * Mirrors {@code SheetJsniUtil.parseHeaderIndex(String)} verbatim —
 * non-digit characters are skipped, ALL digit characters are
 * accumulated. {@code "rh1ch2"} returns 12 (NOT 1), because the
 * scanner accumulates digits across both runs. Preserved for
 * byte-equivalence even though the multi-digit-run case is
 * presumably never hit by upstream callers.
 */
export function parseHeaderIndex(str: string): number {
    let index = 0;
    for (let i = 0; i < str.length; i++) {
        const code = str.charCodeAt(i);
        if (code > 47 && code < 58) {
            index = index * 10 + code - 48;
        }
    }
    return index;
}
