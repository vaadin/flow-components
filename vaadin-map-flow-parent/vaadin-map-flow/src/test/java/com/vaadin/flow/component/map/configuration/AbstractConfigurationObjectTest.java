package com.vaadin.flow.component.map.configuration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.beans.PropertyChangeListener;

public class AbstractConfigurationObjectTest {

    private TestConfiguration testConfiguration;
    private PropertyChangeListener changeListenerMock;

    @Before
    public void setup() {
        testConfiguration = new TestConfiguration();
        testConfiguration.setNestedConfiguration(new TestConfiguration());
        changeListenerMock = Mockito.mock(PropertyChangeListener.class);
    }

    @Test
    public void generatesDefaultId() {
        Assert.assertNotNull(testConfiguration.getId());
    }

    @Test
    public void setProperty_notifyChanges() {
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.setFoo("test");

        Mockito.verify(changeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void setNestedProperty_notifyChanges() {
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.getNestedConfiguration().setFoo("test");

        Mockito.verify(changeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
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
    public void updateWithNotifyChanges_notifyChanges() {
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.update(() -> testConfiguration.setFoo("test"), true);

        Mockito.verify(changeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void updateWithoutNotifyChanges_doesNotNotifyChanges() {
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.update(() -> testConfiguration.setFoo("test"), false);

        Mockito.verify(changeListenerMock, Mockito.times(0))
                .propertyChange(Mockito.any());
    }

    @Test
    public void updateWithoutNotifyChanges_doesNotNotifyNestedChanges() {
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.update(
                () -> testConfiguration.getNestedConfiguration().setFoo("test"),
                false);

        Mockito.verify(changeListenerMock, Mockito.times(0))
                .propertyChange(Mockito.any());
    }

    @Test
    public void updateWithoutNotifyChanges_resetNotifyChangesFlag() {
        // Update without change notifications
        testConfiguration.update(() -> testConfiguration.setFoo("test"), false);

        // Verify the changes are notified again afterwards
        testConfiguration.addPropertyChangeListener(changeListenerMock);
        testConfiguration.setFoo("test");

        Mockito.verify(changeListenerMock, Mockito.times(1))
                .propertyChange(Mockito.any());
    }

    @Test
    public void updateWithoutNotifyChangesAndException_resetNotifyChangesFlag() {
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
            notifyChange();
        }

        public TestConfiguration getNestedConfiguration() {
            return nestedConfiguration;
        }

        public void setNestedConfiguration(
                TestConfiguration nestedConfiguration) {
            updateNestedPropertyObserver(this.nestedConfiguration,
                    nestedConfiguration);
            this.nestedConfiguration = nestedConfiguration;
            this.notifyChange();
        }
    }
}