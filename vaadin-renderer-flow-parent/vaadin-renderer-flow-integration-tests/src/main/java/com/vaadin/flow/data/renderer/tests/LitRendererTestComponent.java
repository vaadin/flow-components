/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.shared.Registration;

@Tag("lit-renderer-test-component")
@JsModule("lit-renderer-test-component.js")
public class LitRendererTestComponent extends Div {
    private final DataCommunicator<String> dataCommunicator;
    private final CompositeDataGenerator<String> dataGenerator;

    private final List<Registration> renderingRegistrations = new ArrayList<>();
    private final List<Registration> detailsRenderingRegistrations = new ArrayList<>();

    public LitRendererTestComponent(DataCommunicator<String> dataCommunicator,
            CompositeDataGenerator<String> dataGenerator) {
        this.dataCommunicator = dataCommunicator;
        this.dataGenerator = dataGenerator;
    }

    public void setRenderer(LitRenderer<String> renderer) {
        renderingRegistrations.forEach(Registration::remove);
        renderingRegistrations.clear();

        if (renderer != null) {
            Rendering<String> rendering = renderer.render(getElement(),
                    dataCommunicator.getKeyMapper());
            renderingRegistrations.add(rendering.getRegistration());
            rendering.getDataGenerator()
                    .ifPresent(generator -> renderingRegistrations
                            .add(dataGenerator.addDataGenerator(generator)));
            dataCommunicator.reset();
        }
    }

    public void setDetailsRenderer(LitRenderer<String> renderer) {
        detailsRenderingRegistrations.forEach(Registration::remove);
        detailsRenderingRegistrations.clear();

        if (renderer != null) {
            Rendering<String> rendering = renderer.render(getElement(),
                    dataCommunicator.getKeyMapper(), "detailsRenderer");
            detailsRenderingRegistrations.add(rendering.getRegistration());
            rendering.getDataGenerator()
                    .ifPresent(generator -> detailsRenderingRegistrations
                            .add(dataGenerator.addDataGenerator(generator)));
            dataCommunicator.reset();
        }
    }
}
