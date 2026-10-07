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

/**
 * Slice 176 (Tier 3 Phase 3.7). Rewrites the legacy 1832-line
 * GWT-era stylesheet down to the slim Lit-aligned shape.
 *
 * The documented public styling API for the Spreadsheet is at
 * ../docs/articles/components/spreadsheet/styling.adoc and consists
 * of three pieces ONLY:
 *
 *   1. {@code ::part(...)} selectors — applied to elements inside
 *      the internal Lit sub-elements' shadow DOM via the {@code part}
 *      attribute. Each internal element owns its own default styles
 *      for these parts; this outer stylesheet does not touch them.
 *   2. {@code --vaadin-spreadsheet-*} CSS custom properties — read,
 *      never declared, so a value an application sets on any
 *      ancestor reaches the sheet. Each read falls back to a
 *      {@code --_vaadin-spreadsheet-*} theme default and then to the
 *      GWT client's own value.
 *   3. {@code Spreadsheet.HIDE_FUNCTION_BAR_STYLE} ("hidefunctionbar")
 *      + {@code Spreadsheet.HIDE_TABSHEET_STYLE} ("hidetabsheet")
 *      Java constants — applied to the host's class list and
 *      handled by CSS selectors below (defense-in-depth — the
 *      Lit connector also toggles display via a MutationObserver,
 *      slice 174-175).
 *   4. {@code Spreadsheet.setTheme(SpreadsheetTheme.LUMO)} — the
 *      host's {@code theme="lumo"}, which the connector copies onto
 *      each internal element. The theme defaults below give every
 *      documented property its Lumo value; each element draws the
 *      rest of the Lumo look under {@code :host([theme~='lumo'])}.
 *
 * GWT-era selectors (.s-top, .sheet-tabsheet, .ch.col1, .rh.row1,
 * .addressfield, .functionfield, .bottom-right-pane, .v-spreadsheet,
 * .merged-cell, .v-contextmenu, etc.) were NOT documented and are
 * removed. End users who customized appearance via those selectors
 * should migrate to {@code ::part(...)} per the docs.
 */
export const spreadsheetStyles = css`
  :host {
    /* No documented property is declared here. A declaration on the
       host would hide whatever an application sets on an ancestor -
       its theme's html rule, a layout around the sheet - as the GWT
       client never did. */

    /* A flex column so the root fills a host that has been given a
       height. Declared here rather than written onto the host inline,
       so an application that lays the component out its own way can
       still override it. */
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 100%;
    flex: 1 1 auto;
    isolation: isolate;
    background-color: #fff;
    /* The server writes these into the per-cell style rules it generates
       (background-color: var(--default-background-color) and the like),
       so they have to resolve to something: an undefined var() is
       invalid at computed-value time, which turns every unformatted
       cell's background transparent. Defined here rather than in the
       grid because custom properties inherit through the shadow
       boundary. No backticks in this comment — the whole stylesheet is
       a template literal and one would end it. */
    --default-background-color: #fff;
    --default-color: #000;
    --default-font-family: Helvetica;
    --default-font-size: 11pt;
    color: var(--vaadin-spreadsheet-text-color, var(--_vaadin-spreadsheet-text-color, #474747));
    /* No font here. The GWT client set its own font on the sheet's parts
       and left the rest to the page: a comment, the context menu and a
       pop-up are drawn in the page's font, as they were then. */
    box-sizing: border-box;
    overflow: hidden;
    position: relative;
  }

  /* The component's own root, which owns the vertical layout and
     carries the visible frame. The border belongs here rather than on
     the host for the same reason the layout does: the host is the
     application's element. Keeping it here also means the root fills
     the host exactly, as the GWT widget's root div did. */
  /* The visible frame. It belongs on the component's own root rather
     than on the host, for the same reason the layout does — and it
     keeps the root exactly the size of the host, as the GWT widget's
     root div was. The root's sizing is set on the element itself; only
     the theming lives here. */
  .spreadsheet-root {
    border: 1px solid var(--vaadin-spreadsheet-border-color, var(--_vaadin-spreadsheet-border-color, #c7c7c7));
  }

  /* A header being dragged, as the GWT client showed it: a line across the
     whole sheet where the edge would go, and the resize cursor wherever the
     pointer goes. The connector places the line. */
  .resize-line {
    position: absolute;
    z-index: 11;
    background: #197de1;
    pointer-events: none;
  }

  .resize-cover {
    position: absolute;
    inset: 0;
    z-index: 11;
    cursor: col-resize;
  }

  .resize-cover.rows {
    cursor: row-resize;
  }

  /* The size the header would take, by the pointer, on the GWT client's
     tooltip. In the top layer, placed in viewport coordinates; the inset,
     margin and border undo the browser's popover defaults. */
  .resize-tooltip {
    position: fixed;
    inset: auto;
    margin: 0;
    border: 0;
    padding: 5px 9px;
    border-radius: 3px;
    background-color: rgba(50, 50, 50, 0.9);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
    color: #fff;
    font-size: 14px;
    max-width: 35em;
    overflow: hidden;
    pointer-events: none;
  }

  /* SpreadsheetTheme.LUMO, as the GWT client drew it: the Lumo value of
     every documented property the application leaves unset. Lumo's
     tokens come from the application's Lumo theme. */
  :host([theme~='lumo']) {
    --_vaadin-spreadsheet-border-color: var(--lumo-contrast-20pct);
    --_vaadin-spreadsheet-cell-border-color: var(--lumo-contrast-20pct);
    --_vaadin-spreadsheet-header-border-color: var(--lumo-contrast-20pct);
    --_vaadin-spreadsheet-text-color: var(--lumo-body-text-color);
    --_vaadin-spreadsheet-cell-background: var(--lumo-base-color);
    --_vaadin-spreadsheet-cell-range-bg: var(--lumo-primary-color-10pct);
    --_vaadin-spreadsheet-selection-color: var(--lumo-primary-color);
    --_vaadin-spreadsheet-selection-border-color: var(--lumo-success-color);
    --_vaadin-spreadsheet-editor-border: var(--lumo-primary-color);
    --_vaadin-spreadsheet-header-bg: var(--lumo-base-color);
    --_vaadin-spreadsheet-header-selected-bg: var(--lumo-base-color);
    --_vaadin-spreadsheet-header-font: var(--lumo-font-family);
    --_vaadin-spreadsheet-header-font-size: var(--lumo-font-size-s);
    --_vaadin-spreadsheet-grouping-toggle-bg: var(--lumo-base-color);
    --_vaadin-spreadsheet-grouping-toggle-border-color: transparent;
    --_vaadin-spreadsheet-comment-bg: var(--lumo-warning-color);
    --_vaadin-spreadsheet-invalidformula-bg: var(--lumo-error-color);
    --_vaadin-spreadsheet-comment-overlay-bg: var(--lumo-base-color);
    --_vaadin-spreadsheet-comment-overlay-border-color: var(--lumo-contrast-20pct);
    --_vaadin-spreadsheet-formulabar-bg: var(--lumo-base-color);
    --_vaadin-spreadsheet-formulabar-border: var(--lumo-contrast-20pct);
    --_vaadin-spreadsheet-formulabar-font: var(--lumo-font-family);
    --_vaadin-spreadsheet-formulabar-font-size: var(--lumo-font-size-s);
    --_vaadin-spreadsheet-tabs-bg: var(--lumo-base-color);
    --_vaadin-spreadsheet-tabs-fg: var(--lumo-body-text-color);
    --_vaadin-spreadsheet-tabs-border: var(--lumo-contrast-20pct);
    --_vaadin-spreadsheet-tabs-active-bg: var(--lumo-base-color);
    --_vaadin-spreadsheet-tabs-active-fg: var(--lumo-primary-text-color);
    --_vaadin-spreadsheet-tabs-font: var(--lumo-font-family);
    --_vaadin-spreadsheet-tabs-font-size: var(--lumo-font-size-s);
    --_vaadin-spreadsheet-tab-button-bg: var(--lumo-primary-text-color);
    --_vaadin-spreadsheet-contextmenu-bg: var(--lumo-base-color);
    --_vaadin-spreadsheet-contextmenu-border: transparent;
    --_vaadin-spreadsheet-contextmenu-hover-bg: var(--lumo-primary-color-10pct);
    --_vaadin-spreadsheet-popup-background: var(--lumo-base-color);
    --_vaadin-spreadsheet-popup-border-color: var(--lumo-contrast-20pct);
    --_vaadin-spreadsheet-popup-button-bg: var(--lumo-base-color);
    --_vaadin-spreadsheet-popup-button-fg: var(--lumo-body-text-color);
    --_vaadin-spreadsheet-popup-button-active-bg: var(--lumo-base-color);
    --default-background-color: var(--lumo-base-color);
    --default-color: var(--vaadin-spreadsheet-text-color, var(--_vaadin-spreadsheet-text-color, #474747));
    background-color: var(--lumo-base-color);
  }

  :host([theme~='lumo']) .resize-line {
    background: var(--lumo-primary-color);
  }

  :host([theme~='lumo']) .resize-tooltip {
    background-color: var(--lumo-contrast-50pct);
    box-shadow: var(--lumo-box-shadow-s);
    color: var(--lumo-base-color);
    padding: var(--lumo-space-xs) var(--lumo-space-s);
    font-size: var(--lumo-font-size-s);
  }

  /* Spreadsheet.HIDE_FUNCTION_BAR_STYLE — public Java constant
     ("hidefunctionbar"). Setting it on the host removes the formula
     bar above the cell grid. The connector also toggles display via
     a MutationObserver (slice 175); this CSS is defense-in-depth so
     the bar hides even before the observer fires (e.g., for a host
     that has the class set at construction time). */
  :host(.hidefunctionbar) vaadin-spreadsheet-internal-formula-bar {
    display: none !important;
  }

  /* Spreadsheet.HIDE_TABSHEET_STYLE — public Java constant
     ("hidetabsheet"). Setting it on the host removes the sheet-tab
     strip below the cell grid. Same defense-in-depth pattern. */
  :host(.hidetabsheet) vaadin-spreadsheet-internal-sheet-tabs {
    display: none !important;
  }
`;

/**
 * Document-level overlay styles, for what the spreadsheet shows from its
 * light DOM: the filter popup's content. Injected into the document head,
 * and into the enclosing shadow root when the spreadsheet is nested in one.
 * The menu, comments and popup card are drawn in the component's own
 * shadow tree and styled there.
 */
export const spreadsheetOverlayStyles = css`
  /* The filter popup's content, laid out as the GWT client laid it out:
     the column's own controls above its values, the values in a column
     that scrolls past 275px. The content is the application's components,
     in the host's light DOM, so these rules live at the document level. */
  vaadin-spreadsheet .spreadsheet-filter-table-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  vaadin-spreadsheet .spreadsheet-item-filter-layout {
    display: flex;
    flex-direction: column;
    gap: 4px;
    max-height: 275px;
    overflow-y: auto;
  }

  vaadin-spreadsheet[theme~='lumo'] .spreadsheet-filter-table-content {
    gap: var(--lumo-space-xs);
  }

  vaadin-spreadsheet[theme~='lumo'] .spreadsheet-item-filter-layout {
    gap: var(--lumo-space-xs);
    border-bottom: 1px solid var(--lumo-contrast-20pct);
  }
`;
