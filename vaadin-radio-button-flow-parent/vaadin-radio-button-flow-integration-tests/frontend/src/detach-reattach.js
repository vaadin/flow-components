import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import '@vaadin/vaadin-radio-button/src/vaadin-radio-group.js';

class DetachReattach extends PolymerElement {
    //language=html
    static get template() {
        return html`            
            <vaadin-radio-group id="testGroup"></vaadin-radio-group>
        `;
    }

    static get is() {
        return 'detach-reattach';
    }
}

customElements.define(DetachReattach.is, DetachReattach);
