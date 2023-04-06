import {html, TemplateResult, LitElement} from "lit";
import {customElement} from "lit/decorators.js";

@customElement("combo-box-lit-wrapper")
export class ComboBoxLitWrapper extends LitElement {

    protected render(): TemplateResult {
        return html`
            <div>
                <vaadin-combo-box id="combo" style="width: 100%;"></vaadin-combo-box>
            </div>
        `;
    }

    constructor() {
        super();
    }

    connectedCallback() {
        super.connectedCallback();
    }

    disconnectedCallback() {
        super.disconnectedCallback();
    }
}
