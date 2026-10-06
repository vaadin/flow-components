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
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.contextmenu.testbench.ContextMenuItemElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-context-menu/download-handler")
public class MenuItemDownloadHandlerIT extends AbstractContextMenuIT {

    private static final String TARGET_ID = "target-div";
    private static final String DOWNLOADED = "200|"
            + MenuItemDownloadHandlerPage.DOWNLOAD_BODY;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id(TARGET_ID));
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
    public void clickDownloadItem_downloadsFileFromHandler() {
        Assert.assertEquals(DOWNLOADED,
                clickAndDownload(openItem("download-item")));
    }

    @Test
    public void clickDownloadItemDisabledOnClick_downloadsFileWhileDisabled() {
        WebElement item = openItem("download-disable-on-click-item");
        Assert.assertEquals(DOWNLOADED, clickAndDownload(item));
        Assert.assertFalse(item.isEnabled());
    }

    @Test
    public void clickDownloadItemInSubMenu_downloadsFileFromHandler() {
        rightClickOn(TARGET_ID);
        ContextMenuItemElement more = getMenuItems().stream()
                .filter(item -> item.getText().equals("More")).findFirst()
                .orElseThrow();
        more.openSubMenu();
        waitForElementPresent(By.id("download-sub-menu-item"));
        Assert.assertEquals(DOWNLOADED,
                clickAndDownload(findElement(By.id("download-sub-menu-item"))));
    }

    @Test
    public void menuRegenerated_clickDownloadItem_downloadsFileFromHandler() {
        clickElementWithJs(openItem("add-item"));
        verifyClosedAndRemoved();

        Assert.assertEquals(DOWNLOADED,
                clickAndDownload(openItem("download-item")));
    }

    private WebElement openItem(String id) {
        rightClickOn(TARGET_ID);
        waitForElementPresent(By.id(id));
        return findElement(By.id(id));
    }

    /**
     * Clicks the item and fetches the URL that the click starts a download
     * from, returning the response status and body separated by {@code |}.
     */
    private String clickAndDownload(WebElement item) {
        executeScript("window.__downloadUrl = null;");
        item.click();
        waitUntil(driver -> executeScript("return window.__downloadUrl;"));

        // click() waits until the server has handled the click, so an item
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
