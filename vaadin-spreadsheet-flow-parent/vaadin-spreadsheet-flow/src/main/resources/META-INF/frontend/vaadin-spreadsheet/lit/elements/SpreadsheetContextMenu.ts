/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { css, html, LitElement, nothing, type TemplateResult } from 'lit';
import { actionIconSlotName } from '../dom/VirtualChildren.js';
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
  /**
   * Parts this element re-exports to the tree above it.
   *
   * `::part()` crosses exactly one shadow boundary. Every part the
   * spreadsheet documents is emitted inside one of these sub-elements,
   * which sit inside the component's own shadow root — two boundaries
   * away from an application's stylesheet. Without re-exporting them,
   * `vaadin-spreadsheet::part(cell)` matches nothing, and the whole
   * documented styling API silently does nothing.
   */
  static readonly EXPORTED_PARTS = 'context-menu,context-menu-item';

  static properties = {
    renderState: { attribute: false }
  };

  static styles = css`
    /*
     * Anchored to the spreadsheet, not to the viewport.
     *
     * The menu opens on a point the user clicked, which arrives in
     * viewport coordinates. Placing a fixed box at those coordinates
     * only works while no ancestor has a transform: a dialog overlay
     * has one while it animates, becomes the containing block for
     * fixed positioning, and then drops it again - so the menu was
     * drawn 16px off and jumped a moment later, and a click aimed at
     * one item landed on its neighbour. Sharing the sheet's own
     * coordinate space, whatever is done to the sheet is done to the
     * menu, and it cannot drift away from what it opened on.
     */
    :host {
      position: absolute;
      left: 0;
      top: 0;
      z-index: 10;
    }

    :host([hidden]) {
      display: none;
    }

    /* The menu as the GWT client drew it: a white card with a soft
       shadow, its items in 27px rows. */
    .menu {
      position: absolute;
      padding: 4px;
      box-sizing: border-box;
      background: var(--vaadin-spreadsheet-contextmenu-bg, var(--_vaadin-spreadsheet-contextmenu-bg, #fff));
      border: 1px solid
        var(--vaadin-spreadsheet-contextmenu-border, var(--_vaadin-spreadsheet-contextmenu-border, rgba(0, 0, 0, 0.09)));
      border-radius: 4px;
      box-shadow: 0 4px 10px 0 rgba(0, 0, 0, 0.1), 0 3px 5px 0 rgba(0, 0, 0, 0.05);
      font: inherit;
      color: #474747;
    }

    .item {
      display: flex;
      align-items: center;
      gap: 5px;
      width: 100%;
      box-sizing: border-box;
      padding: 0 20px 0 10px;
      border: 0;
      border-radius: 3px;
      background: transparent;
      font: inherit;
      font-weight: 400;
      line-height: 27px;
      color: inherit;
      text-align: left;
      cursor: pointer;
      white-space: nowrap;
    }

    /* The item under the pointer on the GWT client's blue, in light type.
       A stylesheet that gives it a lighter ground through the property
       sets its text through ::part(context-menu-item). */
    .item:hover,
    .item:focus {
      background: var(
        --vaadin-spreadsheet-contextmenu-hover-bg,
        var(--_vaadin-spreadsheet-contextmenu-hover-bg, linear-gradient(to bottom, #1b87e3 2%, #166ed6 98%))
      );
      color: #ecf2f8;
      outline: none;
    }

    /* SpreadsheetTheme.LUMO, as the GWT client drew it: Lumo's card and
       spacing, and the item under the pointer in the primary tint. */
    :host([theme~='lumo']) .menu {
      padding: var(--lumo-space-xs);
      border-radius: var(--lumo-border-radius-s);
      color: var(--lumo-body-text-color);
      box-shadow: var(--lumo-box-shadow-s);
    }

    :host([theme~='lumo']) .item {
      gap: var(--lumo-space-xs);
      padding: 0 var(--lumo-space-l) 0 var(--lumo-space-m);
      border-radius: var(--lumo-border-radius-s);
      line-height: calc(1.5 * var(--lumo-line-height-m));
      cursor: var(--lumo-clickable-cursor);
    }

    :host([theme~='lumo']) .item:hover,
    :host([theme~='lumo']) .item:focus {
      color: var(--lumo-body-text-color);
    }
  `;

  declare renderState: ContextMenuRenderState;

  constructor() {
    super();
    this.renderState = emptyContextMenuRenderState();
  }

  connectedCallback(): void {
    super.connectedCallback();
    // Set on attach rather than in the constructor: a custom element
    // may not have attributes when it is created.
    this.setAttribute('exportparts', SpreadsheetContextMenu.EXPORTED_PARTS);
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
              ${action.iconNodeId
                ? html`<slot class="item-icon" name=${actionIconSlotName(String(action.iconNodeId))}></slot>`
                : nothing}
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
