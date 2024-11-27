/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;

public class FeatureFlagTest {
    FeatureFlags mockFeatureFlags;

    @Before
    public void setup() {
        mockFeatureFlags = Mockito.mock(FeatureFlags.class);
    }

    @Test
    public void featureEnabled_attachDashboard_doesNotThrow() {
        Mockito.when(
                mockFeatureFlags.isEnabled(FeatureFlags.DASHBOARD_COMPONENT))
                .thenReturn(true);

        var testDashboard = new TestDashboard();
        testDashboard.onAttach(new AttachEvent(testDashboard, true));
    }

    @Test
    public void featureDisabled_attachDashboard_throwsExperimentalFeatureException() {
        Mockito.when(
                mockFeatureFlags.isEnabled(FeatureFlags.DASHBOARD_COMPONENT))
                .thenReturn(false);

        var testDashboard = new TestDashboard();
        Assert.assertThrows(ExperimentalFeatureException.class,
                () -> testDashboard
                        .onAttach(new AttachEvent(testDashboard, true)));
    }

    @Test
    public void featureDisabled_attachWidget_doesNotThrow() {
        Mockito.when(
                mockFeatureFlags.isEnabled(FeatureFlags.DASHBOARD_COMPONENT))
                .thenReturn(true);

        var testDashboardWidget = new TestDashboardWidget();
        testDashboardWidget
                .onAttach(new AttachEvent(testDashboardWidget, true));
    }

    @Test
    public void featureDisabled_attachWidget_throwsExperimentalFeatureException() {
        Mockito.when(
                mockFeatureFlags.isEnabled(FeatureFlags.DASHBOARD_COMPONENT))
                .thenReturn(false);

        var testDashboardWidget = new TestDashboardWidget();
        Assert.assertThrows(ExperimentalFeatureException.class,
                () -> testDashboardWidget
                        .onAttach(new AttachEvent(testDashboardWidget, true)));
    }

    @Tag("test-dashboard")
    private class TestDashboard extends Dashboard {
        // Override to expose to test class
        @Override
        protected void onAttach(AttachEvent attachEvent) {
            super.onAttach(attachEvent);
        }

        // Override to return mock feature flags
        @Override
        protected FeatureFlags getFeatureFlags() {
            return mockFeatureFlags;
        }
    }

    @Tag("test-dashboard-widget")
    private class TestDashboardWidget extends DashboardWidget {
        // Override to expose to test class
        @Override
        protected void onAttach(AttachEvent attachEvent) {
            super.onAttach(attachEvent);
        }

        // Override to return mock feature flags
        @Override
        protected FeatureFlags getFeatureFlags() {
            return mockFeatureFlags;
        }
    }
}
