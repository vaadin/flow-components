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
 * Tab-Separated-Values codec for the copy/paste lane. The
 * spreadsheet's clipboard format is TSV: rows separated by `\n`,
 * columns within a row separated by `\t`. This matches Excel's default
 * clipboard format and lets the spreadsheet cross-paste with Excel /
 * Sheets / Numbers / etc.
 *
 * Only the copy direction lives here, as in the GWT client's
 * {@code CopyPasteHandlerImpl.getClipboardText}: the server parses what
 * is pasted.
 *
 * IMPORTANT: TSV has no escaping convention for tabs or newlines INSIDE
 * a cell value. If a cell contains `\t` or `\n`, the copy is
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
    rows.push(row.map((cell) => cell ?? '').join('\t'));
  }
  return rows.join('\n');
}
