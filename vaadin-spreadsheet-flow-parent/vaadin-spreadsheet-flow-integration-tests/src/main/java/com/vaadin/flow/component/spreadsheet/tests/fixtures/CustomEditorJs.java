/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import java.io.Serializable;

import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The JavaScript that the combo box custom editors of the fixtures run in the
 * browser, for {@link com.vaadin.flow.dom.Element#executeJs(Class)}.
 */
@JsDefinition
interface CustomEditorJs extends Serializable {

    /**
     * Selects the text of the input element. Runs on the combo box element.
     */
    @JsExpression("this.inputElement.select();")
    void selectInputText();
}
