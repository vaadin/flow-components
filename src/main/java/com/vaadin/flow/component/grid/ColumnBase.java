/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import com.vaadin.flow.internal.HtmlUtils;
import com.vaadin.flow.renderer.TemplateRenderer;

/**
 * Mixin interface for {@link Grid} columns.
 *
 * @param <T>
 *            the subclass type
 *
 * @author Vaadin Ltd.
 */
public interface ColumnBase<T extends ColumnBase<T>> extends HasElement {

    /**
     * When set to {@code true}, the column is user-resizable. By default this
     * is set to {@code false}.
     *
     * @param resizable
     *            whether to allow user resizing of this column
     * @return this column, for method chaining
     */
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
     * Sets a header text to the column.
     *
     * @param labelText
     *            the text to be shown at the column header
     * @return this column, for method chaining
     */
    default T setHeader(String labelText) {
        setHeader(TemplateRenderer.of(HtmlUtils.escape(labelText)));
        return (T) this;
    }

    /**
     * Sets a header template to the column.
     *
     * @param renderer
     *            the template renderer to be used to render the header of the
     *            column
     * @return this column, for method chaining
     */
    T setHeader(TemplateRenderer<?> renderer);

    /**
     * Sets a footer text to the column.
     *
     * @param labelText
     *            the text to be shown at the column footer
     * @return this column, for method chaining
     */
    default T setFooter(String labelText) {
        setFooter(TemplateRenderer.of(HtmlUtils.escape(labelText)));
        return (T) this;
    }

    /**
     * Sets a footer template to the column.
     *
     * @param renderer
     *            the template renderer to be used to render the footer of the
     *            column
     * @return this column, for method chaining
     */
    T setFooter(TemplateRenderer<?> renderer);

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
