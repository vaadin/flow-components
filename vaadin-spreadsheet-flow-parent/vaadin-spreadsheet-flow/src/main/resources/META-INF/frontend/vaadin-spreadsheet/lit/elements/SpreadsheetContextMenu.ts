/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, nothing, type TemplateResult } from 'lit';
import type { SpreadsheetActionDetails } from '../types/wire.js';

/**
 * Input state for the {@link SpreadsheetContextMenu} element.
 *
 * The server decides what a right-click offers: the client reports where the
 * click landed, the server answers with `showActions`, and this element
 * displays whatever came back. Nothing is rendered until then.
 */
export interface ContextMenuRenderState {
  /** Whether the menu is showing. */
  open: boolean;
  /** Viewport x of the click the menu belongs to. */
  x: number;
  /** Viewport y of the click. */
  y: number;
  /** Actions the server offered for this click. */
  actions: ReadonlyArray<SpreadsheetActionDetails>;
}

/** Helper: build a closed, empty context-menu state. */
export function emptyContextMenuRenderState(): ContextMenuRenderState {
  return { open: false, x: 0, y: 0, actions: [] };
}

/**
 * Internal context-menu element, shown on right-click over a cell or a
 * row/column header.
 *
 * Selecting an item dispatches `context-menu-action` carrying the action's
 * `key` and `type`; the connector turns that into the matching server event
 * (`actionOnCurrentSelection`, `actionOnRowHeader` or `actionOnColumnHeader`).
 * Dismissing it dispatches `context-menu-dismissed`, which the connector
 * reports as `contextMenuClosed`.
 *
 * Built as an internal element rather than on `@vaadin/context-menu`: the Lit
 * client has no `@vaadin/*` dependency today, and adding one to the
 * Spreadsheet's dependency graph is a product decision rather than a detail of
 * this rewrite.
 *
 * No-decorator pattern matching every other internal element (slice 132+).
 */
export class SpreadsheetContextMenu extends LitElement {
  static properties = {
    renderState: { attribute: false }
  };

  static styles = css`
    :host {
      position: fixed;
      z-index: 10;
      inset: auto;
    }

    :host([hidden]) {
      display: none;
    }

    .menu {
      position: fixed;
      min-width: 160px;
      padding: 4px 0;
      box-sizing: border-box;
      background: var(--vaadin-spreadsheet-contextmenu-bg, #fff);
      border: 1px solid var(--vaadin-spreadsheet-contextmenu-border, #c0c0c0);
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
      font: inherit;
      color: inherit;
    }

    .item {
      display: block;
      width: 100%;
      padding: 4px 12px;
      border: 0;
      background: transparent;
      font: inherit;
      color: inherit;
      text-align: left;
      cursor: pointer;
      white-space: nowrap;
    }

    .item:hover,
    .item:focus {
      background: var(--vaadin-spreadsheet-contextmenu-hover-bg, #e8e8e8);
      outline: none;
    }
  `;

  declare renderState: ContextMenuRenderState;

  constructor() {
    super();
    this.renderState = emptyContextMenuRenderState();
  }

  firstUpdated(): void {
    const root = this.shadowRoot!;
    root.addEventListener('click', (e: Event) => {
      const target = e.target;
      if (!(target instanceof Element)) {
        return;
      }
      const item = target.closest('.item');
      if (!(item instanceof HTMLElement)) {
        return;
      }
      const key = item.dataset.key;
      const type = Number(item.dataset.type);
      if (key === undefined) {
        return;
      }
      this.dispatchEvent(
        new CustomEvent('context-menu-action', {
          detail: { key, type },
          bubbles: true,
          composed: true
        })
      );
    });
  }

  render(): TemplateResult | typeof nothing {
    const { open, x, y, actions } = this.renderState;
    if (!open || actions.length === 0) {
      return nothing;
    }
    return html`
      <div class="menu" part="context-menu" style="left:${x}px;top:${y}px;">
        ${actions.map(
          (action) => html`
            <button class="item" part="context-menu-item" type="button" data-key=${action.key} data-type=${action.type}>
              ${action.caption}
            </button>
          `
        )}
      </div>
    `;
  }
}

customElements.define('vaadin-spreadsheet-internal-context-menu', SpreadsheetContextMenu);

declare global {
  interface HTMLElementTagNameMap {
    'vaadin-spreadsheet-internal-context-menu': SpreadsheetContextMenu;
  }
}
