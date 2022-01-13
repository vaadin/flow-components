package com.vaadin.flow.component.map;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.map.configuration.Configuration;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.internal.StateTree;
import elemental.json.JsonObject;

@Tag("vaadin-map")
// TODO: Enable once released
// @NpmPackage(value = "@vaadin/map", version = "23.0.0-alpha4")
// TODO: Include non-themed module `@vaadin/map/src/vaadin-map.js` when theme module is ready
@JsModule("@vaadin/map/vaadin-map.js")
public class Map extends Component implements HasSize {

    private Configuration configuration;

    private StateTree.ExecutionRegistration pendingSynchronization;

    public Map() {
        this.configuration = new Configuration();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void render() {
        this.requestConfigurationSync();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        requestConfigurationSync();
    }

    private void requestConfigurationSync() {
        getUI().ifPresent(ui -> {
            if (pendingSynchronization != null) {
                pendingSynchronization.remove();
            }
            pendingSynchronization = ui.beforeClientResponse(this, context -> {
                pendingSynchronization = null;
                synchronizeConfiguration();
            });
        });
    }

    private void synchronizeConfiguration() {
        JsonObject jsonConfiguration = (JsonObject) JsonSerializer.toJson(configuration);

        this.getElement().executeJs("this.updateJson($0)", jsonConfiguration);
    }
}
