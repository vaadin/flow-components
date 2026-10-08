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
package com.vaadin.flow.component.grid.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-grid-sorter&gt;</code>
 * element in a grid header cell.
 * <p>
 * Use {@link #click()} to toggle the sort direction of the column, the same way
 * as a user clicking the sort indicator.
 */
@Element("vaadin-grid-sorter")
public class GridSorterElement extends TestBenchElement {

    /**
     * Gets whether the column is sorted in ascending order.
     *
     * @return {@code true} if the sort direction is ascending, {@code false}
     *         otherwise
     */
    public boolean isAscending() {
        return "asc".equals(getDirection());
    }

    /**
     * Gets whether the column is sorted in descending order.
     *
     * @return {@code true} if the sort direction is descending, {@code false}
     *         otherwise
     */
    public boolean isDescending() {
        return "desc".equals(getDirection());
    }

    private String getDirection() {
        return getPropertyString("direction");
    }
}
