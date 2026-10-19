/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { touchCommandPosition } from '../algorithms/Placement.js';
import { hideFromTopLayer, showInTopLayer } from '../dom/TopLayer.js';
import type { SheetStore } from '../state/SheetStore.js';
import type { PointerSelection } from './PointerSelection.js';

/** A cell, by its column and row, 1-based. */
interface Cell {
  col: number;
  row: number;
}

/** A box on screen, in client pixels. */
interface Box {
  readonly left: number;
  readonly top: number;
  readonly right: number;
  readonly bottom: number;
}

/** What selecting by touch needs of the sheet. */
export interface TouchSelectionHost {
  /** The cell at a point on the cells. */
  cellAt(x: number, y: number): Cell;
  /** Whether the cells scroll for the drag, which then reaches the cell at their edge. */
  scrolling(): boolean;
  /** The finger moved to a point: the cells scroll while it is past their edge. */
  movedTo(x: number, y: number): void;
  /** Where the selection is on screen, as far as it shows; null while none of it does. */
  selectionBox(): Box | null;
  /** Where the cells start on screen, from the top: the command goes no higher. */
  cellsTop(): number;
  /** Runs once the sheet has drawn what it was last told. */
  afterDrawn(work: () => void): void;
}

/**
 * Selecting by touch, as with the GWT client. A finger's press on the sheet
 * makes it used by touch, until a mouse's or a pen's: the selection's corner
 * handle is then drawn for a finger, and dragging it stretches the
 * selection from its active cell. After a tap or a stretch, a "Fill"
 * command is offered over the selection; chosen, it puts a fill handle on
 * each side of the selection, and dragging one fills outward from it, as
 * the fill handle does with a mouse. A finger drags no range out of the
 * cells themselves: that scrolls them.
 */
export class TouchSelection {
  /** The command offered over a selection made by touch, in the top layer. */
  readonly element = document.createElement('div');

  /** The handle a finger is dragging, while one is. */
  private dragging: 'stretch' | 'fill' | null = null;

  constructor(
    private readonly store: SheetStore,
    private readonly selecting: PointerSelection,
    private readonly host: TouchSelectionHost
  ) {
    this.element.className = 'touch-actions';
    this.element.popover = 'manual';
    this.element.hidden = true;
    const fill = document.createElement('button');
    fill.type = 'button';
    fill.textContent = 'Fill';
    fill.addEventListener('click', () => this.fillChosen());
    this.element.append(fill);
  }

  /**
   * A press on the sheet. A finger's makes it used by touch, and ends a fill
   * the press is not on a fill handle for; a mouse's or a pen's makes it not.
   */
  pressed(pointerType: string): void {
    if (pointerType !== 'touch') {
      this.store.useTouch('off');
      this.hide();
      return;
    }
    this.store.useTouch(this.dragging === 'fill' ? 'fill' : 'select');
  }

  /** A finger pressed a handle of the selection: the corner stretches it, a fill handle fills from it. */
  handlePressed(fill: boolean): void {
    this.hide();
    this.dragging = fill ? 'fill' : 'stretch';
    if (fill) {
      this.selecting.fillStarted(true);
    } else {
      this.selecting.stretchStarted();
    }
  }

  /** A pointer moved on the page: a finger dragging a handle takes the drag to the cell under it. */
  moved(e: { pointerType: string; clientX: number; clientY: number }): void {
    if (this.dragging === null || e.pointerType !== 'touch') {
      return;
    }
    this.host.movedTo(e.clientX, e.clientY);
    if (!this.host.scrolling()) {
      const cell = this.host.cellAt(e.clientX, e.clientY);
      this.selecting.over(cell.col, cell.row);
    }
  }

  /**
   * A finger lifted, or the browser took its touch over. The drag of a
   * handle ends as a mouse's does; a stretch offers the command after, and a
   * fill goes back to selecting.
   */
  lifted(): void {
    const dragging = this.dragging;
    if (dragging === null) {
      return;
    }
    this.dragging = null;
    this.selecting.release();
    if (dragging === 'fill') {
      this.store.useTouch('select');
    } else {
      this.offer();
    }
  }

  /** A tap selected a cell: by touch, the command is offered. */
  tapped(): void {
    if (this.store.interaction.touch === 'select') {
      this.offer();
    }
  }

  /**
   * Takes the command away: a press on the page that is not on the command,
   * given the path of its event, a key, a scroll, or the sheet leaving the page.
   */
  dismiss(path?: readonly EventTarget[]): void {
    if (path?.includes(this.element)) {
      return;
    }
    this.hide();
  }

  /** Offers the command over the selection, once the selection is drawn where it now is. */
  private offer(): void {
    this.host.afterDrawn(() => this.show());
  }

  /** Shows the command over the selection; not once "Fill" is chosen, nor for a selection out of view. */
  private show(): void {
    const box = this.store.interaction.touch === 'select' ? this.host.selectionBox() : null;
    if (box === null) {
      this.hide();
      return;
    }
    const command = this.element;
    command.hidden = false;
    showInTopLayer(command);
    const { left, top } = touchCommandPosition(
      box,
      command.getBoundingClientRect(),
      this.host.cellsTop(),
      window.innerWidth
    );
    command.style.left = `${left}px`;
    command.style.top = `${top}px`;
  }

  /** Hides the command, if it shows. */
  private hide(): void {
    if (this.element.hidden) {
      return;
    }
    this.element.hidden = true;
    hideFromTopLayer(this.element);
  }

  /** "Fill" was chosen: the selection's handles fill. */
  private fillChosen(): void {
    this.hide();
    this.store.useTouch('fill');
  }
}
