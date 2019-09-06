/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Tests reorder of columns
 */
@TestPath("grid-order-columns")
public class GridOrderColumnsIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
    }

    @Test
    public void gridOrder_123() {
        findElement(By.id("button-123")).click();
        assertColumnHeaders("Col1", "Col2", "Col3");
    }

    @Test
    public void gridOrder_321() {
        findElement(By.id("button-321")).click();
        assertColumnHeaders("Col3", "Col2", "Col1");
    }

    @Test
    public void gridOrder_31() {
        findElement(By.id("button-31")).click();
        assertColumnHeaders("Col3", "Col1");
    }

    @Test
    public void gridOrder_321_123() {
        findElement(By.id("button-321")).click();
        assertColumnHeaders("Col3", "Col2", "Col1");
        findElement(By.id("button-123")).click();
        assertColumnHeaders("Col1", "Col2", "Col3");
    }

    private void assertColumnHeaders(String... headers) {
        for (int i = 0; i < headers.length; i++) {
            Assert.assertEquals("Unexpected header for column " + i, headers[i], grid.getHeaderCell(i).getText());
        }
    }
}
