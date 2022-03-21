package com.vaadin.flow.component.map.configuration;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.configuration.layer.Layer;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Contains the configuration for the map, such as layers, sources, features.
 */
public class Configuration extends AbstractConfigurationObject {
    private final List<Layer> layers = new ArrayList<>();
    private View view;

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
}
