/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration;

import java.beans.PropertyChangeListener;
import java.io.Serializable;

/**
 * A {@link PropertyChangeListener} that is also {@link Serializable}.
 */
public interface SerializablePropertyChangeListener
        extends PropertyChangeListener, Serializable {
}
