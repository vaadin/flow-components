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

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/hidden-grid-component-column")
public class HiddenGridComponentColumnIT extends AbstractComponentIT {

    /** Counts every node lookup the component directive makes. */
    private static final String COUNT_NODE_LOOKUPS_SCRIPT = """
            window.nodeLookups = 0;
            const client = window.Vaadin.Flow.clients.ROOT;
            const getByNodeId = client.getByNodeId.bind(client);
            client.getByNodeId = function (nodeId) {
              window.nodeLookups++;
              return getByNodeId(nodeId);
            };
            """;

    /**
     * Reports the lookups made right after the click, and the lookups made in
     * the second that follows. The second number tells a render apart from a
     * render that keeps looking the same nodes up.
     */
    private static final String MEASURE_NODE_LOOKUPS_SCRIPT = """
            const done = arguments[arguments.length - 1];
            setTimeout(() => {
              const rendered = window.nodeLookups;
              setTimeout(() => done([rendered, window.nodeLookups - rendered]), 1000);
            }, 1000);
            """;

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).waitForFirst();
    }

    @Test
    public void hideGrid_setItems_showGrid_componentsAreRendered() {
        // Each click is its own round trip, so the new items and the new
        // visibility reach the client in separate responses. Repeat once,
        // because the first pass can pass on a grid that had no rows before.
        for (int round = 1; round <= 2; round++) {
            clickElementWithJs("hide-grid");
            clickElementWithJs("set-items");
            clickElementWithJs("show-grid");

            int generation = round + 1;
            Assert.assertEquals("round " + round,
                    "component of item " + generation + ".1",
                    grid.getCell(0, 1).getText());
        }
    }

    @Test
    public void hideGridClearItemsAndHideColumn_nodesAreLookedUpOnlyOnce() {
        executeScript(COUNT_NODE_LOOKUPS_SCRIPT);
        clickElementWithJs("hide-grid-and-column");

        List<?> lookups = (List<?>) getCommandExecutor().getDriver()
                .executeAsyncScript(MEASURE_NODE_LOOKUPS_SCRIPT);
        long whileRendering = ((Number) lookups.get(0)).longValue();
        long afterRendering = ((Number) lookups.get(1)).longValue();

        // The stale cells do look their nodes up, so the test still covers the
        // scenario it was written for
        Assert.assertNotEquals(
                "Expected the stale cells to look their nodes up after the click",
                0, whileRendering);
        Assert.assertEquals(
                "Expected the component cells to stop looking up node ids that "
                        + "the server has discarded",
                0, afterRendering);
    }

    @Test
    public void hideGridClearItemsAndHideColumn_showGridWithNewItems_componentsAreRendered() {
        clickElementWithJs("hide-grid-and-column");
        clickElementWithJs("show-grid-with-new-items");

        waitUntil(driver -> grid.getRowCount() > 0);

        // The text column is hidden, so the component column is the only one
        // left
        Assert.assertEquals("component of item 2.1",
                grid.getCell(0, 0).getText());
    }
}
