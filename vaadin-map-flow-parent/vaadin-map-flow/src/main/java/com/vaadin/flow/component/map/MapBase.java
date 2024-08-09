/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map;

import java.beans.PropertyChangeEvent;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.Configuration;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.Extent;
import com.vaadin.flow.component.map.configuration.Feature;
import com.vaadin.flow.component.map.configuration.View;
import com.vaadin.flow.component.map.configuration.layer.VectorLayer;
import com.vaadin.flow.component.map.events.MapClickEvent;
import com.vaadin.flow.component.map.events.MapFeatureClickEvent;
import com.vaadin.flow.component.map.events.MapFeatureDropEvent;
import com.vaadin.flow.component.map.events.MapViewMoveEndEvent;
import com.vaadin.flow.component.map.serialization.MapSerializer;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonValue;

/**
 * Base class for the map component. Contains all base functionality for the map
 * component, but does not provide any defaults. This component should not be
 * used directly, instead use {@link Map}, which also provides some
 * out-of-the-box conveniences such as a pre-configured background layer, and a
 * feature layer.
 */
public abstract class MapBase extends Component
        implements HasSize, HasStyle, HasThemeVariant<MapVariant> {
    private final Configuration configuration;
    private final MapSerializer serializer;

    private StateTree.ExecutionRegistration pendingConfigurationSync;

    protected MapBase() {
        this.serializer = new MapSerializer();
        this.configuration = new Configuration();
        this.configuration
                .addPropertyChangeListener(this::configurationPropertyChange);
        registerEventListeners();
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Gets the {@link View} of the map. The view allows controlling properties
     * of the map's viewport, such as center, zoom level and rotation.
     *
     * @return the map's view
     */
    public View getView() {
        return configuration.getView();
    }

    /**
     * Sets the view of the map. This is only necessary when dealing with map
     * services that use custom coordinate projection, in which case a view with
     * a matching projection needs to be created and used.
     *
     * @param view
     *            the new view
     */
    public void setView(View view) {
        configuration.setView(view);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        getElement().executeJs("window.Vaadin.Flow.mapConnector.init(this)");
        // Ensure the full configuration is synced when (re-)attaching the
        // component
        configuration.deepMarkAsDirty();
        requestConfigurationSync();
    }

    /**
     * Schedules a configuration sync, if there isn't a scheduled sync already
     */
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

    /**
     * Synchronize the map configuration to the client-side, into OpenLayers
     * class instances
     */
    private void synchronizeConfiguration() {
        // Use a linked hash set to prevent object duplicates, but guarantee
        // that the changes are synchronized in the order that they were added
        // to the set
        Set<AbstractConfigurationObject> changedObjects = new LinkedHashSet<>();
        configuration.collectChanges(changedObjects::add);

        JsonValue jsonChanges = serializer.toJson(changedObjects);

        this.getElement().executeJs("this.$connector.synchronize($0)",
                jsonChanges);
    }

    private void configurationPropertyChange(PropertyChangeEvent e) {
        this.requestConfigurationSync();
    }

    private void registerEventListeners() {
        // Register an event listener before all the other listeners of the view
        // move end event to update view state to the latest values received
        // from the client
        addViewMoveEndEventListener(event -> {
            double rotation = event.getRotation();
            double zoom = event.getZoom();
            Coordinate center = event.getCenter();
            Extent extent = event.getExtent();
            getView().updateInternalViewState(center, rotation, zoom, extent);
        });
        // Register an event listener before all the other listeners of the
        // feature drop event to update the feature's position
        addFeatureDropListener(event -> {
            double deltaX = event.getCoordinate().getX()
                    - event.getStartCoordinate().getX();
            double deltaY = event.getCoordinate().getY()
                    - event.getStartCoordinate().getY();

            if (event.getFeature() != null
                    && event.getFeature().getGeometry() != null) {
                event.getFeature().getGeometry().translate(deltaX, deltaY);
            }
        });
    }

    /**
     * Adds an event listener for changes to the map's viewport. The event will
     * only be triggered after the user has finished manipulating the viewport,
     * for example after letting go of the mouse button after a mouse drag
     * interaction.
     *
     * @param listener
     * @return a registration object for removing the added listener
     */
    public Registration addViewMoveEndEventListener(
            ComponentEventListener<MapViewMoveEndEvent> listener) {
        return addListener(MapViewMoveEndEvent.class, listener);
    }

    /**
     * Adds a click listener for the map.
     * <p>
     * Note that the listener will also be invoked when clicking on a
     * {@link Feature}. Use {@link MapClickEvent#getFeatures()} to distinguish
     * whether a feature exists at the clicked location.
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
     * overlapping features, the listener will be invoked only for the top-level
     * feature at that location.
     *
     * @param listener
     *            the listener to trigger
     * @return registration for the listener
     * @see Feature
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
     * For clicks on overlapping features, the listener will be invoked only for
     * the top-level feature at that location.
     *
     * @param listener
     *            the listener to trigger
     * @return registration for the listener
     * @see Feature
     */
    public Registration addFeatureClickListener(
            ComponentEventListener<MapFeatureClickEvent> listener) {
        return addListener(MapFeatureClickEvent.class, listener);
    }

    /**
     * Adds an event listener for when a feature is dropped after a drag
     * operation. Features can be made draggable by setting
     * {@link Feature#setDraggable(boolean)}.
     *
     * @param listener
     *            the listener to trigger
     * @return registration for the listener
     * @see Feature
     */
    public Registration addFeatureDropListener(
            ComponentEventListener<MapFeatureDropEvent> listener) {
        return addListener(MapFeatureDropEvent.class, listener);
    }
}
