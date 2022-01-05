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

import java.util.Set;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/text-renderer")
public class TextRendererIT extends AbstractComponentIT {

    @Test
    public void refreshTextComponentRenderer() {
        open();

        Set<String> initialCells = findElements(
                By.tagName("vaadin-grid-cell-content")).stream()
                        .map(cell -> cell.getText())
                        .collect(Collectors.toSet());

        $("button").id("refresh").click();

        // self check: click is handled with a result on the client side
        String classNames = findElement(By.tagName("vaadin-grid"))
                .getAttribute("class");
        Assert.assertThat(classNames, CoreMatchers.containsString("refreshed"));

        Set<String> cellsAfterRefresh = findElements(
                By.tagName("vaadin-grid-cell-content")).stream()
                        .map(cell -> cell.getText())
                        .collect(Collectors.toSet());

        Assert.assertEquals(initialCells, cellsAfterRefresh);
    }
}
