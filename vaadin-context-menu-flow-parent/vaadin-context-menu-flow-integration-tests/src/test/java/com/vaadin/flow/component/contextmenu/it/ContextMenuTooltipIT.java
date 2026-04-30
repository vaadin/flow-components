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

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-context-menu/tooltip")
public class ContextMenuTooltipIT extends AbstractContextMenuIT {

    @Before
    public void init() {
        open();
        rightClickOn("target");
        verifyOpened();
    }

    @Test
    public void hoverOverItem_showItemTooltip() {
        var items = getMenuItems();

        showTooltip(items.get(0));
        Assert.assertEquals("Open the selected file", getActiveTooltipText());
    }

    @Test
    public void hoverOverDisabledItem_showItemTooltip() {
        var items = getMenuItems();

        showTooltip(items.get(1));
        Assert.assertEquals("Not available right now", getActiveTooltipText());
    }

    @Test
    public void updateTooltip_showUpdatedTooltip() {
        // close menu so we can click the update button
        clickBody();
        verifyClosed();

        clickElementWithJs("update-tooltip-button");

        rightClickOn("target");
        verifyOpened();

        var items = getMenuItems();
        showTooltip(items.get(0));
        Assert.assertEquals("Updated tooltip", getActiveTooltipText());
    }

    private void showTooltip(TestBenchElement element) {
        executeScript(
                "arguments[0].dispatchEvent(new Event('mouseover', {bubbles:true}))",
                element);
    }

    private String getActiveTooltipText() {
        return findElement(By.tagName("vaadin-tooltip")).getText();
    }
}
