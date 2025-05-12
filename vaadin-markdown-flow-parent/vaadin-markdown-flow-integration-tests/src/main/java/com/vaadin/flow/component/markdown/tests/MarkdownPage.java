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
package com.vaadin.flow.component.markdown.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.router.Route;

@Route("vaadin-markdown")
public class MarkdownPage extends Div {

    public MarkdownPage() {
        var markdown = new Markdown("**Hello** _World_");

        add(markdown);

        var appendButton = new NativeButton("Append content",
                event -> markdown.appendContent("!"));
        appendButton.setId("append-button");
        add(appendButton);

        var setButton = new NativeButton("Set content",
                event -> markdown.setContent("**Updated** _Markdown_"));
        setButton.setId("set-button");
        add(setButton);
    }

}
