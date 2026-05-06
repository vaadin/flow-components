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

/**
 * A TestBench element representing a <code>&lt;vaadin-grid-column&gt;</code>
 * element.
 */
public class GridColumnElement extends TestBenchElement {
    /**
     * Gets the header cell for this column.
     * <p>
     * A column always has a header cell, even if the header is not shown.
     *
     * @return the header cell for the column
     */
    public GridTHTDElement getHeaderCell() {
        return getPropertyElement("_headerCell").wrap(GridTHTDElement.class);
    }

    /**
     * Gets the footer cell for this column.
     * <p>
     * A column always has a footer cell, even if the footer is not shown.
     *
     * @return the footer cell for the column
     */
    public GridTHTDElement getFooterCell() {
        return getPropertyElement("_footerCell").wrap(GridTHTDElement.class);
    }
}
