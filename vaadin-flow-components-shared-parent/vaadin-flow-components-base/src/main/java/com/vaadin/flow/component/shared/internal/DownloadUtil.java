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
package com.vaadin.flow.component.shared.internal;

import java.io.IOException;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;

/**
 * Internal utility for starting downloads from components.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 *
 * @since 25.4
 */
public final class DownloadUtil {

    private DownloadUtil() {
    }

    /**
     * Wraps the given download handler so that it is also served while the
     * component is {@link DisableOnClickController#isDisabledByClick() disabled
     * by disable on click}, which happens before the browser requests the file.
     * A component disabled explicitly, or inside a disabled parent, still
     * refuses the request unless the handler itself allows it.
     *
     * @param component
     *            the component that starts the download, not {@code null}
     * @param disableOnClickController
     *            the disable on click controller of the component, not
     *            {@code null}
     * @param handler
     *            the download handler to wrap, not {@code null}
     * @return a handler that delegates to the given handler
     */
    public static DownloadHandler allowDisabledByClick(Component component,
            DisableOnClickController<?> disableOnClickController,
            DownloadHandler handler) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(disableOnClickController,
                "disableOnClickController must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        return new DownloadHandler() {
            @Override
            public void handleRequest(VaadinRequest request,
                    VaadinResponse response, VaadinSession session,
                    Element owner) throws IOException {
                handler.handleRequest(request, response, session, owner);
            }

            @Override
            public void handleDownloadRequest(DownloadEvent event)
                    throws IOException {
                handler.handleDownloadRequest(event);
            }

            @Override
            public String getUrlPostfix() {
                return handler.getUrlPostfix();
            }

            @Override
            public boolean isAllowInert() {
                return handler.isAllowInert();
            }

            @Override
            public DisabledUpdateMode getDisabledUpdateMode() {
                Element parent = component.getElement().getParent();
                if (disableOnClickController.isDisabledByClick()
                        && (parent == null || parent.isEnabled())) {
                    return DisabledUpdateMode.ALWAYS;
                }
                return handler.getDisabledUpdateMode();
            }
        };
    }
}
