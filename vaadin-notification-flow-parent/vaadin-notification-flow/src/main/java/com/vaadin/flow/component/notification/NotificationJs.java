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
package com.vaadin.flow.component.notification;

import java.io.Serializable;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The JavaScript that {@link Notification} runs on its element in the browser,
 * for {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface NotificationJs extends Serializable {

    /**
     * Patches the notification element so that it can hold the child components
     * as virtual children.
     */
    @JsExpression("Vaadin.FlowComponentHost.patchVirtualContainer(this)")
    void patchVirtualContainer();

    /**
     * Sets the renderer of the notification, which renders the {@code text}
     * property when it is set, and the virtual child components otherwise.
     *
     * @param appId
     *            the id of the application that the child components belong to
     */
    @JsExpression("""
            this.renderer = (root) => {
              if (this.text) {
                root.textContent = this.text;
              } else {
                Vaadin.FlowComponentHost.setChildNodes($0, this.virtualChildNodeIds, root)
              }
            }
            """)
    void configureRenderer(String appId);
}
