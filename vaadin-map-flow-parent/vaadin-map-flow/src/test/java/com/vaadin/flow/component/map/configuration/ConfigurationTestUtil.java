/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration;

import java.lang.reflect.Field;
import java.util.Set;

public class ConfigurationTestUtil {
    @SuppressWarnings("unchecked")
    public static Set<AbstractConfigurationObject> getChildren(
            AbstractConfigurationObject configurationObject)
            throws IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException {
        Field f = AbstractConfigurationObject.class
                .getDeclaredField("children");
        f.setAccessible(true);
        return (Set<AbstractConfigurationObject>) f.get(configurationObject);
    }
}
