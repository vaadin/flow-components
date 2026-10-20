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
import {
  groupingToggleBg,
  groupingToggleBorderColor,
  headerBg,
  headerBorderColor,
  headerFont,
  headerFontSize,
  headerSelectedBg
} from '../ThemeProperties.js';

/**
 * What the column letters and the row numbers look alike in. Each header
 * adds the rules of its own axis after these: the edge it sticks to and is
 * bordered along, and the shape of its labels, resize handles, freeze line
 * and outline gutter.
 */
export const headerStripStyles = css`
  /* A flex box, which lays the frozen strip and the layer under the labels
     along the header at no size, each as deep as the header is. */
  :host {
    display: flex;
    position: sticky;
    box-sizing: border-box;
    background: ${headerBg};
    font-family: ${headerFont};
    font-size: ${headerFontSize};
  }

  :host([hidden]) {
    display: none !important;
  }

  /* The frozen columns or rows are drawn in their own strip, above the
     scrolling headers, so one sliding underneath does not show through. The
     strip is sticky: the browser holds it at the header's frozen end as it
     scrolls the sheet, in the same frame as the frozen cells. */
  .frozen-header-strip {
    position: sticky;
    flex: none;
    z-index: 2;
    pointer-events: none;
  }

  /* Held in place with the frozen labels, but under the scrolling ones: the
     gap at the freeze line. */
  .frozen-header-under {
    position: sticky;
    flex: none;
    z-index: -1;
    pointer-events: none;
  }

  .frozen-header-strip > * {
    pointer-events: auto;
  }

  /* Over the strip's border along the cells too. */
  .frozen-header-strip > .freeze-line {
    position: absolute;
    z-index: 3;
    background: #6a6a6a;
    pointer-events: none;
  }

  .freeze-gutter-pane {
    position: absolute;
    left: 0;
    top: 0;
    box-sizing: border-box;
    pointer-events: none;
  }

  /* The gap at the freeze line in the labels when only this axis is
     frozen: under the labels, the sheet's background. */
  .freeze-gap {
    position: absolute;
    background: var(--default-background-color, #fff);
    pointer-events: none;
  }

  .col-header-cell,
  .row-header-cell {
    position: absolute;
    box-sizing: border-box;
    overflow: hidden;
    text-align: center;
  }

  /* The active cell's headers are tinted so the cursor's row and
     column stay findable on a wide sheet. The cell also carries the documented
     ::part(header-selected) for consumers who want another one. */
  .col-header-cell[part~='header-selected'],
  .row-header-cell[part~='header-selected'] {
    background: ${headerSelectedBg};
  }

  .col-resize-handle,
  .row-resize-handle {
    position: absolute;
    z-index: 1;
  }

  /* The outline gutter: a bracket along each group - a line with a tick
     where the group starts - and a round button at its summary column or
     row. */
  .col-grouping-line,
  .row-grouping-line {
    position: absolute;
    border-top: 2px solid ${headerBorderColor};
    border-left: 2px solid ${headerBorderColor};
    pointer-events: none;
  }

  /* The sign sits on an 11px line at the top of a borderless button: the ring is drawn inside, taking no room,
     and the pixel of padding fits the box to the line - a box taller than
     its line, and the browser centres the line instead. */
  .col-grouping-toggle,
  .row-grouping-toggle {
    position: absolute;
    width: 12px;
    height: 12px;
    box-sizing: border-box;
    padding: 0 0 1px;
    margin: 0;
    border: 0;
    box-shadow: inset 0 0 0 1px ${groupingToggleBorderColor};
    border-radius: 50%;
    background: ${groupingToggleBg};
    color: #fff;
    font: inherit;
    font-size: 12px;
    line-height: 11px;
    text-align: center;
    cursor: pointer;
    z-index: 2;
  }

  .col-grouping-gutter,
  .row-grouping-gutter {
    position: absolute;
    left: 0;
    top: 0;
    box-sizing: border-box;
    pointer-events: none;
  }

  /* Divides one outline level from the next. Over the freeze line in
     the gutter. */
  .col-grouping-separator,
  .row-grouping-separator {
    position: absolute;
    z-index: 3;
    pointer-events: none;
  }

  /* SpreadsheetTheme.LUMO: the strip and its outline gutter tinted over the
     Lumo ground. */
  :host([theme~='lumo']) {
    background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
  }

  /* Over the sheet's ground, not over the labels' tint. */
  :host([theme~='lumo']) .frozen-header-strip > .freeze-line {
    background: linear-gradient(var(--lumo-contrast-50pct), var(--lumo-contrast-50pct)) var(--lumo-base-color);
  }

  :host([theme~='lumo']) .col-grouping-toggle,
  :host([theme~='lumo']) .row-grouping-toggle {
    background-image: linear-gradient(var(--lumo-contrast-30pct), var(--lumo-contrast-30pct));
    cursor: var(--lumo-clickable-cursor);
  }
`;
