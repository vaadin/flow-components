import {customElement, html, TemplateResult, LitElement} from "lit-element";

@customElement("combo-box-lit-wrapper-lit-template-page")
class ComboBoxLitWrapperLitTemplatePage extends LitElement {
    render() {
        return html`
            <style>
                :host {
                    display: block;
                    border: 1px solid green;
                    margin:30px;
                }
            </style>
            <h4>Como-box parent lit template</h4>
            <combo-box-lit-template-wrapper id="cbw1"></combo-box-lit-template-wrapper>
            <combo-box-lit-template-wrapper id="cbw2"></combo-box-lit-template-wrapper>
            <combo-box-lit-template-wrapper id="cbw3"></combo-box-lit-template-wrapper>
        `
    }
}