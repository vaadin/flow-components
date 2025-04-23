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
package com.vaadin.flow.component.markdown;

import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-markdown")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha13")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
// @NpmPackage(value = "@vaadin/markdown", version = "24.8.0-alpha13")
@JsModule("@vaadin/markdown/src/vaadin-markdown.js")
public class Markdown extends Component implements HasSize {

    private String serverMarkdown;
    private String clientMarkdown = "";
    private boolean pendingUpdate = false;

    /**
     * Default constructor. Creates an empty Markdown.
     */
    public Markdown() {
    }

    /**
     * Creates a Markdown with content.
     *
     * @param markdown
     *            the markdown content
     * @see #setMarkdown(String)
     */
    public Markdown(String markdown) {
        setMarkdown(markdown);
    }

    /**
     */
    public void setMarkdown(String markdown) {
        serverMarkdown = markdown;
        scheduleMarkdownUpdate();
    }

    public String getMarkdown() {
        return serverMarkdown;
    }

    private void scheduleMarkdownUpdate() {
        if (!pendingUpdate) {
            pendingUpdate = true;
            getElement().getNode().runWhenAttached(
                    ui -> ui.beforeClientResponse(this, ctx -> {
                        pendingUpdate = false;
                        if (Objects.equals(clientMarkdown, serverMarkdown)) {
                            return;
                        }

                        if (serverMarkdown != null && clientMarkdown != null
                                && serverMarkdown.startsWith(clientMarkdown)) {
                            getElement().executeJs("this.markdown += $0",
                                    serverMarkdown.substring(
                                            clientMarkdown.length()));
                        } else {
                            getElement().executeJs("this.markdown = $0",
                                    serverMarkdown);
                        }

                        clientMarkdown = serverMarkdown;
                    }));
        }
    }
}
