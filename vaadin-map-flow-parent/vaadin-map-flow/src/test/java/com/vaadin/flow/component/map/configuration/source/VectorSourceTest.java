package com.vaadin.flow.component.map.configuration.source;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;

public class VectorSourceTest {

    @Test
    public void initializeWithOptions() {
        VectorSource.Options options = createOptions();
        VectorSource source = new VectorSource(options);

        Assert.assertEquals("testProjection", source.getProjection());
        Assert.assertEquals("testAttributions",
                source.getAttributions().get(0));
        Assert.assertFalse(source.isAttributionsCollapsible());
    }

    private VectorSource.Options createOptions() {
        VectorSource.Options options = new VectorSource.Options();
        options.setProjection("testProjection");
        options.setAttributions(List.of("testAttributions"));
        options.setAttributionsCollapsible(false);

        return options;
    }

    @Test
    public void removeAllFeatures() throws Exception {
        VectorSource.Options options = createOptions();
        VectorSource source = new VectorSource(options);
        Assert.assertEquals(new ArrayList<>(), source.getFeatures());
        source.addFeature(new MarkerFeature());
        source.addFeature(new MarkerFeature());
        source.addFeature(new MarkerFeature());
        Assert.assertEquals(3, source.getFeatures().size());
        Assert.assertEquals(3, getChildren(source).size());
        source.removeAllFeatures();
        Assert.assertEquals(0, source.getFeatures().size());
        Assert.assertEquals(0, getChildren(source).size());

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
