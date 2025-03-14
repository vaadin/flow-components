/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
