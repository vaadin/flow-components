package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.parallel.BrowserUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EventHandlingIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @After
    public void dismissDialog() {
        CrudElement crud = $(CrudElement.class).first();
        if (crud.isEditorOpen()) {
            crud.getEditorCancelButton().click();
        }
    }

    @Test
    public void newTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        crud.getNewItemButton().get().click();
        Assert.assertEquals("New: Person{id=null, firstName='null', lastName='null'}",
                getLastEvent());
        Assert.assertTrue(crud.isEditorOpen());
    }

    @Test
    public void editTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        crud.openRowForEditing(0);
        Assert.assertEquals("Edit: Person{id=1, firstName='Sayo', lastName='Sayo'}",
                getLastEvent());
        Assert.assertTrue(crud.isEditorOpen());

        dismissDialog();

        crud.openRowForEditing(2);

        Assert.assertEquals("Edit: Person{id=3, firstName='Guille', lastName='Guille'}",
                getLastEvent());

        Assert.assertEquals("Guille", crud.getEditor().$(TextFieldElement.class)
                .attribute("editor-role", "first-name").first().getValue());

        Assert.assertEquals("Guille", crud.getEditor().$(TextFieldElement.class)
                .attribute("editor-role", "last-name").first().getValue());
    }

    @Test
    public void cancelTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        crud.openRowForEditing(2);
        crud.getEditorCancelButton().click();
        Assert.assertEquals("Cancel: Person{id=3, firstName='Guille', lastName='Guille'}",
                getLastEvent());
        Assert.assertFalse(crud.isEditorOpen());
    }

    @Test
    public void deleteTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();

        Assert.assertEquals("3 items available", getFooterText(crud));
        crud.openRowForEditing(2);
        crud.getEditorDeleteButton().click();

        // TODO(oluwasayo): Remove this workaround when vaadin-components-testbench includes Confirm Dialog
        TestBenchElement overlayContext = $("vaadin-dialog-overlay").onPage()
                .last().$(TestBenchElement.class).id("content");
        TestBenchElement confirmButton = overlayContext.$(ButtonElement.class).id("confirm");
        confirmButton.click();

        Assert.assertEquals("Delete: Person{id=3, firstName='Guille', lastName='Guille'}",
                getLastEvent());
        Assert.assertEquals("2 items available", getFooterText(crud));
        Assert.assertFalse(crud.isEditorOpen());
    }

    @Test
    public void saveTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        crud.openRowForEditing(0);
        TextFieldElement lastNameField = crud.getEditor().$(TextFieldElement.class)
                .attribute("editor-role", "last-name").first();
        Assert.assertTrue(lastNameField.hasAttribute("invalid"));

        // Invalid input
        lastNameField.setValue("Manolo");
        crud.getEditorSaveButton().click();
        Assert.assertTrue(lastNameField.hasAttribute("invalid"));
        Assert.assertTrue(crud.isEditorOpen());
        Assert.assertEquals("Sayo",
                $(GridElement.class).first().getCell(0, 2).getText());

        // Valid input
        lastNameField.setValue("Oladeji");
        Assert.assertFalse(lastNameField.hasAttribute("invalid"));

        crud.getEditorSaveButton().click();

        if (BrowserUtil.isIE(getDesiredCapabilities())) {
            // TODO(oluwasayo): Investigate why editor sometimes doesn't disappear on first click in IE
            // especially when server-side validation is involved
            return;
        }

        Assert.assertFalse(crud.isEditorOpen());
        Assert.assertEquals("Oladeji",
                $(GridElement.class).first().getCell(0, 2).getText());
    }

    private static String getFooterText(CrudElement crud) {
        return crud.getToolbar().get(0).getText();
    }
}
