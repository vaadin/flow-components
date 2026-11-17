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
package com.vaadin.flow.component.radiobutton;

import java.io.Serializable;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The JavaScript that {@link RadioButtonGroup} runs in the browser, for
 * {@link Element#executeJs(Class)}. The methods are called on the radio button
 * element of an item.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface RadioButtonGroupJs extends Serializable {

    /**
     * Sets the {@code disabled} property of an item's radio button.
     *
     * @param disabled
     *            {@code true} to disable the radio button, {@code false} to
     *            enable it
     */
    @JsExpression("this.disabled = $0")
    void setItemDisabled(boolean disabled);
}
