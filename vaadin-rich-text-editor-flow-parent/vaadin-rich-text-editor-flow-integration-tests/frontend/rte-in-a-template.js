/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';

class RichTextEditorInATemplate extends PolymerElement {
    static get template() {
      return html`
        <vaadin-rich-text-editor id="richTextEditor"></vaadin-rich-text-editor>
`;
  }
    static get is() {
      return 'rte-in-a-template'
  }
}
customElements.define(RichTextEditorInATemplate.is, RichTextEditorInATemplate);
