/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
            // TemplateRenderer or ComponentRenderer
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
