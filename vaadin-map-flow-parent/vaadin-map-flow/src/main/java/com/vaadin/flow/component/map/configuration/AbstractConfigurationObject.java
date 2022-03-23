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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vaadin.flow.component.map.configuration.layer.Layer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Base class for all map configuration objects that represent an OL class. The
 * class provides functionality to support the synchronization mechanism between
 * server and client, such as:
 * <ul>
 * <li>generates a unique ID for each object, using {@link UUID}</li>
 * <li>implements change tracking / dirty checking using {@link #markAsDirty()}
 * and {@link #collectChanges(Consumer)}</li>
 * <li>implements the observable pattern using {@link PropertyChangeSupport}, in
 * order to notify the map component about changes to the configuration, see
 * {@link #notifyChange()}</li>
 * <li>keeps track of, and notifies about changes to nested configuration
 * objects, see {@link #addChild(AbstractConfigurationObject)} and
 * {@link #removeChild(AbstractConfigurationObject)}</li>
 * </ul>
 * <p>
 * When adding new API to the Map component using this class, there are several
 * rules to follow:
 * <ul>
 * <li>Implement {@link #getType()} to return a unique type name. This type name
 * is used by the client-side synchronization to determine which OpenLayers
 * class to instantiate for objects of this type. See
 * {@code META-INF/resources/frontend/vaadin-map/synchronization/index.js} for
 * how the synchronization resolves type names.</li>
 * <li>Every setter must call {@link #markAsDirty()} in order to mark this
 * object as changed, and to trigger a change event to schedule a sync. of this
 * change. See {@link View#setCenter(Coordinate)} for an example.</li>
 * <li>Setters for nested objects must keep track of nested references using
 * {@link #addChild(AbstractConfigurationObject)} and
 * {@link #removeChild(AbstractConfigurationObject)}. See
 * {@link Configuration#setView(View)} for an example.</li>
 * <li>When using collection properties, do not expose the collection directly
 * for manipulation, as manipulating the collection does not mark the object as
 * changed, and does not trigger a change event. Instead add methods for
 * manipulating the collection, and keep track of objects being added / removed
 * from the collection using {@link #addChild(AbstractConfigurationObject)} and
 * {@link #removeChild(AbstractConfigurationObject)}. See
 * {@link Configuration#addLayer(Layer)} and
 * {@link Configuration#removeLayer(Layer)} for an example.</li>
 * <li>For properties that contain nested configuration objects, or collections,
 * use the Jackson {@link JsonIdentityInfo} and {@link JsonIdentityReference}
 * annotations to only serialize the ID of the object. See
 * {@link Configuration#getLayers()} for an example.</li>
 * <li>For properties that are not needed on the client-side, or do not have a
 * pendant in the OpenLayers API, use the Jackson {@link JsonIgnore} annotation
 * to reduce the JSON payload that is sent to the client on each
 * synchronization, and to prevent possible errors by passing unrecognized
 * options to the OpenLayers API. See {@link View#getExtent()} for an
 * example.</li>
 * <li>Not every class used in configuring the map necessarily needs to extend
 * from {@link AbstractConfigurationObject}. Using this class is only necessary
 * if an object is supposed to be modified by the developer (e.g. a Layer should
 * be modifiable to change its visibility). If the object is small, or can be
 * immutable, it might make sense to not extend from this class, and instead
 * force the developer to create new instances instead. See {@link Coordinate}
 * for an example, where making coordinates modifiable / synchronizable would
 * just add more overhead, and where it's reasonable to just create new
 * instances instead.</li>
 * </ul>
 */
public abstract class AbstractConfigurationObject implements Serializable {

    private String id;
    private boolean dirty;
    private static final ThreadLocal<Boolean> trackObjectChanges = ThreadLocal
            .withInitial(() -> true);
    private final Set<AbstractConfigurationObject> children = new LinkedHashSet<>();

    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
            this);

    public AbstractConfigurationObject() {
        this.id = UUID.randomUUID().toString();
        this.dirty = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The unique type name of this class. Used by the client-side
     * synchronization mechanism to determine which OpenLayers class to
     * synchronize into.
     */
    public abstract String getType();

    /**
     * Marks this configuration object as dirty / as changed, so that it will be
     * picked up for the next synchronization.
     * <p>
     * This also triggers {@link #notifyChange()} to notify observers that a
     * change happened.
     */
    protected void markAsDirty() {
        if (!trackObjectChanges.get())
            return;
        dirty = true;
        notifyChange();
    }

    /**
     * Marks this configuration object, as well as all nested objects, as dirty
     * / as changed, so that the full nested hierarchy will be picked up for the
     * next synchronization.
     * <p>
     * Unlike {@link #markAsDirty()} this does not trigger
     * {@link #notifyChange()}. Currently, there are limited use-cases for this
     * method, and in all of them a change event, or a map synchronization, will
     * already be triggered through other means. Triggering a change event in
     * this method would lead to recursively triggering change events from all
     * nested objects, each of which would then bubble up through the hierarchy
     * again, which seems wasteful and is unnecessary at the moment. If another
     * use-case comes up in the future, consider just calling
     * {@link #notifyChange()} manually after this method.
     */
    protected void deepMarkAsDirty() {
        if (!trackObjectChanges.get())
            return;
        dirty = true;
        children.forEach(AbstractConfigurationObject::deepMarkAsDirty);
    }

    /**
     * Adds a nested object reference to keep track of. This adds the object to
     * an internal set that is used when collecting changed / dirty objects for
     * the next synchronization, and adds a change listener to the nested object
     * in order to let change events bubble up the configuration hierarchy. This
     * method also automatically marks this object as dirty, and triggers a
     * change event to notify observers about changes. One special behavior of
     * this method is that it will trigger a full sync of the nested hierarchy
     * that was added, in order to ensure that all added references can be
     * resolved on the client-side.
     */
    protected void addChild(AbstractConfigurationObject configurationObject) {
        children.add(configurationObject);
        configurationObject.addPropertyChangeListener(this::notifyChange);
        markAsDirty();
        // When adding a sub-hierarchy, we need to make sure that the client
        // receives the whole hierarchy. Otherwise objects that have been synced
        // before, removed, and then added again, might not be in the
        // client-side reference lookup anymore, due to the client removing
        // references from the lookup during garbage collection.
        configurationObject.deepMarkAsDirty();
    }

    /**
     * Removes a nested object reference from tracking. This removes the object
     * from the internal set used for collecting changes, and removes the change
     * listener on it. This method also automatically marks this object as
     * dirty, and triggers a change event to notify observers about changes.
     */
    protected void removeChild(
            AbstractConfigurationObject configurationObject) {
        if (configurationObject == null)
            return;
        children.remove(configurationObject);
        configurationObject.removePropertyChangeListener(this::notifyChange);
        markAsDirty();
    }

    /**
     * Notifies observers that this object has changed. Usually there is no need
     * to use this directly, instead {@link #markAsDirty()},
     * {@link #addChild(AbstractConfigurationObject)}, or
     * {@link #removeChild(AbstractConfigurationObject)} should be used.
     */
    protected void notifyChange() {
        if (!trackObjectChanges.get())
            return;
        propertyChangeSupport.firePropertyChange("property", null, null);
    }

    /**
     * Same behavior as {@link #notifyChange()}, can be used as a shortcut to
     * relay events from nested objects.
     */
    protected void notifyChange(PropertyChangeEvent event) {
        if (!trackObjectChanges.get())
            return;
        propertyChangeSupport.firePropertyChange("property", null, null);
    }

    /**
     * Adds a change listener to the object. This will be called on any change
     * made to the object that results in a call to {@link #notifyChange()}.
     */
    protected void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a change listener from the object.
     */
    protected void removePropertyChangeListener(
            PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Updates an object using a {@link Runnable} that executes code for
     * manipulating this object. The method has a parameter for controlling
     * whether the manipulations from the runnable should trigger change events,
     * and mark the object as dirty. This can be useful to prevent change events
     * and resulting synchronizations when updating the server-side with state
     * from the client. See
     * {@link View#updateInternalViewState(Coordinate, float, float, Extent)}
     * for an example.
     *
     * @param updater
     *            a runnable containing code to manipulate this object
     * @param trackObjectChanges
     *            whether to enable or disable change tracking when executing
     *            the runnable
     */
    protected void update(Runnable updater, boolean trackObjectChanges) {
        AbstractConfigurationObject.trackObjectChanges.set(trackObjectChanges);
        try {
            updater.run();
        } finally {
            AbstractConfigurationObject.trackObjectChanges.remove();
        }
    }

    /**
     * Collects all changed objects from a configuration hierarchy. If this
     * object has been marked as dirty / changed, then it will be collected, and
     * then marked as non-dirty / unchanged. Additionally, all nested objects
     * are also checked, resulting in a recursive collection of changes. It is
     * important that nested objects are collected first, so that during the
     * client-side sync these instances are created and updated first, before
     * higher-level instances that reference them.
     */
    protected void collectChanges(
            Consumer<AbstractConfigurationObject> changeCollector) {
        children.forEach(child -> child.collectChanges(changeCollector));
        if (dirty) {
            changeCollector.accept(this);
            dirty = false;
        }
    }
}
