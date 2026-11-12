import type {} from '@web/test-runner-mocha';
import '../frontend/generated/jar-resources/lit-renderer.ts';
import { expect } from 'chai';
import sinon from 'sinon';

type Renderer = (root: HTMLElement, owner: HTMLElement, model: { item: any; index: number }) => void;

type Component = HTMLElement & { renderer?: Renderer };

type LitRendererApi = {
  setLitRendererTemplate: (
    component: Component,
    rendererName: string,
    template: (...args: any[]) => unknown,
    returnChannel: (name: string, itemKey: string, args: any[]) => void,
    clientCallables: string[],
    propertyNamespace: string,
    appId: string
  ) => void;
  setLitRenderer: (
    component: Component,
    rendererName: string,
    templateExpression: string,
    returnChannel: (name: string, itemKey: string, args: any[]) => void,
    clientCallables: string[],
    propertyNamespace: string,
    appId: string
  ) => void;
  unsetLitRenderer: (component: Component, rendererName: string, rendererId: string) => void;
};

const Vaadin = (window as any).Vaadin as LitRendererApi;

const NAMESPACE = 'lr_test_';

describe('lit-renderer', () => {
  let component: Component;
  let root: HTMLElement;
  let returnChannel: sinon.SinonSpy;

  beforeEach(() => {
    component = document.createElement('div') as Component;
    root = document.createElement('div');
    returnChannel = sinon.spy();
  });

  function renderItem(item: any, index = 0) {
    component.renderer!(root, component, { item, index });
  }

  describe('declared template', () => {
    it('should render the template with the item properties of its namespace', () => {
      Vaadin.setLitRendererTemplate(
        component,
        'renderer',
        (html: any, _root: any, _live: any, _appId: any, _itemKey: any, _model: any, item: any) =>
          html`<span>${item.name}</span>`,
        returnChannel,
        [],
        NAMESPACE,
        'ROOT'
      );

      renderItem({ key: '1', [`${NAMESPACE}name`]: 'Jane', lr_other_name: 'Other' });

      expect(root.querySelector('span')!.textContent).to.equal('Jane');
    });

    it('should call the template with the documented arguments', () => {
      const template = sinon.spy((html: any) => html``);
      Vaadin.setLitRendererTemplate(component, 'renderer', template, returnChannel, ['select'], NAMESPACE, 'app-1');

      renderItem({ key: '7', [`${NAMESPACE}name`]: 'Jane' }, 3);

      const [html, templateRoot, live, appId, itemKey, model, item, index, functions] = template.firstCall.args;
      expect(html).to.be.a('function');
      expect(templateRoot).to.equal(root);
      expect(live).to.be.a('function');
      expect(appId).to.equal('app-1');
      expect(itemKey).to.equal('7');
      expect(model.index).to.equal(3);
      expect(item).to.deep.equal({ name: 'Jane' });
      expect(index).to.equal(3);
      expect(Object.keys(functions)).to.deep.equal(['select']);
    });

    it('should call the server through the functions object', () => {
      Vaadin.setLitRendererTemplate(
        component,
        'renderer',
        (html: any, ...args: any[]) => {
          const functions = args[7];
          return html`<button @click=${functions.select}></button><i @click=${() => functions.select(1, 'a')}></i>`;
        },
        returnChannel,
        ['select'],
        NAMESPACE,
        'ROOT'
      );

      renderItem({ key: '5' });
      root.querySelector('button')!.click();
      root.querySelector('i')!.click();

      expect(returnChannel.callCount).to.equal(2);
      // An event argument is not sent to the server
      expect(returnChannel.firstCall.args).to.deep.equal(['select', '5', []]);
      expect(returnChannel.secondCall.args).to.deep.equal(['select', '5', [1, 'a']]);
    });

    it('should replace the content of another renderer', () => {
      Vaadin.setLitRenderer(component, 'renderer', '<b>old</b>', returnChannel, [], 'lr_old_', 'ROOT');
      renderItem({ key: '1' });

      Vaadin.setLitRendererTemplate(
        component,
        'renderer',
        (html: any) => html`<i>new</i>`,
        returnChannel,
        [],
        NAMESPACE,
        'ROOT'
      );
      renderItem({ key: '1' });

      expect(root.querySelector('b')).to.be.null;
      expect(root.querySelector('i')!.textContent).to.equal('new');
    });
  });

  describe('unsetLitRenderer', () => {
    it('should remove the renderer with the given id', () => {
      Vaadin.setLitRendererTemplate(component, 'renderer', (html: any) => html``, returnChannel, [], NAMESPACE, 'ROOT');

      Vaadin.unsetLitRenderer(component, 'renderer', NAMESPACE);

      expect(component.renderer).to.be.undefined;
    });

    it('should keep a renderer with another id', () => {
      Vaadin.setLitRendererTemplate(component, 'renderer', (html: any) => html``, returnChannel, [], NAMESPACE, 'ROOT');

      Vaadin.unsetLitRenderer(component, 'renderer', 'lr_other_');

      expect(component.renderer).to.be.a('function');
    });
  });
});
