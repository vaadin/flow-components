/*
 * Copyright 2000-2022 Vaadin Ltd.
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
import com.vaadin.flow.component.shared.TooltipConfiguration;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@PreserveOnRefresh
@Route("vaadin-button/tooltip-defaults")
public class TooltipDefaultsPage extends Div {

    public TooltipDefaultsPage() {
        // Default values for focus & hover delay
        TooltipConfiguration.setDefaultFocusDelay(500);
        TooltipConfiguration.setDefaultHoverDelay(500);

        Button button = new Button("Set tooltip default hide delay to 1000");
        // Use component-specific delay to override the default
        button.setTooltipText("Tooltip").setHoverDelay(100);

        // Dynamically change the default hide delay
        button.addClickListener(
                e -> TooltipConfiguration.setDefaultHideDelay(1000));

        add(button);
    }
}
