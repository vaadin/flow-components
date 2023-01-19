import { html, LitElement } from 'lit';

class TemplatedColumns extends LitElement {
  render() {
    return html`
      <vaadin-grid id="grid">
        <vaadin-grid-column
          width="30px"
          flex-grow="0"
          header="#"
          .renderer="${(root, _, { index }) => (root.textContent = index)}"
        >
        </vaadin-grid-column>

        <vaadin-grid-column-group header="Name">
          <vaadin-grid-column
            header="First"
            .renderer="${(root, _, { item }) => (root.textContent = item.name.first)}"
          ></vaadin-grid-column>

          <vaadin-grid-column
            header="Last"
            .renderer="${(root, _, { item }) => (root.textContent = item.name.last)}"
          ></vaadin-grid-column>
        </vaadin-grid-column-group>

        <vaadin-grid-column-group header="Location">
          <vaadin-grid-column
            header="City"
            .renderer="${(root, _, { item }) => (root.textContent = item.location.city)}"
          ></vaadin-grid-column>

          <vaadin-grid-column
            header="State"
            .renderer="${(root, _, { item }) => (root.textContent = item.location.state)}"
          ></vaadin-grid-column>

          <vaadin-grid-column
            header="Street"
            .renderer="${(root, _, { item }) => (root.textContent = item.location.street)}"
          ></vaadin-grid-column>
        </vaadin-grid-column-group>
      </vaadin-grid>
    `;
  }

  static get is() {
    return 'templated-columns';
  }
}

customElements.define(TemplatedColumns.is, TemplatedColumns);
