/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsInvokers;

/**
 * The JavaScript that {@link Crud} runs on its element in the browser, for
 * {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface CrudJs extends JsInvokers {

    /**
     * Replaces the validation of the editor form in the browser with a function
     * that always passes.
     */
    @JsExpression("this.__validate = function () {return true;}")
    void disableClientValidation();

    /**
     * Replaces the check whether the Save button is disabled with a function
     * that returns the given value.
     *
     * @param disabled
     *            {@code true} if the Save button is disabled
     */
    @JsExpression("this.__isSaveBtnDisabled = () => {return $0;}")
    void setSaveButtonDisabled(boolean disabled);

    /**
     * Opens or closes the editor.
     *
     * @param opened
     *            {@code true} to open the editor, {@code false} to close it
     */
    @JsExpression("this.editorOpened = $0")
    void setEditorOpened(boolean opened);

    /**
     * Sets the dirty state of the editor.
     *
     * @param dirty
     *            {@code true} if the editor is dirty
     */
    @JsExpression("this.__isDirty = $0")
    void setDirty(boolean dirty);
}
