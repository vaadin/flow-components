/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableRunnable;

/**
 * Server side implementation for the flow specific grid selection column.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-grid-flow-selection-column")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.5.0-alpha1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("./vaadin-grid-flow-selection-column.js")
public class GridSelectionColumn extends Component {

    private final SerializableRunnable selectAllCallback;
    private final SerializableRunnable deselectAllCallback;

    /**
     * Constructs a new grid selection column configured to use the given
     * callbacks whenever the select all checkbox is toggled on the client side.
     *
     * @param selectAllCallback
     *            the runnable to run when the select all checkbox has been
     *            checked
     * @param deselectAllCallback
     *            the runnable to run when the select all checkbox has been
     *            unchecked
     */
    public GridSelectionColumn(SerializableRunnable selectAllCallback,
            SerializableRunnable deselectAllCallback) {
        this.selectAllCallback = selectAllCallback;
        this.deselectAllCallback = deselectAllCallback;
    }

    /**
     * Sets the checked state of the select all checkbox on the client.
     *
     * @param selectAll
     *            the new state of the select all checkbox
     */
    public void setSelectAllCheckboxState(boolean selectAll) {
        getElement().setProperty("selectAll", selectAll);
    }

    /**
     * Sets the indeterminate state of the select all checkbox on the client.
     *
     * @param indeterminate
     *            the new indeterminate state of the select all checkbox
     */
    public void setSelectAllCheckboxIndeterminateState(boolean indeterminate) {
        getElement().setProperty("_indeterminate", indeterminate);
    }

    /**
     * Sets the visibility of the select all checkbox on the client.
     *
     * @param visible
     *            whether to display the select all checkbox or hide it
     */
    public void setSelectAllCheckBoxVisibility(boolean visible) {
        getElement().setProperty("_selectAllHidden", !visible);
    }

    /**
     * Sets this column's frozen state.
     *
     * @param frozen
     *            whether to freeze or unfreeze this column
     */
    public void setFrozen(boolean frozen) {
        getElement().setProperty("frozen", frozen);
    }

    /**
     * Gets the this column's frozen state.
     *
     * @return whether this column is frozen
     */
    @Synchronize("frozen-changed")
    public boolean isFrozen() {
        return getElement().getProperty("frozen", false);
    }

    /**
     * If <code>true</code>, grid rows can be selected or deselected by dragging
     * the mouse cursor over grid's selection column.
     *
     * @param dragSelect
     *            <code>true</code> to enable drag select feature,
     *            <code>false</code> for disabling it
     */
    public void setDragSelect(boolean dragSelect) {
        getElement().setProperty("dragSelect", dragSelect);
    }

    /**
     * Gets whether grid drag select is enabled or not.
     *
     * @return <code>true</code> if drag select feature is enabled,
     *         <code>false</code> otherwise
     */
    @Synchronize("drag-select-changed")
    public boolean isDragSelect() {
        return getElement().getProperty("dragSelect", false);
    }

    @ClientCallable
    private void selectAll() {
        selectAllCallback.run();
    }

    @ClientCallable
    private void deselectAll() {
        deselectAllCallback.run();
    }
}
