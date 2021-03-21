/**
 * @license
 * Copyright (c) 2019 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at
 * http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at
 * http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */

import {LitElement, html, css, unsafeCSS} from 'lit-element';
import { css_gwt, css_valo, Spreadsheet } from 'spreadsheet-export';

/**
 * An example element.
 *
 * @slot - This element has a slot
 * @csspart button - The button
 */
export class VaadinSpreadsheet extends LitElement {


  static get styles() {
    return css`    
    
      #mislot {
        border: 1px solid green;
        height: 200px;
      }
      
      slot {
        border: 1px solid green;
      }
    
      ${unsafeCSS(css_gwt)}

      ${unsafeCSS(css_valo)}
    `;
  }

  static get properties() {
    return {
      /**
       * The name to say "Hello" to.
       */
      name: {type: String},

      api: {type: Object},

      received: {type: String},

      /**
       * The number of times the button has been clicked.
       */
      count: {type: Number},
    };
  }

  updateSharedState(newVal) {
    this.api.setState(newVal);
  }

  alert(newVal) {
    this.api.alert(newVal);
  }

  constructor() {
    super();
    this.name = 'World';
    this.count = 0;
    this.received = '';
  }

/*
  createRenderRoot() {
    // Do not use a shadow root
    return this;
  }
*/

  render() {
    return html`
      <h1>This is a web component</h1>
      <slot></slot>
    `;
  }

  _onClick() {
    this.count++;
    this.api.setState(this.name + ' - ' + this.count);
    console.log('Estado fijado a ' + this.name)
  }

  connectedCallback() {
    super.connectedCallback()
    console.log('connected')
  }

  addStyle(styleString) {
    const style = document.createElement('style');
    style.textContent = styleString;
    document.head.append(style);
  }

  updated(_changedProperties) {
    super.updated(_changedProperties);
    console.log(this.shadowRoot.querySelector('#mislot'));
    //console.log(this.querySelector('#mislot'));
    if (!this.api) {

      this.addStyle(css_gwt);
      this.addStyle(css_valo);

      const div = document.createElement('div');
      div.setAttribute('class', 'spreadsheetport');
      div.setAttribute('style', 'height: 300px;');
      this.append(div);

      this.api = new Spreadsheet(div);
      //this.api = new Spreadsheet(this.querySelector('#mislot'));
      this.api.registerClicked(e => {
        this.received = '' + e;
        let event = new CustomEvent('my-event', {
          detail: {
            message: '' + e
          }
        });
        this.dispatchEvent(event);
      }); //this.api.alert('webcomponent callback called for ' + e));
      this.api.alert('Hello from web component');
      console.log('updated')
    }
  }

  attributeChangedCallback(name, oldVal, newVal) {
    console.log('attribute change: ', name, newVal);
    if ('name' == name) {
      this.api.setState(newVal);
    }
    super.attributeChangedCallback(name, oldVal, newVal);
  }

}

window.customElements.define('vaadin-spreadsheet', VaadinSpreadsheet);
