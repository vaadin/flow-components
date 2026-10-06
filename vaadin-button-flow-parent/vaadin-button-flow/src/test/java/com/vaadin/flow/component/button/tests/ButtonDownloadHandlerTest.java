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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.communication.StreamRequestHandler;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.tests.MockUIExtension;

class ButtonDownloadHandlerTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final AtomicInteger downloads = new AtomicInteger();
    private final DownloadHandler handler = event -> downloads
            .incrementAndGet();

    private Div parent;
    private Button button;

    @BeforeEach
    void setup() {
        button = new Button();
        parent = new Div(button);
        ui.add(parent);
        button.setDownloadHandler(handler);
    }

    @Test
    void enabled_served() throws IOException {
        Assertions.assertTrue(requestDownload());
    }

    @Test
    void disabled_refused() throws IOException {
        button.setEnabled(false);
        Assertions.assertFalse(requestDownload());
    }

    @Test
    void disabledByClick_served() throws IOException {
        button.setDisableOnClick(true);
        button.click();

        Assertions.assertFalse(button.isEnabled());
        Assertions.assertTrue(requestDownload());
    }

    @Test
    void disabledByClick_thenDisabledExplicitly_refused() throws IOException {
        button.setDisableOnClick(true);
        button.click();
        button.setEnabled(false);

        Assertions.assertFalse(requestDownload());
    }

    @Test
    void disabledByClick_thenParentDisabled_refused() throws IOException {
        button.setDisableOnClick(true);
        button.click();
        parent.setEnabled(false);

        Assertions.assertFalse(requestDownload());
    }

    @Test
    void disabledByClick_parentDisabledAndEnabledAgain_served()
            throws IOException {
        button.setDisableOnClick(true);
        button.click();
        parent.setEnabled(false);
        parent.setEnabled(true);

        Assertions.assertTrue(requestDownload());
    }

    @Test
    void allowDisabled_disabled_served() throws IOException {
        button.setDownloadHandler(handler.allowDisabled());
        button.setEnabled(false);

        Assertions.assertTrue(requestDownload());
    }

    /**
     * Sends a request for the button's download URL through Flow's stream
     * request handler and returns whether the handler served the file.
     */
    private boolean requestDownload() throws IOException {
        String url = button.getElement().getAttributeNames()
                .filter(name -> name.startsWith("data-flow-download-"))
                .map(button.getElement()::getAttribute).findFirst()
                .orElseThrow();
        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        Mockito.when(request.getPathInfo()).thenReturn("/" + url);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);

        int before = downloads.get();
        new StreamRequestHandler().handleRequest(ui.getSession(), request,
                response);
        boolean served = downloads.get() > before;

        // Cross-check with the response, so the test fails if a request is
        // refused for a reason other than the enabled state
        if (served) {
            Mockito.verify(response, Mockito.never())
                    .sendError(Mockito.anyInt(), Mockito.anyString());
        } else {
            Mockito.verify(response).sendError(403, "Resource not available");
        }
        return served;
    }
}
