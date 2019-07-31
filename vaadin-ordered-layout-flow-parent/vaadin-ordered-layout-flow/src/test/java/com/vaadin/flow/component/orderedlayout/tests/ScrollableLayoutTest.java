/*
 * Copyright 2000-2019 Vaadin Ltd.
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
package com.vaadin.flow.component.orderedlayout.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.orderedlayout.ScrollableLayout;

public class ScrollableLayoutTest {

    @Tag("foo")
    public static class ScrollableTestComponent extends Component
            implements ScrollableLayout, HasStyle {
    }

    private final String OVERFLOW = "overflow";

    private ScrollableTestComponent component;

    @Before
    public void init() {
        component = new ScrollableTestComponent();
    }

    @Test
    public void setScrollable_updatesOverflowCssProperty() {
        Assert.assertNull(component.getStyle().get(OVERFLOW));
        component.setScrollable(true);
        Assert.assertEquals("auto", component.getStyle().get(OVERFLOW));
        component.setScrollable(false);
        Assert.assertNull(component.getStyle().get(OVERFLOW));
    }

    @Test
    public void setScrollable_getScrollableReturnsCorrect() {
        Assert.assertFalse(component.isScrollable());
        component.setScrollable(true);
        Assert.assertTrue(component.isScrollable());
        component.setScrollable(false);
        Assert.assertFalse(component.isScrollable());
    }

}
