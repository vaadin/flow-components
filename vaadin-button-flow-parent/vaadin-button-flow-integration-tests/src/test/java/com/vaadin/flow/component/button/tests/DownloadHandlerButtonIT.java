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
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-button/download-handler")
public class DownloadHandlerButtonIT extends AbstractComponentIT {

    @Test
    public void click_downloadsFileFromHandler() {
        open();

        Assert.assertEquals("200|" + DownloadHandlerButtonPage.BODY,
                clickAndFetchDownload("download"));
    }

    @Test
    public void disableOnClickWithAllowDisabled_click_downloadsFileAndDisablesButton() {
        open();

        Assert.assertEquals("200|" + DownloadHandlerButtonPage.BODY,
                clickAndFetchDownload("download-disable-on-click"));
        Assert.assertFalse($(ButtonElement.class)
                .id("download-disable-on-click").isEnabled());
    }

    // Replaces window.Vaadin.Flow.download.start with a shim that fetches the
    // URL right away, like the browser does, without opening a save dialog.
    // Returns "status|body" of the response.
    private String clickAndFetchDownload(String buttonId) {
        executeScript("""
                window.__download = null;
                window.Vaadin.Flow.download.start = url => {
                  fetch(url).then(r => r.text().then(t => {
                    window.__download = r.status + '|' + t;
                  }));
                };
                """);
        $(ButtonElement.class).id(buttonId).click();
        return (String) waitUntil(
                driver -> executeScript("return window.__download;"));
    }
}
