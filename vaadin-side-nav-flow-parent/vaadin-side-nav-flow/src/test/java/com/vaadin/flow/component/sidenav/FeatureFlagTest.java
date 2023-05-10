/*
 * Copyright 2000-2023 Vaadin Ltd.
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

package com.vaadin.flow.component.sidenav;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;

/**
 * @author Bretislav Wajtr
 */
public class FeatureFlagTest {

    FeatureFlags mockFeatureFlags;

    @Before
    public void setup() {
        mockFeatureFlags = Mockito.mock(FeatureFlags.class);
    }

    @Test
    public void featureEnabled() {
        TestSideNav testSideNav = new TestSideNav();
        Mockito.when(
                mockFeatureFlags.isEnabled(FeatureFlags.SIDE_NAV_COMPONENT))
                .thenReturn(true);

        testSideNav.onAttach(new AttachEvent(testSideNav, true));
    }

    @Test
    public void featureDisabled_throwsExperimentalFeatureException() {
        TestSideNav testSideNav = new TestSideNav();
        Mockito.when(
                mockFeatureFlags.isEnabled(FeatureFlags.SIDE_NAV_COMPONENT))
                .thenReturn(false);

        Assert.assertThrows(ExperimentalFeatureException.class,
                () -> testSideNav.onAttach(new AttachEvent(testSideNav, true)));
    }

    @Test
    public void sideNavItemFeatureEnabled() {
        TestSideNavItem testSideNavItem = new TestSideNavItem("test");
        Mockito.when(
                mockFeatureFlags.isEnabled(FeatureFlags.SIDE_NAV_COMPONENT))
                .thenReturn(true);

        testSideNavItem.onAttach(new AttachEvent(testSideNavItem, true));
    }

    @Test
    public void featureDisabled_throwsExperimentalFeatureExceptionOnSideNavItem() {
        TestSideNavItem testSideNavItem = new TestSideNavItem("test");
        Mockito.when(
                mockFeatureFlags.isEnabled(FeatureFlags.SIDE_NAV_COMPONENT))
                .thenReturn(false);

        Assert.assertThrows(ExperimentalFeatureException.class,
                () -> testSideNavItem
                        .onAttach(new AttachEvent(testSideNavItem, true)));
    }

    @Tag("test-side-nav")
    private class TestSideNav extends SideNav {
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

    @Tag("test-side-nav-item")
    private class TestSideNavItem extends SideNavItem {
        public TestSideNavItem(String label) {
            super(label);
        }

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
