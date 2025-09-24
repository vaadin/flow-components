/*
 * Copyright 2000-2025 Vaadin Ltd.
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
