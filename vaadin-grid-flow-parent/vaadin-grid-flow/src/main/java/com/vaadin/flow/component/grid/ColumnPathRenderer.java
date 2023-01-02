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
package com.vaadin.flow.component.grid;

import java.util.Optional;

import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JsonSerializer;

import elemental.json.Json;

/**
 * Renderer for columns that doesn't use a renderer function for rendering its
 * contents (only the value from the object model). In such cases, a
 * {@code renderer} function is not needed on the client-side, only the
 * {@code path} property.
 *
 * @author Vaadin Ltd.
 *
 * @param <SOURCE>
 *            the object model type
 * @see Grid#addColumn(ValueProvider)
 */
public class ColumnPathRenderer<SOURCE> extends Renderer<SOURCE> {

    private ValueProvider<SOURCE, ?> provider;
    private String property;

    /**
     * Creates a new renderer based on the property and the value provider for
     * that property.
     *
     * @param property
     *            the property name
     * @param provider
     *            the value provider for the property
     */
    public ColumnPathRenderer(String property,
            ValueProvider<SOURCE, ?> provider) {
        this.provider = provider;
        this.property = property;
    }

    @Override
    public Rendering<SOURCE> render(Element container,
            DataKeyMapper<SOURCE> keyMapper) {
        container.setProperty("path", property);

        // disables the automatic creation of headers when the path is used
        container.setPropertyJson("header", Json.createNull());

        return new SingleValueProviderRendering();
    }

    private class SingleValueProviderRendering implements Rendering<SOURCE> {

        @Override
        public Optional<DataGenerator<SOURCE>> getDataGenerator() {
            return Optional.of((item, jsonObject) -> jsonObject.put(property,
                    JsonSerializer.toJson(provider.apply(item))));
        }
    }

}
