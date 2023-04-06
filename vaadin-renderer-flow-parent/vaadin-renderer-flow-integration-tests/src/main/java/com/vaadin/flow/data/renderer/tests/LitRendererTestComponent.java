
package com.vaadin.flow.data.renderer.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonValue;

@Tag("lit-renderer-test-component")
@JsModule("lit-renderer-test-component.ts")
public class LitRendererTestComponent extends Div
        implements HasDataProvider<String> {

    private final DataCommunicator<String> dataCommunicator;
    private final CompositeDataGenerator<String> dataGenerator = new CompositeDataGenerator<>();

    private final List<Registration> renderingRegistrations = new ArrayList<>();
    private final List<Registration> detailsRenderingRegistrations = new ArrayList<>();

    private final ArrayUpdater arrayUpdater = new ArrayUpdater() {
        @Override
        public Update startUpdate(int sizeChange) {
            return new Update() {
                @Override
                public void clear(int start, int length) {
                    // not essential for this test
                }

                @Override
                public void set(int start, List<JsonValue> items) {
                    getElement().executeJs("this.items = $0",
                            items.stream().collect(JsonUtils.asArray()));
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

    public LitRendererTestComponent() {
        dataCommunicator = new DataCommunicator<>(dataGenerator, arrayUpdater,
                data -> {
                }, getElement().getNode());
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

    @Override
    public void setItems(Collection<String> items) {
        HasDataProvider.super.setItems(items);
        dataCommunicator.setRequestedRange(0, items.size());
    }

    @Override
    public void setDataProvider(DataProvider<String, ?> dataProvider) {
        dataCommunicator.setDataProvider(dataProvider, null);
    }

}
