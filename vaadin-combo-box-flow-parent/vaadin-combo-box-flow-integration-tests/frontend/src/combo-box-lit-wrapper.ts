/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
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
