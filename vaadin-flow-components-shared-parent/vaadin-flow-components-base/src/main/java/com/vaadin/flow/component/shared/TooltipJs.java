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
package com.vaadin.flow.component.shared;

import java.io.Serializable;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

/**
 * The JavaScript that tooltips run in the browser, for
 * {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface TooltipJs extends Serializable {

    /**
     * Sets the target of the tooltip element.
     *
     * @param target
     *            the element to attach the tooltip to
     */
    @JsExpression("this.target = $0;")
    void setTarget(Element target);

    /**
     * Creates the global tooltip configuration object,
     * {@code window.Vaadin.Flow.tooltip}, if it does not exist yet.
     */
    @JsExpression("((window.Vaadin ||= {}).Flow ||= {}).tooltip ||= {}")
    void initConfiguration();

    /**
     * Sets the default hide delay of all tooltips.
     *
     * @param defaultHideDelay
     *            the default hide delay
     */
    @JsExpression("""
            const tooltip = window.Vaadin.Flow.tooltip;
            tooltip.defaultHideDelay = $0;
            tooltip.setDefaultHideDelay?.($0)
            """)
    void setDefaultHideDelay(int defaultHideDelay);

    /**
     * Sets the default focus delay of all tooltips.
     *
     * @param defaultFocusDelay
     *            the default focus delay
     */
    @JsExpression("""
            const tooltip = window.Vaadin.Flow.tooltip;
            tooltip.defaultFocusDelay = $0;
            tooltip.setDefaultFocusDelay?.($0)
            """)
    void setDefaultFocusDelay(int defaultFocusDelay);

    /**
     * Sets the default hover delay of all tooltips.
     *
     * @param defaultHoverDelay
     *            the default hover delay
     */
    @JsExpression("""
            const tooltip = window.Vaadin.Flow.tooltip;
            tooltip.defaultHoverDelay = $0;
            tooltip.setDefaultHoverDelay?.($0)
            """)
    void setDefaultHoverDelay(int defaultHoverDelay);
}
