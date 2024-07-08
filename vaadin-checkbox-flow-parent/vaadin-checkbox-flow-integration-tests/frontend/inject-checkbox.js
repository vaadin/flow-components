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
import '@vaadin/vaadin-checkbox/vaadin-checkbox.js';

class InjectChecbox extends PolymerElement {
    static get template() {
      return html`
    <vaadin-checkbox id="accept">Accept</vaadin-checkbox>
    <div id="div">A</div>
`;
  }
      static get is() {
      return 'inject-checkbox'
  }
}

customElements.define(InjectChecbox.is, InjectChecbox);