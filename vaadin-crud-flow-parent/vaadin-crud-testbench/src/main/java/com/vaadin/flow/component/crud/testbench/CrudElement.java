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
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

import java.util.List;

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
    public ButtonElement getNewItemButton() {
        return this.$(ButtonElement.class).id("new");
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
     * Gets the footer content. This would typically be a <code>span</code> element containing a text if
     * the footer was set with {@code crud.setFooter(String)}
     *
     * @return the footer content
     */
    public List<TestBenchElement> getFooterItems() {
        return this.$(TestBenchElement.class).attribute("slot", "footer").all();
    }

    /**
     * Opens a grid row for editing using the CRUD edit button on that row
     *
     * @param row the row to open for editing
     */
    public void openRowForEditing(int row) {
        // The first real row is on index 2.
        this.$("vaadin-crud-edit").all().get(row + 2).click();
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
     * Checks if an editor overlay is open
     *
     * @return true if the editor overlay is open and false if otherwise
     */
    public boolean isEditorOpen() {
        return $("vaadin-dialog-overlay").onPage().attribute("opened", "").exists();
    }

    /**
     * Gets the open editor overlay
     *
     * @return the open editor overlay
     */
    public TestBenchElement getEditor() {
        return $("vaadin-dialog-overlay").onPage().attribute("opened", "").first();
    }
}
