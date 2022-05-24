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

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.dom.Element;

/**
 * Mixin interface for {@link Grid} columns.
 *
 * @param <T>
 *            the subclass type
 *
 * @author Vaadin Ltd.
 */
interface ColumnBase<T extends ColumnBase<T>> extends HasElement {

    /**
     * When set to {@code true}, the column is user-resizable. By default this
     * is set to {@code false}.
     *
     * @param resizable
     *            whether to allow user resizing of this column
     * @return this column, for method chaining
     */
    @SuppressWarnings("unchecked")
    default T setResizable(boolean resizable) {
        getElement().setProperty("resizable", resizable);
        return (T) this;
    }

    /**
     * Gets whether this column is user-resizable.
     *
     * @return whether this column is user-resizable
     */
    @Synchronize("resizable-changed")
    default boolean isResizable() {
        return getElement().getProperty("resizable", false);
    }

    /**
     * Sets this column's frozen state.
     * <p>
     * <strong>Note:</strong> Columns are frozen in-place, freeze columns from
     * left to right for a consistent outcome.
     *
     * @param frozen
     *            whether to freeze or unfreeze this column
     * @return this column, for method chaining
     */
    @SuppressWarnings("unchecked")
    default T setFrozen(boolean frozen) {
        getElement().setProperty("frozen", frozen);
        return (T) this;
    }

    /**
     * Gets the this column's frozen state.
     *
     * @return whether this column is frozen
     */
    @Synchronize("frozen-changed")
    default boolean isFrozen() {
        return getElement().getProperty("frozen", false);
    }

    /**
     * Sets this column's frozen state.
     * <p>
     * <strong>Note:</strong> Columns are frozen in-place, freeze columns from
     * right to left for a consistent outcome.
     *
     * @param frozenToEnd
     *            whether to freeze or unfreeze this column
     * @return this column, for method chaining
     * @since 23.1
     */
    @SuppressWarnings("unchecked")
    default T setFrozenToEnd(boolean frozenToEnd) {
        getElement().setProperty("frozenToEnd", frozenToEnd);
        return (T) this;
    }

    /**
     * Gets the this column's frozen state.
     *
     * @return whether this column is frozen to end
     * @since 23.1
     */
    @Synchronize("frozen-to-end-changed")
    default boolean isFrozenToEnd() {
        return getElement().getProperty("frozenToEnd", false);
    }

    /**
     * Sets the column text align.
     *
     * @param textAlign
     *            the text alignment of the column. Setting it to
     *            <code>null</code> resets the alignment to the default value
     *            {@link ColumnTextAlign#START}.
     * @return this column, for method chaining
     */
    @SuppressWarnings("unchecked")
    default T setTextAlign(ColumnTextAlign textAlign) {
        getElement().setProperty("textAlign",
                textAlign == null ? null : textAlign.getPropertyValue());
        return (T) this;
    }

    /**
     * Gets the column text align. The default is {@link ColumnTextAlign#START}.
     *
     * @return the column text align, not <code>null</code>
     */
    @Synchronize("text-align-changed")
    default ColumnTextAlign getTextAlign() {
        return ColumnTextAlign
                .fromPropertyValue(getElement().getProperty("textAlign"));
    }

    /**
     * Gets the underlying column element.
     * <p>
     * <strong>It is highly discouraged to directly use the API exposed by the
     * returned element.</strong>
     *
     * @return the root element of this component
     */
    @Override
    Element getElement();
}
