package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import org.junit.Test;

public class EditorPositionIT extends AbstractParallelTest {

    @Test
    public void compositeTouchesDirtyState() {
        getDriver().get(getBaseURL() + "/editorposition");

        final CrudElement crud = $(CrudElement.class).waitForFirst();

        setPositionButton("Bottom").click();
        assertPositionEquals("bottom");

        setPositionButton("Aside").click();
        assertPositionEquals("aside");

        setPositionButton("Overlay").click();
        assertPositionEquals("");
    }

    private ButtonElement setPositionButton(String position) {
        return $(ButtonElement.class).onPage().id("position" + position);
    }

    private ButtonElement getPositionButton() {
        return $(ButtonElement.class).onPage().id("getEditorPosition");
    }

    private void assertPositionEquals(String position) {
        waitUntil(driver -> {
            getPositionButton().click();
            return position.equals($("#editorPositionLabel").first().getText());
        });
    }
}
