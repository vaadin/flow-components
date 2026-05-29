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
package com.vaadin.flow.component.markdown.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ValueSignal;

@Route("vaadin-markdown/signal-markdown")
public class SignalMarkdownPage extends Div {

    public SignalMarkdownPage() {
        ValueSignal<String> contentSignal = new ValueSignal<>(
                "**Hello** _World_");

        var markdown = new Markdown(contentSignal);
        markdown.setId("signal-markdown");
        add(markdown);

        var setButton = new NativeButton("Set content",
                event -> contentSignal.set("**Updated** _Markdown_"));
        setButton.setId("set-button");
        add(setButton);

        var appendButton = new NativeButton("Append content",
                event -> contentSignal.update(v -> v + "!"));
        appendButton.setId("append-button");
        add(appendButton);
    }
}
