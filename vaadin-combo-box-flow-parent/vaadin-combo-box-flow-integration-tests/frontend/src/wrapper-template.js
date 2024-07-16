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

class WrapperTemplate extends PolymerElement {
  static get template() {
    return html`
        <combo-box-in-a-template id="comboBoxInATemplate"></combo-box-in-a-template>
        <combo-box-in-a-template2 id="comboBoxInATemplate2"></combo-box-in-a-template2>
`;
  }

  static get is() {
      return 'wrapper-template'
  }
}
customElements.define(WrapperTemplate.is, WrapperTemplate);
