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

/**
 * Holds the data for save events fired in {@link Editor}.
 *
 * @author Vaadin Ltd
 *
 * @see EditorSaveListener
 *
 * @param <T>
 *            the item type
 */
public class EditorSaveEvent<T> extends EditorEvent<T> {

    /**
     * Constructor for the editor save event.
     *
     * @param editor
     *            the source of the event
     * @param item
     *            the item being edited
     */
    public EditorSaveEvent(Editor<T> editor, T item) {
        super(editor, item);
    }

}
