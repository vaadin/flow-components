package com.vaadin.flow.component.map;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright (C) 2022 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
        this.configuration
                .addPropertyChangeListener(this::configurationPropertyChange);
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
        getUI().ifPresent(ui -> pendingConfigurationSync = ui
                .beforeClientResponse(this, context -> {
                    pendingConfigurationSync = null;
                    synchronizeConfiguration();
                }));
    }

    private void requestViewSync() {
        if (pendingViewSync != null) {
            return;
        }
        getUI().ifPresent(ui -> pendingViewSync = ui.beforeClientResponse(this,
                context -> {
                    pendingViewSync = null;
                    synchronizeView();
                }));
    }

    private void synchronizeConfiguration() {
        JsonObject jsonConfiguration = (JsonObject) JsonSerializer
                .toJson(configuration);

        this.getElement().executeJs("this.synchronize($0)", jsonConfiguration);
    }

    private void synchronizeView() {
        JsonObject jsonView = (JsonObject) JsonSerializer.toJson(view);

        this.getElement().executeJs(
                "this.synchronize($0, this.configuration.getView())", jsonView);
    }

    private void configurationPropertyChange(PropertyChangeEvent e) {
        this.requestConfigurationSync();
    }

    private void viewPropertyChange(PropertyChangeEvent e) {
        this.requestViewSync();
    }
}
