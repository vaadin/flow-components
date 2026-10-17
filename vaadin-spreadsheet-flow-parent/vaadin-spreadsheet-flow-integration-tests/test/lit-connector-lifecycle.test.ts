/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import { expect } from 'chai';
import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/vaadin-spreadsheet.js';

/**
 * A sheet listens to the page - a drag that ends outside it, a key let go
 * anywhere. Those listeners hold the sheet, so they have to go when the sheet
 * leaves the page: otherwise every removed sheet stays in memory, with its
 * cells, and goes on handling every key press and mouse move in the page.
 */
describe('Spreadsheet — listening to the page', () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;
  let resized: unknown[];

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:400px;height:200px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(10);
    sheet.setCols(5);
    sheet.setColW([50, 50, 50, 50, 50]);
    sheet.setRowH(new Array(10).fill(15));
    resized = [];
    host.addEventListener('spreadsheet-event', (e: Event) => {
      const detail = (e as CustomEvent).detail;
      if (detail.type === 'columnResized') {
        resized.push(detail.data);
      }
    });
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  /** Drag column 2's edge 20px right; the drag ends on the page, not on the sheet. */
  function dragColumnEdge(): void {
    sheet.getColumnHeader().dispatchEvent(
      new CustomEvent('column-resize-start', { detail: { col: 2, clientX: 100 }, bubbles: true, composed: true })
    );
    document.dispatchEvent(new MouseEvent('mousemove', { clientX: 120, bubbles: true }));
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
  }

  it('follows a drag across the page while it is in the page', () => {
    dragColumnEdge();

    expect(resized).to.eql([{ colIndexes: [2], newWidths: [70] }]);
  });

  it('stops listening to the page once disconnected', () => {
    sheet.disconnect();

    dragColumnEdge();

    expect(resized).to.be.empty;
  });

  it('listens again once connected again, as when the element is moved', () => {
    sheet.disconnect();
    sheet.connect();

    dragColumnEdge();

    expect(resized).to.eql([{ colIndexes: [2], newWidths: [70] }]);
  });

  it('listens once however often it is connected', () => {
    sheet.connect();
    sheet.connect();

    dragColumnEdge();

    expect(resized).to.have.length(1);
  });
});

/**
 * A sheet listens to its own parts until it is disposed, and then to none
 * of them: one of them, the outline level buttons, went on being heard.
 */
describe("Spreadsheet — listening to its own parts", () => {
  let host: HTMLElement;
  let sheet: Spreadsheet;
  let sent: string[];

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:400px;height:200px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(10);
    sheet.setCols(5);
    sent = [];
    host.addEventListener('spreadsheet-event', (e: Event) => sent.push((e as CustomEvent).detail.type));
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  /** One event from each of three parts, as each part dispatches it. */
  function useTheParts(): void {
    sheet.getHeaderCorner().dispatchEvent(
      new CustomEvent('level-header-click', { detail: { cols: false, level: 1 }, bubbles: true, composed: true })
    );
    sheet.getColumnHeader().dispatchEvent(
      new CustomEvent('column-grouping-toggle', { detail: { uniqueIndex: 1, collapsed: true }, bubbles: true, composed: true })
    );
    sheet.getScrollContainer().dispatchEvent(
      new KeyboardEvent('keydown', { key: 'z', ctrlKey: true, metaKey: true, bubbles: true, cancelable: true })
    );
  }

  it('hears its parts while it lives', () => {
    useTheParts();

    expect(sent).to.eql(['levelHeaderClicked', 'groupingCollapsed', 'onUndo']);
  });

  it('hears none of them once disposed', () => {
    sheet.dispose();

    useTheParts();

    expect(sent).to.be.empty;
  });
});

describe('<vaadin-spreadsheet> — joining and leaving the page', () => {
  type Element = HTMLElement & { $connector: Spreadsheet; updateComplete: Promise<boolean> };

  it('disconnects its connector when removed, and connects it again when put back', async () => {
    const el = document.createElement('vaadin-spreadsheet') as Element;
    document.body.appendChild(el);
    await el.updateComplete;
    const connector = el.$connector;
    const calls: string[] = [];
    const disconnect = connector.disconnect.bind(connector);
    const connect = connector.connect.bind(connector);
    connector.disconnect = () => {
      calls.push('disconnect');
      disconnect();
    };
    connector.connect = () => {
      calls.push('connect');
      connect();
    };

    el.remove();
    document.body.appendChild(el);

    expect(calls).to.eql(['disconnect', 'connect']);
    expect(el.$connector === connector, 'a moved element keeps its connector').to.be.true;
    el.remove();
    connector.dispose();
  });
});

describe('<vaadin-spreadsheet> — listening to the page as it comes and goes', () => {
  type Element = HTMLElement & { $connector: Spreadsheet; updateComplete: Promise<boolean> };

  let el: Element;
  let resized: unknown[];

  beforeEach(async () => {
    el = document.createElement('vaadin-spreadsheet') as Element;
    el.style.cssText = 'display:flex;flex-direction:column;width:400px;height:200px;';
    document.body.appendChild(el);
    await el.updateComplete;
    el.$connector.setRows(10);
    el.$connector.setCols(5);
    el.$connector.setColW([50, 50, 50, 50, 50]);
    el.$connector.setRowH(new Array(10).fill(15));
    el.$connector.recomputeViewport();
    resized = [];
    el.addEventListener('spreadsheet-event', (e: Event) => {
      const detail = (e as CustomEvent).detail;
      if (detail.type === 'columnResized') {
        resized.push(detail.data);
      }
    });
  });

  afterEach(() => {
    el.remove();
    el.$connector.dispose();
  });

  /** Drag column 2's edge 20px right; the drag ends on the page, not on the sheet. */
  function dragColumnEdge(): void {
    el.$connector
      .getColumnHeader()
      .dispatchEvent(
        new CustomEvent('column-resize-start', { detail: { col: 2, clientX: 100 }, bubbles: true, composed: true })
      );
    document.dispatchEvent(new MouseEvent('mousemove', { clientX: 120, bubbles: true }));
    document.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }));
  }

  it('follows a drag across the page from the start', () => {
    dragColumnEdge();

    expect(resized).to.eql([{ colIndexes: [2], newWidths: [70] }]);
  });

  it('follows no drag across the page once removed from it', () => {
    el.remove();

    dragColumnEdge();

    expect(resized).to.be.empty;
  });

  it('follows a drag across the page again, once, when put back', () => {
    el.remove();
    document.body.appendChild(el);

    dragColumnEdge();

    expect(resized).to.eql([{ colIndexes: [2], newWidths: [70] }]);
  });
});

describe('<vaadin-spreadsheet> — changing size', () => {
  type Element = HTMLElement & { $connector: Spreadsheet; updateComplete: Promise<boolean> };

  function frames(count: number): Promise<void> {
    return new Promise((resolve) => {
      const step = (left: number): void => {
        if (left === 0) {
          resolve();
          return;
        }
        requestAnimationFrame(() => step(left - 1));
      };
      step(count);
    });
  }

  it('draws once when it changes size', async () => {
    const el = document.createElement('vaadin-spreadsheet') as Element;
    el.style.cssText = 'display:flex;flex-direction:column;width:400px;height:300px;';
    document.body.appendChild(el);
    await el.updateComplete;
    const connector = el.$connector;
    connector.setRows(20);
    connector.setCols(10);
    connector.setColW(new Array(10).fill(50));
    connector.setRowH(new Array(20).fill(15));
    await frames(3);
    let draws = 0;
    const draw = connector.recomputeViewport.bind(connector);
    connector.recomputeViewport = () => {
      draws++;
      draw();
    };

    el.style.width = '500px';
    await frames(3);

    expect(draws).to.equal(1);
    el.remove();
    connector.dispose();
  });
});

describe('Spreadsheet — its events to the server', () => {
  it('keeps them to its own element, so a sheet inside another does not drive the outer one', () => {
    // Flow listens on the host. A bubbling event also reached the server
    // component of a spreadsheet this one sits in - as popup content, a
    // custom editor or a cell's component - which applied it to its own
    // cells.
    const outer = document.createElement('div');
    const inner = document.createElement('div');
    inner.style.cssText = 'width:300px;height:200px;display:flex;flex-direction:column;';
    outer.appendChild(inner);
    document.body.appendChild(outer);
    const sheet = new Spreadsheet(inner);
    sheet.setRows(5);
    sheet.setCols(5);
    sheet.setColW([50, 50, 50, 50, 50]);
    sheet.setRowH([15, 15, 15, 15, 15]);
    const atOuter: string[] = [];
    const atInner: string[] = [];
    outer.addEventListener('spreadsheet-event', (e) => atOuter.push((e as CustomEvent).detail.type));
    inner.addEventListener('spreadsheet-event', (e) => atInner.push((e as CustomEvent).detail.type));

    sheet.recomputeViewport();

    expect(atInner).to.include('onSheetScroll');
    expect(atOuter).to.be.empty;
    sheet.dispose();
    outer.remove();
  });
});

describe('Spreadsheet — telling TestBench a request may still come', () => {
  it('reports a request as pending until the loader is done with it', async () => {
    // TestBench waits for Flow, which does not see the loader's timer.
    const host = document.createElement('div');
    host.style.cssText = 'width:300px;height:200px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    const sheet = new Spreadsheet(host);
    try {
      sheet.setRows(50);
      sheet.setCols(5);
      sheet.setColW([50, 50, 50, 50, 50]);
      sheet.setRowH(new Array(50).fill(15));
      expect(sheet.isViewportLoadPending(), 'before anything is drawn').to.be.false;

      sheet.recomputeViewport();
      expect(sheet.isViewportLoadPending(), 'just asked').to.be.true;

      // No server here: the request counts as answered at the next look.
      await new Promise((resolve) => setTimeout(resolve, 100));
      expect(sheet.isViewportLoadPending(), 'answered').to.be.false;
    } finally {
      sheet.dispose();
      host.remove();
    }
  });
});
