package com.vaadin.flow.component.crud.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.parallel.BrowserUtil;

public class CustomGridIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL() + "/customgrid");
    }

    @Test
    public void editTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        crud.openRowForEditing(0);
        Assert.assertTrue(crud.isEditorOpen());
        TextFieldElement lastNameField = crud.getEditor().$(TextFieldElement.class)
                .attribute("editor-role", "last-name").first();

        Assert.assertEquals("Sayo", lastNameField.getValue());

        lastNameField.setValue("Otto");
        crud.getEditorSaveButton().click();

        if (BrowserUtil.isIE(getDesiredCapabilities())) {
            return;
        }

        Assert.assertFalse(crud.isEditorOpen());
        Assert.assertEquals("Otto",
                $(GridElement.class).first().getCell(0, 2).getText());
    }

    @Test
    public void cancelChangesTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        crud.openRowForEditing(0);
        Assert.assertTrue(crud.isEditorOpen());
        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class).attribute("editor-role", "last-name")
                .first();

        Assert.assertEquals("Sayo", lastNameField.getValue());
        lastNameField.setValue("Otto");
        crud.getEditorCancelButton().click();

        if (BrowserUtil.isIE(getDesiredCapabilities())) {
            return;
        }

        ConfirmDialogElement confirmCancel = crud.$(ConfirmDialogElement.class)
                .id("confirmCancel");
        Assert.assertEquals("Discard changes", confirmCancel.getHeaderText());
        confirmCancel.getConfirmButton().click();
        Assert.assertEquals("Sayo",
                $(GridElement.class).first().getCell(0, 2).getText());
    }

    @Test
    public void customGridDoesNotReactToThemeVariantChanges() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        GridElement grid = $(GridElement.class).first();

        Assert.assertNotEquals("no-border", crud.getAttribute("theme"));
        Assert.assertNotEquals("no-border", grid.getAttribute("theme"));

        toggleBordersButton().click();
        Assert.assertEquals("no-border", crud.getAttribute("theme"));
        Assert.assertNotEquals("no-border", grid.getAttribute("theme"));
    }

    private ButtonElement toggleBordersButton() {
        return $(ButtonElement.class).onPage().id("toggleBorders");
    }
}
