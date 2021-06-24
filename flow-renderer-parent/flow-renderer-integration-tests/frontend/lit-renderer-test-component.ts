import { html, LitElement } from 'lit';
import { customElement, property } from 'lit/decorators.js';

type ItemModel = { item: any; index: number };

type Renderer = (root: HTMLElement, _: HTMLElement, model: ItemModel) => void;

@customElement('lit-renderer-test-component')
export class LitRendererTestComponent extends LitElement {
  @property()
  renderer?: Renderer;

  @property({ type: Array })
  items: string[] = [];

  createRenderRoot() {
    return this;
  }

  updated() {
    this.items.forEach((item, index) => {
      const element = this.querySelector(`#item-${index}`);
      if (!(element instanceof HTMLElement)) {
        return;
      }

      if (this.renderer) {
        this.renderer(element, this, { item, index });
      } else {
        element.textContent = item;
      }
    });
  }

  render() {
    return html`
      <b>Items:</b>
      <ul>
        ${this.items.map((_, index) => html`<li id="item-${index}"></li>`)}
      </ul>
    `;
  }
}
