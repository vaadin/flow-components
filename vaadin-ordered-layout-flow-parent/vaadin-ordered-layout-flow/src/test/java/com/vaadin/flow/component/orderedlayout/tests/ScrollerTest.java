/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.Scroller.ScrollDirection;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.HasThemeVariant;

class ScrollerTest {

    private static final String SCROLL_DIRECTION_PROPERTY = "scrollDirection";

    private Scroller scroller;

    @BeforeEach
    void setup() {
        scroller = new Scroller();
    }

    @Test
    void getScrollDirection_defaultsToBoth() {
        Assertions.assertEquals(ScrollDirection.BOTH,
                scroller.getScrollDirection());
        Assertions.assertNull(
                scroller.getElement().getProperty(SCROLL_DIRECTION_PROPERTY));
    }

    @Test
    void setScrollDirection_Horizontal_updatesProperty() {
        scroller.setScrollDirection(ScrollDirection.HORIZONTAL);
        Assertions.assertEquals("horizontal",
                scroller.getElement().getProperty(SCROLL_DIRECTION_PROPERTY));
    }

    @Test
    void setScrollDirection_Vertical_updatesProperty() {
        scroller.setScrollDirection(ScrollDirection.VERTICAL);
        Assertions.assertEquals("vertical",
                scroller.getElement().getProperty(SCROLL_DIRECTION_PROPERTY));
    }

    @Test
    void resetContent_nullPointerExceptionIsNotThrown() {
        Div content = new Div();
        scroller.setContent(content);
        Assertions.assertEquals(content, scroller.getContent());
        scroller.setContent(null);
        Assertions.assertNull(scroller.getContent());
    }

    @Test
    void setScrollDirection_None_updatesProperty() {
        scroller.setScrollDirection(ScrollDirection.NONE);
        Assertions.assertEquals("none",
                scroller.getElement().getProperty(SCROLL_DIRECTION_PROPERTY));
    }

    @Test
    void setScrollDirection_Both_updatesProperty() {
        scroller.setScrollDirection(ScrollDirection.BOTH);
        Assertions.assertNull(
                scroller.getElement().getProperty(SCROLL_DIRECTION_PROPERTY));
    }

    @Test
    void setNullScrollDirection_NullPointerExceptionIsThrown() {
        Assertions.assertThrows(NullPointerException.class,
                () -> scroller.setScrollDirection(null));
    }

    @Test
    void implementsFocusable() {
        Assertions.assertTrue(
                Focusable.class.isAssignableFrom(scroller.getClass()),
                "Scroller should be focusable");
    }

    @Test
    void setEnabled_disableChildren() {
        Input input = new Input();

        scroller.setContent(new VerticalLayout(input));
        Assertions.assertTrue(input.isEnabled());

        scroller.setEnabled(false);
        Assertions.assertFalse(input.isEnabled());
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(Scroller.class));
    }
}
