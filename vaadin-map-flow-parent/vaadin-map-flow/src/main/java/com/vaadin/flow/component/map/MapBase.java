package com.vaadin.flow.component.map;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.map.configuration.Configuration;
import com.vaadin.flow.component.map.configuration.View;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.internal.StateTree;
import elemental.json.JsonObject;

import java.beans.PropertyChangeEvent;

public abstract class MapBase extends Component implements HasSize {
    private Configuration configuration;
    private View view;

    private StateTree.ExecutionRegistration pendingConfigurationSync;
    private StateTree.ExecutionRegistration pendingViewSync;

    protected MapBase() {
        this.configuration = new Configuration();
        this.view = new View();
        this.configuration.addPropertyChangeListener(this::configurationPropertyChange);
        this.view.addPropertyChangeListener(this::viewPropertyChange);
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    public View getView() {
        return view;
    }

    public void render() {
        this.requestConfigurationSync();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        requestConfigurationSync();
        requestViewSync();
    }

    private void requestConfigurationSync() {
        if (pendingConfigurationSync != null) {
            return;
        }
        getUI().ifPresent(ui -> pendingConfigurationSync = ui.beforeClientResponse(this, context -> {
            pendingConfigurationSync = null;
            synchronizeConfiguration();
        }));
    }

    private void requestViewSync() {
        if (pendingViewSync != null) {
            return;
        }
        getUI().ifPresent(ui -> pendingViewSync = ui.beforeClientResponse(this, context -> {
            pendingViewSync = null;
            synchronizeView();
        }));
    }

    private void synchronizeConfiguration() {
        JsonObject jsonConfiguration = (JsonObject) JsonSerializer.toJson(configuration);

        this.getElement().executeJs("this.updateConfigurationJson($0)", jsonConfiguration);
    }

    private void synchronizeView() {
        JsonObject jsonView = (JsonObject) JsonSerializer.toJson(view);

        this.getElement().executeJs("this.updateViewJson($0)", jsonView);
    }

    private void configurationPropertyChange(PropertyChangeEvent e) {
        this.requestConfigurationSync();
    }

    private void viewPropertyChange(PropertyChangeEvent e) {
        this.requestViewSync();
    }
}
