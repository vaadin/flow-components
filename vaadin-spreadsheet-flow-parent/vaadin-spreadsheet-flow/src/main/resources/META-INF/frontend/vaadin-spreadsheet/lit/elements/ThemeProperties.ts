/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css } from 'lit';

/*
 * The documented styling properties, each as the sheet reads it: the
 * application's value; else the theme's, a --_vaadin-spreadsheet-* property
 * a theme sets on the host; else its own default. Every default
 * is written here once. The properties are read, never declared, so a value
 * set on any ancestor reaches the sheet.
 */

/** Color of borders. */
export const borderColor = css`var(--vaadin-spreadsheet-border-color, var(--_vaadin-spreadsheet-border-color, #c7c7c7))`;

/** Background of a cell. */
export const cellBackground = css`var(--vaadin-spreadsheet-cell-background, var(--_vaadin-spreadsheet-cell-background, #fff))`;

/** Color of cell borders. */
export const cellBorderColor = css`var(--vaadin-spreadsheet-cell-border-color, var(--_vaadin-spreadsheet-cell-border-color, #c7c7c7))`;

/** Tint over the selected range. */
export const cellRangeBg = css`var(--vaadin-spreadsheet-cell-range-bg, var(--_vaadin-spreadsheet-cell-range-bg, rgba(75, 155, 235, 0.1)))`;

/** Color of the comment indicator. */
export const commentBg = css`var(--vaadin-spreadsheet-comment-bg, var(--_vaadin-spreadsheet-comment-bg, #ffcf16))`;

/** Background of an open comment. */
export const commentOverlayBg = css`var(--vaadin-spreadsheet-comment-overlay-bg, var(--_vaadin-spreadsheet-comment-overlay-bg, #fff))`;

/** Color of an open comment's edge. */
export const commentOverlayBorderColor = css`var(--vaadin-spreadsheet-comment-overlay-border-color, var(--_vaadin-spreadsheet-comment-overlay-border-color, rgba(0, 0, 0, 0.09)))`;

/** Background of the context menu. */
export const contextmenuBg = css`var(--vaadin-spreadsheet-contextmenu-bg, var(--_vaadin-spreadsheet-contextmenu-bg, #fff))`;

/** Color of the context menu's edge. */
export const contextmenuBorder = css`var(--vaadin-spreadsheet-contextmenu-border, var(--_vaadin-spreadsheet-contextmenu-border, rgba(0, 0, 0, 0.09)))`;

/** Background of the context-menu item under the pointer or with keyboard focus. */
export const contextmenuHoverBg = css`var(--vaadin-spreadsheet-contextmenu-hover-bg, var(--_vaadin-spreadsheet-contextmenu-hover-bg, linear-gradient(to bottom, #1b87e3 2%, #166ed6 98%)))`;

/** Color of the cell editor's outline. */
export const editorBorder = css`var(--vaadin-spreadsheet-editor-border, var(--_vaadin-spreadsheet-editor-border, transparent))`;

/** Background of the formula bar. */
export const formulabarBg = css`var(--vaadin-spreadsheet-formulabar-bg, var(--_vaadin-spreadsheet-formulabar-bg, #fff))`;

/** Font of the formula bar. */
export const formulabarFont = css`var(--vaadin-spreadsheet-formulabar-font, var(--_vaadin-spreadsheet-formulabar-font, Helvetica))`;

/** Size of formula-bar text. */
export const formulabarFontSize = css`var(--vaadin-spreadsheet-formulabar-font-size, var(--_vaadin-spreadsheet-formulabar-font-size, 14px))`;

/** Background of a grouping toggle. */
export const groupingToggleBg = css`var(--vaadin-spreadsheet-grouping-toggle-bg, var(--_vaadin-spreadsheet-grouping-toggle-bg, #c7c7c7))`;

/** Color of a grouping toggle's border. */
export const groupingToggleBorderColor = css`var(--vaadin-spreadsheet-grouping-toggle-border-color, var(--_vaadin-spreadsheet-grouping-toggle-border-color, #c7c7c7))`;

/** Background of the row and column header strips. */
export const headerBg = css`var(--vaadin-spreadsheet-header-bg, var(--_vaadin-spreadsheet-header-bg, #fafafa))`;

/** Color of header borders. */
export const headerBorderColor = css`var(--vaadin-spreadsheet-header-border-color, var(--_vaadin-spreadsheet-header-border-color, #c7c7c7))`;

/** Font of the row and column headers. */
export const headerFont = css`var(--vaadin-spreadsheet-header-font, var(--_vaadin-spreadsheet-header-font, Helvetica))`;

/** Size of header text. */
export const headerFontSize = css`var(--vaadin-spreadsheet-header-font-size, var(--_vaadin-spreadsheet-header-font-size, 13px))`;

/** Background of the active cell's row and column headers. */
export const headerSelectedBg = css`var(--vaadin-spreadsheet-header-selected-bg, var(--_vaadin-spreadsheet-header-selected-bg, #e6edf4))`;

/** Color of the invalid-formula indicator. */
export const invalidformulaBg = css`var(--vaadin-spreadsheet-invalidformula-bg, var(--_vaadin-spreadsheet-invalidformula-bg, #ed473b))`;

/** Background of a pop-up. */
export const popupBackground = css`var(--vaadin-spreadsheet-popup-background, var(--_vaadin-spreadsheet-popup-background, #fff))`;

/** Color of a pop-up's edge. */
export const popupBorderColor = css`var(--vaadin-spreadsheet-popup-border-color, var(--_vaadin-spreadsheet-popup-border-color, rgba(0, 0, 0, 0.09)))`;

/** Background of a pop-up button whose pop-up is open. */
export const popupButtonActiveBg = css`var(--vaadin-spreadsheet-popup-button-active-bg, var(--_vaadin-spreadsheet-popup-button-active-bg, #fff))`;

/** Background of a pop-up button. */
export const popupButtonBg = css`var(--vaadin-spreadsheet-popup-button-bg, var(--_vaadin-spreadsheet-popup-button-bg, #fff))`;

/** Color of pop-up button text. */
export const popupButtonFg = css`var(--vaadin-spreadsheet-popup-button-fg, var(--_vaadin-spreadsheet-popup-button-fg, #808080))`;

/** Color of the fill handle and the fill-drag preview outline. */
export const selectionBorderColor = css`var(--vaadin-spreadsheet-selection-border-color, var(--_vaadin-spreadsheet-selection-border-color, #40b527))`;

/** Color of the selection outline. */
export const selectionColor = css`var(--vaadin-spreadsheet-selection-color, var(--_vaadin-spreadsheet-selection-color, #197de1))`;

/** Color of the buttons on the sheet-tab strip. */
export const tabButtonBg = css`var(--vaadin-spreadsheet-tab-button-bg, var(--_vaadin-spreadsheet-tab-button-bg, #464646))`;

/** Background of the active sheet tab. */
export const tabsActiveBg = css`var(--vaadin-spreadsheet-tabs-active-bg, var(--_vaadin-spreadsheet-tabs-active-bg, #fff))`;

/** Color of the active sheet tab's name. */
export const tabsActiveFg = css`var(--vaadin-spreadsheet-tabs-active-fg, var(--_vaadin-spreadsheet-tabs-active-fg, #2584e2))`;

/** Background of the sheet-tab strip. */
export const tabsBg = css`var(--vaadin-spreadsheet-tabs-bg, var(--_vaadin-spreadsheet-tabs-bg, #fafafa))`;

/** Color of the sheet-tab strip's border. */
export const tabsBorder = css`var(--vaadin-spreadsheet-tabs-border, var(--_vaadin-spreadsheet-tabs-border, #c7c7c7))`;

/** Color of sheet-tab text. */
export const tabsFg = css`var(--vaadin-spreadsheet-tabs-fg, var(--_vaadin-spreadsheet-tabs-fg, #6a6a6a))`;

/** Font of the sheet tabs. */
export const tabsFont = css`var(--vaadin-spreadsheet-tabs-font, var(--_vaadin-spreadsheet-tabs-font, Helvetica))`;

/** Size of sheet-tab text. */
export const tabsFontSize = css`var(--vaadin-spreadsheet-tabs-font-size, var(--_vaadin-spreadsheet-tabs-font-size, 14px))`;

/** Color of the text. */
export const textColor = css`var(--vaadin-spreadsheet-text-color, var(--_vaadin-spreadsheet-text-color, #474747))`;
