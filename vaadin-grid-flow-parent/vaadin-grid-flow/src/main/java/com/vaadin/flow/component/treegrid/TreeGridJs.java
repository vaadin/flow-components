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
package com.vaadin.flow.component.treegrid;

import java.io.Serializable;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The JavaScript that {@link TreeGrid} runs in the browser, in addition to the
 * one of {@link com.vaadin.flow.component.grid.Grid}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface TreeGridJs extends Serializable {

    /**
     * Installs the tree grid connector, <code>$connector</code>, on the tree
     * grid element, if the element exists on the client. Called on the page
     * rather than on the tree grid element, with
     * {@link com.vaadin.flow.component.page.Page#executeJs(Class)}, so that it
     * runs before the JavaScript that is called on the tree grid element.
     *
     * @param treeGrid
     *            the tree grid element
     */
    @JsExpression("if ($0) window.Vaadin.Flow.treeGridConnector.initLazy($0)")
    void initConnector(Element treeGrid);
}
