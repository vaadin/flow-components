import { render, html } from 'lit';

type RenderRoot = HTMLElement & { __litRendererId?: number; _$litPart$?: any };

type ItemModel = { item: any; index: number };

type Renderer = (root: RenderRoot, _: HTMLElement, model?: ItemModel) => void;

let rendererId = 0;

const _window = window as any;
_window.Vaadin = _window.Vaadin || {};

_window.Vaadin.assignLitRenderer = (
  component: HTMLElement,
  rendererName: string,
  templateExpression: string,
  returnChannel: (name: string, itemKey: string, args: any[]) => void,
  clientCallables: string[]
) => {
  // Dynamically constructed function that renders the templateExpression
  // inside the given root element using Lit
  const renderFunction = Function(`
    "use strict";

    const [render, html, returnChannel] = arguments;

    return (root, {item, index}) => {
      ${clientCallables
        .map((clientCallable) => {
          // Map all the client-callables as inline functions so they can be accessed from the template (with @event-binding)
          // TODO: check "arguments"
          return `const ${clientCallable} = () => {
            returnChannel('${clientCallable}', item.key, arguments[0] instanceof Event ? [] : [...arguments])
          }`;
        })
        .join('\n')}

      render(html\`${templateExpression}\`, root)
    }
  `)(render, html, returnChannel);

  const instanceRendererId = rendererId++;

  const renderer: Renderer = (root, _, model) => {
    // TODO: Remove? Needed in virtual list?
    if (model?.item === undefined) {
      return;
    }

    // Clean up the root element of any existing content
    // (and Lit's _$litPart$ property) from other renderers
    if (root.__litRendererId !== instanceRendererId) {
      root.innerHTML = '';
      delete root._$litPart$;
      root.__litRendererId = instanceRendererId;
    }

    renderFunction(root, model);
  };

  (component as any)[rendererName] = renderer;
};
