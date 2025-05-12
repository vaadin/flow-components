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
 * Markdown is a component for rendering Markdown content. It takes Markdown
 * source as input and renders the corresponding HTML.
 * 
 * @author Vaadin Ltd
 */
@Tag("vaadin-markdown")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/markdown", version = "24.8.0-alpha18")
@JsModule("@vaadin/markdown/src/vaadin-markdown.js")
public class Markdown extends Component implements HasSize {

    private String serverContent;
    private String clientContent;

    /**
     * Default constructor. Creates an empty Markdown.
     */
    public Markdown() {
    }

    /**
     * Creates a Markdown with content.
     *
     * @param content
     *            the markdown content
     * @see #setContent(String)
     */
    public Markdown(String content) {
        setContent(content);
    }

    /**
     * Sets the markdown content.
     * 
     * @param content
     *            the markdown content
     */
    public void setContent(String content) {
        serverContent = content;
        scheduleContentUpdate();
    }

    /**
     * Appends the markdown content.
     * 
     * @param content
     *            the markdown content to append
     */
    public void appendContent(String content) {
        if (serverContent == null) {
            serverContent = "";
        }
        setContent(serverContent + content);
    }

    /**
     * Gets the markdown content.
     * 
     * @return the markdown content
     */
    public String getContent() {
        return serverContent;
    }

    private void scheduleContentUpdate() {
        getElement().getNode()
                .runWhenAttached(ui -> ui.beforeClientResponse(this, ctx -> {
                    if (Objects.equals(clientContent, serverContent)) {
                        return;
                    }

                    if (serverContent != null && clientContent != null
                            && serverContent.startsWith(clientContent)) {
                        // This is a simple optimization to a common case where
                        // new content is streamed at the end of the existing
                        // content.
                        // If the updated serverContent starts with the
                        // clientContent, we can just send the difference to
                        // the client.
                        getElement().executeJs("this.content += $0",
                                serverContent
                                        .substring(clientContent.length()));
                    } else {
                        // In all other cases, we need to send the whole updated
                        // content.
                        getElement().executeJs("this.content = $0",
                                serverContent);
                    }

                    clientContent = serverContent;
                }));
    }
}
