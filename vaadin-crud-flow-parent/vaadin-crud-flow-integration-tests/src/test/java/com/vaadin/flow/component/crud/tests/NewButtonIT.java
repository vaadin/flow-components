package com.vaadin.flow.component.crud.tests;

import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-crud/new-button")
public class NewButtonIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void newButtonNull_noNewButtonPresent() {
        CrudElement crud = $(CrudElement.class).id("crud-new-button-null");
        Assert.assertFalse("New button should not be rendered",
                verifyButtonRendered(crud));
    }

    @Test
    public void newButtonVisibleFalse_noNewButtonPresent() {
        CrudElement crud = $(CrudElement.class).id("crud-new-button-hidden");
        Assert.assertFalse("New button should not be rendered",
                verifyButtonRendered(crud));
    }

    private boolean verifyButtonRendered(CrudElement crud) {
        try {
            WebElement el = crud.$("*").attribute("slot", "new-button").first();
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }
}
