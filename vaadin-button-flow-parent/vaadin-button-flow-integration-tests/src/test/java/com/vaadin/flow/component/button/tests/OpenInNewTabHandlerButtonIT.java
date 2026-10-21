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

@TestPath("vaadin-button/open-in-new-tab-handler")
public class OpenInNewTabHandlerButtonIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        // Replace window.open with a shim that only records the URL, so the
        // test stays in the same tab
        executeScript("""
                window.__openedUrl = null;
                window.open = url => {
                  window.__openedUrl = url;
                  return {};
                };
                """);
    }

    @Test
    public void clickButtonDisabledOnClick_opensInlineContentInNewTab() {
        ButtonElement button = $(ButtonElement.class).first();
        button.click();
        Assert.assertNotNull("Click did not open a new tab",
                executeScript("return window.__openedUrl;"));
        Assert.assertFalse(button.isEnabled());

        executeScript("""
                window.__content = null;
                fetch(window.__openedUrl).then(r => r.text().then(t => {
                  window.__content = r.status + '|'
                      + r.headers.get('Content-Disposition').split(';')[0]
                      + '|' + t;
                }));
                """);
        Assert.assertEquals(
                "200|inline|" + OpenInNewTabHandlerButtonPage.CONTENT,
                waitUntil(driver -> executeScript("return window.__content;")));
    }
}
