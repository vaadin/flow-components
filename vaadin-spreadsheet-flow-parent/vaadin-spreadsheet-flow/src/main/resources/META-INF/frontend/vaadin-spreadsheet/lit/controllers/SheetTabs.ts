/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { Memo } from '../algorithms/Memo.js';
import { resolveSheetRename } from '../algorithms/SheetName.js';
import { listen } from '../dom/Listen.js';
import type { SheetTabsRenderState, SpreadsheetSheetTabs } from '../elements/SpreadsheetSheetTabs.js';
import type { SpreadsheetEvent } from '../types/rpc-client-to-server.js';

/** What the sheet tabs need of the sheet. */
export interface SheetTabsHost {
  /** Where the sheet is scrolled to, which a switch and a new sheet report. */
  scrollOffsets(): { scrollLeft: number; scrollTop: number };
  /** Gives the keyboard to the sheet, as switching to a sheet moves to it. */
  focusSheet(): void;
  /** Gives the keyboard back to the sheet, unless a custom editor has it. */
  returnFocusToSheet(): void;
  /** Tells the server. */
  send(event: SpreadsheetEvent): void;
  /** Asks for the sheet to be drawn, the strip with it; see {@link SheetTabs.renderState}. */
  scheduleRender(): void;
  /**
   * Runs a handler of the strip's events as one of the sheet's own: what it
   * changes is drawn as it ends, and before it tells the server.
   */
  handle(work: () => void): void;
}

/**
 * The sheet tabs: which sheets there are and which one shows, the tab the
 * strip starts at, a name being typed, and the status label at the end.
 * A click on a tab switches to its sheet, the + button asks for a new one,
 * and a double-click on the active tab renames it; a protected workbook
 * allows neither of the last two.
 */
export class SheetTabs {
  /** The visible sheets' names, in order. */
  sheetNames: string[] = [];
  /** The sheet shown, 1-based among the visible sheets; 0 before any. */
  sheetIndex = 0;

  /**
   * The leftmost tab the strip shows, 0-based. The strip scrolls by whole
   * tabs, and which one is leftmost is the client's alone.
   */
  private firstVisibleTab = 0;

  /**
   * The tab whose name the user is typing, 1-based, or 0 when none is. The
   * server hears of a rename once, when it is finished.
   */
  private renamingIndex = 0;

  /** Whether the workbook is protected, so no sheet is added or renamed. */
  private workbookProtected = false;

  /** The status label, or null while there is none. */
  private infoLabel: string | null = null;

  /** What the strip draws, kept while nothing it shows changes. */
  private readonly drawn = new Memo<SheetTabsRenderState>();

  constructor(strip: SpreadsheetSheetTabs, private readonly host: SheetTabsHost, signal: AbortSignal) {
    const on = <E extends Event>(type: string, listener: (e: E) => void): void =>
      listen<E>(strip, type, (e) => host.handle(() => listener(e)), signal);
    on('sheet-tab-click', this.handleSheetTabClick);
    on('sheet-tabs-scroll', this.handleSheetTabsScroll);
    on('sheet-tab-add', this.handleSheetTabAdd);
    on('sheet-tab-rename-start', this.handleSheetRenameStart);
    on('sheet-tab-rename', this.handleSheetRename);
    on('sheet-tab-rename-cancel', this.handleSheetRenameCancel);
  }

  /** The name of the sheet shown, or '' before there is one. */
  shownSheetName(): string {
    return this.sheetNames[this.sheetIndex - 1] ?? '';
  }

  /** The visible sheets' names, as the server sends them. */
  setSheetNames(names: string[]): void {
    this.sheetNames = names;
    this.changed();
  }

  /** The sheet the server shows, 1-based. */
  setSheetIndex(index: number): void {
    this.sheetIndex = index;
    this.changed();
  }

  /** Whether the workbook is protected: the strip then offers no + button. */
  setWorkbookProtected(isProtected: boolean): void {
    this.workbookProtected = isProtected;
    this.changed();
  }

  /** The status label, or null for none. */
  setInfoLabel(label: string | null): void {
    this.infoLabel = label;
    this.changed();
  }

  /**
   * Sheet-tab click handler — translates to a `sheetSelected`
   * spreadsheet-event for the server. The server is expected
   * to call back into {@code setSheetIndex} + push fresh data
   * via the cell value APIs, which the next render picks up.
   */
  private readonly handleSheetTabClick = (e: CustomEvent<{ index: number }>): void => {
    const { index } = e.detail;
    // Clicking a tab focused it. Switching sheets is navigation, so the
    // keyboard goes back to the grid — the arrow keys that naturally follow
    // must move the selection, not the tab strip. This holds for a click on
    // the already-active tab too, which is otherwise a no-op.
    this.host.focusSheet();
    if (index === this.sheetIndex) {
      // Clicking the tab that is already active selects nothing — Excel
      // does nothing here either. Reporting it anyway makes the server run
      // `setActiveSheetIndex` on the sheet it is already showing, and that
      // goes through `reloadActiveSheetData`, which throws away every
      // overlay, comment, hyperlink, table and popup button `loadCells`
      // had just loaded.
      return;
    }
    // Optimistically reflect the click in the tab strip so the
    // active tab visually moves immediately, without waiting
    // for the server's response. The server's setSheetIndex
    // will overwrite this when it lands.
    this.sheetIndex = index;
    this.changed();
    this.host.send({
      type: 'sheetSelected',
      data: {
        // The server reads a 0-based tab index here; `index` is the
        // 1-based position the strip renders.
        sheetIndex: index - 1,
        ...this.host.scrollOffsets()
      }
    });
  };

  /** The + button: ask the server for a new sheet. */
  private readonly handleSheetTabAdd = (): void => {
    if (this.workbookProtected) {
      return;
    }
    // The strip button took the focus on the way in. Adding a sheet is a
    // way of moving to it, so the keyboard belongs to the new sheet.
    this.host.focusSheet();
    this.host.send({
      type: 'sheetCreated',
      data: this.host.scrollOffsets()
    });
  };

  /** Double-click on the active tab: show its name in an input. */
  private readonly handleSheetRenameStart = (e: CustomEvent<{ index: number }>): void => {
    if (this.workbookProtected) {
      return;
    }
    this.renamingIndex = e.detail.index;
    this.changed();
  };

  /** Escape: leave the sheet called what it was called. */
  private readonly handleSheetRenameCancel = (): void => {
    this.renamingIndex = 0;
    this.changed();
    this.host.returnFocusToSheet();
  };

  /**
   * A finished rename.
   *
   * A name the workbook would refuse, one that is what the sheet is
   * already called, and one another sheet has taken all leave the tab
   * as it was and the server none the wiser. Anything else is shown on
   * the tab straight away - the server will send the names back, and
   * waiting for that leaves the old name sitting there in the meantime.
   */
  private readonly handleSheetRename = (e: CustomEvent<{ index: number; name: string }>): void => {
    const { index, name } = e.detail;
    this.renamingIndex = 0;
    if (index < 1 || index > this.sheetNames.length) {
      this.changed();
      return;
    }
    const outcome = resolveSheetRename({
      proposed: name,
      index: index - 1,
      names: this.sheetNames
    });
    if (outcome.kind === 'revert') {
      this.changed();
      this.host.returnFocusToSheet();
      return;
    }
    const renamed = [...this.sheetNames];
    renamed[index - 1] = outcome.name;
    this.sheetNames = renamed;
    this.changed();
    this.host.returnFocusToSheet();
    this.host.send({
      // The server counts sheets from 0; the strip renders from 1.
      type: 'sheetRenamed',
      data: { sheetIndex: index - 1, newName: outcome.name }
    });
  };

  /**
   * A scroll button moved the strip.
   *
   * Nothing is reported: the server has no use for it, so which
   * tab is leftmost is the client's business alone. Scrolling the strip
   * does not change the sheet.
   *
   * The end button goes to the leftmost tab that still shows the last
   * one, which the strip measured: as far as the strip can scroll
   * without showing empty space after the last tab. A change of the active
   * sheet goes to where the strip measured that its tab shows whole.
   */
  private readonly handleSheetTabsScroll = (
    e: CustomEvent<{
      direction: 'start' | 'backward' | 'forward' | 'end' | 'reveal';
      lastFirstTab?: number;
      firstVisibleTab?: number;
    }>
  ): void => {
    const last = Math.max(0, this.sheetNames.length - 1);
    const moved = {
      start: 0,
      backward: this.firstVisibleTab - 1,
      forward: this.firstVisibleTab + 1,
      end: e.detail.lastFirstTab ?? last,
      reveal: e.detail.firstVisibleTab ?? this.firstVisibleTab
    }[e.detail.direction];
    const next = Math.min(last, Math.max(0, moved));
    if (next === this.firstVisibleTab) {
      return;
    }
    this.firstVisibleTab = next;
    this.changed();
  };

  /**
   * What the strip draws. The sheet draws it with the rest of the sheet, on
   * the drawing a change asks for; see {@link changed}.
   */
  renderState(): SheetTabsRenderState {
    return this.drawn.get(
      [
        this.sheetNames,
        this.sheetIndex,
        this.firstVisibleTab,
        this.workbookProtected,
        this.renamingIndex,
        this.infoLabel
      ],
      () => ({
        sheetNames: this.sheetNames,
        activeIndex: this.sheetIndex,
        firstVisibleTab: this.firstVisibleTab,
        readOnly: this.workbookProtected,
        renamingIndex: this.renamingIndex,
        infoLabel: this.infoLabel
      })
    );
  }

  /** Something the strip shows changed: the sheet is drawn, the strip with it. */
  private changed(): void {
    this.host.scheduleRender();
  }
}
