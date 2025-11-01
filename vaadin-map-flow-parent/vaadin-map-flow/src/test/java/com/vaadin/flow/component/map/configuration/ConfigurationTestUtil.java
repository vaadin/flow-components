/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Assert;
import org.mockito.Mockito;

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

    public static <T extends AbstractConfigurationObject> void testCollectionChangeTracking(
            T configurationObject, Consumer<T> addChild, Runnable changeChild,
            Consumer<T> removeChild) {
        Set<AbstractConfigurationObject> changes = new HashSet<>();
        PropertyChangeListener changeListener = Mockito
                .mock(PropertyChangeListener.class);
        configurationObject.addPropertyChangeListener(changeListener);

        // Adding a child marks the configuration object as dirty, fires change
        // event
        addChild.accept(configurationObject);
        configurationObject.collectChanges(changes::add);
        Assert.assertTrue(
                "Adding a child should mark the configuration object as dirty",
                changes.contains(configurationObject));
        Mockito.verify(changeListener, Mockito.atLeastOnce())
                .propertyChange(Mockito.any());

        changes.clear();
        Mockito.clearInvocations(changeListener);

        // Changing something in a child while it is added propagates change
        // events
        changeChild.run();
        configurationObject.collectChanges(changes::add);
        Mockito.verify(changeListener, Mockito.atLeastOnce())
                .propertyChange(Mockito.any());

        changes.clear();
        Mockito.clearInvocations(changeListener);

        // Removing a child marks the configuration object as dirty, fires
        // change event
        removeChild.accept(configurationObject);
        configurationObject.collectChanges(changes::add);
        Assert.assertTrue(
                "Removing a child should mark the configuration object as dirty",
                changes.contains(configurationObject));
        Mockito.verify(changeListener, Mockito.atLeastOnce())
                .propertyChange(Mockito.any());

        changes.clear();
        Mockito.clearInvocations(changeListener);

        // Changing something in a child that has been removed does not fire
        // change event
        changeChild.run();
        Mockito.verify(changeListener, Mockito.never())
                .propertyChange(Mockito.any());
    }
}
