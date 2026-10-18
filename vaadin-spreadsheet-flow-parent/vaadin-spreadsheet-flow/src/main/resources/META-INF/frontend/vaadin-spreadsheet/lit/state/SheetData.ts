/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { CellData } from '../types/wire.js';

/**
 * What the client knows of the sheet the server shows, from what the two
 * have told each other. A sheet shown anew - another sheet, a new workbook, a
 * reset - starts it over: the server has thrown all of it away, and what is
 * added here later is thrown away with it.
 */
export class SheetData {
  /**
   * The cells the server sent, by {@code toKey(col, row)}, and those a
   * committed edit changed until the server answers. The server only sends
   * cells with something in them: a cell empty on a sheet shown anew is never
   * mentioned, and a value kept from the sheet before would show through.
   */
  readonly cells = new Map<string, CellData>();

  /**
   * The cell the server holds as selected, as far as this client knows: the
   * one it last told the server, or the server told it. Null before either.
   */
  serverSelectedCell: { col: number; row: number } | null = null;
}
