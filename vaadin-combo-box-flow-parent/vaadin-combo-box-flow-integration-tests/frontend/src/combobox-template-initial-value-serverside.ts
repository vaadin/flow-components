import {customElement, html, TemplateResult, LitElement} from "lit-element";

@customElement("combobox-initial-value")
export class TestForm extends LitElement {

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