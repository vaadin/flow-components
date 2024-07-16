/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-combo-box/refresh-empty-lazy-data-provider")
public class RefreshEmptyLazyDataProviderIT extends AbstractComponentIT {
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
        comboBox.closePopup();

        refreshDataProvider.click();
        comboBox.openPopup();

        // Verify the overlay is closed
        // When there are no items to display then the overlay is hidden, but
        // the opened state of the combo box is still true, so we can't check
        // the opened state. Instead, we test that there is no overlay element.
        Assert.assertFalse($("vaadin-combo-box-overlay").exists());
        Assert.assertFalse(comboBox.getPropertyBoolean("loading"));
    }
}
