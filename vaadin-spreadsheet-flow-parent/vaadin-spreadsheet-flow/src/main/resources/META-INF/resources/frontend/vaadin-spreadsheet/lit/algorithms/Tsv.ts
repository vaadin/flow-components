/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Tab-Separated-Values codec for the copy/paste lane. The
 * spreadsheet's clipboard format is TSV: rows separated by `\n`,
 * columns within a row separated by `\t`. This matches Excel's default
 * clipboard format and lets the spreadsheet cross-paste with Excel /
 * Sheets / Numbers / etc.
 *
 * Two pure functions: `serializeToTsv` (2D values → TSV text) and
 * `parseTsv` (TSV text → 2D values). The GWT-side
 * {@code CopyPasteHandlerImpl.getClipboardText} only had the serialize
 * direction (paste is delegated to the server). The TS port adds the
 * parse direction because the new client may handle paste locally for
 * trivial cases.
 *
 * IMPORTANT: TSV has no escaping convention for tabs or newlines INSIDE
 * a cell value. If a cell contains `\t` or `\n`, both directions are
 * lossy. The GWT version doesn't escape; neither does this port.
 * Cells containing newlines will round-trip wrong; this is the
 * documented Excel-clipboard quirk. Use cell-display-value (which
 * doesn't usually contain control chars) rather than raw cell content
 * for the serialize input.
 */

/**
 * Serialize a 2D array of cell values into TSV format.
 *
 * Algorithm ported from
 * {@code CopyPasteHandlerImpl.getClipboardText} (GWT client, slice
 * 119's survey). Byte-equivalent: `null` values emit as empty strings
 * (no token); column separator is `\t`; row separator is `\n`; no
 * trailing newline on the last row.
 *
 * @param values  2D row-major array. Outer is rows, inner is columns.
 *                `null` cells emit as empty strings (the column tab
 *                still fires).
 * @returns The TSV-formatted text.
 */
export function serializeToTsv(values: readonly (string | null)[][]): string {
    if (values.length === 0) {
        return '';
    }
    const rows: string[] = [];
    for (const row of values) {
        // Each cell's value or '' for null. The join handles the
        // column separators correctly (n-1 tabs for n cells, no
        // trailing tab — matching the Java version's
        // `if (col != xMax) sb.append("\t")` behavior).
        rows.push(row.map((cell) => (cell ?? '')).join('\t'));
    }
    return rows.join('\n');
}

/**
 * Parse TSV text into a 2D array of cell values.
 *
 * Behavior:
 * - Rows split on `\n`. CRLF-style `\r\n` line endings are normalized
 *   to `\n` first (Windows-clipboard tolerance).
 * - Empty cells preserved as empty strings (NOT as null) — the caller
 *   converts to null if needed.
 * - Trailing empty row from a trailing newline is dropped.
 * - No escaping convention — cells containing `\t` or `\n` aren't
 *   recoverable; the parser's output will look "shifted" in those
 *   cases. This matches Excel's clipboard behavior.
 *
 * @param text  TSV text from the clipboard.
 * @returns A 2D array of strings. Empty input returns an empty array
 *          (not `[[]]`).
 */
export function parseTsv(text: string): string[][] {
    if (text.length === 0) {
        return [];
    }
    // Normalize CRLF and CR to LF for cross-platform clipboard
    // tolerance (Windows / classic-Mac).
    const normalized = text.replace(/\r\n?/g, '\n');
    // Strip a single trailing newline if present, so a TSV ending in
    // `\n` doesn't produce an empty trailing row.
    const trimmed = normalized.endsWith('\n')
        ? normalized.slice(0, -1)
        : normalized;
    const rows = trimmed.split('\n');
    return rows.map((row) => row.split('\t'));
}
