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
 *   2. {@code --vaadin-spreadsheet-*} CSS custom properties —
 *      declared here with default values so consumer CSS can
 *      override at any cascade level.
 *   3. {@code Spreadsheet.HIDE_FUNCTION_BAR_STYLE} ("hidefunctionbar")
 *      + {@code Spreadsheet.HIDE_TABSHEET_STYLE} ("hidetabsheet")
 *      Java constants — applied to the host's class list and
 *      handled by CSS selectors below (defense-in-depth — the
 *      Lit connector also toggles display via a MutationObserver,
 *      slice 174-175).
 *
 * GWT-era selectors (.s-top, .sheet-tabsheet, .ch.col1, .rh.row1,
 * .addressfield, .functionfield, .bottom-right-pane, .v-spreadsheet,
 * .merged-cell, .v-contextmenu, etc.) were NOT documented and are
 * removed. End users who customized appearance via those selectors
 * should migrate to {@code ::part(...)} per the docs.
 */
export const spreadsheetStyles = css`
  :host {
    /* Documented CSS custom properties. Defaults match the GWT-era
       visual baseline so consumers see equivalent appearance out of
       the box. */
    --vaadin-spreadsheet-border-color: #c7c7c7;
    --vaadin-spreadsheet-cell-border-color: #c7c7c7;
    --vaadin-spreadsheet-header-border-color: #c7c7c7;
    --vaadin-spreadsheet-text-color: #474747;

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
    color: var(--vaadin-spreadsheet-text-color);
    font-family: Helvetica;
    font-size: 14px;
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
    border: 1px solid var(--vaadin-spreadsheet-border-color);
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
 * Document-level overlay styles. Slice 176 reduces this to an empty
 * export — the Lit path uses Vaadin's standard tooltip and
 * context-menu components, which carry their own styles. The export
 * is preserved so the existing import in vaadin-spreadsheet.js
 * doesn't break; consumers who relied on .v-tooltip / .v-errormessage
 * selectors should migrate to Vaadin's documented overlay parts.
 */
export const spreadsheetOverlayStyles = css``;
