import {customElement, html, TemplateResult, LitElement} from "lit-element";

@customElement("combo-box-initial-value")
export class ComboBoxInitialValue extends LitElement {

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