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
package com.vaadin.flow.component.contextmenu.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.contextmenu.testbench.ContextMenuElement;
import com.vaadin.flow.component.contextmenu.testbench.ContextMenuItemElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-context-menu/download-handler")
public class MenuItemDownloadHandlerIT extends AbstractContextMenuIT {

    private static final String DOWNLOADED = "200|"
            + MenuItemDownloadHandlerPage.DOWNLOAD_BODY;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id("target"));
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
    public void clickItem_downloadsFileFromHandler() {
        clickItem(openMenu().getMenuItem("Download").orElseThrow());
        Assert.assertEquals(DOWNLOADED, download());
    }

    @Test
    public void clickItemDisabledOnClick_downloadsFileWhileDisabled() {
        clickItem(openMenu().getMenuItem("Disable on click").orElseThrow());
        Assert.assertEquals(DOWNLOADED, download());
    }

    @Test
    public void clickSubMenuItem_downloadsFileFromHandler() {
        ContextMenuItemElement subItem = openMenu().getMenuItem("Sub menu")
                .orElseThrow().openSubMenu().getMenuItem("Sub download")
                .orElseThrow();
        clickItem(subItem);
        Assert.assertEquals(DOWNLOADED, download());
    }

    @Test
    public void clickItem_menuClosedAndRemoved_downloadsFileFromHandler() {
        clickItem(openMenu().getMenuItem("Download").orElseThrow());
        // The menu removes itself from the UI after it closed. The browser
        // can request the file after that, which must still succeed.
        verifyClosedAndRemoved();
        Assert.assertEquals(DOWNLOADED, download());
    }

    private ContextMenuElement openMenu() {
        return ContextMenuElement
                .openByRightClick($(TestBenchElement.class).id("target"));
    }

    private void clickItem(ContextMenuItemElement item) {
        item.click();
        Assert.assertNotNull("Click did not start a download",
                executeScript("return window.__downloadUrl;"));
    }

    /**
     * Fetches the URL that the click started a download from, returning the
     * response status and body separated by {@code |}.
     */
    private String download() {
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
