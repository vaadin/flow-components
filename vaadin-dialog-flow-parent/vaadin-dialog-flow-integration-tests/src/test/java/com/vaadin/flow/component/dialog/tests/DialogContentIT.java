/*
 * Copyright 2023 Vaadin Ltd.
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
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath("vaadin-dialog/dialog-content")
public class DialogContentIT extends AbstractComponentIT {

    private TestBenchElement dialogOverlay;

    @Before
    public void init() {
        open();
        $("button").id("open-dialog").click();
        waitForElementPresent(By.tagName("vaadin-dialog-overlay"));
        dialogOverlay = $("vaadin-dialog-overlay").first();
    }

    @Test
    public void removeDefaultContent_contentNotPresent() {
        Assert.assertTrue(dialogOverlay.getText().contains("Default content"));

        $("button").id("remove-default-content").click();

        Assert.assertFalse(dialogOverlay.getText().contains("Default content"));
    }

    @Test
    public void addContent_contentAdded() {
        Assert.assertFalse(dialogOverlay.getText().contains("Extra content"));

        $("button").id("add-extra-content").click();

        Assert.assertTrue(dialogOverlay.getText().contains("Extra content"));
    }

    @Test
    public void addContent_removeAddedContent_contentNotPresent() {
        $("button").id("add-extra-content").click();

        $("button").id("remove-extra-content").click();

        Assert.assertFalse(dialogOverlay.getText().contains("Extra content"));
    }

    @Test
    public void addContent_removeAllContent_headerAndFooterComponentsStillActive() {
        $("button").id("add-extra-content").click();

        $("button").id("remove-all-content").click();

        assertHeaderAndFooterBehaviour();
    }

    @Test
    public void removeDefaultContent_headerAndFooterComponentsStillActive() {
        $("button").id("remove-default-content").click();

        assertHeaderAndFooterBehaviour();
    }

    private void assertHeaderAndFooterBehaviour() {
        $("button").id("header-button").click();
        Assert.assertEquals("Header button clicked",
                $("span").id("logs").getText());
        $("button").id("footer-button").click();
        Assert.assertEquals("Footer button clicked",
                $("span").id("logs").getText());
    }
}
