import { html, LitElement } from 'lit';

class SideNavInTemplate extends LitElement {
  render() {
    return html`
      <vaadin-side-nav id="sideNav">
        <vaadin-side-nav-item path="/home">
          <span slot="prefix">prefix</span>
            Home
          <span slot="suffix">suffix</span>
        </vaadin-side-nav-item>
        <vaadin-side-nav-item path="/about">
          About
          <br>
          this project
        </vaadin-side-nav-item>
      </vaadin-side-nav>
    `;
  }

  static get is() {
    return 'side-nav-in-template';
  }
}

customElements.define(SideNavInTemplate.is, SideNavInTemplate);
