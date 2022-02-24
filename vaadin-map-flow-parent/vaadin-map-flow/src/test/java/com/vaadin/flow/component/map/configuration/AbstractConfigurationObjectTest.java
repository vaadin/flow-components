package com.vaadin.flow.component.map.configuration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

public class AbstractConfigurationObjectTest {

    private TestConfiguration testConfiguration;
    private PropertyChangeListener changeListenerMock;
    private Consumer<AbstractConfigurationObject> changeCollectorMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        testConfiguration = new TestConfiguration();
        testConfiguration.setNestedConfiguration(new TestConfiguration());
        // Clear initial dirty flag for tests
        testConfiguration.collectChanges(o -> {
        });
        changeListenerMock = Mockito.mock(PropertyChangeListener.class);
        changeCollectorMock = (Consumer<AbstractConfigurationObject>) Mockito
                .mock(Consumer.class);
    }

    @Test
    public void generatesDefaultId() {
        Assert.assertNotNull(testConfiguration.getId());
    }

    @Test
    public void collectChanges_resetsDirtyFlag() {
        TestConfiguration testConfiguration = new TestConfiguration();
        // Collect changes to clear dirty flag
        testConfiguration.collectChanges(o -> {
        });
        // Collect second time to verify that there are no changes
        testConfiguration.collectChanges(changeCollectorMock);
        Mockito.verify(changeCollectorMock, Mockito.times(0))
                .accept(Mockito.any());
    }

    @Test
    public void setProperty_notifyChanges() {
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.setFoo("test");

        Mockito.verify(changeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void setProperty_marksAsDirty() {
        testConfiguration.setFoo("test");
        // Collect changes, should only include root object as changed
        testConfiguration.collectChanges(changeCollectorMock);
        Mockito.verify(changeCollectorMock, Mockito.times(1))
                .accept(Mockito.any());
        Mockito.verify(changeCollectorMock).accept(testConfiguration);
    }

    @Test
    public void setNestedProperty_notifyChanges() {
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.getNestedConfiguration().setFoo("test");

        Mockito.verify(changeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void setNestedProperty_marksNestedObjectAsDirty() {
        testConfiguration.getNestedConfiguration().setFoo("test");
        // Collect changes, should only include nested object as changed
        testConfiguration.collectChanges(changeCollectorMock);
        Mockito.verify(changeCollectorMock, Mockito.times(1))
                .accept(Mockito.any());
        Mockito.verify(changeCollectorMock)
                .accept(testConfiguration.getNestedConfiguration());
    }

    @Test
    public void setNestedObject_notifyChanges() {
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.setNestedConfiguration(new TestConfiguration());

        // Called two times, one each for removing existing and adding new
        // nested object
        Mockito.verify(changeListenerMock, Mockito.times(2))
                .propertyChange(Mockito.any());
    }

    @Test
    public void setNestedObject_marksNestedHierarchyAsDirty() {
        // Create new nested object, clear dirty flag on it
        TestConfiguration newNestedObject = new TestConfiguration();
        newNestedObject.collectChanges(o -> {
        });
        // Change nested object
        testConfiguration.setNestedConfiguration(newNestedObject);
        // Collect changes, should include root config because the nested
        // reference changed,
        // and nested object, because we ensure that the full hierarchy that was
        // added is
        // synced as well (see addChild implementation)
        testConfiguration.collectChanges(changeCollectorMock);
        Mockito.verify(changeCollectorMock, Mockito.times(2))
                .accept(Mockito.any());
        Mockito.verify(changeCollectorMock)
                .accept(testConfiguration.getNestedConfiguration());
        Mockito.verify(changeCollectorMock).accept(testConfiguration);
    }

    @Test
    public void removeChangeListener_doesNotNotifyChanges() {
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.removePropertyChangeListener(changeListenerMock);
        testConfiguration.setFoo("test");

        Mockito.verify(changeListenerMock, Mockito.times(0))
                .propertyChange(Mockito.any());
    }

    @Test
    public void updateWithChangeTracking_notifyChanges() {
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.update(() -> testConfiguration.setFoo("test"), true);

        Mockito.verify(changeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void updateWithChangeTracking_marksAsDirty() {
        testConfiguration.update(() -> testConfiguration.setFoo("test"), true);

        // Collect changes, should include root object as changed
        testConfiguration.collectChanges(changeCollectorMock);
        Mockito.verify(changeCollectorMock, Mockito.times(1))
                .accept(Mockito.any());
        Mockito.verify(changeCollectorMock).accept(testConfiguration);
    }

    @Test
    public void updateWithoutChangeTracking_doesNotNotifyChanges() {
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.update(() -> testConfiguration.setFoo("test"), false);

        Mockito.verify(changeListenerMock, Mockito.times(0))
                .propertyChange(Mockito.any());
    }

    @Test
    public void updateWithoutChangeTracking_doesNotMarkAsDirty() {
        testConfiguration.update(() -> testConfiguration.setFoo("test"), false);

        testConfiguration.collectChanges(changeCollectorMock);
        Mockito.verify(changeCollectorMock, Mockito.times(0))
                .accept(Mockito.any());
    }

    @Test
    public void updateWithoutChangeTracking_doesNotNotifyNestedChanges() {
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.update(
                () -> testConfiguration.getNestedConfiguration().setFoo("test"),
                false);

        Mockito.verify(changeListenerMock, Mockito.times(0))
                .propertyChange(Mockito.any());
    }

    @Test
    public void updateWithoutChangeTracking_doesNotMarkNestedObjectAsDirty() {
        testConfiguration.update(
                () -> testConfiguration.getNestedConfiguration().setFoo("test"),
                false);

        testConfiguration.collectChanges(changeCollectorMock);
        Mockito.verify(changeCollectorMock, Mockito.times(0))
                .accept(Mockito.any());
    }

    @Test
    public void updateWithoutChangeTracking_resetTrackChangesFlag() {
        // Update without change notifications
        testConfiguration.update(() -> testConfiguration.setFoo("test"), false);

        // Verify the changes are notified again afterwards
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.setFoo("test");

        Mockito.verify(changeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void updateWithoutChangeTrackingAndException_resetTrackChangesFlag() {
        // Update without change notifications, throws an exception during
        // execution
        try {
            testConfiguration.update(() -> {
                throw new RuntimeException("Error");
            }, false);
        } catch (Throwable t) {
            // Ignore
        }

        // Verify the changes are notified again afterwards
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.setFoo("test");

        Mockito.verify(changeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void deepMarkAsDirty_marksFullHierarchyAsDirty() {
        testConfiguration.deepMarkAsDirty();
        testConfiguration.collectChanges(changeCollectorMock);
        Mockito.verify(changeCollectorMock, Mockito.times(2))
                .accept(Mockito.any());
        Mockito.verify(changeCollectorMock)
                .accept(testConfiguration.getNestedConfiguration());
        Mockito.verify(changeCollectorMock).accept(testConfiguration);
    }

    /**
     * For current use-cases there is no need to have deepMarkAsDirty trigger
     * change events, especially considering that all nested objects would
     * trigger a change event as well
     */
    @Test
    public void deepMarkAsDirty_doesNotNotifyChanges() {
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.deepMarkAsDirty();
        Mockito.verify(changeListenerMock, Mockito.times(0))
                .propertyChange(Mockito.any());
    }

    private static class TestConfiguration extends AbstractConfigurationObject {
        private String foo;
        private TestConfiguration nestedConfiguration;

        @Override
        public String getType() {
            return "test";
        }

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
            markAsDirty();
        }

        public TestConfiguration getNestedConfiguration() {
            return nestedConfiguration;
        }

        public void setNestedConfiguration(
                TestConfiguration nestedConfiguration) {
            removeChild(this.nestedConfiguration);
            this.nestedConfiguration = nestedConfiguration;
            addChild(nestedConfiguration);
        }

        // Expose method for testing
        @Override
        protected void deepMarkAsDirty() {
            super.deepMarkAsDirty();
        }
    }
}