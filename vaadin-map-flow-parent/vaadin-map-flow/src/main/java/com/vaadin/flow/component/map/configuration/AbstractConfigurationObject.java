package com.vaadin.flow.component.map.configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.UUID;

public abstract class AbstractConfigurationObject implements Serializable {

    private String id;

    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public AbstractConfigurationObject() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public abstract String getType();

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected void updateNestedPropertyObserver(AbstractConfigurationObject oldValue, AbstractConfigurationObject newValue) {
        if (oldValue != null) {
            oldValue.removePropertyChangeListener(this::notifyChange);
        }
        if(newValue != null) {
            newValue.addPropertyChangeListener(this::notifyChange);
        }
    }

    protected void notifyChange() {
        this.propertyChangeSupport.firePropertyChange("property", null, null);
    }

    protected void notifyChange(PropertyChangeEvent event) {
        this.propertyChangeSupport.firePropertyChange("property", null, null);
    }
}
