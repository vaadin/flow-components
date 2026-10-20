/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { serializeToTsv } from '../algorithms/Tsv.js';
import { listen } from '../dom/Listen.js';
import type { CellRange } from '../state/SelectionState.js';
import type { SpreadsheetEvent } from '../types/rpc-client-to-server.js';

/** What the clipboard needs of the sheet. */
export interface ClipboardHost {
  /** Whether a cell is being edited: its editor then has the clipboard. */
  isEditing(): boolean;
  /** The range a copy or a cut takes: the active one. */
  activeRange(): CellRange;
  /** The value of a cell, if the client has one. */
  valueAt(col: number, row: number): string | null | undefined;
  /** Whether the client has every cell of a range. */
  hasCells(range: CellRange): boolean;
  /**
   * Puts the server's text for a range on the clipboard, once it comes; false
   * when it cannot - no server to ask, or no clipboard to write to later.
   */
  copyLater(range: CellRange): boolean;
  /** Tells the server. */
  send(event: SpreadsheetEvent): void;
}

/**
 * The sheet's clipboard. Copy puts the active range on the clipboard as
 * tab-separated text; cut does too, and asks the server to clear the range;
 * paste hands the server the clipboard's text, which it writes from the
 * active cell. While a cell is edited, all three are the editor's. The
 * browser sends them to the focused element, so the target is the one the
 * sheet takes the focus with. Listens until the signal is aborted.
 *
 * A range the client has goes on the clipboard at once; the text of one it
 * does not have comes from the server, and goes on the clipboard when it
 * comes. The server does not send every selection to the client in case it
 * is copied: that would be 10 MB for one column of 100,000 rows.
 */
export function listenToClipboard(target: EventTarget, host: ClipboardHost, signal: AbortSignal): void {
  // Whether the range went on the clipboard.
  const copy = (e: ClipboardEvent): boolean => {
    if (host.isEditing()) {
      return false;
    }
    const range = host.activeRange();
    e.preventDefault();
    if (host.hasCells(range) || !host.copyLater(range)) {
      e.clipboardData?.setData('text/plain', rangeAsTsv(host, range));
    }
    return true;
  };
  listen(target, 'copy', copy, signal);
  listen(
    target,
    'cut',
    (e: ClipboardEvent) => {
      if (copy(e)) {
        host.send({ type: 'clearSelectedCellsOnCut', data: {} });
      }
    },
    signal
  );
  listen(
    target,
    'paste',
    (e: ClipboardEvent) => {
      if (host.isEditing()) {
        return;
      }
      const text = e.clipboardData?.getData('text/plain') ?? '';
      if (text === '') {
        return;
      }
      e.preventDefault();
      host.send({ type: 'onPaste', data: { text } });
    },
    signal
  );
}

/** A range's values as tab-separated text, a cell without a value as empty. */
function rangeAsTsv(host: ClipboardHost, range: CellRange): string {
  const rows: (string | null)[][] = [];
  for (let r = range.r1; r <= range.r2; r++) {
    const row: (string | null)[] = [];
    for (let c = range.c1; c <= range.c2; c++) {
      row.push(host.valueAt(c, r) ?? '');
    }
    rows.push(row);
  }
  return serializeToTsv(rows);
}
