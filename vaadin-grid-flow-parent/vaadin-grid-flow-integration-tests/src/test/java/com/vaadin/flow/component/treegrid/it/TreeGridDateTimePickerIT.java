/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-grid/tree-grid-date-time-picker")
public class TreeGridDateTimePickerIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test
    public void shouldHaveI18nAppliedToRoot() {
        Assert.assertEquals("13.06.2000", $(DateTimePickerElement.class)
                .id("id-Row-1").getDatePresentation());
    }

    @Test
    public void shouldHaveI18nAppliedToChild() {
        Assert.assertEquals("13.06.2000", $(DateTimePickerElement.class)
                .id("id-Child 1").getDatePresentation());
    }
}
