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
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

class FlexComponentTest {

    @Test
    void setFlexShrink() {
        TestComponent component = new TestComponent();
        Div div = new Div();
        component.add(div);
        component.setFlexShrink(2, div);

        Assertions.assertEquals(2, component.getFlexShrink(div), 0,
                "should set flex-shrink");
    }

    @Test
    void getFlexShrink_returnOneIfNotSet() {
        TestComponent component = new TestComponent();
        Div div = new Div();
        component.add(div);

        Assertions.assertEquals(1, component.getFlexShrink(div), 0,
                "should return 1 if flex-shirk not set");
    }

    @Test
    void setFlexShrink_throwExceptionIfNegative() {
        TestComponent component = new TestComponent();
        Div div = new Div();
        component.add(div);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> component.setFlexShrink(-1, div));
    }

    @Test
    void setFlexGrow() {
        TestComponent component = new TestComponent();
        Div div = new Div();
        component.add(div);

        component.setFlexGrow(2, div);
        Assertions.assertEquals(2, component.getFlexGrow(div), 0);
        Assertions.assertEquals("2.0", div.getStyle().get("flex-grow"));

        component.setFlexGrow(0, div);
        Assertions.assertEquals(0, component.getFlexGrow(div), 0);
        Assertions.assertEquals("0.0", div.getStyle().get("flex-grow"));
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements FlexComponent {
    }
}
