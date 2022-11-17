import { html, LitElement } from 'lit';
import { customElement, property } from 'lit/decorators.js';

type ItemModel = { item: any; index: number };

type Renderer = (root: HTMLElement, _: HTMLElement, model: ItemModel) => void;

@customElement('lit-renderer-test-component')
export class LitRendererTestComponent extends LitElement {
  @property()
  renderer?: Renderer;

  @property()
  detailsRenderer?: Renderer;

  @property({ type: Array })
  items: string[] = [];

  createRenderRoot() {
    return this;
  }

  updated() {
    this.items.forEach((item, index) => {
      const main = this.querySelector(`#item-${index} .main`);
      if (!(main instanceof HTMLElement)) {
        return;
      }

      if (this.renderer) {
        this.renderer(main, this, { item, index });
      } else {
        main.textContent = item;
      }

      const details = this.querySelector(`#item-${index} .details`);
      if (!(details instanceof HTMLElement)) {
        return;
      }

      if (this.detailsRenderer) {
        this.detailsRenderer(details, this, { item, index });
      } else {
        details.textContent = '';
      }
    });
  }

  render() {
    return html`
      <b>Items:</b>
      <ul>
        ${this.items.map(
          (_, index) => html`
            <li id="item-${index}">
              <div class="main"></div>
              <i class="details"></i>
            </li>
          `
        )}
      </ul>
    `;
  }
}
