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
package com.vaadin.flow.component.markdown;

import java.util.Objects;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.SignalPropertySupport;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.signals.Signal;

/**
 * Markdown is a component for rendering Markdown content. It takes Markdown
 * source as input and renders the corresponding HTML.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-markdown")
@NpmPackage(value = "@vaadin/markdown", version = "25.1.0-alpha7")
@JsModule("@vaadin/markdown/src/vaadin-markdown.js")
public class Markdown extends Component implements HasSize {

    private String serverContent;
    private String clientContent;

    private final SignalPropertySupport<String> contentSupport = SignalPropertySupport
            .create(this, this::applyContent);

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
        contentSupport.set(content);
    }

    /**
     * Binds the given signal to the markdown content of this component.
     * <p>
     * When a signal is bound, the content is kept synchronized with the signal
     * value while the component is attached. When the component is detached,
     * signal value changes have no effect.
     * <p>
     * While a signal is bound, any attempt to set the content manually through
     * {@link #setContent(String)} or {@link #appendContent(String)} throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.
     *
     * @param signal
     *            the signal to bind the content to, not {@code null}
     * @see #setContent(String)
     * @since 25.1
     */
    public void bindContent(Signal<String> signal) {
        contentSupport.bind(signal);
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (serverContent != null) {
            clientContent = null;
            scheduleContentUpdate();
        }
    }

    private void applyContent(String content) {
        serverContent = content;
        scheduleContentUpdate();
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
