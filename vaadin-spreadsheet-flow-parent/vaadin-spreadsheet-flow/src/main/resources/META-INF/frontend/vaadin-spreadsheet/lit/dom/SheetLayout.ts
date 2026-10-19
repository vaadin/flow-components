/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { SpreadsheetCellGrid } from '../elements/SpreadsheetCellGrid.js';
import type { SpreadsheetColumnHeader } from '../elements/SpreadsheetColumnHeader.js';
import type { SpreadsheetContextMenu } from '../elements/SpreadsheetContextMenu.js';
import type { SpreadsheetFormulaBar } from '../elements/SpreadsheetFormulaBar.js';
import type { SpreadsheetHeaderCorner } from '../elements/SpreadsheetHeaderCorner.js';
import type { SpreadsheetRowHeader } from '../elements/SpreadsheetRowHeader.js';
import type { SpreadsheetSheetTabs } from '../elements/SpreadsheetSheetTabs.js';
import { listen } from './Listen.js';

/** The parts of the sheet that are laid out. */
export interface LaidOutParts {
  readonly cellGrid: SpreadsheetCellGrid;
  readonly columnHeader: SpreadsheetColumnHeader;
  readonly rowHeader: SpreadsheetRowHeader;
  readonly headerCorner: SpreadsheetHeaderCorner;
  readonly formulaBar: SpreadsheetFormulaBar;
  readonly sheetTabs: SpreadsheetSheetTabs;
  readonly contextMenu: SpreadsheetContextMenu;
}

/**
 * The sheet's own elements, laid out: a column of the formula bar, the sheet
 * and the sheet tabs, and in the sheet a grid of the header corner, the
 * column letters, the row numbers and the cells, which scroll. Also follows
 * the host's classes and theme, which the parts cannot see from their shadow
 * roots.
 */
export class SheetLayout {
  /**
   * The component's own root.
   *
   * Everything the spreadsheet draws lives in here, and this element — not
   * the host — owns the vertical layout. The host belongs to the
   * application: writing a layout onto it means an application that
   * positions the component its own way is silently overridden.
   */
  readonly root: HTMLElement;

  /** The scroll container: the headers and the cells, scrolled together, and the keyboard's place on the sheet. */
  readonly scrollContainer: HTMLElement;

  /** Watches the host's classes and theme; see {@link followHost}. */
  private readonly hostObserver = new MutationObserver(() => this.applyHost());

  /**
   * @param host the component's element, whose classes and theme are followed
   * @param parts the elements laid out
   * @param signal ends the listeners
   * @param stripsChanged told after the host's classes show or hide the
   *   formula bar or the sheet tabs
   */
  constructor(
    private readonly host: HTMLElement,
    private readonly parts: LaidOutParts,
    signal: AbortSignal,
    private readonly stripsChanged: () => void
  ) {
    // A flex column holding the formula bar, the sheet, and the tabs.
    // `flex:1 1 auto` fills a host that has a definite height and
    // content-sizes against one that does not; a percentage height
    // would resolve to `auto` against a host whose own height is a
    // percentage, and the sheet would grow to the height of the whole
    // grid. Set on the element rather than in the stylesheet because
    // this is the component's own element and its sizing has to hold
    // wherever the connector is used, stylesheet or not.
    this.root = document.createElement('div');
    this.root.className = 'spreadsheet-root';
    // min-height is a floor: a spreadsheet nobody has given a height to is still a spreadsheet,
    // not a one-line strip.
    this.root.style.cssText =
      'display:flex;flex-direction:column;width:100%;flex:1 1 auto;' +
      // position:relative so the context menu, which is a child, is
      // placed in the sheet's own coordinate space rather than the
      // viewport's.
      'min-height:100px;box-sizing:border-box;position:relative;';
    this.scrollContainer = document.createElement('div');
    this.scrollContainer.className = 'sheet-scroll';
    // CSS Grid layout inside the scroll container: 2 columns ×
    // 2 rows so each header sits in its own cell. Column 1 =
    // row-header (auto-sized to its intrinsic width); column
    // 2 = cell-grid (auto-sized to its intrinsic width). Same
    // for rows. Sticky positioning on each header pins it to
    // its viewport edge during scroll.
    // `flex:1` so the container takes the remaining vertical
    // space inside the flex-column host, leaving room for the
    // sheet tabs below.
    // height:0 with flex:1 1 0 is what keeps the sheet from sizing the
    // component. A column flex item still offers its content height
    // when the container's own height is indefinite, and the grid
    // inside is as tall as the whole sheet, so a spreadsheet with no
    // height became thousands of pixels tall. A stated height of 0 is
    // definite, so it offers nothing, and flex-grow still gives it
    // every pixel left over once the host does have a height.
    // A sheet smaller than its view keeps to the top left corner: left to
    // stretch, the grid shares the room left over between the header track
    // and the cells', and the cells drop away from their headers.
    this.scrollContainer.style.cssText =
      'position:relative;overflow:auto;width:100%;flex:1 1 0;height:0;min-height:0;' +
      'display:grid;grid-template-columns:auto auto;' +
      'grid-template-rows:auto auto;align-content:start;justify-content:start;' +
      // outline:none so the browser-default focus ring doesn't
      // render around the spreadsheet; we get focus via
      // tabindex=0 but draw selection feedback via
      // the .sheet-selection overlay instead.
      'outline:none;' +
      // Cells and headers are not text to select: a drag selects cells, and a long press on a phone opens the sheet's
      // menu rather than the browser's. Fields typed into still select.
      'user-select:none;-webkit-user-select:none;-webkit-touch-callout:none;';
    // tabindex=0 makes the scroll container focusable so it can
    // receive keydown events for keyboard navigation.
    this.scrollContainer.tabIndex = 0;
    // To assistive technology the sheet is one control that takes its keys
    // for itself, rather than a page to read, named after the sheet shown.
    this.scrollContainer.setAttribute('role', 'application');
    this.scrollContainer.setAttribute('aria-roledescription', 'spreadsheet');
    this.scrollContainer.setAttribute('aria-label', 'Spreadsheet');
    const { cellGrid, columnHeader, rowHeader, headerCorner, formulaBar, sheetTabs, contextMenu } = parts;
    // Header corner at (col 1, row 1) — sticky on both axes
    // so it overlays both header strips at their intersection.
    // Hidden when either header dimension is 0.
    headerCorner.className = 'sheet-header-corner';
    headerCorner.style.cssText =
      'grid-column:1;grid-row:1;' + 'position:sticky;top:0;left:0;z-index:6;' + 'width:0;height:0;';
    // Column-header at (col 2, row 1) — top strip above cells.
    columnHeader.style.gridColumn = '2';
    columnHeader.style.gridRow = '1';
    // Row-header at (col 1, row 2) — left strip alongside cells.
    rowHeader.style.gridColumn = '1';
    rowHeader.style.gridRow = '2';
    // Cell-grid at (col 2, row 2).
    cellGrid.style.gridColumn = '2';
    cellGrid.style.gridRow = '2';
    this.scrollContainer.append(headerCorner, columnHeader, rowHeader, cellGrid);
    // The formula bar sits above the scroll container in the flex column,
    // and the sheet tabs below it, each at its intrinsic height. The
    // context menu goes beside the formula bar, in the root's space.
    formulaBar.style.flex = '0 0 auto';
    sheetTabs.style.flex = '0 0 auto';
    this.root.append(formulaBar, contextMenu, this.scrollContainer, sheetTabs);
    listen(this.scrollContainer, 'focusin', this.focusChanged, signal);
    listen(this.scrollContainer, 'focusout', this.focusChanged, signal);
  }

  /**
   * Starts following the host's classes and theme. The host's element is
   * light DOM, so no stylesheet in the parts' shadow roots can see them.
   */
  followHost(): void {
    this.applyHost();
    this.hostObserver.observe(this.host, { attributes: true, attributeFilter: ['class', 'theme'] });
  }

  /** The sheet's width, not the component's. See {@link setHeight}. */
  setWidth(width: string): void {
    this.root.style.width = width;
  }

  /**
   * Size the sheet, not the component.
   *
   * The wrapper asks for 100% as it starts the connector up, which is
   * the sheet filling whatever room the component has. Writing that on
   * the host instead threw away the size the application had given the
   * component with its own setHeight, and the sheet then took the
   * height of the page rather than the 250px it was asked for. The host
   * is the application's to size.
   */
  setHeight(height: string): void {
    this.root.style.height = height;
  }

  /** Stops following the host. */
  dispose(): void {
    this.hostObserver.disconnect();
  }

  private applyHost(): void {
    this.applyHostHideClasses();
    this.applyHostTheme();
  }

  /**
   * Read the spreadsheet host's class list + apply
   * the two public "hide bar" classes by hiding the matching
   * internal elements. The constants
   * {@code Spreadsheet#HIDE_FUNCTION_BAR_STYLE} ("hidefunctionbar")
   * and {@code Spreadsheet#HIDE_TABSHEET_STYLE} ("hidetabsheet")
   * are public JavaDoc-documented API; consumers call
   * {@code addClassName(HIDE_TABSHEET_STYLE)} on the host
   * (or use {@code setSheetSelectionBarVisible(false)} which
   * does the same) to suppress the corresponding strip.
   */
  private applyHostHideClasses(): void {
    this.parts.sheetTabs.hidden = this.host.classList.contains('hidetabsheet');
    this.parts.formulaBar.hidden = this.host.classList.contains('hidefunctionbar');
    this.stripsChanged();
  }

  /**
   * The host's theme variant, on every internal element as well.
   *
   * `Spreadsheet.setTheme(SpreadsheetTheme.LUMO)` sets `theme="lumo"` on
   * the host. Each internal element draws in its own shadow root, where no
   * selector can see the host's attributes, so each carries the attribute
   * itself and draws its theme under `:host([theme~='lumo'])`.
   */
  private applyHostTheme(): void {
    const theme = this.host.getAttribute('theme');
    const { formulaBar, contextMenu, headerCorner, columnHeader, rowHeader, cellGrid, sheetTabs } = this.parts;
    for (const element of [formulaBar, contextMenu, headerCorner, columnHeader, rowHeader, cellGrid, sheetTabs]) {
      if (theme === null) {
        element.removeAttribute('theme');
      } else if (element.getAttribute('theme') !== theme) {
        element.setAttribute('theme', theme);
      }
    }
  }

  /**
   * Tells the cell grid whether the sheet has the keyboard, which it shows
   * by dimming the selection when it does not, so a sheet left for another field does not look like it still takes the
   * typing. Read once the focus has settled: moving between the sheet and
   * its own cell editor fires a focusout and a focusin, and only the
   * outcome matters.
   */
  private readonly focusChanged = (): void => {
    queueMicrotask(() => {
      this.parts.cellGrid.toggleAttribute('sheet-focused', this.scrollContainer.matches(':focus-within'));
    });
  };
}
