/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.map.configuration.controls.Control;

class ConfigurationTest {

    private Configuration configuration;
    private Consumer<AbstractConfigurationObject> changeCollectorMock;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        configuration = new Configuration();
        // Clear initial dirty flag for tests
        configuration.collectChanges(o -> {
        });
        changeCollectorMock = (Consumer<AbstractConfigurationObject>) Mockito
                .mock(Consumer.class);
    }

    @Test
    void addControl() {
        TestControl control1 = new TestControl("type-1");
        TestControl control2 = new TestControl("type-2");

        configuration.addControl(control1);
        configuration.addControl(control2);

        List<Control> controls = configuration.getControls();
        Assertions.assertEquals(2, controls.size());
        Assertions.assertEquals(control1, controls.get(0));
        Assertions.assertEquals(control2, controls.get(1));
    }

    @Test
    void addControl_doesNotAllowNull() {
        Assertions.assertThrows(NullPointerException.class,
                () -> configuration.addControl(null));
    }

    @Test
    void addControl_doesNotAllowDuplicateType() {
        TestControl control1 = new TestControl("same-type");
        TestControl control2 = new TestControl("same-type");

        configuration.addControl(control1);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> configuration.addControl(control2));
    }

    @Test
    void removeControl() {
        TestControl control1 = new TestControl("type-1");
        TestControl control2 = new TestControl("type-2");

        configuration.addControl(control1);
        configuration.addControl(control2);
        configuration.removeControl(control1);

        List<Control> controls = configuration.getControls();
        Assertions.assertEquals(1, controls.size());
        Assertions.assertEquals(control2, controls.getFirst());
    }

    @Test
    void removeControl_doesNotAllowNull() {
        Assertions.assertThrows(NullPointerException.class,
                () -> configuration.removeControl(null));
    }

    @Test
    void controls_changeTracking() {
        TestControl control = new TestControl();

        ConfigurationTestUtil.testCollectionChangeTracking(configuration,
                config -> config.addControl(control),
                () -> control.setVisible(true),
                config -> config.removeControl(control));
    }

    @Test
    void getVisibleControls() {
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
        Assertions.assertEquals(2, visibleControls.size());
        Assertions.assertTrue(visibleControls.contains(control1));
        Assertions.assertFalse(visibleControls.contains(control2));
        Assertions.assertTrue(visibleControls.contains(control3));
    }

    @Test
    void controlVisibilityChange_marksConfigurationAsDirty() {
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
    void removeControl_controlVisibilityChange_doesNotMarkConfigurationAsDirty() {
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
    void controlOtherPropertyChange_marksConfigurationAsDirty() {
        TestControl control = new TestControl();
        configuration.addControl(control);
        // Clear dirty flag
        configuration.collectChanges(o -> {
        });

        control.fireOtherPropertyChange();

        configuration.collectChanges(changeCollectorMock);
        Mockito.verify(changeCollectorMock, Mockito.atLeastOnce())
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
