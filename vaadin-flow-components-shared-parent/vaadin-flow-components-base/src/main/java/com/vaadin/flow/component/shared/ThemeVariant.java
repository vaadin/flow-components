/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import java.io.Serializable;

/**
 * Base definition for a typed theme variant enum of a component
 */
public interface ThemeVariant extends Serializable {
    /**
     * @return The string value of the specific theme variant
     */
    String getVariantName();
}
