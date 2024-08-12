/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/initial-multi-sort")
public class InitialMultiSortIT extends AbstractComponentIT {
    private TestBenchElement clientSortEventCount;

    @Before
    public void init() {
        open();
        clientSortEventCount = $("span").id("client-sort-event-count");
    }

    @Test
    public void initialMultiSort_noClientSideSortEvents() {
        Assert.assertEquals("Client sort events: 0",
                clientSortEventCount.getText());
    }
}
