package com.vaadin.flow.component.confirmdialog.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.examples.Dimensions;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractParallelTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * DimensionsIT
 */
public class DimensionsIT extends AbstractParallelTest {

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-confirm-dialog") + "/Dimensions";
        getDriver().get(url);

        waitUntil(ExpectedConditions
                .presenceOfElementLocated(By.id(Dimensions.VIEW_ID)));
    }

    @Test
    public void testWidthCanChange() {
        changeDialogWidth();
        openDialog();

        String width = getCssContentValue("width");

        Assert.assertEquals(Dimensions.DIMENSION_BIGGER, width);
    }

    @Test
    public void testHeightCanChange() {
        changeDialogHeight();
        openDialog();

        String height = getCssContentValue("height");

        Assert.assertEquals(Dimensions.DIMENSION_BIGGER, height);
    }

    @Test
    public void testChangeDimensionOnAttachedDialog() {
        attachDialog();
        changeDialogWidth();
        changeDialogHeight();
        openDialog();

        Assert.assertEquals(Dimensions.DIMENSION_BIGGER,
                getCssContentValue("height"));
        Assert.assertEquals(Dimensions.DIMENSION_BIGGER,
                getCssContentValue("width"));
    }

    @Test
    public void testDimensionsChangeAreKeptAfterClosingAndOpening() {
        changeDialogHeight();
        openDialog();

        String height = getCssContentValue("height");

        Assert.assertEquals(Dimensions.DIMENSION_BIGGER, height);

        getConfirmDialog().getConfirmButton().click();

        openDialog();

        waitUntil(driver -> Dimensions.DIMENSION_BIGGER
                .equals(getCssContentValue("height")));
    }

    @Test
    public void testDimensionsCanBeReset() {
        openDialog();

        String originalHeight = getCssContentValue("height");
        String originalWidth = getCssContentValue("width");

        getConfirmDialog().getConfirmButton().click();

        changeDialogWidth();
        changeDialogHeight();

        openDialog();
        getConfirmDialog().getConfirmButton().click();
        resetDimensions();
        openDialog();

        Assert.assertEquals(originalHeight, getCssContentValue("height"));
        Assert.assertEquals(originalWidth, getCssContentValue("width"));
    }

    @Test
    public void testChangeOpenedDialogDimensions() {
        openDialog();

        ConfirmDialogElement confirmDialog = getConfirmDialog();

        confirmDialog.$(ButtonElement.class)
                .id(Dimensions.CHANGE_DIALOG_ATTACHED_WIDTH_ID).click();
        confirmDialog.$(ButtonElement.class)
                .id(Dimensions.CHANGE_DIALOG_ATTACHED_HEIGHT_ID).click();

        waitUntil(driver -> Dimensions.DIMENSION_SMALLER
                .equals(getCssContentValue("height"))
                && Dimensions.DIMENSION_SMALLER
                        .equals(getCssContentValue("width")));
    }

    private ConfirmDialogElement getConfirmDialog() {
        return $(ConfirmDialogElement.class).waitForFirst();
    }

    private String getCssContentValue(String value) {
        return getContent().getCssValue(value);
    }

    private TestBenchElement getContent() {
        return getOverlay().getPropertyElement("$", "overlay");
    }

    private TestBenchElement getOverlay() {
        return ((TestBenchElement) getConfirmDialog().getContext());
    }

    private void attachDialog() {
        $(ButtonElement.class).id(Dimensions.ATTACH_DIALOG_ID).click();
    }

    private void openDialog() {
        $(ButtonElement.class).id(Dimensions.OPEN_DIALOG_ID).click();
    }

    private void changeDialogWidth() {
        $(ButtonElement.class).id(Dimensions.CHANGE_DIALOG_WIDTH_ID).click();
    }

    private void changeDialogHeight() {
        $(ButtonElement.class).id(Dimensions.CHANGE_DIALOG_HEIGHT_ID).click();
    }

    private void resetDimensions() {
        $(ButtonElement.class).id(Dimensions.RESET_DIALOG_DIMENSIONS_ID)
                .click();
    }

}
