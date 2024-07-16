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

class TemplateButton extends PolymerElement {
    static get template() {
      return html`
        <div id="container"></div>
        <button id="btn">Click me!</button>
`;
  }
    static get is() {
      return 'vaadin-dialog-flow-test-template'
  }
}
customElements.define(TemplateButton.is, TemplateButton);
