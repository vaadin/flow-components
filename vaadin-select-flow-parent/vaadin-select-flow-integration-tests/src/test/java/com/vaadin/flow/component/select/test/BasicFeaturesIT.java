/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.select.test;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-select/")
public class BasicFeaturesIT extends AbstractSelectIT {

    @Test
    public void test_initialClientValue() {
        Assert.assertEquals("", selectElement.getProperty("value"));
    }

    @Test
    public void testEnabled_disabling_userCannotSelect() {
        page.toggleEnabled(false);
        verify.selectDisabled();

        selectElement.setProperty("disabled", false);
        verify.userSelectionDoesntFireEvent(0);
    }

    @Test
    public void testEnabled_toggleDisabled_doesntClearSelected() {
        selectElement.selectItemByIndex(1);
        verify.selectedItem("Item-1");

        page.toggleEnabled(false);
        verify.selectDisabled();

        page.toggleEnabled(true);
        verify.selectedItem("Item-1");
    }

    @Test
    public void testEnabled_initiallyDisabled_userCannotSelect() {
        openWithExtraParameter("disabled");
        verify.selectDisabled();

        selectElement.setProperty("disabled", false);
        verify.userSelectionDoesntFireEvent(0);
    }

    @Test
    public void testReadOnly_readOnly_userCannotSelect() {
        page.toggleReadOnly(true);
        verify.selectReadOnly();

        selectElement.setProperty("readonly", false);
        verify.userSelectionDoesntFireEvent(0);
    }

    @Test
    public void testReadOnly_initiallyReadOnly_userCannotSelect() {
        openWithExtraParameter("readonly");

        verify.selectReadOnly();

        selectElement.setProperty("readonly", false);
        verify.userSelectionDoesntFireEvent(0);
    }

    @Test
    public void testReadOnly_toggleReadOnly_doesntClearSelected() {
        selectElement.selectItemByIndex(1);
        verify.selectedItem("Item-1");

        page.toggleReadOnly(true);
        verify.selectReadOnly();

        page.toggleReadOnly(false);
        verify.selectedItem("Item-1");
    }

    @Test
    public void testVisibility_invisible_noSelect() {
        selectElement.selectItemByIndex(2);
        page.toggleVisible(false);
        Assert.assertEquals(
                "No select should be found from page when invisible", 0,
                findElements(By.tagName("select")).size());

        page.clickSelectFirstItem();
        page.toggleVisible(true);
        verify.selectedItem("Item-0");
    }

    @Test
    public void testVisibility_initiallyInvisible_noSelect() {
        openWithExtraParameter("invisible");

        Assert.assertEquals(
                "No select should be found from page when invisible", 0,
                findElements(By.tagName("select")).size());

        page.clickSelectFirstItem();
        page.toggleVisible(true);

        verify.selectedItem("Item-0");
    }

    @Test
    public void testHelper_text() {
        page.toggleHelperText(true);
        verify.helperTextVisible();

        page.toggleHelperText(false);
        verify.noHelperText();

    }

    @Override
    protected int getInitialNumberOfItems() {
        return 5;
    }
}
