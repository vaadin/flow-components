/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor;

import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsInvokers;

/**
 * The JavaScript that {@link RichTextEditor} runs on its element in the
 * browser, for {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
interface RichTextEditorJs extends JsInvokers {

    /**
     * Gets the HTML value of the editor.
     *
     * @return the pending result, which resolves to the HTML value
     */
    @JsExpression("return this.htmlValue")
    PendingJavaScriptResult getHtmlValue();
}
