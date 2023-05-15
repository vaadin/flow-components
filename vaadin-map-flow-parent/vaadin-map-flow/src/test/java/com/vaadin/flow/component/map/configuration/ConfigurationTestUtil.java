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
