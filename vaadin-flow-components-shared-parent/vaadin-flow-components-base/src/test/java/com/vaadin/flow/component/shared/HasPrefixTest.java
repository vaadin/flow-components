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
package com.vaadin.flow.component.shared;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

public class HasPrefixTest {

    @Tag("test")
    private static class TestComponent extends Component implements HasPrefix {
    }

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void getPrefix_noComponentByDefault() {
        Assert.assertNull(component.getPrefixComponent());
    }

    @Test
    public void setPrefix_replacesPrefix() {
        TestComponent foo = new TestComponent();
        component.setPrefixComponent(foo);

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(component, "prefix").count());
        Assert.assertEquals(foo, component.getPrefixComponent());

        TestComponent bar = new TestComponent();
        component.setPrefixComponent(bar);

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(component, "prefix").count());
        Assert.assertEquals(bar, component.getPrefixComponent());
    }

    @Test
    public void setPrefix_setPrefixNull_prefixRemoved() {
        component.setPrefixComponent(new TestComponent());
        component.setPrefixComponent(null);

        Assert.assertNull(component.getPrefixComponent());
        Assert.assertEquals(0,
                SlotUtils.getElementsInSlot(component, "prefix").count());
    }
}
