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
package com.vaadin.flow.component.button.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.shared.TooltipConfiguration;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@PreserveOnRefresh
@Route("vaadin-button/tooltip-defaults")
public class TooltipDefaultsPage extends Div {
    public TooltipDefaultsPage() {
        Button buttonWithTooltip = new Button("Button with tooltip");
        buttonWithTooltip.setTooltipText("Tooltip");

        NativeButton setDefaultDelaysTo2000Button = new NativeButton(
                "Set default delays to 2000", e -> {
                    TooltipConfiguration.setDefaultFocusDelay(2000);
                    TooltipConfiguration.setDefaultHoverDelay(2000);
                    TooltipConfiguration.setDefaultHideDelay(2000);
                });
        setDefaultDelaysTo2000Button.setId("set-default-delays-to-2000");

        NativeButton setDefaultDelaysTo5000Button = new NativeButton(
                "Set default delays to 5000", e -> {
                    TooltipConfiguration.setDefaultFocusDelay(5000);
                    TooltipConfiguration.setDefaultHoverDelay(5000);
                    TooltipConfiguration.setDefaultHideDelay(5000);
                });
        setDefaultDelaysTo5000Button.setId("set-default-delays-to-5000");

        add(buttonWithTooltip, new Div(setDefaultDelaysTo2000Button,
                setDefaultDelaysTo5000Button));
    }
}
