/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Abstract base class for model classes to be serialized to JSON. Mainly
 * com.vaadin.flow.component.charts.model.Configuration and the stuff it uses.
 * <p>
 * When the experimental {@code reactiveCharts} feature flag is enabled,
 * subclasses can opt in to reactive synchronization by calling
 * {@link #markAsDirty()} from their setters and by wiring nested objects with
 * {@link #addChild(AbstractConfigurationObject)} and
 * {@link #removeChild(AbstractConfigurationObject)}. The infrastructure is
 * dormant unless the owning {@code Chart} installed a reactive sync trigger on
 * the root {@code Configuration}; therefore these methods add no behavior when
 * the flag is off.
 */
public abstract class AbstractConfigurationObject implements Serializable {

    @JsonIgnore
    private transient AbstractConfigurationObject parent;

    /**
     * Walks up the parent chain so the root {@code Configuration} can observe
     * that something below it changed.
     */
    protected void markAsDirty() {
        if (parent != null) {
            parent.markAsDirty();
        }
    }

    /**
     * Registers a nested object so dirty notifications from it reach this one.
     * The actual "dirty" state lives only on the root; intermediate nodes are
     * pure relay.
     */
    protected void addChild(AbstractConfigurationObject child) {
        if (child == null) {
            return;
        }
        if (child.parent != null && child.parent != this) {
            child.parent.removeChild(child);
        }
        child.parent = this;
        markAsDirty();
    }

    /**
     * Unregisters a nested object. Harmless if the object was never registered
     * or was already removed.
     */
    protected void removeChild(AbstractConfigurationObject child) {
        if (child == null) {
            return;
        }
        if (child.parent == this) {
            child.parent = null;
        }
        markAsDirty();
    }
}
