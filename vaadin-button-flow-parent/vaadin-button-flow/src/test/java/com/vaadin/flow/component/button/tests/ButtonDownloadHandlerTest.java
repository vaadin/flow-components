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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.tests.MockUIExtension;

class ButtonDownloadHandlerTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final Button button = new Button("Download");

    @BeforeEach
    void setUp() {
        ui.add(button);
    }

    @Test
    void downloadHandlerIsNullByDefault() {
        Assertions.assertNull(button.getDownloadHandler());
        Assertions.assertEquals(0, countDownloadResources());
    }

    @Test
    void setDownloadHandler_registersDownloadForClicks() {
        DownloadHandler handler = createHandler();
        button.setDownloadHandler(handler);

        Assertions.assertSame(handler, button.getDownloadHandler());
        Assertions.assertEquals(1, countDownloadResources());
    }

    @Test
    void setDownloadHandler_replaceAndClear_unregistersPreviousHandler() {
        button.setDownloadHandler(createHandler());
        DownloadHandler replacement = createHandler();
        button.setDownloadHandler(replacement);

        Assertions.assertSame(replacement, button.getDownloadHandler());
        Assertions.assertEquals(1, countDownloadResources());

        button.setDownloadHandler(null);

        Assertions.assertNull(button.getDownloadHandler());
        Assertions.assertEquals(0, countDownloadResources());
    }

    @Test
    void forDownload_createsButtonWithTextAndHandler() {
        DownloadHandler handler = createHandler();
        Button download = Button.forDownload("Export", handler);

        Assertions.assertEquals("Export", download.getText());
        Assertions.assertSame(handler, download.getDownloadHandler());
    }

    private static DownloadHandler createHandler() {
        return event -> event.getOutputStream().write(1);
    }

    // Each registered download handler is kept as a stream resource attribute
    // on the button element, so counting those tells how many handlers the
    // browser can still download from.
    private long countDownloadResources() {
        ui.fakeClientCommunication();
        return button.getElement().getAttributeNames()
                .filter(name -> name.startsWith("data-flow-download-")).count();
    }
}
