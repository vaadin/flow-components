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
import type { SlottedVirtualChildren } from '../dom/VirtualChildren.js';
import { emptyContextMenuRenderState, type SpreadsheetContextMenu } from '../elements/SpreadsheetContextMenu.js';
import type { SpreadsheetEvent } from '../types/rpc-client-to-server.js';
import type { SpreadsheetActionDetails } from '../types/wire.js';

/** What the context menu needs of the sheet. */
export interface ContextMenuHost {
  /**
   * Selects a right-clicked cell outside the selection, as a click on it
   * would, so that the menu acts on it; a cell in the selection leaves it.
   */
  selectForMenu(col: number, row: number): void;
  /** Gives the keyboard back to the sheet, unless a custom editor has it. */
  returnFocusToSheet(): void;
  /** Tells the server. */
  send(event: SpreadsheetEvent): void;
}

/** The parts of the sheet a right-click on which opens the menu. */
export interface ContextMenuTargets {
  cells: EventTarget;
  columnHeader: EventTarget;
  rowHeader: EventTarget;
}

/**
 * The sheet's context menu. A right-click on a cell or a header asks the
 * server what it offers there; the menu opens where the click landed once
 * the server answers, and a pick is reported against what was clicked.
 * Without actions there is no menu, and the browser's own opens instead.
 */
export class ContextMenu {
  /** Whether the server has actions to offer at all. */
  private hasActions = false;

  /**
   * Where the most recent right-click landed, in the menu's own space, so
   * the menu opens there: the click and the server's answer arrive in
   * separate turns.
   */
  private position: { x: number; y: number } = { x: 0, y: 0 };

  constructor(
    private readonly element: SpreadsheetContextMenu,
    private readonly icons: SlottedVirtualChildren,
    private readonly root: HTMLElement,
    targets: ContextMenuTargets,
    private readonly host: ContextMenuHost,
    signal: AbortSignal
  ) {
    listen(targets.cells, 'cell-contextmenu', this.handleCellContextMenu, signal);
    listen(targets.columnHeader, 'column-header-contextmenu', this.handleColumnHeaderContextMenu, signal);
    listen(targets.rowHeader, 'row-header-contextmenu', this.handleRowHeaderContextMenu, signal);
    listen(element, 'context-menu-action', this.handleContextMenuAction, signal);
    listen(element, 'context-menu-dismiss', this.handleContextMenuDismiss, signal);
  }

  setHasActions(hasActions: boolean): void {
    this.hasActions = hasActions;
  }

  /** A press somewhere on the page: anywhere but in the menu, it closes it. */
  closeUnlessPressedIn(path: EventTarget[]): void {
    if (!this.element.renderState.open || path.includes(this.element)) {
      return;
    }
    this.close();
  }

  /**
   * Right-click on a cell. Excel behavior: if the
   * clicked cell is outside the current selection, move selection
   * to it first (firing a selection event), then dispatch
   * {@code contextMenuOpenOnSelection}. If inside the existing
   * selection, leave the selection alone and dispatch the event
   * with the clicked cell's coordinates.
   */
  private readonly handleCellContextMenu = (
    e: CustomEvent<{ col: number; row: number; clientX?: number; clientY?: number }>
  ): void => {
    const { col, row } = e.detail;
    // Without actions there is no menu, and the GWT client left the
    // selection alone.
    if (!this.hasActions) {
      return;
    }
    // Taken for the sheet's own menu: the browser's stays closed.
    e.preventDefault();
    this.remember(e.detail);
    this.host.selectForMenu(col, row);
    this.host.send({
      type: 'contextMenuOpenOnSelection',
      data: { row, column: col }
    });
  };


  /**
   * Right-click on a column header: asks the server for the
   * column's actions with {@code columnHeaderContextMenuOpen}.
   */
  private readonly handleColumnHeaderContextMenu = (
    e: CustomEvent<{ col: number; clientX?: number; clientY?: number }>
  ): void => {
    const { col } = e.detail;
    // The menu acts on the column whatever is selected, so the selection
    // stays as it is, as in the GWT client - and there is no menu without
    // actions.
    if (!this.hasActions) {
      return;
    }
    // Taken for the sheet's own menu: the browser's stays closed.
    e.preventDefault();
    this.remember(e.detail);
    this.host.send({
      type: 'columnHeaderContextMenuOpen',
      data: { columnIndex: col }
    });
  };


  /**
   * Right-click on a row header. Mirror of the column
   * variant — dispatches {@code rowHeaderContextMenuOpen}.
   */
  private readonly handleRowHeaderContextMenu = (
    e: CustomEvent<{ row: number; clientX?: number; clientY?: number }>
  ): void => {
    const { row } = e.detail;
    // As for a column: the selection stays, and no actions means no menu.
    if (!this.hasActions) {
      return;
    }
    // Taken for the sheet's own menu: the browser's stays closed.
    e.preventDefault();
    this.remember(e.detail);
    this.host.send({
      type: 'rowHeaderContextMenuOpen',
      data: { rowIndex: row }
    });
  };


  /** The actions the server offers for the right-click: the menu opens with them. */
  showActions(actions: SpreadsheetActionDetails[]): void {
    this.element.renderState = {
      open: true,
      x: this.position.x,
      y: this.position.y,
      // Arrives JSON-serialized through the Flow lane, like the
      // collection-shaped properties.
      actions
    };
    // An action can carry an icon component; 0 means it has none. The
    // slots that show them are in the menu's template, so place the
    // elements only once that has rendered.
    const iconIds = actions
      .filter((action) => Number(action.iconNodeId) > 0)
      .map((action) => String(action.iconNodeId));
    void this.element.updateComplete.then(() => this.icons.sync(iconIds));
  }


  /**
   * Note where a right-click landed. The menu is placed here once the server
   * answers with the actions it offers.
   */
  private remember(detail: { clientX?: number; clientY?: number }): void {
    // Kept relative to the sheet's own box, because that is the space
    // the menu is placed in. Viewport coordinates stop meaning the
    // viewport as soon as an ancestor has a transform - a dialog
    // overlay has one while it animates - and the menu then opens
    // somewhere other than where the user clicked.
    // From inside the root's border, where the menu's space starts.
    const origin = this.root.getBoundingClientRect();
    this.position = {
      x: (detail.clientX ?? 0) - origin.left - this.root.clientLeft,
      y: (detail.clientY ?? 0) - origin.top - this.root.clientTop
    };
  }


  /**
   * Close the context menu and tell the server, unless it is already closed.
   */
  close(): void {
    if (!this.element.renderState.open) {
      return;
    }
    // The menu had the keyboard; the GWT client gave it back to where it
    // had been. A click elsewhere still moves it on, once this is done.
    const menuHadFocus = this.element.shadowRoot?.activeElement != null;
    this.element.renderState = emptyContextMenuRenderState();
    this.icons.clear();
    if (menuHadFocus) {
      this.host.returnFocusToSheet();
    }
    this.host.send({ type: 'contextMenuClosed', data: {} });
  }


  /** An item was picked: report it against whatever the menu was opened on. */
  private readonly handleContextMenuAction = (e: CustomEvent<{ key: string; type: number }>): void => {
    const { key, type } = e.detail;
    // 0 = cell, 1 = row, 2 = column, matching SpreadsheetActionDetails.
    const eventType =
      type === 1 ? 'actionOnRowHeader' : type === 2 ? 'actionOnColumnHeader' : 'actionOnCurrentSelection';
    this.element.renderState = emptyContextMenuRenderState();
    this.icons.clear();
    // The menu item had the focus; picking it is done, so the keyboard
    // goes back to the sheet.
    this.host.returnFocusToSheet();
    this.host.send({ type: eventType, data: { actionKey: key } });
  };


  /** Left or Tab in the menu closes it, as in the GWT client. */
  private readonly handleContextMenuDismiss = (): void => {
    this.close();
  };
}
