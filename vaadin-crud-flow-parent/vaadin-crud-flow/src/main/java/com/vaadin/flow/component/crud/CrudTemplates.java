/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud;

import com.vaadin.flow.data.renderer.CompiledLitTemplate;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsSnippets;

/**
 * The Lit templates of {@link Crud}, declared so that the browser does not
 * compile them from strings.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
interface CrudTemplates extends JsSnippets {

    /**
     * Returns the template of the renderer of {@link Crud#addEditColumn}. It
     * renders a {@code <vaadin-crud-edit>} element with the given text as its
     * {@code aria-label} attribute.
     *
     * @param editLabel
     *            the accessible label of the edit button
     * @return the template of the renderer of {@link Crud#addEditColumn}
     */
    @JsExpression("return html`<vaadin-crud-edit aria-label=${$0}></vaadin-crud-edit>`;")
    CompiledLitTemplate editColumnRenderer(String editLabel);
}
