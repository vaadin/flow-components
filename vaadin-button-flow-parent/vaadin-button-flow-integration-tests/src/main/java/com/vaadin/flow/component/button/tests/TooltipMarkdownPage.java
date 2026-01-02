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
package com.vaadin.flow.component.button.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-button/tooltip-markdown")
public class TooltipMarkdownPage extends Div {
    public TooltipMarkdownPage() {
        Button buttonWithTooltip = new Button("Button with tooltip");
        buttonWithTooltip.setTooltipText("Initial tooltip");

        NativeButton setMarkdownTooltip = new NativeButton(
                "Set markdown tooltip", e -> {
                    buttonWithTooltip
                            .setTooltipMarkdown("**Markdown** _tooltip_");
                });
        setMarkdownTooltip.setId("set-markdown-tooltip");

        NativeButton setTextTooltip = new NativeButton("Set text tooltip",
                e -> {
                    buttonWithTooltip
                            .setTooltipText("**Plain text** _tooltip_");
                });
        setTextTooltip.setId("set-text-tooltip");

        add(buttonWithTooltip, new Div(setMarkdownTooltip, setTextTooltip));
    }
}
