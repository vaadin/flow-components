/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */

import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';

class MultiSelectComboBoxPolymerWrapper extends PolymerElement {
  static get template() {
    return html`
        <vaadin-multi-select-combo-box id="combo-box"></vaadin-multi-select-combo-box>
    `;
  }

  static get is() {
      return 'multi-select-combo-box-polymer-wrapper'
  }
}
customElements.define(MultiSelectComboBoxPolymerWrapper.is, MultiSelectComboBoxPolymerWrapper);
