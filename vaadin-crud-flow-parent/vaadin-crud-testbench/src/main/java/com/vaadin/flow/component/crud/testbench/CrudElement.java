package com.vaadin.flow.component.crud.testbench;

/*
 * #%L
 * Vaadin Crud Testbench API
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        ElementQuery<TestBenchElement> newButtonQuery = this
                .$(TestBenchElement.class).attribute("new-button", "");
        return newButtonQuery.exists() ? Optional.of(newButtonQuery.last())
                : Optional.empty();
    }

    /**
     * Gets the filter fields if the Crud built-in Grid is being used with
     * filters enabled
     *
     * @return the filter field for each column
     */
    public List<TextFieldElement> getFilterFields() {
        return this.$(TextFieldElement.class).attribute("crud-role", "Search")
                .all();
    }

    /**
     * Gets the toolbar content.
     *
     * @return the toolbar content
     */
    public List<TestBenchElement> getToolbar() {
        return this.$(TestBenchElement.class).attribute("slot", "toolbar")
                .all();
    }

    /**
     * Opens a grid row for editing using the CRUD edit button on that row
     *
     * @param row
     *            the row to open for editing
     */
    public void openRowForEditing(int row) {
        if (isEditOnClick()) {
            this.getGrid().getCell(row, 0).click();
        } else {
            GridTRElement editedRow = getGrid().getRow(row);
            GridTHTDElement editCell = getGrid().getAllColumns().stream()
                    .map(column -> editedRow.getCell(column))
                    .filter(cell -> cell.getInnerHTML()
                            .contains("vaadin-crud-edit"))
                    .collect(Collectors.toList()).get(0);
            editCell.$("vaadin-crud-edit").get(0).click();
        }
    }

    /**
     * Gets the editor save button
     *
     * @return the editor save button
     */
    public ButtonElement getEditorSaveButton() {
        return ((TestBenchElement) getPropertyElement("_saveButton"))
                .wrap(ButtonElement.class);
    }

    /**
     * Gets the editor cancel button
     *
     * @return the editor cancel button
     */
    public ButtonElement getEditorCancelButton() {
        return ((TestBenchElement) getPropertyElement("_cancelButton"))
                .wrap(ButtonElement.class);
    }

    /**
     * Gets the editor delete button
     *
     * @return the editor delete button
     */
    public ButtonElement getEditorDeleteButton() {
        return ((TestBenchElement) getPropertyElement("_deleteButton"))
                .wrap(ButtonElement.class);
    }

    /**
     * Checks if an editor overlay is open on the default editor position
     * Otherwise, checks the value of editorOpened property
     *
     * @return {@code true} if the editor is open and {@code false}, otherwise
     */
    public boolean isEditorOpen() {
        if (getEditorPosition().isEmpty()) {
            return $("vaadin-crud-dialog-overlay").onPage()
                    .attribute("opened", "").exists();
        }
        return getPropertyBoolean("editorOpened");
    }

    /**
     * Gets the editor position selected for the CRUD Possible values are ""
     * (default), "bottom" and "aside"
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
            return $("vaadin-crud-dialog-overlay").onPage()
                    .attribute("opened", "").first();
        }
        return this;
    }

    /**
     * Gets the confirm cancel dialog
     *
     * @return the confirm cancel dialog
     */
    public ConfirmDialogElement getConfirmCancelDialog() {
        return this.$(ConfirmDialogElement.class).id("confirmCancel");
    }

    /**
     * Gets the confirm delete dialog
     *
     * @return the confirm delete dialog
     */
    public ConfirmDialogElement getConfirmDeleteDialog() {
        return this.$(ConfirmDialogElement.class).id("confirmDelete");
    }
}
