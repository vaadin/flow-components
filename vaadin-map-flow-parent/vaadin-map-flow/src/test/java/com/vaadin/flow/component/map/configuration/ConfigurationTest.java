/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration;

import java.util.List;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.map.configuration.controls.Control;

public class ConfigurationTest {

    private Configuration configuration;
    private Consumer<AbstractConfigurationObject> changeCollectorMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        configuration = new Configuration();
        // Clear initial dirty flag for tests
        configuration.collectChanges(o -> {
        });
        changeCollectorMock = (Consumer<AbstractConfigurationObject>) Mockito
                .mock(Consumer.class);
    }

    @Test
    public void addControl() {
        TestControl control1 = new TestControl("type-1");
        TestControl control2 = new TestControl("type-2");

        configuration.addControl(control1);
        configuration.addControl(control2);

        List<Control> controls = configuration.getControls();
        Assert.assertEquals(2, controls.size());
        Assert.assertEquals(control1, controls.get(0));
        Assert.assertEquals(control2, controls.get(1));
    }

    @Test(expected = NullPointerException.class)
    public void addControl_doesNotAllowNull() {
        configuration.addControl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addControl_doesNotAllowDuplicateType() {
        TestControl control1 = new TestControl("same-type");
        TestControl control2 = new TestControl("same-type");

        configuration.addControl(control1);
        configuration.addControl(control2);
    }

    @Test
    public void removeControl() {
        TestControl control1 = new TestControl("type-1");
        TestControl control2 = new TestControl("type-2");

        configuration.addControl(control1);
        configuration.addControl(control2);
        configuration.removeControl(control1);

        List<Control> controls = configuration.getControls();
        Assert.assertEquals(1, controls.size());
        Assert.assertEquals(control2, controls.getFirst());
    }

    @Test(expected = NullPointerException.class)
    public void removeControl_doesNotAllowNull() {
        configuration.removeControl(null);
    }

    @Test
    public void controls_changeTracking() {
        TestControl control = new TestControl();

        ConfigurationTestUtil.testCollectionChangeTracking(configuration,
                config -> config.addControl(control),
                () -> control.setVisible(true),
                config -> config.removeControl(control));
    }

    @Test
    public void getVisibleControls() {
        TestControl control1 = new TestControl("type-1");
        control1.setVisible(true);
        TestControl control2 = new TestControl("type-2");
        control2.setVisible(false);
        TestControl control3 = new TestControl("type-3");
        control3.setVisible(true);

        configuration.addControl(control1);
        configuration.addControl(control2);
        configuration.addControl(control3);

        List<Control> visibleControls = configuration.getVisibleControls();
        Assert.assertEquals(2, visibleControls.size());
        Assert.assertTrue(visibleControls.contains(control1));
        Assert.assertFalse(visibleControls.contains(control2));
        Assert.assertTrue(visibleControls.contains(control3));
    }

    @Test
    public void controlVisibilityChange_marksConfigurationAsDirty() {
        TestControl control = new TestControl();
        configuration.addControl(control);
        // Clear dirty flag
        configuration.collectChanges(o -> {
        });

        control.setVisible(true);

        configuration.collectChanges(changeCollectorMock);
        Mockito.verify(changeCollectorMock, Mockito.atLeastOnce())
                .accept(configuration);
    }

    @Test
    public void removeControl_controlVisibilityChange_doesNotMarkConfigurationAsDirty() {
        TestControl control = new TestControl();
        configuration.addControl(control);
        configuration.removeControl(control);
        // Clear dirty flag
        configuration.collectChanges(o -> {
        });

        control.setVisible(true);

        configuration.collectChanges(changeCollectorMock);
        Mockito.verify(changeCollectorMock, Mockito.never())
                .accept(configuration);
    }

    @Test
    public void controlOtherPropertyChange_doesNotMarkConfigurationAsDirty() {
        TestControl control = new TestControl();
        configuration.addControl(control);
        // Clear dirty flag
        configuration.collectChanges(o -> {
        });

        control.fireOtherPropertyChange();

        configuration.collectChanges(changeCollectorMock);
        Mockito.verify(changeCollectorMock, Mockito.never())
                .accept(configuration);
    }

    private static class TestControl extends Control {
        private final String type;

        public TestControl() {
            this("test-control");
        }

        public TestControl(String type) {
            this.type = type;
        }

        @Override
        public String getType() {
            return type;
        }

        public void fireOtherPropertyChange() {
            propertyChangeSupport.firePropertyChange("otherProperty", null,
                    null);
        }
    }
}
