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
package com.vaadin.flow.component.tabs;

import java.io.Serializable;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The JavaScript that {@link Tabs} runs in the browser, for
 * {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface TabsJs extends Serializable {

    /**
     * Adds a listener to the tabs element that updates the selected tab on the
     * server when the tabs change in the browser. Called on the page rather
     * than on the tabs element, with
     * {@link com.vaadin.flow.component.page.Page#executeJs(Class)}.
     *
     * @param tabs
     *            the tabs element
     */
    @JsExpression("$0.addEventListener('items-changed', function(){ this.$server.updateSelectedTab(true); });")
    void addItemsChangedListener(Element tabs);
}
