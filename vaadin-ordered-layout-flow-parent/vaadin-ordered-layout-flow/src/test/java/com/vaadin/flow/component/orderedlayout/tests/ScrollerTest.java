/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.Scroller.ScrollDirection;

public class ScrollerTest {

    private static final String SCROLL_DIRECTION_PROPERTY = "scrollDirection";

    private Scroller scroller;

    @Before
    public void init() {
        scroller = new Scroller();
    }

    @Test
    public void getScrollDirection_defaultsToBoth() {
        Assert.assertEquals(ScrollDirection.BOTH,
                scroller.getScrollDirection());
        Assert.assertNull(
                scroller.getElement().getProperty(SCROLL_DIRECTION_PROPERTY));
    }

    @Test
    public void setScrollDirection_Horizontal_updatesProperty() {
        scroller.setScrollDirection(ScrollDirection.HORIZONTAL);
        Assert.assertEquals("horizontal",
                scroller.getElement().getProperty(SCROLL_DIRECTION_PROPERTY));
    }

    @Test
    public void setScrollDirection_Vertical_updatesProperty() {
        scroller.setScrollDirection(ScrollDirection.VERTICAL);
        Assert.assertEquals("vertical",
                scroller.getElement().getProperty(SCROLL_DIRECTION_PROPERTY));
    }

    @Test
    public void resetContent_nullPointerExceptionIsNotThrown() {
        Div content = new Div();
        scroller.setContent(content);
        Assert.assertEquals(content, scroller.getContent());
        scroller.setContent(null);
        Assert.assertNull(scroller.getContent());
    }

    @Test
    public void setScrollDirection_None_updatesProperty() {
        scroller.setScrollDirection(ScrollDirection.NONE);
        Assert.assertEquals("none",
                scroller.getElement().getProperty(SCROLL_DIRECTION_PROPERTY));
    }

    @Test
    public void setScrollDirection_Both_updatesProperty() {
        scroller.setScrollDirection(ScrollDirection.BOTH);
        Assert.assertNull(
                scroller.getElement().getProperty(SCROLL_DIRECTION_PROPERTY));
    }

    @Test(expected = NullPointerException.class)
    public void setNullScrollDirection_NullPointerExceptionIsThrown() {
        scroller.setScrollDirection(null);
    }

}