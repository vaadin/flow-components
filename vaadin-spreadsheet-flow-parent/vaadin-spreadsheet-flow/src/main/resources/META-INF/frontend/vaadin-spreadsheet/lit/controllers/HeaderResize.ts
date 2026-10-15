/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { pixelsToPoints } from '../algorithms/ViewportMath.js';
import { listen } from '../dom/Listen.js';
import { hideFromTopLayer, showInTopLayer } from '../dom/TopLayer.js';
import type { SpreadsheetEvent } from '../types/rpc-client-to-server.js';

/** What resizing the columns and rows needs of the sheet. */
export interface HeaderResizeHost {
  /** Whether the sheet is protected; its format flags then decide. */
  sheetProtected(): boolean;
  /** A column's width, in pixels. */
  colWidthPx(col: number): number;
  /** A row's height, in pixels. */
  rowHeightPx(row: number): number;
  /** A row's height, in points, as the server keeps it. */
  rowHeightPt(row: number): number;
  /** Gives a column a width in pixels, 0 hiding it, and redraws. */
  setColumnWidth(col: number, width: number): void;
  /** Gives a row a height in points, 0 hiding it, and redraws. */
  setRowHeight(row: number, height: number): void;
  /** Tells the server. */
  send(event: SpreadsheetEvent): void;
}

/** The headers whose edges are dragged. */
export interface HeaderResizeTargets {
  columnHeader: EventTarget;
  rowHeader: EventTarget;
}

/**
 * A column or row edge being dragged: the size it gives runs from where the
 * column or row starts, `edge`, to the pointer at `x`, `y`.
 */
interface EdgeDrag {
  axis: 'column' | 'row';
  index: number;
  edge: number;
  x: number;
  y: number;
}

/**
 * Resizing columns and rows by their header edges, as the GWT client did.
 * The column or row keeps its size while the edge is dragged - a line and
 * a tooltip follow the pointer - and takes the new one when the drag ends,
 * here at once and then on the server; 0 hides it. A double-click on an
 * edge fits the column or row to its content, which only the server can
 * measure. A protected sheet allows either only where its format flags do.
 */
export class HeaderResize {
  /** The edge being dragged; null between drags. */
  private drag: EdgeDrag | null = null;

  /** Whether a protected sheet's columns keep their widths. */
  private lockFormatColumns = false;

  /** Whether a protected sheet's rows keep their heights. */
  private lockFormatRows = false;

  /** Where a dragged header edge would go: a line across the whole sheet. */
  private readonly line: HTMLElement;

  /** The size a dragged header would take, by the pointer. */
  private readonly tooltip: HTMLElement;

  /**
   * Over the sheet while a resize drag moves, so the resize cursor shows
   * wherever the pointer goes, as the GWT client's did. Not before the
   * pointer moves: the second click of a double-click, which fits the
   * column to its content, has to reach the handle.
   */
  private readonly cover: HTMLElement;

  constructor(
    private readonly root: HTMLElement,
    targets: HeaderResizeTargets,
    private readonly host: HeaderResizeHost,
    signal: AbortSignal
  ) {
    // What a header drag shows, as the GWT client showed it - see
    // showGuide. Drawn by the host's stylesheet.
    this.line = document.createElement('div');
    this.line.className = 'resize-line';
    this.line.hidden = true;
    this.cover = document.createElement('div');
    this.cover.className = 'resize-cover';
    this.cover.hidden = true;
    // In the top layer, as the GWT client's tooltip was outside the sheet,
    // so no edge cuts it off.
    this.tooltip = document.createElement('div');
    this.tooltip.className = 'resize-tooltip';
    this.tooltip.popover = 'manual';
    this.tooltip.hidden = true;
    listen(targets.columnHeader, 'column-resize-start', this.handleColumnResizeStart, signal);
    listen(targets.rowHeader, 'row-resize-start', this.handleRowResizeStart, signal);
    listen(targets.columnHeader, 'column-autofit', this.handleColumnAutofit, signal);
    listen(targets.rowHeader, 'row-autofit', this.handleRowAutofit, signal);
  }

  /** What a drag shows, in the order they go in the sheet: cover, line, tooltip. */
  elements(): HTMLElement[] {
    return [this.cover, this.line, this.tooltip];
  }

  setLockFormatColumns(locked: boolean): void {
    this.lockFormatColumns = locked;
  }

  setLockFormatRows(locked: boolean): void {
    this.lockFormatRows = locked;
  }

  /**
   * The pointer moved on the page: during a drag, the line and the tooltip
   * follow it, and the sheet stays as it is until the drag ends.
   */
  moved(e: MouseEvent): void {
    const drag = this.drag;
    if (drag === null) {
      return;
    }
    drag.x = e.clientX;
    drag.y = e.clientY;
    this.cover.hidden = false;
    this.showGuide();
  }

  /**
   * The pointer was let go: a dragged column takes the width from its left
   * edge to where the pointer is, and a row the height, when it is a
   * different one. Returns whether a drag ended.
   */
  finish(): boolean {
    const drag = this.drag;
    if (drag === null) {
      return false;
    }
    this.drag = null;
    this.hideGuide();
    const { axis, index, edge, x, y } = drag;
    if (axis === 'column') {
      const width = Math.max(0, Math.round(x - edge));
      if (width !== this.host.colWidthPx(index)) {
        this.host.setColumnWidth(index, width);
        this.host.send({ type: 'columnResized', data: { colIndexes: [index], newWidths: [width] } });
      }
      return true;
    }
    // The row's height and the event's are in points.
    const height = pixelsToPoints(Math.max(0, Math.round(y - edge)));
    if (height !== this.host.rowHeightPt(index)) {
      this.host.setRowHeight(index, height);
      this.host.send({ type: 'rowsResized', data: { rowIndexes: [index], newHeights: [height] } });
    }
    return true;
  }

  /**
   * Whether the user may change column widths.
   *
   * A protected sheet can still allow formatting; `lockFormatColumns` is
   * the workbook's own flag for that, and the server sends it precisely so
   * the client can refuse the drag rather than let it happen and be undone
   * by the next repaint.
   */
  private canResizeColumn(): boolean {
    return !this.host.sheetProtected() || !this.lockFormatColumns;
  }

  /** Whether the user may change row heights. See {@link canResizeColumn}. */
  private canResizeRow(): boolean {
    return !this.host.sheetProtected() || !this.lockFormatRows;
  }

  /**
   * Column resize drag — start. The header reports where the column starts
   * on screen; without that, the drag is taken to start on the column's
   * right edge.
   */
  private readonly handleColumnResizeStart = (
    e: CustomEvent<{ col: number; clientX: number; clientY?: number; left?: number }>
  ): void => {
    if (!this.canResizeColumn()) {
      return;
    }
    const { col, clientX, clientY = 0, left } = e.detail;
    const edge = left ?? clientX - this.host.colWidthPx(col);
    this.drag = { axis: 'column', index: col, edge, x: clientX, y: clientY };
    this.showGuide();
  };

  /** Row resize drag — start. Mirror of the column's. */
  private readonly handleRowResizeStart = (
    e: CustomEvent<{ row: number; clientX?: number; clientY: number; top?: number }>
  ): void => {
    if (!this.canResizeRow()) {
      return;
    }
    const { row, clientX = 0, clientY, top } = e.detail;
    const edge = top ?? clientY - this.host.rowHeightPx(row);
    this.drag = { axis: 'row', index: row, edge, x: clientX, y: clientY };
    this.showGuide();
  };

  /**
   * Double-click on a column's resize handle — size it to its widest
   * value. Only the server can measure that: it holds every value in the
   * column, including the ones outside the viewport.
   */
  private readonly handleColumnAutofit = (e: CustomEvent<{ col: number }>): void => {
    if (!this.canResizeColumn()) {
      return;
    }
    this.host.send({ type: 'onColumnAutofit', data: { columnIndex: e.detail.col } });
  };

  /** Double-click on a row's resize handle — size it to its tallest value. */
  private readonly handleRowAutofit = (e: CustomEvent<{ row: number }>): void => {
    if (!this.canResizeRow()) {
      return;
    }
    this.host.send({ type: 'onRowAutofit', data: { rowIndex: e.detail.row } });
  };

  /**
   * The line and the tooltip of a resize drag, as the GWT client drew them:
   * a line across the whole sheet where the edge would go - no further
   * back than where the column or row starts - and the size it would make,
   * 10px right of the pointer and 25px above it. Nothing at all is
   * "Hide column" or "Hide row"; a row's height shows in points too.
   */
  private showGuide(): void {
    const drag = this.drag;
    if (drag === null) {
      return;
    }
    const origin = this.root.getBoundingClientRect();
    const line = this.line;
    const { axis, edge, x, y } = drag;
    let text: string;
    if (axis === 'column') {
      const width = Math.max(0, Math.round(x - edge));
      text = width > 0 ? `Width: ${width}px` : 'Hide column';
      const at = Math.max(x, edge) - 1 - origin.left - this.root.clientLeft;
      line.style.cssText = `left:${at}px;top:0;width:1px;height:100%;`;
    } else {
      const height = Math.max(0, Math.round(y - edge));
      text = height > 0 ? `Height: ${height}px ≈ ${pixelsToPoints(height)}pt` : 'Hide row';
      const at = Math.max(y, edge) - 1 - origin.top - this.root.clientTop;
      line.style.cssText = `left:0;top:${at}px;width:100%;height:1px;`;
    }
    line.hidden = false;
    this.cover.classList.toggle('rows', axis === 'row');
    const tooltip = this.tooltip;
    tooltip.textContent = text;
    tooltip.style.left = `${x + 10}px`;
    tooltip.style.top = `${y - 25}px`;
    tooltip.hidden = false;
    showInTopLayer(tooltip);
  }

  /** Take the line, the tooltip and the cover of a resize drag away. */
  private hideGuide(): void {
    this.line.hidden = true;
    this.cover.hidden = true;
    this.tooltip.hidden = true;
    hideFromTopLayer(this.tooltip);
  }
}
