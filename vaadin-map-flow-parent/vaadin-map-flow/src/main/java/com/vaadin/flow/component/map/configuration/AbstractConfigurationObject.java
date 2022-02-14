package com.vaadin.flow.component.map.configuration;

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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractConfigurationObject implements Serializable {

    private String id;
    private boolean dirty;
    private final ThreadLocal<Boolean> notifyChanges = ThreadLocal
            .withInitial(() -> true);
    private final Set<AbstractConfigurationObject> children = new HashSet<>();

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

    public abstract String getType();

    protected void addChild(AbstractConfigurationObject configurationObject) {
        children.add(configurationObject);
        configurationObject.addPropertyChangeListener(this::notifyChange);
        notifyChange();
        // When adding a sub-hierarchy, we need to make sure that the client
        // receives the whole hierarchy. Otherwise objects that have been synced
        // before, removed, and then added again, might not be in the
        // client-side reference lookup anymore, due to the client removing
        // references from the lookup during garbage collection.
        configurationObject.deepMarkAsDirty();
    }

    protected void removeChild(
            AbstractConfigurationObject configurationObject) {
        if (configurationObject == null)
            return;
        children.remove(configurationObject);
        configurationObject.removePropertyChangeListener(this::notifyChange);
        notifyChange();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected void updateNestedPropertyObserver(
            AbstractConfigurationObject oldValue,
            AbstractConfigurationObject newValue) {
        if (oldValue != null) {
            oldValue.removePropertyChangeListener(this::notifyChange);
        }
        if (newValue != null) {
            newValue.addPropertyChangeListener(this::notifyChange);
        }
    }

    protected void deepMarkAsDirty() {
        dirty = true;
        children.forEach(AbstractConfigurationObject::deepMarkAsDirty);
    }

    protected void notifyChange() {
        if (!this.notifyChanges.get())
            return;
        this.dirty = true;
        this.propertyChangeSupport.firePropertyChange("property", null, null);
    }

    protected void notifyChange(PropertyChangeEvent event) {
        if (!this.notifyChanges.get())
            return;
        this.propertyChangeSupport.firePropertyChange("property", null, null);
    }

    public void update(Runnable updater, boolean notifyChanges) {
        this.notifyChanges.set(notifyChanges);
        try {
            updater.run();
        } finally {
            this.notifyChanges.remove();
        }
    }

    public void collectChanges(
            Consumer<AbstractConfigurationObject> changeCollector) {
        children.forEach(child -> child.collectChanges(changeCollector));
        if (this.dirty) {
            changeCollector.accept(this);
            this.dirty = false;
        }
    }
}
