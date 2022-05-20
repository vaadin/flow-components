package com.vaadin.flow.component;

import java.io.Serializable;

/**
 * Base definition for a typed theme variant enum of a component
 */
public interface ThemeVariant extends Serializable {
    /**
     * Returns the string value of the specific theme variant
     */
    String getVariantName();
}
