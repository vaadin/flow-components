/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.data.renderer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.function.ValueProvider;

/**
 *
 * Abstract renderer used as the base implementation for renderers that outputs
 * a simple value in the UI, such as {@link NumberRenderer} and
 * {@link LocalDateRenderer}.
 *
 * For components that use a client-side renderer, such as {@code Grid} or
 * {@code ComboBox}, the {@code BasicRenderer} works as a {@link LitRenderer}
 * with a preconfigured template. It also implements the
 * {@link ComponentRenderer} API, so components that use renderers server-side
 * to generate content, such as {@code RadioButtonGroup} or {@code ListBox}, can
 * use it as well.
 *
 * @author Vaadin Ltd
 *
 * @param <SOURCE>
 *            the type of the item used inside the renderer
 * @param <TARGET>
 *            the type of the output object, such as Number or LocalDate
 */
public abstract class BasicRenderer<SOURCE, TARGET>
        extends ComponentRenderer<Component, SOURCE> {

    private final ValueProvider<SOURCE, TARGET> valueProvider;

    /**
     * Builds a new renderer using the value provider as the source of values to
     * be rendered.
     *
     * @param valueProvider
     *            the callback to provide a objects to the renderer, not
     *            <code>null</code>
     */
    protected BasicRenderer(ValueProvider<SOURCE, TARGET> valueProvider) {
        super((SerializableSupplier<Component>) null);

        if (valueProvider == null) {
            throw new IllegalArgumentException("valueProvider may not be null");
        }
        this.valueProvider = valueProvider;

        withProperty("label",
                item -> getFormattedValue(valueProvider.apply(item)));
    }

    @Override
    protected String getTemplateExpression() {
        return "${item.label}";
    }

    protected ValueProvider<SOURCE, TARGET> getValueProvider() {
        return valueProvider;
    }

    /**
     * Override to make the method return the {@code Rendering} originating from
     * LitRenderer as it is. BasicRenderer should not render Components for
     * client-side renderers even though it extends a ComponentRenderer.
     *
     * @param rendering
     *            the rendering instance
     * @param keyMapper
     *            the key mapper
     * @return a rendering instance configured for the purposes of this renderer
     */
    @Override
    Rendering<SOURCE> configureRendering(Rendering<SOURCE> rendering,
            DataKeyMapper<SOURCE> keyMapper) {
        return rendering;
    }

    @Override
    public Component createComponent(SOURCE item) {
        Element span = ElementFactory
                .createSpan(getFormattedValue(valueProvider.apply(item)));
        return ComponentUtil.componentFromElement(span, Span.class, true);
    }

    /**
     * Gets the String representation of the target object, to be used inside
     * the template.
     * <p>
     * By default it uses {@link String#valueOf(Object)} of the object.
     *
     * @param object
     *            the target object
     * @return the string representation of the object
     */
    protected String getFormattedValue(TARGET object) {
        return String.valueOf(object);
    }

}
