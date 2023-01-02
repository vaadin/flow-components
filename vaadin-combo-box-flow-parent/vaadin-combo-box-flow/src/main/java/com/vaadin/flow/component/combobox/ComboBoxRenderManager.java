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
package com.vaadin.flow.component.combobox;

import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Internal class for managing rendering related logic for combo box components
 *
 * @param <TItem>
 *            Type of individual items that are selectable in the combo box
 */
class ComboBoxRenderManager<TItem> implements Serializable {

    private final ComboBoxBase<?, TItem, ?> comboBox;
    private Renderer<TItem> renderer;

    private boolean renderScheduled;
    private final List<Registration> renderingRegistrations = new ArrayList<>();
    private Element template;

    ComboBoxRenderManager(ComboBoxBase<?, TItem, ?> comboBox) {
        this.comboBox = comboBox;
    }

    void setRenderer(Renderer<TItem> renderer) {
        Objects.requireNonNull(renderer, "The renderer must not be null");
        this.renderer = renderer;

        scheduleRender();
    }

    void scheduleRender() {
        if (renderScheduled || comboBox.getDataCommunicator() == null
                || renderer == null) {
            return;
        }
        renderScheduled = true;
        comboBox.runBeforeClientResponse(ui -> {
            render();
            renderScheduled = false;
        });
    }

    private void render() {
        renderingRegistrations.forEach(Registration::remove);
        renderingRegistrations.clear();

        Rendering<TItem> rendering;
        if (renderer instanceof LitRenderer) {
            // LitRenderer
            if (template != null && template.getParent() != null) {
                comboBox.getElement().removeChild(template);
            }
            rendering = renderer.render(comboBox.getElement(),
                    comboBox.getDataCommunicator().getKeyMapper());
        } else {
            // TemplateRenderer
            if (template == null) {
                template = new Element("template");
            }
            if (template.getParent() == null) {
                comboBox.getElement().appendChild(template);
            }
            rendering = renderer.render(comboBox.getElement(),
                    comboBox.getDataCommunicator().getKeyMapper(), template);
        }

        rendering.getDataGenerator().ifPresent(renderingDataGenerator -> {
            Registration renderingDataGeneratorRegistration = comboBox
                    .getDataGenerator()
                    .addDataGenerator(renderingDataGenerator);
            renderingRegistrations.add(renderingDataGeneratorRegistration);
        });

        renderingRegistrations.add(rendering.getRegistration());

        comboBox.getDataController().reset();
    }
}
