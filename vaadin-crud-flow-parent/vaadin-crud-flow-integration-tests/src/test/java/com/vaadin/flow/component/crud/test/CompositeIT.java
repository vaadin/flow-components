package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import org.junit.Assert;
import org.junit.Test;

public class CompositeIT extends AbstractParallelTest {

    @Test
    public void compositeTouchesDirtyState() {
        String url = getBaseURL().replace(super.getBaseURL(), super.getBaseURL() + "/vaadin-crud") + "/composite";
        getDriver().get(url);

        final CrudElement crud = $(CrudElement.class).waitForFirst();
        crud.getNewItemButton().get().click();

        Assert.assertFalse(crud.getEditorSaveButton().isEnabled());

        $("vaadin-dialog-overlay").first().$("div")
                .attribute("editor-role", "language")
                .first()
                .$(ButtonElement.class)
                .first()
                .click();

        $(TextFieldElement.class)
                .attribute("editor-role", "language-field")
                .first()
                .setValue("English");

        $("vaadin-dialog-overlay").first().$(ButtonElement.class)
                .attribute("editor-role", "language-confirm")
                .first()
                .click();

        $(DialogElement.class)
                .attribute("editor-role", "composite-dialog")
                .first()
                .callFunction("set", "opened", false);

        Assert.assertTrue($(CrudElement.class).first().getEditorSaveButton().isEnabled());
    }
}
