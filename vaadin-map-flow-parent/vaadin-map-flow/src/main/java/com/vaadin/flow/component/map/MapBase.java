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
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.events.MapFeatureClickEvent;
import com.vaadin.flow.component.map.configuration.Extent;
import com.vaadin.flow.component.map.events.MapClickEvent;
import com.vaadin.flow.component.map.events.MapViewMoveEndEvent;
import com.vaadin.flow.component.map.serialization.MapSerializer;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonValue;

import java.beans.PropertyChangeEvent;
import java.util.Objects;

public abstract class MapBase extends Component implements HasSize {
    private final Configuration configuration;
    private final View view;
    private final MapSerializer serializer;

    private StateTree.ExecutionRegistration pendingConfigurationSync;
    private StateTree.ExecutionRegistration pendingViewSync;

    protected MapBase() {
        this.configuration = new Configuration();
        this.view = new View();
        this.serializer = new MapSerializer();
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
        JsonValue jsonConfiguration = serializer.toJson(configuration);

        this.getElement().executeJs("this.$connector.synchronize($0)",
                jsonConfiguration);
    }

    private void synchronizeView() {
        JsonValue jsonView = serializer.toJson(view);

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

    private void registerEventListeners() {
        // Register an event listener before all the other listeners of the view
        // move end event to update view state to the latest values received
        // from the client
        addViewMoveEndEventListener(event -> {
            float rotation = event.getRotation();
            float zoom = event.getZoom();
            Coordinate center = event.getCenter();
            Extent extent = event.getExtent();
            getView().updateInternalViewState(center, rotation, zoom, extent);
        });
    }

    /**
     * Adds event listener for OpenLayers' "moveend" event.
     *
     * @param listener
     * @return a registration object for removing the added listener
     */
    public Registration addViewMoveEndEventListener(
            ComponentEventListener<MapViewMoveEndEvent> listener) {
        return addListener(MapViewMoveEndEvent.class, listener);
    }

    /**
     * Adds event listener for OpenLayers' @code{click} event.
     *
     * @param listener
     * @return a registration object for removing the added listener
     */
    public Registration addClickEventListener(
            ComponentEventListener<MapClickEvent> listener) {
        return addListener(MapClickEvent.class, listener);
    }

    /**
     * Adds a click listener for geographical features. The listener will be
     * invoked for a click on any feature in the specified layer. For clicks on
     * overlapping features, the listener will be invoked individually for each
     * feature at the clicked position.
     *
     * @param listener
     *            the listener to trigger
     * @return registration for the listener
     * @see com.vaadin.flow.component.map.configuration.Feature
     */
    public Registration addFeatureClickListener(VectorLayer layer,
            ComponentEventListener<MapFeatureClickEvent> listener) {
        return addListener(MapFeatureClickEvent.class, event -> {
            // Filter events for specified layer
            if (!Objects.equals(layer, event.getLayer()))
                return;
            listener.onComponentEvent(event);
        });
    }

    /**
     * Adds a click listener for geographical features. The listener will be
     * invoked for a click on any feature, in any layer. To listen for feature
     * clicks in a specific layer, see
     * {@link #addFeatureClickListener(VectorLayer, ComponentEventListener)}.
     * For clicks on overlapping features, the listener will be invoked
     * individually for each feature at the clicked position.
     *
     * @param listener
     *            the listener to trigger
     * @return registration for the listener
     * @see com.vaadin.flow.component.map.configuration.Feature
     */
    public Registration addFeatureClickListener(
            ComponentEventListener<MapFeatureClickEvent> listener) {
        return addListener(MapFeatureClickEvent.class, listener);
    }

    /**
     * Checks whether the map component feature flag is active. Succeeds if the
     * flag is enabled, and throws otherwise.
     *
     * @throws ExperimentalFeatureException
     *             when the {@link FeatureFlags#MAP_COMPONENT} feature is not
     *             enabled
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
