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
package com.vaadin.flow.component.popover;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsInvokers;

/**
 * The JavaScript that {@link Popover} runs in the browser, for
 * {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
interface PopoverJs extends JsInvokers {

    /**
     * Sets the target of the popover element.
     *
     * @param target
     *            the element to attach the popover to, or {@code null} to clear
     *            the target
     */
    @JsExpression("this.target = $0")
    void setTarget(Element target);

    /**
     * Creates the global popover configuration object,
     * {@code window.Vaadin.Flow.popover}, if it does not exist yet. Runs on the
     * UI element.
     */
    @JsExpression("((window.Vaadin ||= {}).Flow ||= {}).popover ||= {}")
    void initConfiguration();

    /**
     * Sets the default hide delay of all popovers. Runs on the UI element.
     *
     * @param defaultHideDelay
     *            the default hide delay
     */
    @JsExpression("""
            const popover = window.Vaadin.Flow.popover;
            popover.defaultHideDelay = $0;
            popover.setDefaultHideDelay?.($0)
            """)
    void setDefaultHideDelay(int defaultHideDelay);

    /**
     * Sets the default focus delay of all popovers. Runs on the UI element.
     *
     * @param defaultFocusDelay
     *            the default focus delay
     */
    @JsExpression("""
            const popover = window.Vaadin.Flow.popover;
            popover.defaultFocusDelay = $0;
            popover.setDefaultFocusDelay?.($0)
            """)
    void setDefaultFocusDelay(int defaultFocusDelay);

    /**
     * Sets the default hover delay of all popovers. Runs on the UI element.
     *
     * @param defaultHoverDelay
     *            the default hover delay
     */
    @JsExpression("""
            const popover = window.Vaadin.Flow.popover;
            popover.defaultHoverDelay = $0;
            popover.setDefaultHoverDelay?.($0)
            """)
    void setDefaultHoverDelay(int defaultHoverDelay);
}
