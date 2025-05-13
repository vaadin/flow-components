/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.server.VaadinService;

/**
 * A configuration class for a tooltips default behavior.
 *
 * @author Vaadin Ltd
 */
@NpmPackage(value = "@vaadin/tooltip", version = "24.8.0-alpha18")
@JsModule("./tooltip.ts")
public class TooltipConfiguration implements Serializable {

    private static Integer defaultHideDelay;
    private static Integer defaultFocusDelay;
    private static Integer defaultHoverDelay;
    private static boolean uiInitListenerRegistered = false;

    /**
     * Sets the default focus delay to be used by all tooltip instances (running
     * in the same JVM), except for those that have focus delay configured using
     * property.
     *
     * @param defaultFocusDelay
     *            the default focus delay
     */
    public static void setDefaultFocusDelay(int defaultFocusDelay) {
        TooltipConfiguration.defaultFocusDelay = defaultFocusDelay;
        applyConfiguration();
    }

    /**
     * Sets the default hide delay to be used by all tooltip instances (running
     * in the same JVM), except for those that have hide delay configured using
     * property.
     *
     * @param defaultHideDelay
     *            the default hide delay
     */
    public static void setDefaultHideDelay(int defaultHideDelay) {
        TooltipConfiguration.defaultHideDelay = defaultHideDelay;
        applyConfiguration();
    }

    /**
     * Sets the default hover delay to be used by all tooltip instances (running
     * in the same JVM), except for those that have hover delay configured using
     * property.
     *
     * @param defaultHoverDelay
     *            the default hover delay
     */
    public static void setDefaultHoverDelay(int defaultHoverDelay) {
        TooltipConfiguration.defaultHoverDelay = defaultHoverDelay;
        applyConfiguration();
    }

    private static void applyConfiguration() {
        if (UI.getCurrent() != null) {
            // Apply the default tooltip configuration for the current UI
            applyConfigurationForUI(UI.getCurrent());
        }

        if (!uiInitListenerRegistered) {
            // Apply the tooltip configuration for all new UIs
            VaadinService.getCurrent()
                    .addUIInitListener(e -> applyConfigurationForUI(e.getUI()));
            uiInitListenerRegistered = true;
        }
    }

    private static void applyConfigurationForUI(UI ui) {
        ui.getElement().executeJs(
                "((window.Vaadin ||= {}).Flow ||= {}).tooltip ||= {}");

        if (defaultHideDelay != null) {
            ui.getElement().executeJs(
                    "const tooltip = window.Vaadin.Flow.tooltip;"
                            + "tooltip.defaultHideDelay = $0;"
                            + "tooltip.setDefaultHideDelay?.($0)",
                    defaultHideDelay);
        }

        if (defaultFocusDelay != null) {
            ui.getElement().executeJs(
                    "const tooltip = window.Vaadin.Flow.tooltip;"
                            + "tooltip.defaultFocusDelay = $0;"
                            + "tooltip.setDefaultFocusDelay?.($0)",
                    defaultFocusDelay);
        }

        if (defaultHoverDelay != null) {
            ui.getElement().executeJs(
                    "const tooltip = window.Vaadin.Flow.tooltip;"
                            + "tooltip.defaultHoverDelay = $0;"
                            + "tooltip.setDefaultHoverDelay?.($0)",
                    defaultHoverDelay);
        }
    }

}
