/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import { Outline } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/Outline.js';
import type { SpreadsheetEvent } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/rpc-client-to-server.js';

/**
 * The outline of grouped rows and columns, on its own: the buttons' events
 * become the events the server acts on.
 */
describe('Outline', () => {
  let columnHeader: EventTarget;
  let rowHeader: EventTarget;
  let headerCorner: EventTarget;
  let sent: SpreadsheetEvent[];
  let listening: AbortController;

  beforeEach(() => {
    columnHeader = new EventTarget();
    rowHeader = new EventTarget();
    headerCorner = new EventTarget();
    sent = [];
    listening = new AbortController();
    new Outline({ columnHeader, rowHeader, headerCorner }, (event) => sent.push(event), listening.signal);
  });

  function fire(target: EventTarget, type: string, detail: unknown): void {
    target.dispatchEvent(new CustomEvent(type, { detail }));
  }

  it("collapses a column group by its unique index", () => {
    fire(columnHeader, 'column-grouping-toggle', { uniqueIndex: 7, collapsed: true });

    expect(sent).to.eql([{ type: 'groupingCollapsed', data: { cols: true, colIndex: 7, collapsed: true } }]);
  });

  it('expands a row group by its unique index', () => {
    fire(rowHeader, 'row-grouping-toggle', { uniqueIndex: 3, collapsed: false });

    expect(sent).to.eql([{ type: 'groupingCollapsed', data: { cols: false, colIndex: 3, collapsed: false } }]);
  });

  it('shows the sheet down to a level', () => {
    fire(headerCorner, 'level-header-click', { cols: false, level: 2 });

    expect(sent).to.eql([{ type: 'levelHeaderClicked', data: { cols: false, level: 2 } }]);
  });

  it('stops listening once its signal is aborted', () => {
    listening.abort();

    fire(columnHeader, 'column-grouping-toggle', { uniqueIndex: 7, collapsed: true });
    fire(headerCorner, 'level-header-click', { cols: true, level: 1 });

    expect(sent).to.eql([]);
  });
});
