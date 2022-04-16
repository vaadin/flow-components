package com.vaadin.flow.component.crud.test;

import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.parallel.BrowserUtil;

public class ProtectedBackendIT extends AbstractParallelTest {

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-crud") + "/protectedbackend";
        getDriver().get(url);
    }

    @Test
    public void tryDelete() {
        CrudElement crud = $(CrudElement.class).waitForFirst();

        GridElement grid = crud.getGrid();
        Assert.assertEquals(2, grid.getRowCount());

        crud.openRowForEditing(0);

        Assert.assertTrue(crud.isEditorOpen());

        crud.getEditorDeleteButton().click();
        crud.getConfirmDeleteDialog().getConfirmButton().click();

        Assert.assertTrue("Editor should stay opened if exception happened",
                crud.isEditorOpen());

        Assert.assertEquals(2, grid.getRowCount());
    }

    @Test
    public void tryCancel() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        crud.openRowForEditing(0);
        Assert.assertTrue(crud.isEditorOpen());
        crud.getEditorCancelButton().click();
        Assert.assertTrue("Editor should stay opened if exception happened",
                crud.isEditorOpen());
    }

    @Test
    public void tryModify() {
        if (BrowserUtil.isIE(getDesiredCapabilities())) {
            throw new AssumptionViolatedException(
                    "Skipped IE11, cause textfield setValue doesn't make the editor dirty");
        }

        CrudElement crud = $(CrudElement.class).waitForFirst();

        crud.openRowForEditing(0);

        modify(crud, "Other", false);

        crud.openRowForEditing(1);
        // A click in another row when editor is dirty opens confirmCancel
        // dialog
        ConfirmDialogElement confirmCancel = crud.getConfirmCancelDialog();
        Assert.assertEquals("Discard changes", confirmCancel.getHeaderText());

        confirmCancel.getConfirmButton().click();
        modify(crud, "Other", true);

        crud.openRowForEditing(1);
        modify(crud, "Oth", false);
    }

    private void modify(CrudElement crud, String newValue,
            boolean isModifyAllowed) {
        Assert.assertTrue(crud.isEditorOpen());

        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class).last();

        lastNameField.setValue(newValue);
        crud.getEditorSaveButton().click();

        if (!isModifyAllowed) {
            Assert.assertTrue("Editor should stay opened if exception happened",
                    crud.isEditorOpen());
        }

        GridElement grid = crud.getGrid();
        try {
            grid.getCell(newValue);
            Assert.assertTrue(
                    "Modify was not allowed, but the value in grid was changed",
                    isModifyAllowed);
        } catch (NoSuchElementException | TimeoutException e) {
            Assert.assertFalse(
                    "Modify was allowed, but the value in grid was not changed",
                    isModifyAllowed);
        }
    }
}
