/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-multi-select-combo-box/server-side-filtering")
public class MultiSelectComboBoxServerSideFilteringIT
        extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
    }

    @Test
    public void openPopup_showsAllItems() {
        comboBox.openPopup();
        comboBox.waitForLoadingFinished();

        List<String> options = comboBox.getOptions();
        Assert.assertEquals(100, options.size());
    }

    @Test
    public void setFilter_noErrors() {
        comboBox.setFilter("Item 10");
        checkLogsForErrors();
    }

    @Test
    public void setFilter_closePopup_noErrors() {
        comboBox.setFilter("Item 10");
        comboBox.closePopup();
        checkLogsForErrors();
    }

    @Test
    public void setMatchingFilter_filtersItems() {
        comboBox.setFilter("Item 10");

        List<String> options = comboBox.getOptions();
        Assert.assertEquals(2, options.size());
        Assert.assertTrue("Should display Item 10",
                options.contains("Item 10"));
        Assert.assertTrue("Should display Item 100",
                options.contains("Item 100"));
    }

    @Test
    public void setNonMatchingFilter_noItems() {
        comboBox.setFilter("Item XYZ");

        List<String> options = comboBox.getOptions();
        Assert.assertEquals(0, options.size());
    }

    @Test
    public void setFilter_clearFilter_showsAllItems() {
        comboBox.setFilter("Item 10");
        comboBox.setFilter("");

        List<String> options = comboBox.getOptions();
        Assert.assertEquals(100, options.size());
    }
}
