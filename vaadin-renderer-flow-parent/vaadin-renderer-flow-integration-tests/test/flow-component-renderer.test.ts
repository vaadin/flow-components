import type {} from '@web/test-runner-mocha';
import '../frontend/generated/jar-resources/flow-component-renderer.js';
import { render, html, LitElement } from 'lit';
import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import sinon from 'sinon';

type Vaadin = {
  FlowComponentHost: {
    getNode: (appId: string, nodeId?: number) => HTMLElement;
  };
  Flow: {
    clients: {
      [appId: string]: {
        getByNodeId: ((nodeId: number) => HTMLElement) & sinon.SinonSpy;
      };
    };
  };
};

declare global {
  interface Window {
    Vaadin: Vaadin;
  }
}

interface TestComponent extends LitElement {
  nodeId?: number;
}

class TestComponent extends LitElement {
  static get properties() {
    return {
      nodeId: { type: Number }
    };
  }

  protected createRenderRoot() {
    return this;
  }

  render() {
    return html`${window.Vaadin.FlowComponentHost.getNode('ROOT', this.nodeId)}`;
  }
}
customElements.define('test-component', TestComponent);

describe('flow-component-renderer', () => {
  let elements: { [key: number]: HTMLElement };

  before(() => {
    window.Vaadin.Flow = {
      clients: {
        ROOT: {
          getByNodeId: sinon.spy((nodeId: number) => {
            return elements[nodeId];
          })
        }
      }
    };
  });

  beforeEach(() => {
    elements = {};
  });

  it('should get lazily added node', async () => {
    const container = fixtureSync<HTMLDivElement>(`<div></div>`);

    render(html`${window.Vaadin.FlowComponentHost.getNode('ROOT', 0)}`, container);
    await nextFrame();

    const element = document.createElement('div');
    elements[0] = element;
    await nextFrame();

    expect(container.firstElementChild).to.equal(element);
  });

  it('should try to retrieve a node only once', async () => {
    const { getByNodeId } = window.Vaadin.Flow.clients.ROOT;
    const component = fixtureSync<TestComponent>(`<test-component></test-component>`);
    component.nodeId = 0;

    getByNodeId.resetHistory();

    await nextFrame();
    await nextFrame();
    await nextFrame();

    expect(getByNodeId).to.have.been.calledTwice;
  });

  it('should remove old node', async () => {
    const container = fixtureSync<HTMLDivElement>(`<div></div>`);
    const element = document.createElement('div');
    elements[0] = element;

    render(html`${window.Vaadin.FlowComponentHost.getNode('ROOT', 0)}`, container);
    await nextFrame();
    expect(container.firstElementChild).to.equal(element);

    render(html`${window.Vaadin.FlowComponentHost.getNode('ROOT', undefined)}`, container);
    await nextFrame();
    expect(container.firstElementChild).to.equal(null);
  });

  it('should not try to re-render a removed node', async () => {
    const { getByNodeId } = window.Vaadin.Flow.clients.ROOT;
    const component = fixtureSync<TestComponent>(`<test-component></test-component>`);

    // Create an element to render
    elements[0] = document.createElement('button');
    component.nodeId = 0;
    await nextFrame();

    // Remove the element (simulate removing from the Flow's registry)
    delete elements[0];

    // Render once again with the old node id (this can happen if Grid's items haven't
    // yet been updated not to include the removed node id and re-render is invoked)
    component.requestUpdate();
    await nextFrame();

    getByNodeId.resetHistory();
    // Finally, render with undefined node id (the Grid's items have been updated)
    component.nodeId = undefined;

    await nextFrame();
    expect(getByNodeId).to.not.have.been.called;
    expect(component.firstElementChild).to.equal(null);
  });

  it('should not try to re-render a replaced node', async () => {
    const { getByNodeId } = window.Vaadin.Flow.clients.ROOT;
    const component = fixtureSync<TestComponent>(`<test-component></test-component>`);

    // Create an element to render
    elements[0] = document.createElement('button');
    component.nodeId = 0;
    await nextFrame();

    // Remove the element
    delete elements[0];
    // Add new element
    elements[1] = document.createElement('button');
    // Render once again with the old node id
    component.requestUpdate();
    await nextFrame();

    getByNodeId.resetHistory();
    // Finally, render with the new node id
    component.nodeId = 1;
    await nextFrame();

    // getByNodeId should only have been called for node id 1
    getByNodeId.getCalls().forEach((call) => expect(call.args[0]).to.equal(1));
    expect(component.firstElementChild).to.equal(elements[1]);
  });

  it('should not replace new synchronous node with the old lazily added node', async () => {
    const component = fixtureSync<TestComponent>(`<test-component></test-component>`);

    // Try to render a lazily added node (not added to the Flow's registry yet)
    component.nodeId = 0;
    await nextFrame();

    // Add and render a new node synchronously
    elements[1] = document.createElement('button');
    component.nodeId = 1;
    await nextFrame();

    // Add the lazy node to the Flow's registry
    elements[0] = document.createElement('button');
    await nextFrame();

    // The new node should still be rendered
    expect(component.firstElementChild).to.equal(elements[1]);
    expect(component.contains(elements[0])).to.be.false;
  });

  it('should not try to re-render an old node after disconnect', async () => {
    const { getByNodeId } = window.Vaadin.Flow.clients.ROOT;
    const component = fixtureSync<TestComponent>(`<test-component></test-component>`);

    // Try to render a lazily added node
    component.nodeId = 0;
    await nextFrame();

    // Disconnect
    getByNodeId.resetHistory();
    component.remove();
    await nextFrame();

    expect(getByNodeId).to.not.have.been.called;
  });
});
