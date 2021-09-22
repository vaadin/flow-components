import {customElement, html, TemplateResult, LitElement} from "lit-element";

@customElement("combo-box-lit-template-wrapper")
class ComboBoxLitTemplateWrapper extends LitElement {
 render() {
        return html`
            <style>
                :host {
                    display: block;
                    border: 1px solid orange;
                    margin: 30px;
                    padding: 30px;
                }
            </style>
            <h5>Combo-box wrapper</h5>
            <vaadin-combo-box id="cb"></vaadin-combo-box>
        `;
    }
}