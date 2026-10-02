/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { LitElement, css, html, nothing } from 'lit';
import type { TemplateResult } from 'lit';

// One row (or column) of outline gutter. Shared with the header strips so
// the buttons stay in line with the gutters they belong to.
import { GROUPING_LEVEL_HEIGHT_PX as GROUPING_LEVEL_SIZE_PX } from './SpreadsheetColumnHeader.js';

export interface HeaderCornerRenderState {
  /** Width of the row-header column, 0 when the headers are hidden. */
  headerWidth: number;
  /** Height of the column-header row, 0 when the headers are hidden. */
  headerHeight: number;
  /** Deepest column outline level, 0 when no columns are grouped. */
  colGroupingMax: number;
  /** Deepest row outline level, 0 when no rows are grouped. */
  rowGroupingMax: number;
}

export function emptyHeaderCornerRenderState(): HeaderCornerRenderState {
  return {
    headerWidth: 0,
    headerHeight: 0,
    colGroupingMax: 0,
    rowGroupingMax: 0
  };
}

/**
 * The square where the two header strips meet.
 *
 * It also holds the outline level buttons - the numbered squares Excel
 * puts above and beside the outline gutters. Clicking one shows the sheet
 * down to that level: everything shallower expanded, that level collapsed.
 * The column levels stack down the gutter rows and the row levels run
 * along the gutter columns, each button sitting in line with the gutter
 * it belongs to.
 *
 * The GWT client drew one button more than there are levels, the last
 * meaning "expand everything". That one has no gutter row to sit in, and
 * inventing a row for it would put the corner out of line with the strips
 * beside it; the +/- toggles on the groups expand them.
 */
export class SpreadsheetHeaderCorner extends LitElement {
  static readonly EXPORTED_PARTS = 'header-corner,outline-level-button';

  static properties = {
    renderState: { attribute: false }
  };

  static styles = css`
    :host {
      display: block;
      position: relative;
      box-sizing: border-box;
      background: var(--vaadin-spreadsheet-header-bg, #fafafa);
      border-right: 1px solid var(--vaadin-spreadsheet-header-border-color, #c7c7c7);
      border-bottom: 1px solid var(--vaadin-spreadsheet-header-border-color, #c7c7c7);
    }

    .level-header {
      position: absolute;
      box-sizing: border-box;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0;
      margin: 0;
      border: 1px solid var(--vaadin-spreadsheet-grouping-toggle-border-color, #888);
      background: var(--vaadin-spreadsheet-grouping-toggle-bg, #fff);
      color: inherit;
      font: inherit;
      font-size: 10px;
      line-height: 1;
      cursor: pointer;
    }

    .level-header:hover {
      background: var(--vaadin-spreadsheet-header-hover-bg, #e8e8e8);
    }
  `;

  declare renderState: HeaderCornerRenderState;

  constructor() {
    super();
    this.renderState = emptyHeaderCornerRenderState();
  }

  connectedCallback(): void {
    super.connectedCallback();
    this.setAttribute('exportparts', SpreadsheetHeaderCorner.EXPORTED_PARTS);
    this.setAttribute('part', 'header-corner');
  }

  firstUpdated(): void {
    this.shadowRoot!.addEventListener('click', this.handleShadowClick);
  }

  private readonly handleShadowClick = (e: Event): void => {
    const target = e.target;
    if (!(target instanceof Element)) {
      return;
    }
    const button = target.closest<HTMLElement>('.level-header');
    if (!button) {
      return;
    }
    const level = Number(button.dataset.level);
    if (!Number.isFinite(level) || level < 1) {
      return;
    }
    this.dispatchEvent(
      new CustomEvent('level-header-click', {
        detail: { cols: button.dataset.axis === 'cols', level },
        bubbles: true,
        composed: true
      })
    );
  };

  render(): TemplateResult | typeof nothing {
    const { headerWidth, headerHeight, colGroupingMax, rowGroupingMax } = this.renderState;
    if (colGroupingMax === 0 && rowGroupingMax === 0) {
      return nothing;
    }
    const rowGutterPx = rowGroupingMax * GROUPING_LEVEL_SIZE_PX;
    const colGutterPx = colGroupingMax * GROUPING_LEVEL_SIZE_PX;

    const buttons: TemplateResult[] = [];
    for (let level = 1; level <= colGroupingMax; level++) {
      const style =
        `left:${rowGutterPx}px;top:${(level - 1) * GROUPING_LEVEL_SIZE_PX}px;` +
        `width:${headerWidth}px;height:${GROUPING_LEVEL_SIZE_PX}px;`;
      buttons.push(html`
        <button
          class="level-header"
          part="outline-level-button"
          type="button"
          style=${style}
          data-axis="cols"
          data-level=${level}
        >
          ${level}
        </button>
      `);
    }
    for (let level = 1; level <= rowGroupingMax; level++) {
      const style =
        `left:${(level - 1) * GROUPING_LEVEL_SIZE_PX}px;top:${colGutterPx}px;` +
        `width:${GROUPING_LEVEL_SIZE_PX}px;height:${headerHeight}px;`;
      buttons.push(html`
        <button
          class="level-header"
          part="outline-level-button"
          type="button"
          style=${style}
          data-axis="rows"
          data-level=${level}
        >
          ${level}
        </button>
      `);
    }
    return html`${buttons}`;
  }
}

customElements.define('vaadin-spreadsheet-internal-header-corner', SpreadsheetHeaderCorner);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-header-corner': SpreadsheetHeaderCorner;
  }
}
