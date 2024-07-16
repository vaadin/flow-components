/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';
import '@vaadin/vaadin-button/vaadin-button.js';
import '@polymer/iron-icon/iron-icon.js';

class TemplateButton extends PolymerElement {
  static get template() {
    return html`
        <vaadin-button id="button">
            Template caption
        </vaadin-button>

        <vaadin-button id="icon-button">
            <iron-icon icon="lumo:edit"></iron-icon>
            <span>Template with icon</span>
        </vaadin-button>
`;
  }
    static get is() {
      return 'template-button'
  }
}
  
customElements.define(TemplateButton.is, TemplateButton);
