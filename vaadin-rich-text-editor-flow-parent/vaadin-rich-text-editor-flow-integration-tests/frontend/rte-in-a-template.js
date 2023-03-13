import { html, LitElement } from 'lit';

class RichTextEditorInATemplate extends LitElement {
  render() {
    return html`<vaadin-rich-text-editor id="richTextEditor"></vaadin-rich-text-editor>`;
  }

  static get is() {
    return 'rte-in-a-template';
  }
}

customElements.define(RichTextEditorInATemplate.is, RichTextEditorInATemplate);
