/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.combobox;

import java.io.Serializable;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The JavaScript that {@link ComboBox} and {@link MultiSelectComboBox} run in
 * the browser, for {@link Element#executeJs(Class)}. Unless stated otherwise,
 * the methods are called on the combo box element.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface ComboBoxJs extends Serializable {

    /**
     * Installs the combo box connector, <code>$connector</code>, on the combo
     * box element.
     */
    @JsExpression("window.Vaadin.Flow.comboBoxConnector.initLazy(this)")
    void initConnector();

    /**
     * Resets the data of the combo box connector, if it is initialized. Called
     * on the page rather than on the combo box element, with
     * {@link com.vaadin.flow.component.page.Page#executeJs(Class)}. On the
     * first attach, it runs before the connector is installed.
     *
     * @param comboBox
     *            the combo box element
     */
    @JsExpression("$0.$connector?.reset()")
    void resetConnector(Element comboBox);

    /**
     * Sets the value of the input element, even if the value of the combo box
     * has not changed.
     *
     * @param inputElementValue
     *            the text to show in the input element
     */
    @JsExpression("this._inputElementValue = $0")
    void setInputElementValue(String inputElementValue);
}
