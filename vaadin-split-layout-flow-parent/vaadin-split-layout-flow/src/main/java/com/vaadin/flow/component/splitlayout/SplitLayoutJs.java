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
package com.vaadin.flow.component.splitlayout;

import java.io.Serializable;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The JavaScript that {@link SplitLayout} runs on its element in the browser,
 * for {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface SplitLayoutJs extends Serializable {

    /**
     * Sets a style property of a child element of the split layout, if the
     * child exists.
     *
     * @param index
     *            the index of the child element, {@code 0} for the primary and
     *            {@code 1} for the secondary component
     * @param styleName
     *            the name of the style property
     * @param value
     *            the value of the style property
     */
    @JsExpression("var element = this.children[$0]; if (element) { element.style[$1]=$2; }")
    void setChildStyle(int index, String styleName, String value);
}
