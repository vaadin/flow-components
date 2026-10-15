/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import { html, TemplateResult, LitElement } from 'lit';
import { customElement } from 'lit/decorators.js';

@customElement('multi-select-combo-box-lit-wrapper')
export class MultiSelectComboBoxLitWrapper extends LitElement {
  protected render(): TemplateResult {
    return html`
      <div>
        <vaadin-multi-select-combo-box id="combo-box"></vaadin-multi-select-combo-box>
      </div>
    `;
  }
}
