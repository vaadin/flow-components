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
import com.vaadin.flow.dom.SignalBinding;
import com.vaadin.flow.signals.Signal;

/**
 * Markdown is a component for rendering Markdown content. It takes Markdown
 * source as input and renders the corresponding HTML.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-markdown")
@NpmPackage(value = "@vaadin/markdown", version = "25.2.0-rc2")
@JsModule("@vaadin/markdown/src/vaadin-markdown.js")
public class Markdown extends Component implements HasSize {

    private final SignalPropertySupport<String> contentSupport = SignalPropertySupport
            .create(this, this::handleContentChange);

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
     * Creates a Markdown with content bound to a signal.
     *
     * @param contentSignal
     *            the signal providing the markdown content
     * @see #bindContent(Signal)
     */
    public Markdown(Signal<String> contentSignal) {
        bindContent(contentSignal);
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
     * Appends the markdown content.
     *
     * @param content
     *            the markdown content to append
     */
    public void appendContent(String content) {
        var current = contentSupport.get();
        setContent((current == null ? "" : current) + content);
    }

    /**
     * Gets the markdown content.
     *
     * @return the markdown content
     */
    public String getContent() {
        return contentSupport.get();
    }

    /**
     * Binds the markdown content to the given signal. While the binding is
     * active, calling {@link #setContent(String)} or
     * {@link #appendContent(String)} will throw a
     * {@code BindingActiveException}.
     *
     * @param contentSignal
     *            the signal providing the markdown content
     * @return a {@link SignalBinding} that can be used to register
     *         {@link SignalBinding#onChange(com.vaadin.flow.function.SerializableConsumer)
     *         onChange} callbacks
     */
    public SignalBinding<String> bindContent(Signal<String> contentSignal) {
        return contentSupport.bind(contentSignal);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (contentSupport.get() != null) {
            clientContent = null;
            scheduleContentUpdate();
        }
    }

    private void handleContentChange(String content) {
        scheduleContentUpdate();
    }

    private void scheduleContentUpdate() {
        getElement().getNode()
                .runWhenAttached(ui -> ui.beforeClientResponse(this, ctx -> {
                    var serverContent = contentSupport.get();
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
