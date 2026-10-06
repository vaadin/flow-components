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
        // Replace the client-side download helper with a shim that only
        // records the URL, so no save dialog opens
        executeScript("""
                window.__downloadUrl = null;
                window.Vaadin.Flow.download.start = url => {
                  window.__downloadUrl = url;
                };
                """);
    }

    @Test
    public void clickDownloadButton_downloadsFileFromHandler() {
        Assert.assertEquals("200|" + DownloadHandlerButtonPage.DOWNLOAD_BODY,
                clickAndDownload("download-button"));
    }

    @Test
    public void clickDownloadButtonDisabledOnClick_downloadsFileWhileDisabled() {
        Assert.assertEquals("200|" + DownloadHandlerButtonPage.DOWNLOAD_BODY,
                clickAndDownload("download-disable-on-click-button"));
        Assert.assertFalse($(ButtonElement.class)
                .id("download-disable-on-click-button").isEnabled());
    }

    @Test
    public void clickDownloadButtonDisabledByListener_downloadRefused() {
        Assert.assertTrue(
                clickAndDownload("download-explicitly-disabled-button")
                        .startsWith("403|"));
    }

    // Disabling the parent leaves the button disabled by its own click, so
    // this relies on the button refusing to serve the file while disabled by
    // click when a parent is disabled, unlike
    // clickDownloadButtonDisabledByListener_downloadRefused
    @Test
    public void clickDownloadButtonParentDisabledByListener_downloadRefused() {
        Assert.assertTrue(clickAndDownload("download-parent-disabled-button")
                .startsWith("403|"));
    }

    /**
     * Clicks the button and fetches the URL that the click starts a download
     * from, returning the response status and body separated by {@code |}.
     */
    private String clickAndDownload(String buttonId) {
        $(ButtonElement.class).id(buttonId).click();
        waitUntil(driver -> executeScript("return window.__downloadUrl;"));

        // click() waits until the server has handled the click, so a button
        // that disables itself on click is already disabled on the server
        // when the file is requested
        executeScript("""
                window.__download = null;
                fetch(window.__downloadUrl).then(r => r.text().then(t => {
                  window.__download = r.status + '|' + t;
                }));
                """);
        return (String) waitUntil(
                driver -> executeScript("return window.__download;"));
    }
}
