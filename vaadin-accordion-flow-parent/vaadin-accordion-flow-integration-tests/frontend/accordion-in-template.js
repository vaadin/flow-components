import { css, html, LitElement } from 'lit';
import '@vaadin/accordion/vaadin-accordion.js';
import '@vaadin/text-field/vaadin-text-field.js';
import '@vaadin/vertical-layout/vaadin-vertical-layout.js';

class AccordionApp extends LitElement {
  static get styles() {
    return css`
      :host {
        height: 100%;
        width: 100%;
        display: block;
      }
    `;
  }

  render() {
    return html`
      <h1>Accordion in template</h1>

      <vaadin-accordion id="accordion">
        <vaadin-accordion-panel>
          <span slot="summary">Personal Information</span>
          <vaadin-vertical-layout>
            <vaadin-text-field label="Name" theme="small"></vaadin-text-field>
            <vaadin-text-field label="Phone" theme="small"></vaadin-text-field>
            <vaadin-text-field label="Email" theme="small"></vaadin-text-field>
          </vaadin-vertical-layout>
        </vaadin-accordion-panel>
        <vaadin-accordion-panel>
          <span slot="summary">Billing Address</span>
          <vaadin-vertical-layout>
            <vaadin-text-field label="Address" theme="small"></vaadin-text-field>
            <vaadin-text-field label="City" theme="small"></vaadin-text-field>
            <vaadin-text-field label="State" theme="small"></vaadin-text-field>
            <vaadin-text-field label="Zip Code" theme="small"></vaadin-text-field>
          </vaadin-vertical-layout>
        </vaadin-accordion-panel>
        <vaadin-accordion-panel disabled>
          <span slot="summary">Payment</span>
          <span>Not yet implemented</span>
        </vaadin-accordion-panel>
      </vaadin-accordion>

      <vaadin-vertical-layout id="events"></vaadin-vertical-layout>
    `;
  }

  static get is() {
    return 'accordion-app';
  }
}

customElements.define(AccordionApp.is, AccordionApp);
