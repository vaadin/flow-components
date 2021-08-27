package com.vaadin.flow.component.crud.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;

public class EditorButtonsIT extends AbstractParallelTest {

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-crud") + "/editorbuttons";
        getDriver().get(url);
    }

    @Test
    public void saveButtonCanBeDisabledOnTheServer() {
        CrudElement crud = getCrud();

        crud.getNewItemButton().get().click();
        assertTrue(crud.isEditorOpen());

        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class).attribute("editor-role", "last-name")
                .first();
        lastNameField.setValue("Otto");

        ButtonElement saveButton = crud.getEditorSaveButton();
        assertTrue("Save button should be enabled", saveButton.isEnabled());
        getTestButton("disable-save-button").click();
        assertFalse("Save button should be disabled", saveButton.isEnabled());
    }

    @Test
    public void saveButtonDisabledStateCanBeControlledOnTheServer() {
        CrudElement crud = getCrud();

        crud.getNewItemButton().get().click();
        assertTrue(crud.isEditorOpen());

        getTestButton("disable-save-button").click();
        ButtonElement saveButton = crud.getEditorSaveButton();
        assertFalse("Save button should be disabled", saveButton.isEnabled());

        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class).attribute("editor-role", "last-name")
                .first();
        lastNameField.setValue("Otto");

        assertFalse("Save button should remain disabled",
                saveButton.isEnabled());

        getTestButton("enable-save-button").click();
        assertTrue("Save button should be enabled", saveButton.isEnabled());
    }

    @Test
    public void cancelButtonDisabledStateCanBeControlledOnTheServer() {
        CrudElement crud = getCrud();

        crud.getNewItemButton().get().click();
        assertTrue(crud.isEditorOpen());

        getTestButton("disable-cancel-button").click();
        ButtonElement cancelButton = crud.getEditorCancelButton();
        assertFalse("Cancel button should be disabled",
                cancelButton.isEnabled());

        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class).attribute("editor-role", "last-name")
                .first();
        lastNameField.setValue("Otto");

        assertFalse("Cancel button should remain disabled",
                cancelButton.isEnabled());

        getTestButton("enable-cancel-button").click();
        assertTrue("Cancel button should be enabled", cancelButton.isEnabled());
    }

    @Test
    public void deleteButtonDisabledStateCanBeControlledOnTheServer() {
        CrudElement crud = getCrud();

        crud.openRowForEditing(0);
        assertTrue(crud.isEditorOpen());

        getTestButton("disable-delete-button").click();
        ButtonElement deleteButton = crud.getEditorDeleteButton();
        assertFalse("Delete button should be disabled",
                deleteButton.isEnabled());

        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class).attribute("editor-role", "last-name")
                .first();
        lastNameField.setValue("Otto");

        assertFalse("Delete button should remain disabled",
                deleteButton.isEnabled());

        getTestButton("enable-delete-button").click();
        assertTrue("Delete button should be enabled", deleteButton.isEnabled());
    }

    @Test
    public void buttonsCanWorkWithShortcutsDefinedOnServer() {
        String LAST_NAME_EXPECTED = "OTTO";
        CrudElement crud = getCrud();
        crud.openRowForEditing(0);
        assertTrue(crud.isEditorOpen());

        getTestButton("add-enter-shortcut-button").click();

        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class).attribute("editor-role", "last-name")
                .first();
        lastNameField.setValue(LAST_NAME_EXPECTED);

        // invoke shortcut
        WebElement body = getDriver().findElement(By.xpath("//body"));
        body.sendKeys(Keys.ENTER);

        String LAST_NAME_ACTUAL = crud.getGrid().getCell(0, 2).getText();

        assertFalse("Editor is closed", crud.isEditorOpen());
        assertEquals("Last name is updated", LAST_NAME_EXPECTED,
                LAST_NAME_ACTUAL);
    }

    private CrudElement getCrud() {
        return $(CrudElement.class).waitForFirst();
    }

    private ButtonElement getTestButton(String id) {
        return $(ButtonElement.class).onPage().id(id);
    }
}
