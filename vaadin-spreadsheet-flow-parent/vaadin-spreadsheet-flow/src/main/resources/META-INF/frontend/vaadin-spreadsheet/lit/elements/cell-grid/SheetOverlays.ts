/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { html, type TemplateResult } from 'lit';
import { keyedList } from '../../dom/KeyedList.js';
import { overlayComponentSlotName } from '../../dom/VirtualChildren.js';
import type { CellGridRenderState } from '../types.js';
import { type QuadrantName, quadrantOf } from './GridGeometry.js';

/**
 * Build the sheet-overlay rendering — one element per positioned
 * overlay. IMAGE overlays render as {@code <img class="sheet-
 * overlay sheet-overlay-image" src="...">}. COMPONENT overlays
 * render as a placeholder {@code <div class="sheet-overlay
 * sheet-overlay-component" data-overlay-id="...">} pending
 * Vaadin-component slotting in a later slice.
 *
 * Keyed on the overlay id. The list arrives from the server's overlay map,
 * whose iteration order shifts whenever an entry is removed and re-added —
 * which is what scrolling one out of view and back does. Rendered
 * positionally, a reordering makes Lit rewrite each {@code <img>} in place,
 * so every image re-downloads and flickers even though nothing about it
 * changed. Keyed, Lit moves the existing elements instead.
 */
export function buildSheetOverlays(state: CellGridRenderState, quadrant: QuadrantName): unknown {
  // Defensive default — some test fixtures do not declare the
  // overlays array. Real connector pushes always populate the field.
  //
  // Each in the pane its anchor cell is in, so a picture anchored in a frozen row stays with that row. The frozen
  // panes' coordinates start where the sheet does, so the position the
  // connector worked out serves in any pane.
  const overlays = (state.overlays ?? []).filter(
    (overlay) =>
      quadrantOf(overlay.col, overlay.row, state.horizontalSplitPosition, state.verticalSplitPosition) === quadrant
  );
  return keyedList(
    overlays,
    (overlay) => overlay.id,
    (overlay) => {
      const style =
        `left:${overlay.leftPx}px;top:${overlay.topPx}px;` + `width:${overlay.widthPx}px;height:${overlay.heightPx}px;`;
      if (overlay.type === 'IMAGE') {
        const src = overlay.src ?? '';
        return html`
          <img
            class="sheet-overlay sheet-overlay-image"
            data-overlay-id=${overlay.id}
            data-col=${overlay.col}
            data-row=${overlay.row}
            src=${src}
            style=${style}
          />
        `;
      }
      // COMPONENT overlay — a chart or any component the server put on the
      // sheet. Flow created the element and left it out of the document;
      // the connector parents it to this element and points its `slot`
      // attribute here. The wrapper div carries the geometry so the
      // component itself only has to fill it.
      return html`
        <div
          class="sheet-overlay sheet-overlay-component"
          data-overlay-id=${overlay.id}
          data-col=${overlay.col}
          data-row=${overlay.row}
          style=${style}
        >
          <slot name=${overlayComponentSlotName(overlay.id)}></slot>
        </div>
      `;
    }
  );
}

/**
 * Build the popup-button rendering. One small
 * triangle/icon per anchor cell at its bottom-right corner.
 * Click on a button dispatches `popup-button-click { col, row }`
 * which the connector translates to a `popupButtonClick`
 * spreadsheet-event for the server.
 */
export function buildPopupButtons(state: CellGridRenderState, quadrant: QuadrantName): TemplateResult[] {
  // In the pane of the cell each belongs to, like the sheet overlays: a
  // filter button in a frozen header row stays in that row on a scroll.
  const buttons = (state.popupButtons ?? []).filter(
    (b) => quadrantOf(b.col, b.row, state.horizontalSplitPosition, state.verticalSplitPosition) === quadrant
  );
  return buttons.map((b) => {
    const className = b.active ? 'popup-button active' : 'popup-button';
    // The row's height rides along for a theme whose button is as tall as
    // its cell.
    const style = `left:${b.leftPx}px;top:${b.topPx}px;--_cell-height:${b.cellHeightPx ?? 20}px;`;
    const press = (button: Element, keyboard: boolean) => {
      button.dispatchEvent(
        new CustomEvent('popup-button-click', {
          detail: { col: b.col, row: b.row, keyboard },
          bubbles: true,
          composed: true
        })
      );
    };
    const onClick = (e: Event) => {
      e.stopPropagation();
      press(e.currentTarget as Element, false);
    };
    // A button to assistive technology and the keyboard, pressed with Enter
    // or Space like any button. The
    // keys are its own: the sheet would edit the cell with them.
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key !== 'Enter' && e.key !== ' ') {
        return;
      }
      e.preventDefault();
      e.stopPropagation();
      press(e.currentTarget as Element, true);
    };
    return html`
      <div
        class=${className}
        part="popup-button"
        role="button"
        tabindex="0"
        aria-label="Open pop-up"
        style=${style}
        data-col=${b.col}
        data-row=${b.row}
        @click=${onClick}
        @keydown=${onKeyDown}
      ></div>
    `;
  });
}
