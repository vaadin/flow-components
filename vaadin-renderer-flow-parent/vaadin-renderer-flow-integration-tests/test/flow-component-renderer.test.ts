import type {} from '@web/test-runner-mocha';
import '../frontend/generated/jar-resources/flow-component-renderer.js';
import { render, html, LitElement } from 'lit';
import { expect } from 'chai';
import { fixtureSync, nextFrame, nextUpdate } from '@vaadin/testing-helpers';
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

  it('should render node', async () => {
    const container = fixtureSync<HTMLDivElement>(`<div></div>`);
    const element = document.createElement('div');
    elements[0] = element;

    render(html`${window.Vaadin.FlowComponentHost.getNode('ROOT', 0)}`, container);
    await nextFrame();

    expect(container.firstElementChild).to.equal(element);
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

  describe('node that is not in the registry', () => {
    // Flow sends the nodes of a response before the JavaScript that renders
    // them, so a node id that does not resolve belongs to a component the
    // server has discarded, and it never arrives

    it('should keep the content rendered before', async () => {
      const component = fixtureSync<TestComponent>(`<test-component></test-component>`);

      const element = document.createElement('div');
      elements[0] = element;
      component.nodeId = 0;
      await nextUpdate(component);

      // The server discards the component, and the row is rendered again from
      // the client cache before the new row data arrives
      delete elements[0];
      component.requestUpdate();
      await nextUpdate(component);

      expect(component.firstElementChild).to.equal(element);
    });

    it('should render the node once it is rendered again with a new id', async () => {
      const component = fixtureSync<TestComponent>(`<test-component></test-component>`);

      component.nodeId = 0;
      await nextUpdate(component);
      expect(component.firstElementChild).to.equal(null);

      // The server sends new row data, and the cell renders the new component
      const element = document.createElement('div');
      elements[1] = element;
      component.nodeId = 1;
      await nextUpdate(component);

      expect(component.firstElementChild).to.equal(element);
    });
  });
});
