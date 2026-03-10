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
package com.vaadin.flow.component.shared;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

class HasPrefixTest {

    @Tag("test")
    private static class TestComponent extends Component implements HasPrefix {
    }

    private TestComponent component;

    @BeforeEach
    void setup() {
        component = new TestComponent();
    }

    @Test
    void getPrefix_noComponentByDefault() {
        Assertions.assertNull(component.getPrefixComponent());
    }

    @Test
    void setPrefix_replacesPrefix() {
        TestComponent foo = new TestComponent();
        component.setPrefixComponent(foo);

        Assertions.assertEquals(1,
                SlotUtils.getElementsInSlot(component, "prefix").count());
        Assertions.assertEquals(foo, component.getPrefixComponent());

        TestComponent bar = new TestComponent();
        component.setPrefixComponent(bar);

        Assertions.assertEquals(1,
                SlotUtils.getElementsInSlot(component, "prefix").count());
        Assertions.assertEquals(bar, component.getPrefixComponent());
    }

    @Test
    void setPrefix_setPrefixNull_prefixRemoved() {
        component.setPrefixComponent(new TestComponent());
        component.setPrefixComponent(null);

        Assertions.assertNull(component.getPrefixComponent());
        Assertions.assertEquals(0,
                SlotUtils.getElementsInSlot(component, "prefix").count());
    }
}
