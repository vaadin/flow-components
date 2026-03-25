/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.source;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;

class VectorSourceTest {

    @Test
    void initializeWithOptions() {
        VectorSource.Options options = createOptions();
        VectorSource source = new VectorSource(options);

        Assertions.assertEquals("testProjection", source.getProjection());
        Assertions.assertEquals("testAttributions",
                source.getAttributions().get(0));
        Assertions.assertFalse(source.isAttributionsCollapsible());
    }

    private VectorSource.Options createOptions() {
        VectorSource.Options options = new VectorSource.Options();
        options.setProjection("testProjection");
        options.setAttributions(List.of("testAttributions"));
        options.setAttributionsCollapsible(false);

        return options;
    }

    @Test
    void removeAllFeatures() throws Exception {
        VectorSource.Options options = createOptions();
        VectorSource source = new VectorSource(options);
        Assertions.assertEquals(new ArrayList<>(), source.getFeatures());
        source.addFeature(new MarkerFeature());
        source.addFeature(new MarkerFeature());
        source.addFeature(new MarkerFeature());
        Assertions.assertEquals(3, source.getFeatures().size());
        Assertions.assertEquals(3, getChildren(source).size());
        source.removeAllFeatures();
        Assertions.assertEquals(0, source.getFeatures().size());
        Assertions.assertEquals(0, getChildren(source).size());

    }

    @SuppressWarnings("unchecked")
    private Set<AbstractConfigurationObject> getChildren(
            AbstractConfigurationObject confObject)
            throws IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException {
        Field f = AbstractConfigurationObject.class
                .getDeclaredField("children");
        f.setAccessible(true);
        return (Set<AbstractConfigurationObject>) f.get(confObject);
    }

}
