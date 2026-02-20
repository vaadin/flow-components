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
package com.vaadin.flow.component.tabs.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.tabs.testbench.TabSheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-tabs/tabsheet-initial-selection")
public class TabSheetInitialSelectionIT extends AbstractComponentIT {

    private TabSheetElement tabSheet;

    @Before
    public void init() {
        open();
        tabSheet = $(TabSheetElement.class).first();
    }

    @Test
    public void initialSelection_tabSelectedAndContentDisplayed() {
        Assert.assertEquals(1, tabSheet.getSelectedTabIndex());
        Assert.assertEquals("Tab two content", tabSheet.getContent().getText());
    }
}
