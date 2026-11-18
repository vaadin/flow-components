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
package com.vaadin.flow.component.contextmenu;

import java.io.Serializable;

import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The JavaScript that {@link ContextMenuBase} runs in the browser, for
 * {@link Element#executeJs(Class)}. Unless stated otherwise, the methods are
 * called on the context menu element.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface ContextMenuJs extends Serializable {

    /**
     * Installs the context menu connector on the context menu element.
     */
    @JsExpression("window.Vaadin.Flow.contextMenuConnector.initLazy(this)")
    void initConnector();

    /**
     * Sets the element that opens the context menu.
     *
     * @param listenOn
     *            the element of the target, or the context menu element when
     *            there is no target
     */
    @JsExpression("this.listenOn=$0;")
    void setListenOn(Element listenOn);

    /**
     * Installs the target connector, <code>$contextMenuTargetConnector</code>,
     * and sets the event that opens the context menu. Runs on the target
     * element.
     *
     * @param openOn
     *            the name of the event that opens the context menu
     * @return the pending result of the call
     */
    @JsExpression("""
            window.Vaadin.Flow.contextMenuTargetConnector.init(this);
            this.$contextMenuTargetConnector.updateOpenOn($0);""")
    PendingJavaScriptResult initTargetConnector(String openOn);

    /**
     * Removes the target connector, if it is installed. Runs on the target
     * element.
     */
    @JsExpression("if (this.$contextMenuTargetConnector) { this.$contextMenuTargetConnector.removeConnector() }")
    void removeTargetConnector();
}
