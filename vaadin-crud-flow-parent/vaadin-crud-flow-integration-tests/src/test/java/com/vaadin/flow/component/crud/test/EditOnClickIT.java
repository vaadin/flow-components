package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.crud.examples.EditOnClickView;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EditOnClickIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL() + "/editonclick");
    }

    @After
    public void dismissEditor() {
        CrudElement crud = $(CrudElement.class).first();
        if (crud.isEditorOpen()) {
            crud.getEditorCancelButton().click();
        }
    }

    @Test
    public void editButtonsArePresentByDefault() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        Assert.assertFalse(crud.getPropertyBoolean("editOnClick"));

        Assert.assertTrue("Number of edit columns are greated than zero", crud.$("vaadin-crud-edit").all().size() > 0);
    }

    @Test
    public void editButtonsAreHiddenIfEditOnClickIsEnabled() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());

        $(ButtonElement.class).onPage().id(EditOnClickView.CLICKTOEDIT_BUTTON_ID).click();

        Assert.assertTrue(crud.getPropertyBoolean("editOnClick"));

        Assert.assertTrue("Number of edit columns is zero", crud.$("vaadin-crud-edit").all().size() == 0);
    }

    @Test
    public void editItemOnRowClickIfEnabled() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());

        $(ButtonElement.class).onPage().id(EditOnClickView.CLICKTOEDIT_BUTTON_ID).click();

        crud.openRowForEditing(0);
        Assert.assertTrue(crud.isEditorOpen());
    }

    @Test
    public void secondClickShouldCloseEditor() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());

        $(ButtonElement.class).onPage().id(EditOnClickView.CLICKTOEDIT_BUTTON_ID).click();

        crud.openRowForEditing(0);
        Assert.assertTrue(crud.isEditorOpen());

        crud.openRowForEditing(0);
        Assert.assertFalse(crud.isEditorOpen());
    }
}
