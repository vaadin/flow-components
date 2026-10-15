/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.card;

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
    public void featureEnabled_attachCard_doesNotThrow() {
        Mockito.when(mockFeatureFlags.isEnabled(FeatureFlags.CARD_COMPONENT))
                .thenReturn(true);
        var testCard = new TestCard();
        testCard.onAttach(new AttachEvent(testCard, true));
    }

    @Test(expected = ExperimentalFeatureException.class)
    public void featureDisabled_attachCard_throwsExperimentalFeatureException() {
        Mockito.when(mockFeatureFlags.isEnabled(FeatureFlags.CARD_COMPONENT))
                .thenReturn(false);
        var testCard = new TestCard();
        testCard.onAttach(new AttachEvent(testCard, true));
    }

    @Tag("test-card")
    private class TestCard extends Card {
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
