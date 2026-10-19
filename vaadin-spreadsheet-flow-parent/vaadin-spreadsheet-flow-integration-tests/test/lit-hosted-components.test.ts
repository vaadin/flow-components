/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { toKey } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/CellKeys.js';
import {
  type DrawnSheet,
  HostedComponents
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/HostedComponents.js';
import {
  actionIconSlotName,
  cellComponentSlotName,
  cellEditorSlotName,
  type CellSlotAssignment,
  overlayComponentSlotName
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/dom/VirtualChildren.js';

/**
 * The components the sheet hosts, on their own: which of them a drawn sheet
 * places and where, which events came out of them, and the custom editors'
 * cells and focus.
 */
describe('HostedComponents', () => {
  let host: HTMLElement;
  let cellGrid: HTMLElement;
  let contextMenu: HTMLElement;
  let elements: Record<string, HTMLElement>;
  let assignments: Record<string, CellSlotAssignment>;
  let hosted: HostedComponents;

  /** A sheet drawn with rows 1-10 and columns 1-5 on screen, nothing frozen. */
  function drawn(overlays: DrawnSheet['overlays'] = []): DrawnSheet {
    return {
      overlays,
      viewport: { firstRowIndex: 1, lastRowIndex: 10, firstColumnIndex: 1, lastColumnIndex: 5 },
      verticalSplitPosition: 0,
      horizontalSplitPosition: 0
    };
  }

  function component(nodeId: string, col: number, row: number): void {
    assignments[toKey(col, row)] = { nodeId, slot: cellComponentSlotName(nodeId), kind: 'component' };
  }

  function editor(nodeId: string, col: number, row: number): void {
    assignments[toKey(col, row)] = { nodeId, slot: cellEditorSlotName(nodeId), kind: 'editor' };
  }

  /** What a check says of an event raised at the target, asked while the event is on its way. */
  function raisedAt<T>(target: EventTarget, check: (e: Event) => T): T {
    let seen: T | undefined;
    const listener = (e: Event): void => {
      seen = check(e);
    };
    host.addEventListener('probe', listener);
    target.dispatchEvent(new Event('probe', { bubbles: true, composed: true }));
    host.removeEventListener('probe', listener);
    return seen as T;
  }

  beforeEach(() => {
    host = document.createElement('div');
    cellGrid = document.createElement('div');
    contextMenu = document.createElement('div');
    document.body.append(host);
    elements = {};
    for (const id of ['1', '2', '3', '4', '5', '9']) {
      // An input, so that a custom editor among them can take the focus.
      elements[id] = document.createElement('input');
    }
    assignments = {};
    hosted = new HostedComponents(
      host,
      { cellGrid, contextMenu },
      (nodeId) => elements[nodeId] ?? null,
      { cellSlotAssignments: () => assignments }
    );
  });

  afterEach(() => {
    hosted.clear();
    host.remove();
  });

  it('places the component overlays, and no other kind', () => {
    hosted.place(
      drawn([
        { type: 'COMPONENT', id: '1' },
        { type: 'IMAGE', id: '9' }
      ]),
      assignments
    );

    expect(elements['1'].parentElement).to.equal(host);
    expect(elements['1'].slot).to.equal(overlayComponentSlotName('1'));
    expect(cellGrid.querySelector(`slot[name="${overlayComponentSlotName('1')}"]`)).to.exist;
    expect(elements['9'].parentElement).to.equal(null);
  });

  it('places a cell component and a custom editor each through its own slot in the cell grid', () => {
    component('2', 1, 1);
    editor('3', 2, 1);

    hosted.place(drawn(), assignments);

    expect(elements['2'].slot).to.equal(cellComponentSlotName('2'));
    expect(elements['3'].slot).to.equal(cellEditorSlotName('3'));
    expect(cellGrid.querySelector(`slot[name="${cellComponentSlotName('2')}"]`)).to.exist;
    expect(cellGrid.querySelector(`slot[name="${cellEditorSlotName('3')}"]`)).to.exist;
  });

  it('places only the components and editors of the cells on screen', () => {
    component('2', 1, 1);
    editor('3', 2, 40);

    hosted.place(drawn(), assignments);

    expect(elements['2'].parentElement).to.equal(host);
    expect(elements['3'].parentElement).to.equal(null);
  });

  it('takes off what a later draw no longer shows', () => {
    component('2', 1, 1);
    hosted.place(drawn([{ type: 'COMPONENT', id: '1' }]), assignments);

    hosted.place(drawn(), {});

    expect(elements['1'].parentElement).to.equal(null);
    expect(elements['2'].parentElement).to.equal(null);
  });

  it('places the icons of menu actions through slots in the context menu', () => {
    hosted.actionIcons.sync(['4']);

    expect(elements['4'].parentElement).to.equal(host);
    expect(contextMenu.querySelector(`slot[name="${actionIconSlotName('4')}"]`)).to.exist;
    expect(cellGrid.querySelector('slot')).to.equal(null);
  });

  it('tells an event raised inside any hosted component from one raised elsewhere', () => {
    component('2', 1, 1);
    editor('3', 2, 1);
    hosted.place(drawn([{ type: 'COMPONENT', id: '1' }]), assignments);
    const elsewhere = document.createElement('input');
    host.append(elsewhere);

    expect(raisedAt(elements['1'], (e) => hosted.raisedInside(e)), 'overlay').to.be.true;
    expect(raisedAt(elements['2'], (e) => hosted.raisedInside(e)), 'cell component').to.be.true;
    expect(raisedAt(elements['3'], (e) => hosted.raisedInside(e)), 'editor').to.be.true;
    expect(raisedAt(elsewhere, (e) => hosted.raisedInside(e)), 'elsewhere').to.be.false;
  });

  it('tells an event raised inside a custom editor from one raised inside another hosted component', () => {
    component('2', 1, 1);
    editor('3', 2, 1);
    hosted.place(drawn([{ type: 'COMPONENT', id: '1' }]), assignments);

    expect(raisedAt(elements['3'], (e) => hosted.raisedInEditor(e))).to.be.true;
    expect(raisedAt(elements['2'], (e) => hosted.raisedInEditor(e))).to.be.false;
    expect(raisedAt(elements['1'], (e) => hosted.raisedInEditor(e))).to.be.false;
  });

  it('knows the element of a custom editor, and nothing else', () => {
    component('2', 1, 1);
    editor('3', 2, 1);
    hosted.place(drawn(), assignments);

    expect(hosted.isEditor(elements['3'])).to.be.true;
    expect(hosted.isEditor(elements['2'])).to.be.false;
    expect(hosted.isEditor(null)).to.be.false;
  });

  it('finds the cell whose custom editor an event came out of', () => {
    editor('3', 2, 7);
    hosted.place(drawn(), assignments);

    expect(raisedAt(elements['3'], (e) => hosted.cellOfEditor(e))).to.deep.equal({ col: 2, row: 7 });
  });

  it('finds no cell for an event from a cell component, or from an editor the cell no longer shows', () => {
    component('2', 1, 1);
    editor('3', 2, 7);
    hosted.place(drawn(), assignments);
    delete assignments[toKey(2, 7)];

    expect(raisedAt(elements['2'], (e) => hosted.cellOfEditor(e))).to.equal(null);
    expect(raisedAt(elements['3'], (e) => hosted.cellOfEditor(e))).to.equal(null);
  });

  it('gives the keyboard to the custom editor of a cell', () => {
    editor('3', 2, 7);
    hosted.place(drawn(), assignments);

    expect(hosted.focusEditorOf(2, 7)).to.be.true;
    expect(document.activeElement).to.equal(elements['3']);
  });

  it('gives the keyboard to nothing for a cell without an editor, with a component, or with its editor off screen', () => {
    component('2', 1, 1);
    editor('3', 2, 40);
    hosted.place(drawn(), assignments);

    expect(hosted.focusEditorOf(3, 3), 'nothing').to.be.false;
    expect(hosted.focusEditorOf(1, 1), 'component').to.be.false;
    expect(hosted.focusEditorOf(2, 40), 'off screen').to.be.false;
    expect(document.activeElement).to.equal(document.body);
  });

  it('takes every hosted component off on clear', () => {
    component('2', 1, 1);
    editor('3', 2, 1);
    hosted.place(drawn([{ type: 'COMPONENT', id: '1' }]), assignments);
    hosted.actionIcons.sync(['4']);

    hosted.clear();

    for (const id of ['1', '2', '3', '4']) {
      expect(elements[id].parentElement, id).to.equal(null);
    }
    expect(cellGrid.querySelector('slot')).to.equal(null);
    expect(contextMenu.querySelector('slot')).to.equal(null);
  });
});
