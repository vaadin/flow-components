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
package com.vaadin.flow.component.grid.it;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/optimistic-row-reorder")
public class OptimisticRowReorderIT extends AbstractComponentIT {

    // Drags row 0 below row 2, and returns the first rows rendered right
    // after the drop, before the server has responded
    private static final String DRAG_AND_DROP = """
            const [grid] = arguments;
            const content = (i) => [...grid.shadowRoot.querySelectorAll('tr')]
              .find((row) => row.index === i && row.parentNode.id === 'items')
              .querySelector('slot').assignedNodes()[0];
            fireDragStart(content(0));
            fireDrop(content(2), 'below');
            fireDragEnd(grid);
            return [0, 1, 2, 3].map((i) => content(i).textContent);
            """;

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
        $("button").id("slow-server").click();
    }

    @Test
    public void dropRow_movedBeforeServerResponds_serverKeepsOrder() {
        Assert.assertEquals(List.of("Item 1", "Item 2", "Item 0", "Item 3"),
                dragAndDrop());
        Assert.assertEquals("Item 1,Item 2,Item 0,Item 3,Item 4",
                findElement(By.id("server-order")).getText());
        Assert.assertEquals(List.of("Item 1", "Item 2", "Item 0", "Item 3"),
                getRenderedTexts());
        checkLogsForErrors();
    }

    @Test
    public void rejectDrops_dropRow_movedBeforeServerResponds_reverted() {
        $("button").id("reject-drops").click();
        Assert.assertEquals(List.of("Item 1", "Item 2", "Item 0", "Item 3"),
                dragAndDrop());
        Assert.assertEquals(List.of("Item 0", "Item 1", "Item 2", "Item 3"),
                getRenderedTexts());
        checkLogsForErrors();
    }

    @SuppressWarnings("unchecked")
    private List<String> dragAndDrop() {
        return (List<String>) executeScript(DRAG_AND_DROP, grid);
    }

    private List<String> getRenderedTexts() {
        return List.of(0, 1, 2, 3).stream()
                .map(i -> grid.getCell(i, 0).getText()).toList();
    }
}
