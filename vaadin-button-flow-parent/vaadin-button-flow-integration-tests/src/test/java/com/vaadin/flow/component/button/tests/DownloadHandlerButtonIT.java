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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-button/download-handler")
public class DownloadHandlerButtonIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        // Replace the client-side download helper and window.open with shims
        // that only record how the click delivers the content, so no save
        // dialog or new tab opens
        executeScript("""
                window.__delivery = null;
                window.Vaadin.Flow.download.start = url => {
                  window.__delivery = { via: 'save', url };
                };
                window.open = url => {
                  window.__delivery = { via: 'new-tab', url };
                  return {};
                };
                """);
    }

    @Test
    public void clickDownloadButton_downloadsFileFromHandler() {
        Assert.assertEquals(
                "save|200|attachment|"
                        + DownloadHandlerButtonPage.DOWNLOAD_BODY,
                clickAndFetch("download-button"));
    }

    @Test
    public void clickDownloadButtonDisabledOnClick_downloadsFileWhileDisabled() {
        Assert.assertEquals(
                "save|200|attachment|"
                        + DownloadHandlerButtonPage.DOWNLOAD_BODY,
                clickAndFetch("download-disable-on-click-button"));
        Assert.assertFalse($(ButtonElement.class)
                .id("download-disable-on-click-button").isEnabled());
    }

    @Test
    public void clickNewTabButton_opensInlineContentInNewTab() {
        Assert.assertEquals(
                "new-tab|200|inline|" + DownloadHandlerButtonPage.DOWNLOAD_BODY,
                clickAndFetch("new-tab-button"));
    }

    @Test
    public void clickNewTabButtonDisabledOnClick_opensInlineContentWhileDisabled() {
        Assert.assertEquals(
                "new-tab|200|inline|" + DownloadHandlerButtonPage.DOWNLOAD_BODY,
                clickAndFetch("new-tab-disable-on-click-button"));
        Assert.assertFalse($(ButtonElement.class)
                .id("new-tab-disable-on-click-button").isEnabled());
    }

    @Test
    public void clickNewTabButtonWithCustomAllowDisabledHandler_opensInlineContentInNewTab() {
        Assert.assertEquals(
                "new-tab|200|inline|" + DownloadHandlerButtonPage.DOWNLOAD_BODY,
                clickAndFetch("new-tab-custom-allow-disabled-button"));
    }

    /**
     * Clicks the button and fetches the URL that the click delivers content
     * from. Returns how the content is delivered ({@code save} or
     * {@code new-tab}), the response status, the {@code Content-Disposition}
     * type and the body, separated by {@code |}.
     */
    private String clickAndFetch(String buttonId) {
        $(ButtonElement.class).id(buttonId).click();
        Assert.assertNotNull("Click did not deliver any content",
                executeScript("return window.__delivery;"));

        // click() waits until the server has handled the click, so a button
        // that disables itself on click is already disabled on the server
        // when the file is requested. In a real browser the download request
        // and the click event are sent independently, in no fixed order.
        executeScript(
                """
                        window.__download = null;
                        const delivery = window.__delivery;
                        fetch(delivery.url).then(r => r.text().then(t => {
                          const disposition = r.headers.get('Content-Disposition') ?? '';
                          window.__download = delivery.via + '|' + r.status + '|'
                              + disposition.split(';')[0] + '|' + t;
                        }));
                        """);
        return (String) waitUntil(
                driver -> executeScript("return window.__download;"));
    }
}
