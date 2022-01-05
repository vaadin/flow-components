/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-grid/preserve-on-refresh")
public class PreserveOnRefreshIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void refresh_componentRendered() {
        getDriver().navigate().refresh();
        Assert.assertThat(
                "Unexpected cell content after refreshing with @PreserveOnRefresh.",
                getGrid().getCell(0, 0).getInnerHTML(),
                CoreMatchers.containsString("<span>foo</span>"));
    }

    @Test
    public void refresh_headerComponentRendered() {
        getDriver().navigate().refresh();
        Assert.assertThat(
                "Unexpected header content after refreshing with @PreserveOnRefresh.",
                getGrid().getHeaderCell(0).getInnerHTML(),
                CoreMatchers.containsString("<span>header</span>"));
    }

    @Test
    public void refresh_footerComponentRendered() {
        getDriver().navigate().refresh();
        Assert.assertThat(
                "Unexpected footer content after refreshing with @PreserveOnRefresh.",
                getGrid().getFooterCell(0).getInnerHTML(),
                CoreMatchers.containsString("<span>footer</span>"));
    }

    @Test
    public void refresh_editorOpen() {
        findElement(By.id("edit-button")).click();
        getDriver().navigate().refresh();
        WebElement closed = findElement(By.id("closed"));
        Assert.assertEquals(closed.getText(), "Closed");

        // Test that editor still works after refresh
        findElement(By.id("edit-button")).click();
        WebElement open = findElement(By.id("open-2"));
        Assert.assertEquals(open.getText(), "Open: 2");
    }

    private GridElement getGrid() {
        return $(GridElement.class).first();
    }

}
