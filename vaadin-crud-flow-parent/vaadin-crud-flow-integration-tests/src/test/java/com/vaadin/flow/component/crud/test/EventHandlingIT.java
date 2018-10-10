package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.parallel.BrowserUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class EventHandlingIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @After
    public void dismissDialog() {
        if (isEditorOpen()) {
            getEditorCancelButton().click();
        }
    }

    @Test
    public void newTest() {
        Assert.assertFalse(isEditorOpen());
        getNewButton().click();
        Assert.assertEquals("New: Person{id=null, firstName='null', lastName='null'}",
                getLastEvent());
        Assert.assertTrue(isEditorOpen());
    }

    @Test
    public void editTest() {
        Assert.assertFalse(isEditorOpen());
        openRowForEditing(0);
        Assert.assertEquals("Edit: Person{id=1, firstName='Sayo', lastName='Sayo'}",
                getLastEvent());
        Assert.assertTrue(isEditorOpen());

        dismissDialog();

        openRowForEditing(2);
        Assert.assertEquals("Edit: Person{id=3, firstName='Guille', lastName='Guille'}",
                getLastEvent());
        Assert.assertEquals("Guille", $(TextFieldElement.class)
                .attribute("editor-role", "first-name").first().getValue());
        Assert.assertEquals("Guille", $(TextFieldElement.class)
                .attribute("editor-role", "last-name").first().getValue());
    }

    @Test
    public void cancelTest() {
        openRowForEditing(2);
        getEditorCancelButton().click();
        Assert.assertEquals("Cancel: Person{id=3, firstName='Guille', lastName='Guille'}",
                getLastEvent());
        Assert.assertFalse(isEditorOpen());
    }

    @Test
    public void deleteTest() {
        Assert.assertEquals("3 items available", getFooterText());
        openRowForEditing(2);
        getEditorDeleteButton().click();

        // TODO(oluwasayo): Remove this workaround when vaadin-components-testbench includes Confirm Dialog
        TestBenchElement overlayContext = $("vaadin-dialog-overlay").onPage()
                .last().$(TestBenchElement.class).id("content");
        TestBenchElement confirmButton = overlayContext.$(ButtonElement.class).id("confirm");
        confirmButton.click();

        Assert.assertEquals("Delete: Person{id=3, firstName='Guille', lastName='Guille'}",
                getLastEvent());
        Assert.assertEquals("2 items available", getFooterText());
        Assert.assertFalse(isEditorOpen());
    }

    @Test
    public void saveTest() {
        openRowForEditing(0);
        TextFieldElement lastNameField = $(TextFieldElement.class)
                .attribute("editor-role", "last-name").first();
        Assert.assertTrue(lastNameField.hasAttribute("invalid"));

        // Invalid input
        lastNameField.setValue("Manolo");
        getEditorSaveButton().click();
        Assert.assertTrue(lastNameField.hasAttribute("invalid"));
        Assert.assertTrue(isEditorOpen());
        Assert.assertEquals("Sayo",
                $(GridElement.class).first().getCell(0, 2).getText());

        // Valid input
        lastNameField.setValue("Oladeji");
        Assert.assertFalse(lastNameField.hasAttribute("invalid"));

        getEditorSaveButton().click();

        if (BrowserUtil.isIE(getDesiredCapabilities())) {
            // TODO(oluwasayo): Investigate why editor sometimes doesn't disappear on first click in IE
            // especially when server-side validation is involved
            return;
        }

        Assert.assertFalse(isEditorOpen());
        Assert.assertEquals("Oladeji",
                $(GridElement.class).first().getCell(0, 2).getText());
    }

    private String getFooterText() {
        return getFooterItems().stream()
                .filter(e -> e.getTagName().equals("span"))
                .findFirst()
                .map(TestBenchElement::getText)
                .orElse(null);
    }

    private List<TestBenchElement> getFooterItems() {
        return $("*").attribute("slot", "footer").all();
    }

    private void openRowForEditing(int row) {
        // The first real row is on index 2.
        $("vaadin-crud-edit").all().get(row + 2).click();
    }

    private ButtonElement getEditorSaveButton() {
        return getEditorButton(0);
    }

    private ButtonElement getEditorCancelButton() {
        return getEditorButton(1);
    }

    private ButtonElement getEditorDeleteButton() {
        return getEditorButton(2);
    }

    private ButtonElement getEditorButton(int index) {
        return getEditor().$(ButtonElement.class).attribute("slot", "footer").get(index);
    }

    private boolean isEditorOpen() {
        return $("vaadin-dialog-overlay").attribute("opened", "").exists();
    }

    private TestBenchElement getEditor() {
        return $("vaadin-dialog-overlay").attribute("opened", "").first();
    }
}
