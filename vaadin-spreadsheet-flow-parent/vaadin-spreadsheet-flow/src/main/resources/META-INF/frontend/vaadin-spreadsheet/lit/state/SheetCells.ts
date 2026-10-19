/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { parseColRow } from '../algorithms/CellKeys.js';
import type { CellData } from '../types/wire.js';

/**
 * The cells of a sheet by {@code toKey(col, row)}, counting the changes to
 * each row. The map is changed in place - the server sends a few cells at a
 * time - so what draws a row cannot tell a change from the map itself; it
 * can from {@link rowVersion}, without looking at the row's cells.
 */
export class SheetCells extends Map<string, CellData> {
  /** The changes made so far: each cell set or deleted, and each clearing, is one. */
  private changes = 0;
  /** The change each row was last changed by. */
  private readonly rowChanges = new Map<number, number>();
  /** The change the map was last cleared by: that of every row no cell was set in since. */
  private clearedBy = 0;

  // Empty to start with: Map's constructor adds the entries it is given with
  // set, before the fields above are there.
  constructor() {
    super();
  }

  override set(key: string, cell: CellData): this {
    super.set(key, cell);
    this.changed(key);
    return this;
  }

  override delete(key: string): boolean {
    const deleted = super.delete(key);
    if (deleted) {
      this.changed(key);
    }
    return deleted;
  }

  override clear(): void {
    super.clear();
    this.rowChanges.clear();
    this.clearedBy = ++this.changes;
  }

  /** A number that stays the same until a cell of the row is set or deleted, or the map is cleared. */
  rowVersion(row: number): number {
    return this.rowChanges.get(row) ?? this.clearedBy;
  }

  private changed(key: string): void {
    this.rowChanges.set(parseColRow(key).row, ++this.changes);
  }
}
