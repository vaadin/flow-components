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
package com.vaadin.flow.component.grid.editor;

import java.io.Serializable;

import com.vaadin.flow.component.grid.Grid;

/**
 * An event listener for a {@link Grid} editor close events.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            the bean type
 *
 * @see EditorCloseEvent
 * @see Editor#addCloseListener(EditorCloseListener)
 */
@FunctionalInterface
public interface EditorCloseListener<T> extends Serializable {

    /**
     * Called when the editor is closed.
     *
     * @param event
     *            close event
     */
    public void onEditorClose(EditorCloseEvent<T> event);
}
