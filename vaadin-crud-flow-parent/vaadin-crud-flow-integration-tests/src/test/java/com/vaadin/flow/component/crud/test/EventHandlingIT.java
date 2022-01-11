package com.vaadin.flow.component.crud.test;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.parallel.BrowserUtil;

public class EventHandlingIT extends AbstractParallelTest {

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-crud");
        getDriver().get(url);
    }

    @After
    public void dismissDialog() {
        CrudElement crud = $(CrudElement.class).first();
        if (crud.isEditorOpen()) {
            crud.getEditorCancelButton().click();
        }
    }

    private void dismissConfirmDialog(CrudElement crud,
            ConfirmDialogType type) {
        final TestBenchElement confirmButton = crud
                .$(ConfirmDialogElement.class).id(type.getId())
                .getConfirmButton();
        confirmButton.click();
    }

    @Test
    public void newTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        crud.getNewItemButton().get().click();
        Assert.assertEquals(
                "New: Person{id=null, firstName='null', lastName='null'}",
                getLastEvent());
        Assert.assertTrue(crud.isEditorOpen());
    }

    @Test
    public void newTest_serverSide() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        getTestButton("newServerItem").click();
        Assert.assertEquals(
                "New: Person{id=null, firstName='null', lastName='null'}",
                getLastEvent());
        Assert.assertTrue(crud.isEditorOpen());
    }

    @Test
    public void editTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        crud.openRowForEditing(0);
        Assert.assertEquals(
                "Edit: Person{id=1, firstName='Sayo', lastName='Sayo'}",
                getLastEvent());
        Assert.assertTrue(crud.isEditorOpen());

        dismissDialog();

        crud.openRowForEditing(2);

        Assert.assertEquals(
                "Edit: Person{id=3, firstName='Guille', lastName='Guille'}",
                getLastEvent());

        Assert.assertEquals("Guille", crud.getEditor().$(TextFieldElement.class)
                .attribute("editor-role", "first-name").first().getValue());

        Assert.assertEquals("Guille", crud.getEditor().$(TextFieldElement.class)
                .attribute("editor-role", "last-name").first().getValue());
    }

    @Test
    public void editTest_serverSide() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        getTestButton("editServerItem").click();
        Assert.assertEquals(
                "Edit: Person{id=1, firstName='Sayo', lastName='Oladeji'}",
                getLastEvent());
        Assert.assertTrue(crud.isEditorOpen());

        dismissDialog();
        Assert.assertFalse(crud.isEditorOpen());
        Assert.assertFalse(isConfirmDialogOpen(crud, ConfirmDialogType.CANCEL));

        // Ensure editor is marked dirty on edit
        getTestButton("editServerItem").click();
        crud.getEditor().$(TextFieldElement.class)
                .attribute("editor-role", "first-name").first()
                .setValue("Vaadin");

        dismissDialog();
        dismissConfirmDialog(crud, ConfirmDialogType.CANCEL);
    }

    @Test
    public void cancelTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        crud.openRowForEditing(2);
        crud.getEditorCancelButton().click();
        Assert.assertEquals(
                "Cancel: Person{id=3, firstName='Guille', lastName='Guille'}",
                getLastEvent());
        Assert.assertFalse(crud.isEditorOpen());
    }

    @Test
    public void deleteTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();

        Assert.assertEquals("3 items available", getFooterText(crud));
        crud.openRowForEditing(2);
        crud.getEditorDeleteButton().click();
        dismissConfirmDialog(crud, ConfirmDialogType.DELETE);

        Assert.assertEquals(
                "Delete: Person{id=3, firstName='Guille', lastName='Guille'}",
                getLastEvent());
        Assert.assertEquals("2 items available", getFooterText(crud));
        Assert.assertFalse(crud.isEditorOpen());
    }

    @Test
    public void saveTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        crud.openRowForEditing(0);
        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class).attribute("editor-role", "last-name")
                .first();
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
            // TODO(oluwasayo): Investigate why editor sometimes doesn't
            // disappear on first click in IE
            // especially when server-side validation is involved
            return;
        }

        Assert.assertFalse(crud.isEditorOpen());
        Assert.assertEquals("Oladeji",
                $(GridElement.class).first().getCell(0, 2).getText());
    }

    @Test
    public void emptyInvalidFieldsIndicatedOnSave() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        crud.getNewItemButton().get().click();

        TextFieldElement firstNameField = crud.getEditor()
                .$(TextFieldElement.class)
                .attribute("editor-role", "first-name").first();

        Assert.assertFalse(firstNameField.hasAttribute("invalid"));

        // To avoid editor being dirty
        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class).attribute("editor-role", "last-name")
                .first();
        lastNameField.setValue("Oladeji");

        crud.getEditorSaveButton().click();

        Assert.assertTrue(firstNameField.hasAttribute("invalid"));
    }

    @Test
    public void invalidFieldsIndicatedOnSave() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        crud.openRowForEditing(1);

        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class).attribute("editor-role", "last-name")
                .first();

        Assert.assertFalse(lastNameField.hasAttribute("invalid"));

        lastNameField.setValue("Raiden");
        crud.getEditorSaveButton().click();

        Assert.assertTrue(lastNameField.hasAttribute("invalid"));
    }

    @Test
    public void newItemDialogFields_ShouldPrefilledWithExpectedValues_SetInNewEventListener() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        getTestButton("newEventListener").click();

        crud.getNewItemButton().get().click();

        TestBenchElement editor = crud.getEditor();
        TextFieldElement firstNameField = editor.$(TextFieldElement.class)
                .attribute("editor-role", "first-name").first();
        TextFieldElement lastNameField = editor.$(TextFieldElement.class)
                .attribute("editor-role", "last-name").first();

        Assert.assertEquals("firstName", firstNameField.getValue());
        Assert.assertEquals("lastName", lastNameField.getValue());

    }

    private static String getFooterText(CrudElement crud) {
        return crud.getToolbar().get(0).getText();
    }

    private ButtonElement getTestButton(String id) {
        return $(ButtonElement.class).onPage().id(id);
    }

    private boolean isConfirmDialogOpen(CrudElement crud,
            ConfirmDialogType type) {
        ConfirmDialogElement confirmDialog = crud.$(ConfirmDialogElement.class)
                .id(type.getId());
        TestBenchElement overlay = ((TestBenchElement) confirmDialog
                .getContext());
        return overlay.getPropertyBoolean("opened");
    }

    private enum ConfirmDialogType {
        CANCEL, DELETE;

        private String getId() {
            return "confirm" + StringUtils.capitalize(name().toLowerCase());
        }
    }
}
