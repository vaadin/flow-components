/*
  ~ Copyright 2000-2018 Vaadin Ltd.
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
import {html, LitElement} from "lit-element";

class ComboBoxPolymerWrapperLitView extends LitElement {
    render() {
        return html`
            <style>
                :host {
                    display: block;
                }
            </style>
            <combo-box-polymer-wrapper id="cbw1"></combo-box-polymer-wrapper>
        `
    }
}

customElements.define("combo-box-polymer-wrapper-lit-view", ComboBoxPolymerWrapperLitView)