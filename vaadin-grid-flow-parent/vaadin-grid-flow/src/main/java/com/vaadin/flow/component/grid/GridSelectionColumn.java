/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
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
        getElement().setProperty("indeterminate", indeterminate);
    }

    /**
     * Sets the visibility of the select all checkbox on the client.
     *
     * @param visible
     *            whether to display the select all checkbox or hide it
     */
    public void setSelectAllCheckBoxVisibility(boolean visible) {
        getElement().setProperty("selectAllHidden", !visible);
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

    @ClientCallable
    private void selectAll() {
        selectAllCallback.run();
    }

    @ClientCallable
    private void deselectAll() {
        deselectAllCallback.run();
    }
}
