/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.icon;

import java.io.Serializable;

/**
 * Factory for icons.
 */
@FunctionalInterface
public interface IconFactory extends Serializable {
    Icon create();
}
