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
package com.vaadin.flow.component.shared;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.dom.Element;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link SlotUtils}.
 */
public class SlotUtilsTest {

    @Tag("div")
    private static class TestComponent extends Component
            implements HasComponents {
    }

    private static final String TEST_SLOT = "testSlot";
    private static final String OTHER_SLOT = "otherSlot";

    private TestComponent parent;

    @Before
    public void setup() {
        parent = new TestComponent();
    }

    @Test
    public void addToSlot_componentIsAdded() {
        SlotUtils.addToSlot(parent, TEST_SLOT, new TestComponent());

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
    }

    @Test
    public void addToSlot_elementIsAdded() {
        SlotUtils.addToSlot(parent, TEST_SLOT, new Element("div"));

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
    }

    @Test
    public void setSlot_componentIsAdded() {
        SlotUtils.setSlot(parent, TEST_SLOT, new TestComponent());

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
    }

    @Test
    public void setSlot_elementIsAdded() {
        SlotUtils.setSlot(parent, TEST_SLOT, new Element("div"));

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
    }

    @Test
    public void addToSlot_oldComponentIsNotRemoved() {
        SlotUtils.addToSlot(parent, TEST_SLOT, new TestComponent());

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());

        SlotUtils.addToSlot(parent, TEST_SLOT, new TestComponent());

        Assert.assertEquals(2,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
    }

    @Test
    public void setSlot_oldComponentIsRemoved() {
        SlotUtils.setSlot(parent, TEST_SLOT, new TestComponent());

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());

        SlotUtils.setSlot(parent, TEST_SLOT, new TestComponent());

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
    }

    @Test
    public void clearSlot_onlyMatchingSlotChildIsRemoved() {
        SlotUtils.addToSlot(parent, TEST_SLOT, new TestComponent());
        SlotUtils.addToSlot(parent, OTHER_SLOT, new TestComponent());

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, OTHER_SLOT).count());

        SlotUtils.clearSlot(parent, TEST_SLOT);

        Assert.assertEquals(0,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, OTHER_SLOT).count());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addToSlot_textNodeAsComponent_throws() {
        Text textNode = new Text("Text");
        SlotUtils.addToSlot(parent, TEST_SLOT, textNode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSlot_textNodeAsComponent_throws() {
        Text textNode = new Text("Text");
        SlotUtils.setSlot(parent, TEST_SLOT, textNode);
    }

    @Test
    public void addToSlot_nullAsComponent_doesNotThrow() {
        SlotUtils.addToSlot(parent, TEST_SLOT, new TestComponent(), null);
    }

    @Test
    public void setSlot_nullAsComponent_doesNotThrow() {
        SlotUtils.setSlot(parent, TEST_SLOT, new TestComponent(), null);
    }

    @Test
    public void addToSlot_nullAsElement_doesNotThrow() {
        SlotUtils.addToSlot(parent, TEST_SLOT, new Element("div"), null);
    }

    @Test
    public void setSlot_nullAsElement_doesNotThrow() {
        SlotUtils.setSlot(parent, TEST_SLOT, new Element("div"), null);
    }
}
