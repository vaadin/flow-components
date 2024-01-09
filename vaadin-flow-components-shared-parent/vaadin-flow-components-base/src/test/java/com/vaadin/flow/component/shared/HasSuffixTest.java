/*
 * Copyright 2000-2024 Vaadin Ltd.
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

public class HasSuffixTest {

    @Tag("test")
    private static class TestComponent extends Component implements HasSuffix {
    }

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void getSuffix_noComponentByDefault() {
        Assert.assertNull(component.getSuffixComponent());
    }

    @Test
    public void setSuffix_replacesSuffix() {
        TestComponent foo = new TestComponent();
        component.setSuffixComponent(foo);

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(component, "suffix").count());
        Assert.assertEquals(foo, component.getSuffixComponent());

        TestComponent bar = new TestComponent();
        component.setSuffixComponent(bar);

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(component, "suffix").count());
        Assert.assertEquals(bar, component.getSuffixComponent());
    }

    @Test
    public void setSuffix_setSuffixNull_suffixRemoved() {
        component.setSuffixComponent(new TestComponent());
        component.setSuffixComponent(null);

        Assert.assertNull(component.getSuffixComponent());
        Assert.assertEquals(0,
                SlotUtils.getElementsInSlot(component, "suffix").count());
    }
}
