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
 * Only the copy direction lives here: the server parses what is pasted.
 *
 * A cell that holds a tab, a line break or a double quote is written in
 * double quotes, with its own quotes doubled, as spreadsheets put such
 * cells on the clipboard; the server's {@code ClipboardText} reads them
 * back the same way. Written as they were, a cell with a line break would
 * paste as two rows.
 */

/**
 * Serialize a 2D array of cell values into TSV format.
 *
 * `null` values emit as empty strings; column
 * separator is `\t`; row separator is `\n`; no trailing newline on the
 * last row.
 *
 * @param values  2D row-major array. Outer is rows, inner is columns.
 *                `null` cells emit as empty strings (the column tab
 *                still fires).
 * @returns The TSV-formatted text.
 */
/**
 * A cell as spreadsheets put it on the clipboard: in double quotes, with its
 * own quotes doubled, when it holds a tab, a line break or a quote. Left as
 * it was, a cell with a line break pasted as two rows.
 */
function quoted(cell: string): string {
  return /[\t\n\r"]/.test(cell) ? `"${cell.replace(/"/g, '""')}"` : cell;
}

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
    rows.push(row.map((cell) => quoted(cell ?? '')).join('\t'));
  }
  return rows.join('\n');
}
