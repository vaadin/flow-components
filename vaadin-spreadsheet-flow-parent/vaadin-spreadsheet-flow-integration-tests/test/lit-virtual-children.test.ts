/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { expect } from 'chai';
import {
  SlottedVirtualChildren,
  flowVirtualChildResolver,
  type VirtualChildResolver
} from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/dom/VirtualChildren.js';

/**
 * Placing server-side components inside the sheet.
 *
 * The server attaches charts, custom editors and in-cell components as Flow
 * virtual children: the element exists on the client but is not in the
 * document until the client puts it somewhere. These tests pin where it
 * goes, and — the part that matters for live components — that repainting
 * does not move it.
 */

function makeHost(): HTMLElement {
  const host = document.createElement('div');
  document.body.appendChild(host);
  return host;
}

/** A resolver over a fixed id → element table, with a call counter. */
function tableResolver(table: Record<string, HTMLElement>): VirtualChildResolver & {
  calls: string[];
} {
  const calls: string[] = [];
  const resolve = ((nodeId: string) => {
    calls.push(nodeId);
    return table[nodeId] ?? null;
  }) as VirtualChildResolver & { calls: string[] };
  resolve.calls = calls;
  return resolve;
}

function component(id: string): HTMLElement {
  const el = document.createElement('div');
  el.dataset.testId = id;
  return el;
}

describe('SlottedVirtualChildren — placing components', () => {
  let host: HTMLElement;

  afterEach(() => {
    host.remove();
  });

  it('parents the component to the host and names its slot', () => {
    host = makeHost();
    const chart = component('chart');
    const children = new SlottedVirtualChildren(
      host,
      host,
      tableResolver({ '7': chart }),
      (id) => `overlay-component-${id}`
    );

    children.sync(['7']);

    expect(chart.parentElement).to.equal(host);
    expect(chart.getAttribute('slot')).to.equal('overlay-component-7');
  });

  it('reports which ids it managed to place', () => {
    host = makeHost();
    const children = new SlottedVirtualChildren(
      host,
      host,
      tableResolver({ '1': component('a') }),
      (id) => `s-${id}`
    );

    // '2' has no element yet — the component and the geometry that
    // positions it arrive in separate server responses.
    expect(children.sync(['1', '2'])).to.deep.equal(['1']);
  });

  it('retries an id the resolver could not find yet', () => {
    host = makeHost();
    const table: Record<string, HTMLElement> = {};
    const resolve = tableResolver(table);
    const children = new SlottedVirtualChildren(host, host, resolve, (id) => `s-${id}`);

    expect(children.sync(['4'])).to.deep.equal([]);
    table['4'] = component('late');
    expect(children.sync(['4'])).to.deep.equal(['4']);
    expect(table['4'].parentElement).to.equal(host);
  });

  it('leaves a placed component untouched when the set is unchanged', () => {
    // Re-parenting a live Vaadin component tears it down and rebuilds it,
    // so an idempotent sync is the whole point: this runs after every
    // repaint, and the sheet repaints on every scroll tick.
    host = makeHost();
    const chart = component('chart');
    const resolve = tableResolver({ '7': chart });
    const children = new SlottedVirtualChildren(host, host, resolve, (id) => `s-${id}`);
    children.sync(['7']);

    let moved = false;
    new MutationObserver(() => {
      moved = true;
    }).observe(host, { childList: true });

    children.sync(['7']);
    children.sync(['7']);

    expect(moved).to.be.false;
    expect(resolve.calls).to.deep.equal(['7']);
  });

  it('removes a component that is no longer wanted', () => {
    host = makeHost();
    const chart = component('chart');
    const children = new SlottedVirtualChildren(
      host,
      host,
      tableResolver({ '7': chart }),
      (id) => `s-${id}`
    );
    children.sync(['7']);

    children.sync([]);

    expect(chart.parentElement).to.be.null;
    expect(children.get('7')).to.be.null;
  });

  it('clears the slot name on the way out', () => {
    // The server may place the same component again later — a chart
    // scrolled out of view and back. A stale slot name would leave it
    // projected nowhere.
    host = makeHost();
    const chart = component('chart');
    const children = new SlottedVirtualChildren(
      host,
      host,
      tableResolver({ '7': chart }),
      (id) => `s-${id}`
    );
    children.sync(['7']);
    children.sync([]);

    expect(chart.hasAttribute('slot')).to.be.false;
  });

  it('keeps the survivors when only some ids go away', () => {
    host = makeHost();
    const a = component('a');
    const b = component('b');
    const children = new SlottedVirtualChildren(
      host,
      host,
      tableResolver({ '1': a, '2': b }),
      (id) => `s-${id}`
    );
    children.sync(['1', '2']);

    expect(children.sync(['2'])).to.deep.equal(['2']);
    expect(a.parentElement).to.be.null;
    expect(b.parentElement).to.equal(host);
  });

  it('hands back the element placed for an id', () => {
    host = makeHost();
    const chart = component('chart');
    const children = new SlottedVirtualChildren(
      host,
      host,
      tableResolver({ '7': chart }),
      (id) => `s-${id}`
    );
    children.sync(['7']);

    expect(children.get('7')).to.equal(chart);
    expect(children.get('nope')).to.be.null;
  });

  it('takes everything out on clear', () => {
    host = makeHost();
    const a = component('a');
    const b = component('b');
    const children = new SlottedVirtualChildren(
      host,
      host,
      tableResolver({ '1': a, '2': b }),
      (id) => `s-${id}`
    );
    children.sync(['1', '2']);

    children.clear();

    expect(host.children).to.have.length(0);
    expect(children.get('1')).to.be.null;
  });
});

describe('SlottedVirtualChildren — crossing a second shadow boundary', () => {
  let host: HTMLElement;

  afterEach(() => {
    host.remove();
  });

  /**
   * The production shape: the component belongs in the host's light DOM,
   * where an application can still find it, but the element that shows it
   * lives inside a nested shadow root. A slot only projects its own host's
   * light children, so two are chained.
   */
  function nestedFixture(): { inner: HTMLElement; chart: HTMLElement } {
    host = document.createElement('div');
    document.body.appendChild(host);
    const shadow = host.attachShadow({ mode: 'open' });
    const inner = document.createElement('div');
    // A second shadow root, standing in for the cell-grid element.
    inner.attachShadow({ mode: 'open' }).innerHTML = '<slot name="s-7"></slot>';
    shadow.appendChild(inner);
    return { inner, chart: component('chart') };
  }

  it('leaves the component in the host light DOM', () => {
    const { inner, chart } = nestedFixture();
    const children = new SlottedVirtualChildren(
      host,
      inner,
      tableResolver({ '7': chart }),
      (id) => `s-${id}`
    );

    children.sync(['7']);

    expect(chart.parentElement).to.equal(host);
    expect(host.contains(chart)).to.be.true;
  });

  it('projects it all the way into the inner shadow root', () => {
    const { inner, chart } = nestedFixture();
    const children = new SlottedVirtualChildren(
      host,
      inner,
      tableResolver({ '7': chart }),
      (id) => `s-${id}`
    );

    children.sync(['7']);

    const innerSlot = inner.shadowRoot!.querySelector('slot')!;
    const assigned = innerSlot.assignedElements({ flatten: true });
    expect(assigned).to.have.length(1);
    expect(assigned[0]).to.equal(chart);
  });

  it('puts a forwarding slot in the slot host', () => {
    const { inner, chart } = nestedFixture();
    const children = new SlottedVirtualChildren(
      host,
      inner,
      tableResolver({ '7': chart }),
      (id) => `s-${id}`
    );

    children.sync(['7']);

    const forwarder = inner.querySelector('slot')!;
    expect(forwarder.getAttribute('name')).to.equal('s-7');
    expect(forwarder.getAttribute('slot')).to.equal('s-7');
  });

  it('takes the forwarding slot away with the component', () => {
    const { inner, chart } = nestedFixture();
    const children = new SlottedVirtualChildren(
      host,
      inner,
      tableResolver({ '7': chart }),
      (id) => `s-${id}`
    );
    children.sync(['7']);

    children.sync([]);

    expect(inner.querySelector('slot')).to.be.null;
    expect(chart.parentElement).to.be.null;
  });

  it('adds no forwarder when both hosts are the same element', () => {
    // One shadow boundary needs no chain.
    host = document.createElement('div');
    document.body.appendChild(host);
    const chart = component('chart');
    const children = new SlottedVirtualChildren(
      host,
      host,
      tableResolver({ '7': chart }),
      (id) => `s-${id}`
    );

    children.sync(['7']);

    expect(host.querySelector('slot')).to.be.null;
    expect(chart.parentElement).to.equal(host);
  });
});

describe('flowVirtualChildResolver — asking Flow for the element', () => {
  const original = (window as any).Vaadin;

  afterEach(() => {
    (window as any).Vaadin = original;
  });

  function withFlowClient(appId: string, getByNodeId: (n: number) => unknown): void {
    (window as any).Vaadin = { Flow: { clients: { [appId]: { getByNodeId } } } };
  }

  it('resolves through the Flow client for its application', () => {
    const chart = document.createElement('div');
    withFlowClient('ROOT', (n) => (n === 12 ? chart : null));

    expect(flowVirtualChildResolver('ROOT')('12')).to.equal(chart);
  });

  it('parses the node id, which arrives as a string', () => {
    let seen: unknown = null;
    withFlowClient('ROOT', (n) => {
      seen = n;
      return null;
    });

    flowVirtualChildResolver('ROOT')('12');

    expect(seen).to.equal(12);
  });

  it('returns null before the application has started', () => {
    (window as any).Vaadin = undefined;

    expect(flowVirtualChildResolver('ROOT')('12')).to.be.null;
  });

  it('returns null for an application that is not this one', () => {
    withFlowClient('OTHER', () => document.createElement('div'));

    expect(flowVirtualChildResolver('ROOT')('12')).to.be.null;
  });

  it('returns null for an id that is not a number', () => {
    let called = false;
    withFlowClient('ROOT', () => {
      called = true;
      return null;
    });

    expect(flowVirtualChildResolver('ROOT')('sheet-overlay-3')).to.be.null;
    expect(called).to.be.false;
  });

  it('returns null when Flow knows nothing about the node', () => {
    withFlowClient('ROOT', () => undefined);

    expect(flowVirtualChildResolver('ROOT')('12')).to.be.null;
  });
});
