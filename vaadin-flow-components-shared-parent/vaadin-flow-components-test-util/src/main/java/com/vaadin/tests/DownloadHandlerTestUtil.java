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
package com.vaadin.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamResourceRegistry.ElementStreamResource;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.StreamRequestHandler;
import com.vaadin.flow.server.streams.ElementRequestHandler;

/**
 * Helpers for testing how a component serves the content of a download handler.
 */
public final class DownloadHandlerTestUtil {

    private DownloadHandlerTestUtil() {
    }

    /**
     * Serves a request for the given registered resource, like the browser gets
     * it, and returns the {@code Content-Disposition} header of the response.
     * The disposition of the resource is passed on to the handler.
     *
     * @param resource
     *            the resource a component registered
     * @return the header value, or {@code null} if the handler did not set it
     * @throws IOException
     *             if the handler fails
     */
    public static String servedContentDisposition(
            ElementStreamResource resource) throws IOException {
        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        Mockito.when(request.getAttribute(
                StreamRequestHandler.DISPOSITION_REQUEST_ATTRIBUTE))
                .thenReturn(resource.getDisposition());
        return serve(resource.getElementRequestHandler(), request,
                resource.getOwner());
    }

    /**
     * Serves a request with the given handler and returns the
     * {@code Content-Disposition} header of the response.
     *
     * @param handler
     *            the handler to serve, such as the one a component registered
     * @param owner
     *            the element that owns the handler
     * @return the header value, or {@code null} if the handler did not set it
     * @throws IOException
     *             if the handler fails
     */
    public static String servedContentDisposition(ElementRequestHandler handler,
            Element owner) throws IOException {
        return serve(handler, Mockito.mock(VaadinRequest.class), owner);
    }

    private static String serve(ElementRequestHandler handler,
            VaadinRequest request, Element owner) throws IOException {
        VaadinResponse response = Mockito.mock(VaadinResponse.class);
        Mockito.when(response.getOutputStream())
                .thenReturn(new ByteArrayOutputStream());
        AtomicReference<String> header = new AtomicReference<>();
        Mockito.doAnswer(invocation -> {
            header.set(invocation.getArgument(1));
            return null;
        }).when(response).setHeader(ArgumentMatchers.eq("Content-Disposition"),
                ArgumentMatchers.any());
        handler.handleRequest(request, response,
                Mockito.mock(VaadinSession.class), owner);
        return header.get();
    }
}
