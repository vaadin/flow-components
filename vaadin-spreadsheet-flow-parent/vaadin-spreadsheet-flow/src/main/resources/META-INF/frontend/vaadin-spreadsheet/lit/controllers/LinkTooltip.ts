/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { linkTooltipPosition } from '../algorithms/Placement.js';
import { hideFromTopLayer, showInTopLayer } from '../dom/TopLayer.js';

/** What a link cell's tooltip needs of the sheet. */
export interface LinkTooltipHost {
  /** The target the server named for a link cell, or undefined for any other cell. */
  tooltipAt(col: number, row: number): string | undefined;
  /** Whether the pointer is dragging, which takes the tooltip away. */
  dragging(): boolean;
}

/**
 * A link cell's tooltip - the target the server named for it - while the
 * pointer is over the cell: below the cell, or above it when there is no room
 * below. Any other cell, or a drag in progress, takes it away.
 */
export class LinkTooltip {
  /** The tooltip, shown in the top layer. */
  readonly element = document.createElement('div');

  constructor(private readonly host: LinkTooltipHost) {
    this.element.className = 'link-tooltip';
    this.element.popover = 'manual';
    this.element.hidden = true;
  }

  /** The pointer is over the cell, whose box is where the grid drew it. */
  show(at: { col: number; row: number; box?: DOMRect }): void {
    const text = this.host.tooltipAt(at.col, at.row);
    if (text === undefined || at.box === undefined || this.host.dragging()) {
      this.hide();
      return;
    }
    const tooltip = this.element;
    tooltip.textContent = text;
    tooltip.hidden = false;
    showInTopLayer(tooltip);
    const { left, top } = linkTooltipPosition(at.box, tooltip.getBoundingClientRect(), {
      width: window.innerWidth,
      height: window.innerHeight
    });
    tooltip.style.left = `${left}px`;
    tooltip.style.top = `${top}px`;
  }

  /** Takes the tooltip away, if it shows. */
  readonly hide = (): void => {
    if (this.element.hidden) {
      return;
    }
    this.element.hidden = true;
    hideFromTopLayer(this.element);
  };
}
