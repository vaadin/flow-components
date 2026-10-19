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
import { indexedTarget } from '../../dom/IndexedTarget.js';
import { keyedList } from '../../dom/KeyedList.js';
import { SpreadsheetPart } from '../SpreadsheetPart.js';
import type { PositionedGrouping } from './Grouping.js';

/** What sets the column letters and the row numbers apart, for what they do alike. */
export interface HeaderAxis {
  /** The data attribute a header cell and a resize handle hold their index in, and the name it is sent under. */
  readonly key: 'col' | 'row';
  /** The class of a header cell; it carries the index too, as `col<N>` or `row<N>`. */
  readonly cellClass: string;
  /** The class of a resize handle. */
  readonly handleClass: string;
  /** The class of a grouping button. */
  readonly toggleClass: string;
  /** The edge of a header cell on screen that a resize is measured from. */
  readonly edge: 'left' | 'top';
  /** The events it fires, by what the user did. */
  readonly events: {
    readonly click: string;
    readonly contextMenu: string;
    readonly resizeStart: string;
    readonly autofit: string;
    readonly groupingToggle: string;
  };
}

/** A header cell or a resize handle, keyed by its column or row. */
export interface KeyedTemplate {
  key: number;
  template: TemplateResult;
}

/** The columns or rows along one axis: the ones in view, and the frozen ones before them. */
export interface HeaderLine {
  /** The first and last column or row in view, and where each is drawn, from the first on. */
  first: number;
  last: number;
  starts: ReadonlyArray<number>;
  sizes: ReadonlyArray<number>;
  /** How many are frozen, and where each is drawn, from 1 on. */
  frozen: number;
  frozenStarts: ReadonlyArray<number>;
  frozenSizes: ReadonlyArray<number>;
}

/** The header cells and resize handles of a line: those in view keyed, the frozen ones apart. */
export interface HeaderLabels {
  cells: KeyedTemplate[];
  handles: KeyedTemplate[];
  frozenCells: TemplateResult[];
  frozenHandles: TemplateResult[];
}

/**
 * What the column letters and the row numbers have in common: the clicks,
 * presses and right-clicks they turn into the sheet's events, the grouping
 * buttons, building a label for each column or row, and putting the strip
 * together. Each header keeps the geometry of its own axis.
 */
export abstract class HeaderStrip extends SpreadsheetPart {
  /** Which axis this header runs along. */
  protected abstract readonly axis: HeaderAxis;

  /**
   * Delegates at the shadow root, which exists once Lit has drawn: a click
   * on a header cell selects its column or row, or on a grouping button
   * opens or closes the group; a press on a resize handle starts a resize
   * and a double-click on one autofits; a right-click on a header cell asks
   * for the sheet's menu.
   */
  firstUpdated(): void {
    const root = this.shadowRoot!;
    root.addEventListener('click', this.clicked);
    root.addEventListener('mousedown', this.pressed);
    root.addEventListener('contextmenu', this.contextMenuAsked);
    root.addEventListener('dblclick', this.doubleClicked);
  }

  /**
   * The header cells and resize handles along a line, each built by
   * `build` - those in view, and then the frozen ones apart, to be drawn in
   * a strip of their own over them. None for a column or row of no size.
   */
  protected labels(
    line: HeaderLine,
    build: (index: number, start: number, size: number) => { cell: TemplateResult; handle: TemplateResult } | null
  ): HeaderLabels {
    const cells: KeyedTemplate[] = [];
    const handles: KeyedTemplate[] = [];
    const add = (
      index: number,
      start: number,
      size: number,
      into: { cells: KeyedTemplate[]; handles: KeyedTemplate[] }
    ) => {
      if (size === 0) {
        return;
      }
      const label = build(index, start, size);
      if (label !== null) {
        into.cells.push({ key: index, template: label.cell });
        into.handles.push({ key: index, template: label.handle });
      }
    };
    for (let index = line.first; index <= line.last; index++) {
      add(index, line.starts[index - line.first] ?? 0, line.sizes[index - line.first] ?? 0, { cells, handles });
    }
    const frozen = { cells: [] as KeyedTemplate[], handles: [] as KeyedTemplate[] };
    for (let index = 1; index <= line.frozen; index++) {
      add(index, line.frozenStarts[index - 1] ?? 0, line.frozenSizes[index - 1] ?? 0, frozen);
    }
    return {
      cells,
      handles,
      frozenCells: frozen.cells.map((cell) => cell.template),
      frozenHandles: frozen.handles.map((handle) => handle.template)
    };
  }

  /** The button over a group's summary column or row: "+" opens a closed group, "−" closes an open one. */
  protected groupingToggle(g: PositionedGrouping, style: string): TemplateResult {
    return html`
      <button
        class=${this.axis.toggleClass}
        type="button"
        aria-expanded=${g.collapsed ? 'false' : 'true'}
        style=${style}
        data-unique-index=${g.uniqueIndex}
        data-collapsed=${g.collapsed ? 'true' : 'false'}
      >
        ${g.collapsed ? '+' : '−'}
      </button>
    `;
  }

  /**
   * The strip: the outline gutter, the header cells in view and their
   * resize handles - keyed, so a label keeps its element as the sheet
   * scrolls, where matched by position every one was rewritten on every
   * scroll step - and the frozen ones over them.
   */
  protected strip(
    grouping: TemplateResult[],
    labels: Pick<HeaderLabels, 'cells' | 'handles'>,
    freezeGap: unknown,
    frozenStrip: unknown
  ): TemplateResult {
    return html`${grouping}${keyedList(
      labels.cells,
      (cell) => cell.key,
      (cell) => cell.template
    )}${keyedList(
      labels.handles,
      (handle) => handle.key,
      (handle) => handle.template
    )}${freezeGap}${frozenStrip}`;
  }

  /**
   * A click on a grouping button opens or closes its group, and goes no
   * further: it is no click on the header cell under it. A click on a header
   * cell selects its column or row, with the modifier keys held.
   */
  private readonly clicked = (e: Event): void => {
    const ev = e as MouseEvent;
    const target = ev.target;
    if (!(target instanceof Element)) {
      return;
    }
    const { key, events } = this.axis;
    const toggle = target.closest<HTMLElement>(`.${this.axis.toggleClass}`);
    if (toggle) {
      const uniqueIndex = Number(toggle.dataset.uniqueIndex);
      const collapsed = toggle.dataset.collapsed === 'true';
      if (Number.isFinite(uniqueIndex)) {
        this.dispatchEvent(
          new CustomEvent(events.groupingToggle, {
            detail: { uniqueIndex, collapsed: !collapsed },
            bubbles: true,
            composed: true
          })
        );
      }
      ev.stopPropagation();
      return;
    }
    const at = indexedTarget(ev, `.${this.axis.cellClass}`, key);
    if (at === null) {
      return;
    }
    this.dispatchEvent(
      new CustomEvent(events.click, {
        detail: { [key]: at.index, shiftKey: ev.shiftKey, ctrlKey: ev.ctrlKey, metaKey: ev.metaKey, altKey: ev.altKey },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * A press on a resize handle starts resizing its column or row, from
   * where the column or row starts on screen: it is sized from there to the
   * pointer.
   */
  private readonly pressed = (e: Event): void => {
    const { key, edge } = this.axis;
    const at = indexedTarget(e, `.${this.axis.handleClass}`, key);
    if (at === null) {
      return;
    }
    e.preventDefault();
    const ev = e as MouseEvent;
    const cell = this.shadowRoot!.querySelector<HTMLElement>(`.${this.axis.cellClass}.${key}${at.index}`);
    this.dispatchEvent(
      new CustomEvent(this.axis.events.resizeStart, {
        detail: {
          [key]: at.index,
          clientX: ev.clientX,
          clientY: ev.clientY,
          [edge]: cell?.getBoundingClientRect()[edge]
        },
        bubbles: true,
        composed: true
      })
    );
  };

  /**
   * A right-click on a header cell asks for the sheet's menu for its column
   * or row. The browser's own menu stays unless the sheet takes the
   * right-click for a menu of its own.
   */
  private readonly contextMenuAsked = (event: Event): void => {
    const e = event as MouseEvent;
    const { key } = this.axis;
    const at = indexedTarget(e, `.${this.axis.cellClass}`, key);
    if (at === null) {
      return;
    }
    const taken = !this.dispatchEvent(
      new CustomEvent(this.axis.events.contextMenu, {
        detail: { [key]: at.index, clientX: e.clientX, clientY: e.clientY },
        bubbles: true,
        composed: true,
        cancelable: true
      })
    );
    if (taken) {
      e.preventDefault();
    }
  };

  /**
   * A double-click on a resize handle fits its column or row to its
   * content, the way Excel does; a drag on the same handle resizes it by
   * hand.
   */
  private readonly doubleClicked = (e: Event): void => {
    const { key } = this.axis;
    const at = indexedTarget(e, `.${this.axis.handleClass}`, key);
    if (at === null) {
      return;
    }
    e.preventDefault();
    this.dispatchEvent(
      new CustomEvent(this.axis.events.autofit, {
        detail: { [key]: at.index },
        bubbles: true,
        composed: true
      })
    );
  };
}
