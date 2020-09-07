package com.vaadin.flow.component.icon;

import java.io.Serializable;

/**
 * Factory for icons.
 */
@FunctionalInterface
public interface IconFactory extends Serializable {
    Icon create();
}
