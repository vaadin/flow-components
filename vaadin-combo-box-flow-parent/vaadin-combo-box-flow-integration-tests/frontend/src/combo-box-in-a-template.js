/*
  ~ Copyright 2000-2022 Vaadin Ltd.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License. You may obtain a copy of
  ~ the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  ~ WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  ~ License for the specific language governing permissions and limitations under
  ~ the License.
  */

import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';

class ComboBoxInATemplate extends PolymerElement {
  static get template() {
    return html`
        <vaadin-combo-box id="comboBox"></vaadin-combo-box>
`;
  }

  static get is() {
      return 'combo-box-in-a-template'
  }
}
customElements.define(ComboBoxInATemplate.is, ComboBoxInATemplate);
