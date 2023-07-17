package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

@TestPath("vaadin-dialog/inert-dialog")
public class InertDialogIT extends AbstractComponentIT {
    private DialogElement dialog;
    private TestBenchElement setInert;

    @Before
    public void init() {
        open();
        dialog = $(DialogElement.class).waitForFirst();
        setInert = dialog.$(TestBenchElement.class).id("set-inert");
    }

    @Test
    public void notInert_closesOnOutsideClick() {
        $("body").first().click();
        Assert.assertFalse("Dialog should have closed", dialog.isOpen());
    }

    @Test
    public void notInert_closesOnEscape() {
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();
        Assert.assertFalse("Dialog should have closed", dialog.isOpen());
    }

    @Test
    public void inert_doesNotCloseOnOutsideClick() {
        setInert.click();
        $("body").first().click();
        Assert.assertTrue("Dialog should stay open", dialog.isOpen());
    }

    @Test
    public void inert_doesNotCloseOnEscape() {
        setInert.click();
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();
        Assert.assertTrue("Dialog should stay open", dialog.isOpen());
    }
}
