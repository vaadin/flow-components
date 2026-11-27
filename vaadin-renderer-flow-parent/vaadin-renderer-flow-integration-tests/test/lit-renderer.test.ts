import type {} from '@web/test-runner-mocha';
import '../frontend/generated/jar-resources/lit-renderer.ts';
import { expect } from 'chai';
import sinon from 'sinon';

type Renderer = (root: HTMLElement, owner: HTMLElement, model: { item: any; index: number }) => void;

type Component = HTMLElement & { renderer?: Renderer };

type LitRendererApi = {
  setCompiledLitRenderer: (
    component: Component,
    rendererName: string,
    template: (...args: any[]) => unknown,
    returnChannel: (name: string, itemKey: string, args: any[]) => void,
    clientCallables: string[],
    propertyNamespace: string,
    appId: string,
    clientActions?: string[],
    clientActionFunctions?: ((event: Event | undefined, context: { args: any[] }) => void)[]
  ) => void;
  setLitRenderer: (
    component: Component,
    rendererName: string,
    templateExpression: string,
    returnChannel: (name: string, itemKey: string, args: any[]) => void,
    clientCallables: string[],
    propertyNamespace: string,
    appId: string,
    clientActions?: string[],
    clientActionFunctions?: ((event: Event | undefined, context: { args: any[] }) => void)[]
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

  describe('string template', () => {
    it('should render the template with the item properties of its namespace', () => {
      Vaadin.setLitRenderer(
        component,
        'renderer',
        '<span id="item-${index}">${item.name}</span>',
        returnChannel,
        [],
        NAMESPACE,
        'ROOT'
      );

      renderItem({ key: '1', [`${NAMESPACE}name`]: 'Jane', lr_other_name: 'Other' }, 2);

      const span = root.querySelector('span')!;
      expect(span.id).to.equal('item-2');
      expect(span.textContent).to.equal('Jane');
    });

    it('should call the server through the client callables', () => {
      Vaadin.setLitRenderer(
        component,
        'renderer',
        '<button @click=${select}></button><i @click=${() => remove(1, item.name)}></i>',
        returnChannel,
        ['select', 'remove'],
        NAMESPACE,
        'ROOT'
      );

      renderItem({ key: '5', [`${NAMESPACE}name`]: 'Jane' });
      root.querySelector('button')!.click();
      root.querySelector('i')!.click();

      expect(returnChannel.callCount).to.equal(2);
      // An event argument is not sent to the server
      expect(returnChannel.firstCall.args).to.deep.equal(['select', '5', []]);
      expect(returnChannel.secondCall.args).to.deep.equal(['remove', '5', [1, 'Jane']]);
    });
  });

  describe('declared template', () => {
    it('should render the template with the item properties of its namespace', () => {
      Vaadin.setCompiledLitRenderer(
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
      Vaadin.setCompiledLitRenderer(component, 'renderer', template, returnChannel, ['select'], NAMESPACE, 'app-1');

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
      Vaadin.setCompiledLitRenderer(
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

    it('should run client actions through the functions object without calling the server', () => {
      const copy = sinon.spy();
      const share = sinon.spy();
      Vaadin.setCompiledLitRenderer(
        component,
        'renderer',
        (html: any, ...args: any[]) => {
          const [item, functions] = [args[5], args[7]];
          return html`<button @click=${(e: Event) => functions.copy(e, item.email)}></button>
            <a @click=${() => functions.share(item.email)}></a>
            <i @click=${() => functions.select()}></i>`;
        },
        returnChannel,
        ['select'],
        NAMESPACE,
        'ROOT',
        ['copy', 'share'],
        [copy, share]
      );

      renderItem({ key: '5', [`${NAMESPACE}email`]: 'jane@example.com' });
      root.querySelector('button')!.click();
      root.querySelector('a')!.click();

      expect(returnChannel.called).to.be.false;
      expect(copy.calledOnce).to.be.true;
      const [event, context] = copy.firstCall.args;
      expect(event).to.be.an.instanceOf(Event);
      expect(context.args[1]).to.equal('jane@example.com');
      expect(share.calledOnce).to.be.true;
      expect(share.firstCall.args).to.deep.equal([undefined, { args: ['jane@example.com'] }]);

      root.querySelector('i')!.click();
      expect(returnChannel.firstCall.args).to.deep.equal(['select', '5', []]);
    });

    it('should replace the content of another renderer', () => {
      Vaadin.setLitRenderer(component, 'renderer', '<b>old</b>', returnChannel, [], 'lr_old_', 'ROOT');
      renderItem({ key: '1' });

      Vaadin.setCompiledLitRenderer(
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

  describe('string template', () => {
    it('should run client actions by name without calling the server', () => {
      const copy = sinon.spy();
      const share = sinon.spy();
      Vaadin.setLitRenderer(
        component,
        'renderer',
        '<button @click=${(e) => copy(e, item.email)}></button><a @click=${() => share(item.email)}></a><i @click=${() => select()}></i>',
        returnChannel,
        ['select'],
        NAMESPACE,
        'ROOT',
        ['copy', 'share'],
        [copy, share]
      );

      renderItem({ key: '5', [`${NAMESPACE}email`]: 'jane@example.com' });
      root.querySelector('button')!.click();
      root.querySelector('a')!.click();

      expect(returnChannel.called).to.be.false;
      expect(copy.calledOnce).to.be.true;
      const [event, context] = copy.firstCall.args;
      expect(event).to.be.an.instanceOf(Event);
      expect(context.args[1]).to.equal('jane@example.com');
      expect(share.calledOnce).to.be.true;
      expect(share.firstCall.args).to.deep.equal([undefined, { args: ['jane@example.com'] }]);

      root.querySelector('i')!.click();
      expect(returnChannel.firstCall.args).to.deep.equal(['select', '5', []]);
    });
  });

  describe('unsetLitRenderer', () => {
    it('should remove the renderer with the given id', () => {
      Vaadin.setCompiledLitRenderer(component, 'renderer', (html: any) => html``, returnChannel, [], NAMESPACE, 'ROOT');

      Vaadin.unsetLitRenderer(component, 'renderer', NAMESPACE);

      expect(component.renderer).to.be.undefined;
    });

    it('should keep a renderer with another id', () => {
      Vaadin.setCompiledLitRenderer(component, 'renderer', (html: any) => html``, returnChannel, [], NAMESPACE, 'ROOT');

      Vaadin.unsetLitRenderer(component, 'renderer', 'lr_other_');

      expect(component.renderer).to.be.a('function');
    });
  });
});
