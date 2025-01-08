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
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-combo-box/refresh-empty-lazy-data-provider")
public class RefreshEmptyLazyDataProviderIT extends AbstractComboBoxIT {
    private ComboBoxElement comboBox;
    private TestBenchElement refreshDataProvider;

    @Before
    public void init() {
        open();
        comboBox = $(ComboBoxElement.class).waitForFirst();
        refreshDataProvider = $("button").id("refresh-data-provider");
    }

    // Regression test for:
    // https://github.com/vaadin/flow-components/issues/3432
    @Test
    public void open_close_refreshDataProvider_open_overlayIsHiddenAndLoadingStateIsCleared() {
        comboBox.openPopup();
        assertLoadingStateResolved(comboBox);

        comboBox.closePopup();

        refreshDataProvider.click();

        comboBox.openPopup();

        // Verify the combo-box does not get stuck in a loading state
        // when refreshing an empty data provider while it is closed.
        assertLoadingStateResolved(comboBox);

        waitForElementNotPresent(By.tagName("vaadin-combo-box-overlay"));

        // Verify the overlay is closed
        // When there are no items to display then the overlay is hidden, but
        // the opened state of the combo box is still true, so we can't check
        // the opened state. Instead, we test that there is no overlay element.
        Assert.assertFalse($("vaadin-combo-box-overlay").exists());
    }
}
