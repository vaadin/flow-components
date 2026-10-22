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
package com.vaadin.flow.component.menubar.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.menubar.testbench.MenuBarButtonElement;
import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/download-handler")
public class MenuBarDownloadHandlerIT extends AbstractComponentIT {

    private MenuBarElement menuBar;

    @Before
    public void init() {
        open();
        menuBar = $(MenuBarElement.class).first();
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
    public void clickRootItem_downloadsFileFromHandler() {
        Assert.assertEquals("200|" + MenuBarDownloadHandlerPage.DOWNLOAD_BODY,
                clickAndDownload(menuBar.getButtons().get(0)));
    }

    @Test
    public void clickRootItemDisabledOnClick_downloadsFileWhileDisabled() {
        MenuBarButtonElement button = menuBar.getButtons().get(1);

        Assert.assertEquals("200|" + MenuBarDownloadHandlerPage.DOWNLOAD_BODY,
                clickAndDownload(button));
        Assert.assertFalse(button.isEnabled());
    }

    @Test
    public void clickSubMenuItem_downloadsFileFromHandler() {
        TestBenchElement subItem = menuBar.getButtons().get(2).openSubMenu()
                .getMenuItems().get(0);

        Assert.assertEquals("200|" + MenuBarDownloadHandlerPage.DOWNLOAD_BODY,
                clickAndDownload(subItem));
    }

    /**
     * Clicks the element and fetches the URL that the click starts a download
     * from, returning the response status and body separated by {@code |}.
     */
    private String clickAndDownload(TestBenchElement element) {
        element.click();
        Assert.assertNotNull("Click did not start a download",
                executeScript("return window.__downloadUrl;"));

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
