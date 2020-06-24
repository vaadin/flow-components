package com.vaadin.flow.component.crud.testbench;


/*
 * #%L
 * Vaadin Crud Testbench API
 * %%
 * Copyright (C) 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

import java.util.List;
import java.util.Optional;

/**
 * TestBench element for the vaadin-crud element
 */
@Element("vaadin-crud")
public class CrudElement extends TestBenchElement {

    /**
     * Gets the crud grid
     *
     * @return the crud grid
     */
    public GridElement getGrid() {
        return this.$(GridElement.class).first();
    }

    /**
     * Gets the new item button
     *
     * @return the new item button
     */
    public Optional<TestBenchElement> getNewItemButton() {
        ElementQuery<TestBenchElement> newButtonQuery
                = this.$(TestBenchElement.class).attribute("new-button", "");
        return newButtonQuery.exists() ? Optional.of(newButtonQuery.last()) : Optional.empty();
    }

    /**
     * Gets the filter fields if the Crud built-in Grid is being used with filters enabled
     *
     * @return the filter field for each column
     */
    public List<TextFieldElement> getFilterFields() {
        return this.$(TextFieldElement.class)
                .attribute("crud-role", "Search").all();
    }

    /**
     * Gets the toolbar content.
     *
     * @return the toolbar content
     */
    public List<TestBenchElement> getToolbar() {
        return this.$(TestBenchElement.class).attribute("slot", "toolbar").all();
    }

    /**
     * Opens a grid row for editing using the CRUD edit button on that row
     *
     * @param row the row to open for editing
     */
    public void openRowForEditing(int row) {
        if (isEditOnClick()) {
            this.getGrid().getCell(row, 0).click();
        } else {
            this.$("vaadin-crud-edit").all().get(row).click();
        }
    }

    /**
     * Gets the editor save button
     *
     * @return the editor save button
     */
    public ButtonElement getEditorSaveButton() {
        return getEditorButton(0);
    }

    /**
     * Gets the editor cancel button
     *
     * @return the editor cancel button
     */
    public ButtonElement getEditorCancelButton() {
        return getEditorButton(1);
    }

    /**
     * Gets the editor delete button
     *
     * @return the editor delete button
     */
    public ButtonElement getEditorDeleteButton() {
        return getEditorButton(2);
    }

    private ButtonElement getEditorButton(int index) {
        return getEditor().$(ButtonElement.class).attribute("slot", "footer").get(index);
    }

    /**
     * Checks if an editor overlay is open on the default editor position
     * Otherwise, checks the value of editorOpened property
     *
     * @return {@code true} if the editor is open and {@code false}, otherwise
     */
    public boolean isEditorOpen() {
        if (getEditorPosition().isEmpty()) {
            return $("vaadin-dialog-overlay").onPage().attribute("opened", "").exists();
        }
        return getPropertyBoolean("editorOpened");
    }

    /**
     * Gets the editor position selected for the CRUD
     * Possible values are "" (default), "bottom" and "aside"
     *
     * @return a string containing the value defined for the editor position
     */
    public String getEditorPosition() {
        return getPropertyString("editorPosition");
    }

    /**
     * Gets whether editor can be opened by a click on the row or not
     *
     * @return {@code true} if feature is enabled or {@code false} otherwise
     */
    public boolean isEditOnClick() {
        return getPropertyBoolean("editOnClick");
    }

    /**
     * Gets the open editor overlay
     *
     * @return the open editor overlay
     */
    public TestBenchElement getEditor() {
        if (getEditorPosition().isEmpty()) {
            return $("vaadin-dialog-overlay").onPage().attribute("opened", "").first();
        }
        return this.$("vaadin-dialog-layout").first();
    }
}
