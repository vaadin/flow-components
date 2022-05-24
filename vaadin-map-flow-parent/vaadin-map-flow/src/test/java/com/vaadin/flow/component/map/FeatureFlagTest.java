package com.vaadin.flow.component.map;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class FeatureFlagTest {

    FeatureFlags mockFeatureFlags;

    @Before
    public void setup() {
        mockFeatureFlags = Mockito.mock(FeatureFlags.class);
    }

    @Test
    public void featureEnabled() {
        TestMap testMap = new TestMap();
        Mockito.when(mockFeatureFlags.isEnabled(FeatureFlags.MAP_COMPONENT))
                .thenReturn(true);

        testMap.onAttach(new AttachEvent(testMap, true));
    }

    @Test
    public void featureDisabled_throwsExperimentalFeatureException() {
        TestMap testMap = new TestMap();
        Mockito.when(mockFeatureFlags.isEnabled(FeatureFlags.MAP_COMPONENT))
                .thenReturn(false);

        Assert.assertThrows(ExperimentalFeatureException.class,
                () -> testMap.onAttach(new AttachEvent(testMap, true)));
    }

    @Tag("test-map")
    private class TestMap extends MapBase {
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
