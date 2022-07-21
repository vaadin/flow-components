package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

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
