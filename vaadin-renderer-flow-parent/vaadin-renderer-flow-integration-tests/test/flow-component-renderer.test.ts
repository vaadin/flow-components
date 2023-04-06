import type {} from '@web/test-runner-mocha';
import '../frontend/generated/jar-resources/flow-component-renderer.js';
import { render, html } from 'lit';
import { expect, fixtureSync, nextFrame } from '@open-wc/testing';

type Vaadin = {
  FlowComponentHost: {
    getNode: (appId: string, nodeId: number) => HTMLElement;
  };
  Flow: {
    clients: {
      [appId: string]: {
        getByNodeId: (nodeId: number) => HTMLElement;
      };
    };
  };
};

declare global {
  interface Window {
    Vaadin: Vaadin;
  }
}

describe('flow-component-renderer', () => {
  let elements: { [key: number]: HTMLElement } = {};

  beforeEach(() => {
    window.Vaadin.Flow = {
      clients: {
        ROOT: {
          getByNodeId: (nodeId: number) => {
            return elements[nodeId];
          }
        }
      }
    };
  });

  it('should get lazily added node', async () => {
    const container = fixtureSync(`<div></div>`) as HTMLDivElement;

    render(html`${window.Vaadin.FlowComponentHost.getNode('ROOT', 0)}`, container);
    await nextFrame();

    const element = document.createElement('div');
    elements[0] = element;
    await nextFrame();

    expect(container.firstElementChild).to.equal(element);
  });
});
