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
package com.vaadin.flow.component.combobox;

import com.vaadin.flow.component.ComponentEvent;
import elemental.json.JsonObject;

import java.io.Serializable;

/**
 * @deprecated This class is not used in any API, and will be removed in a
 *             future major version.
 */
public abstract class GeneratedVaadinComboBox implements Serializable {

    /**
     * @deprecated Use
     *             {@link com.vaadin.flow.component.combobox.events.CustomValueSetEvent}
     *             instead. This class will be removed in a future major
     *             version.
     * @param <TComponent>
     *            The specific combo box component type
     */
    public static class CustomValueSetEvent<TComponent extends ComboBoxBase<TComponent, ?, ?>>
            extends
            com.vaadin.flow.component.combobox.events.CustomValueSetEvent<TComponent> {
        public CustomValueSetEvent(TComponent source, boolean fromClient,
                String detail) {
            super(source, fromClient, detail);
        }
    }

    /**
     * @deprecated This class is not used in any API, and will be removed in a
     *             future major version.
     * @param <TComponent>
     *            The specific combo box component type
     */
    public static class SelectedItemChangeEvent<TComponent extends ComboBoxBase<TComponent, ?, ?>>
            extends ComponentEvent<TComponent> {
        private final JsonObject selectedItem;

        public SelectedItemChangeEvent(TComponent source, boolean fromClient) {
            super(source, fromClient);
            this.selectedItem = (JsonObject) source.getElement()
                    .getPropertyRaw("selectedItem");
        }

        public JsonObject getSelectedItem() {
            return selectedItem;
        }
    }

    /**
     * @deprecated This class is not used in any API, and will be removed in a
     *             future major version.
     * @param <TComponent>
     *            The specific combo box component type
     */
    public static class OpenedChangeEvent<TComponent extends ComboBoxBase<TComponent, ?, ?>>
            extends ComponentEvent<TComponent> {
        private final boolean opened;

        public OpenedChangeEvent(TComponent source, boolean fromClient) {
            super(source, fromClient);
            this.opened = source.isOpened();
        }

        public boolean isOpened() {
            return opened;
        }
    }

    /**
     * @deprecated This class is not used in any API, and will be removed in a
     *             future major version.
     */
    public static class FilterChangeEvent<R extends ComboBoxBase<R, ?, ?>>
            extends ComponentEvent<R> {
        private final String filter;

        public FilterChangeEvent(R source, boolean fromClient) {
            super(source, fromClient);
            this.filter = source.getFilter();
        }

        public String getFilter() {
            return filter;
        }
    }

    /**
     * @deprecated This class is not used in any API, and will be removed in a
     *             future major version.
     */
    public static class InvalidChangeEvent<R extends ComboBoxBase<R, ?, ?>>
            extends ComponentEvent<R> {
        private final boolean invalid;

        public InvalidChangeEvent(R source, boolean fromClient) {
            super(source, fromClient);
            this.invalid = source.isInvalid();
        }

        public boolean isInvalid() {
            return invalid;
        }
    }
}
