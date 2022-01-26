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

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.map.configuration.Configuration;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.View;
import com.vaadin.flow.component.map.configuration.Extent;
import com.vaadin.flow.component.map.events.MapViewMoveEndEvent;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;
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
        registerEventListeners();
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Gets the view of the map. The view gives access to properties like center
     * and zoom level of the viewport.
     *
     * @return the map's view
     */
    public View getView() {
        return view;
    }

    public void render() {
        this.requestConfigurationSync();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag();
        getElement().executeJs("window.Vaadin.Flow.mapConnector.init(this)");
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

        this.getElement().executeJs("this.$connector.synchronize($0)",
                jsonConfiguration);
    }

    private void synchronizeView() {
        JsonObject jsonView = (JsonObject) JsonSerializer.toJson(view);

        this.getElement().executeJs(
                "this.$connector.synchronize($0, this.configuration.getView())",
                jsonView);
    }

    private void configurationPropertyChange(PropertyChangeEvent e) {
        this.requestConfigurationSync();
    }

    private void viewPropertyChange(PropertyChangeEvent e) {
        this.requestViewSync();
    }

    /**
     * Register an event listener before all the other listeners of this event
     * to update view state data to latest values received from the client.
     */
    private void registerEventListeners() {
        addViewMoveEndEventListener(event -> {
            float rotation = event.getRotation();
            float zoom = event.getZoom();
            Coordinate center = event.getCenter();
            Extent extent = event.getExtent();
            getView().update(() -> getView().updateInternalViewState(center,
                    rotation, zoom, extent), false);
        });
    }

    public Registration addViewMoveEndEventListener(
            ComponentEventListener<MapViewMoveEndEvent> listener) {
        return addListener(MapViewMoveEndEvent.class, listener);
    }

    /**
     * Checks whether the map component feature flag is active. Succeeds if the
     * flag is enabled, and throws otherwise.
     *
     * @throws ExperimentalFeatureException when the {@link FeatureFlags#MAP_COMPONENT} feature is not
     *                                      enabled
     */
    private void checkFeatureFlag() {
        boolean enabled = getFeatureFlags()
                .isEnabled(FeatureFlags.MAP_COMPONENT);

        if (!enabled) {
            throw new ExperimentalFeatureException();
        }
    }

    /**
     * Gets the feature flags for the current UI.
     * <p>
     * Extracted with protected visibility to support mocking
     *
     * @return the current set of feature flags
     */
    protected FeatureFlags getFeatureFlags() {
        return FeatureFlags
                .get(UI.getCurrent().getSession().getService().getContext());
    }
}
