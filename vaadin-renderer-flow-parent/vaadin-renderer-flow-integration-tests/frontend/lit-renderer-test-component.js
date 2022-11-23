import { html, LitElement } from 'lit';

export class LitRendererTestComponent extends LitElement {
  static get properties() {
    return {
      renderer: { type: Object },

      detailsRenderer: { type: Object },

      items: { type: Array }
    };
  }

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

customElements.define('lit-renderer-test-component', LitRendererTestComponent);
