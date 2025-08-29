/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.data.renderer.tests;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.internal.JacksonUtils;

public class LitRendererTestComponentWrapper extends Div
        implements HasDataProvider<String> {
    private final DataCommunicator<String> dataCommunicator;
    private final CompositeDataGenerator<String> dataGenerator = new CompositeDataGenerator<>();

    private final ArrayUpdater arrayUpdater = new ArrayUpdater() {
        @Override
        public Update startUpdate(int sizeChange) {
            return new Update() {
                @Override
                public void clear(int start, int length) {
                    // not essential for this test
                }

                @Override
                public void set(int start, List<JsonNode> items) {
                    getChildren().forEach((component) -> {
                        component.getElement().executeJs("this.items = $0",
                                items.stream().collect(JacksonUtils.asArray()));
                    });
                }

                @Override
                public void commit(int updateId) {
                    // not essential for this test
                }
            };
        }

        @Override
        public void initialize() {
            // not essential for this test
        }
    };

    /**
     * Creates a wrapper with one LitRendererTestComponent.
     */
    public LitRendererTestComponentWrapper() {
        this(1);
    }

    /**
     * Creates a wrapper with the given number of LitRendererTestComponent.
     *
     * @param childCount
     *            the number of children to add
     */
    public LitRendererTestComponentWrapper(int childCount) {
        dataCommunicator = new DataCommunicator<>(dataGenerator, arrayUpdater,
                data -> {
                }, getElement().getNode());

        for (int i = 0; i < childCount; i++) {
            add(new LitRendererTestComponent(dataCommunicator, dataGenerator));
        }
    }

    /**
     * Sets the renderer for all children.
     */
    public void setRenderer(LitRenderer<String> renderer) {
        getComponents().forEach(component -> {
            component.setRenderer(renderer);
        });
    }

    /**
     * Sets the renderer for the component at the given index.
     */
    public void setRenderer(int componentIndex, LitRenderer<String> renderer) {
        getComponents().get(componentIndex).setRenderer(renderer);
    }

    /**
     * Sets the details renderer for all children.
     */
    public void setDetailsRenderer(LitRenderer<String> renderer) {
        getComponents().forEach(component -> {
            component.setDetailsRenderer(renderer);
        });
    }

    /**
     * Sets the details renderer for the component at the given index.
     */
    public void setDetailsRenderer(int componentIndex,
            LitRenderer<String> renderer) {
        getComponents().get(componentIndex).setDetailsRenderer(renderer);
    }

    private List<LitRendererTestComponent> getComponents() {
        return getChildren().map(LitRendererTestComponent.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public void setItems(Collection<String> items) {
        HasDataProvider.super.setItems(items);
        dataCommunicator.setViewportRange(0, items.size());
    }

    @Override
    public void setDataProvider(DataProvider<String, ?> dataProvider) {
        dataCommunicator.setDataProvider(dataProvider, null);
    }
}
