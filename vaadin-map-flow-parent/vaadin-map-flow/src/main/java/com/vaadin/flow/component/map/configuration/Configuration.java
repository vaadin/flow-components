/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.configuration.controls.Control;
import com.vaadin.flow.component.map.configuration.layer.Layer;

/**
 * Contains the configuration for the map, such as layers, sources, features.
 */
public class Configuration extends AbstractConfigurationObject {
    private final List<Layer> layers = new ArrayList<>();
    private final List<Control> controls = new ArrayList<>();
    private View view;

    private final SerializablePropertyChangeListener controlPropertyChangeListener = this::handleControlPropertyChange;

    public Configuration() {
        setView(new View());
    }

    @Override
    public String getType() {
        return Constants.OL_MAP;
    }

    /**
     * The list of layers managed by this map. This returns an immutable list,
     * meaning the list can not be modified. Instead, use
     * {@link #addLayer(Layer)} and {@link #removeLayer(Layer)} to manage the
     * layers of the list.
     *
     * @return the list of layers managed by this map
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public List<Layer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    /**
     * Adds a layer to the map. The layer will be appended to the list of
     * layers, meaning that it will be rendered last / on top of previously
     * added layers by default. For more fine-grained control of the layer
     * rendering order, use {@link Layer#setzIndex(Integer)}.
     *
     * @param layer
     *            the layer to be added
     */
    public void addLayer(Layer layer) {
        Objects.requireNonNull(layer);

        layers.add(layer);
        addChild(layer);
    }

    /**
     * Adds a layer to the map by prepending it to the list of layers. That
     * means that it will be rendered first / behind all other layers by
     * default. Consider using {@link Layer#setzIndex(Integer)} for more
     * fine-grained control of the layer rendering order.
     *
     * @param layer
     *            the layer to be added
     */
    public void prependLayer(Layer layer) {
        Objects.requireNonNull(layer);

        layers.add(0, layer);
        addChild(layer);
    }

    /**
     * Remove a layer from the map
     *
     * @param layer
     *            the layer to be removed
     */
    public void removeLayer(Layer layer) {
        Objects.requireNonNull(layer);

        layers.remove(layer);
        removeChild(layer);
    }

    /**
     * The list of controls added to the map. This returns an immutable list.
     * 
     * @return the list of controls added to the map
     */
    @JsonIgnore
    public List<Control> getControls() {
        return Collections.unmodifiableList(controls);
    }

    /**
     * The list of visible controls added to the map. This returns an immutable
     * list.
     *
     * @return the list of visible controls added to the map
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public List<Control> getVisibleControls() {
        return controls.stream().filter(Control::isVisible).toList();
    }

    /**
     * Adds a control to the map.
     *
     * @param control
     *            the control to be added
     */
    public void addControl(Control control) {
        Objects.requireNonNull(control);

        controls.add(control);
        addChild(control);
        control.addPropertyChangeListener(this.controlPropertyChangeListener);
    }

    /**
     * Removes a control from the map.
     *
     * @param control
     *            the control to be removed
     */
    public void removeControl(Control control) {
        Objects.requireNonNull(control);

        controls.remove(control);
        removeChild(control);
        control.removePropertyChangeListener(
                this.controlPropertyChangeListener);
    }

    /**
     * Gets the view of the map. The view gives access to properties like center
     * and zoom level of the viewport.
     *
     * @return the map's view
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public View getView() {
        return view;
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
        removeChild(this.view);
        this.view = view;
        addChild(view);
    }

    /**
     * For internal use only.
     * <p>
     * Exposes the method to allow the map component to mark the full
     * configuration hierarchy as changed.
     */
    @Override
    public void deepMarkAsDirty() {
        super.deepMarkAsDirty();
    }

    /**
     * For internal use only.
     * <p>
     * Exposes the method to allow the map component to listen for changes to
     * the configuration.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
    }

    /**
     * For internal use only.
     * <p>
     * Exposes the method to allow the map component to collect changes from the
     * configuration.
     */
    @Override
    public void collectChanges(
            Consumer<AbstractConfigurationObject> changeCollector) {
        super.collectChanges(changeCollector);
    }

    private void handleControlPropertyChange(PropertyChangeEvent event) {
        // When visibility of a control changes, resync the configuration itself
        // to send an updated list of visible controls to the client
        if ("visible".equals(event.getPropertyName())) {
            markAsDirty();
        }
    }
}
