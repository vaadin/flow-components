/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { listen } from '../dom/Listen.js';
import type { SpreadsheetEvent } from '../types/rpc-client-to-server.js';

/** Where the outline's buttons are. */
export interface OutlineTargets {
  /** The column header, with the column groups' +/- buttons. */
  readonly columnHeader: EventTarget;
  /** The row header, with the row groups' +/- buttons. */
  readonly rowHeader: EventTarget;
  /** The corner, with the outline level buttons. */
  readonly headerCorner: EventTarget;
}

/**
 * The outline of grouped rows and columns: a group's +/- button collapses or
 * expands it, and a level button shows the sheet down to that level. The
 * server does both and sends the sheet back; the client only tells it.
 */
export class Outline {
  constructor(targets: OutlineTargets, private readonly send: (event: SpreadsheetEvent) => void, signal: AbortSignal) {
    listen(targets.columnHeader, 'column-grouping-toggle', this.handleColumnGroupingToggle, signal);
    listen(targets.rowHeader, 'row-grouping-toggle', this.handleRowGroupingToggle, signal);
    listen(targets.headerCorner, 'level-header-click', this.handleLevelHeaderClick, signal);
  }

  /**
   * A column group's +/- button. {@code colIndex} names the group by its
   * {@code uniqueIndex}, and {@code collapsed} is the state asked for.
   */
  private readonly handleColumnGroupingToggle = (e: CustomEvent<{ uniqueIndex: number; collapsed: boolean }>): void => {
    const { uniqueIndex, collapsed } = e.detail;
    this.send({ type: 'groupingCollapsed', data: { cols: true, colIndex: uniqueIndex, collapsed } });
  };

  /** A row group's +/- button, as {@link handleColumnGroupingToggle} for rows. */
  private readonly handleRowGroupingToggle = (e: CustomEvent<{ uniqueIndex: number; collapsed: boolean }>): void => {
    const { uniqueIndex, collapsed } = e.detail;
    this.send({ type: 'groupingCollapsed', data: { cols: false, colIndex: uniqueIndex, collapsed } });
  };

  /**
   * An outline level button: the server expands everything shallower and
   * collapses that level, which hides whatever is nested under it.
   */
  private readonly handleLevelHeaderClick = (e: CustomEvent<{ cols: boolean; level: number }>): void => {
    this.send({ type: 'levelHeaderClicked', data: { cols: e.detail.cols, level: e.detail.level } });
  };
}
