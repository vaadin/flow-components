/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { parseColRow, toKey } from '../algorithms/CellKeys.js';
import {
  actionIconSlotName,
  cellComponentSlotName,
  cellEditorSlotName,
  type CellSlotAssignment,
  overlayComponentSlotName,
  SlottedVirtualChildren,
  visibleCellSlots
} from '../dom/VirtualChildren.js';

/** What the components the sheet hosts need of the sheet. */
export interface HostedComponentsHost {
  /** Which component or custom editor each cell shows, by cell key. */
  cellSlotAssignments(): Readonly<Record<string, CellSlotAssignment>>;
}

/** Where the hosted components are projected. */
export interface HostedComponentsTargets {
  /** The cell grid, whose shadow root has the slots of the overlays, cell components and editors. */
  readonly cellGrid: HTMLElement;
  /** The context menu, whose shadow root has the slots of the actions' icons. */
  readonly contextMenu: HTMLElement;
}

/** What a drawn sheet says about which hosted components are on screen. */
export type DrawnSheet = Parameters<typeof visibleCellSlots>[1] & {
  readonly overlays: ReadonlyArray<{ readonly type: string; readonly id: string }>;
};

/**
 * The server-side components the sheet hosts: overlays such as charts, the
 * components inside cells, custom cell editors, and the icons of context-menu
 * actions. Flow creates their elements and leaves placing them to the sheet.
 * They live in the host's light DOM, where an application's own components
 * belong, findable by an ordinary document query, and reach the screen
 * through forwarding slots.
 */
export class HostedComponents {
  /** Overlays: charts, and anything else added with {@code Spreadsheet.setComponent}. */
  private readonly overlays: SlottedVirtualChildren;

  /**
   * Components inside a cell, kept apart from the overlays because the two
   * project through differently named slots. A component is either an overlay
   * or in a cell, never both.
   */
  private readonly cellComponents: SlottedVirtualChildren;

  /**
   * Custom cell editors, kept apart from the cell components because a cell
   * can carry both and each needs its own slot: swapping one for the other
   * must not disturb the other's element.
   */
  private readonly cellEditors: SlottedVirtualChildren;

  /** Icons on context-menu actions, projected into the menu's shadow root. */
  readonly actionIcons: SlottedVirtualChildren;

  constructor(
    host: HTMLElement,
    targets: HostedComponentsTargets,
    resolve: (nodeId: string) => HTMLElement | null,
    private readonly sheet: HostedComponentsHost
  ) {
    this.overlays = new SlottedVirtualChildren(host, targets.cellGrid, resolve, overlayComponentSlotName);
    this.cellComponents = new SlottedVirtualChildren(host, targets.cellGrid, resolve, cellComponentSlotName);
    this.cellEditors = new SlottedVirtualChildren(host, targets.cellGrid, resolve, cellEditorSlotName);
    this.actionIcons = new SlottedVirtualChildren(host, targets.contextMenu, resolve, actionIconSlotName);
  }

  /**
   * Places the components the drawn sheet shows. The slots that project them
   * are in the template just handed to the cell grid, so they are parented
   * before the browser resolves the slots. Only the cells on screen render a
   * slot, so only their components and editors are placed: one off screen
   * would be parented with nowhere to project. Placing again what is placed
   * costs a set comparison and nothing else.
   */
  place(drawn: DrawnSheet, assignments: Readonly<Record<string, CellSlotAssignment>>): void {
    this.overlays.sync(drawn.overlays.filter((o) => o.type === 'COMPONENT').map((o) => o.id));
    const visible = visibleCellSlots(assignments, drawn);
    this.cellComponents.sync(visible.filter((a) => a.kind === 'component').map((a) => a.nodeId));
    this.cellEditors.sync(visible.filter((a) => a.kind === 'editor').map((a) => a.nodeId));
  }

  /**
   * Whether an event was raised inside a hosted component: a custom editor,
   * an in-cell component, an overlay. Their events travel the flattened tree
   * and arrive at the sheet's own listeners, but they are not the sheet's.
   */
  raisedInside(e: Event): boolean {
    return e
      .composedPath()
      .some(
        (target) => this.cellEditors.owns(target) || this.cellComponents.owns(target) || this.overlays.owns(target)
      );
  }

  /** Whether an event was raised inside a custom cell editor. */
  raisedInEditor(e: Event): boolean {
    return e.composedPath().some((target) => this.cellEditors.owns(target));
  }

  /**
   * Whether a node is the element of one of the custom cell editors, or is
   * inside one: a field's input has the keyboard rather than the field.
   */
  isEditor(node: EventTarget | null): boolean {
    for (let n = node instanceof Node ? node : null; n !== null; ) {
      if (this.cellEditors.owns(n)) {
        return true;
      }
      n = n.parentNode instanceof ShadowRoot ? n.parentNode.host : n.parentNode;
    }
    return false;
  }

  /** The cell whose custom editor an event came out of, if any. */
  cellOfEditor(e: Event): { col: number; row: number } | null {
    if (!this.raisedInEditor(e)) {
      return null;
    }
    const path = e.composedPath();
    for (const [cellKey, assignment] of Object.entries(this.sheet.cellSlotAssignments())) {
      if (assignment.kind !== 'editor') {
        continue;
      }
      const element = this.cellEditors.get(assignment.nodeId);
      if (element !== null && path.includes(element)) {
        const { col, row } = parseColRow(cellKey);
        return { col, row };
      }
    }
    return null;
  }

  /** Gives the keyboard to the custom editor of a cell, if it has one; whether it did. */
  focusEditorOf(col: number, row: number): boolean {
    const assignment = this.sheet.cellSlotAssignments()[toKey(col, row)];
    if (assignment === undefined || assignment.kind !== 'editor') {
      return false;
    }
    const element = this.cellEditors.get(assignment.nodeId);
    if (element === null) {
      return false;
    }
    element.focus();
    return true;
  }

  /** Takes every hosted component off the sheet. */
  clear(): void {
    this.overlays.clear();
    this.cellComponents.clear();
    this.cellEditors.clear();
    this.actionIcons.clear();
  }
}
