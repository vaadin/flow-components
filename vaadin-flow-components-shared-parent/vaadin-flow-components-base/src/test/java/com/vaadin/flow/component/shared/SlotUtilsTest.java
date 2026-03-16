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
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.dom.Element;

/**
 * Tests for the {@link SlotUtils}.
 */
class SlotUtilsTest {

    @Tag("div")
    private static class TestComponent extends Component
            implements HasComponents {
    }

    private static final String TEST_SLOT = "testSlot";
    private static final String OTHER_SLOT = "otherSlot";

    private TestComponent parent;

    @BeforeEach
    void setup() {
        parent = new TestComponent();
    }

    @Test
    void addToSlot_componentIsAdded() {
        SlotUtils.addToSlot(parent, TEST_SLOT, new TestComponent());

        Assertions.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
    }

    @Test
    void addToSlot_elementIsAdded() {
        SlotUtils.addToSlot(parent, TEST_SLOT, new Element("div"));

        Assertions.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
    }

    @Test
    void setSlot_componentIsAdded() {
        SlotUtils.setSlot(parent, TEST_SLOT, new TestComponent());

        Assertions.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
    }

    @Test
    void setSlot_elementIsAdded() {
        SlotUtils.setSlot(parent, TEST_SLOT, new Element("div"));

        Assertions.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
    }

    @Test
    void addToSlot_oldComponentIsNotRemoved() {
        SlotUtils.addToSlot(parent, TEST_SLOT, new TestComponent());

        Assertions.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());

        SlotUtils.addToSlot(parent, TEST_SLOT, new TestComponent());

        Assertions.assertEquals(2,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
    }

    @Test
    void setSlot_oldComponentIsRemoved() {
        SlotUtils.setSlot(parent, TEST_SLOT, new TestComponent());

        Assertions.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());

        SlotUtils.setSlot(parent, TEST_SLOT, new TestComponent());

        Assertions.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
    }

    @Test
    void clearSlot_onlyMatchingSlotChildIsRemoved() {
        SlotUtils.addToSlot(parent, TEST_SLOT, new TestComponent());
        SlotUtils.addToSlot(parent, OTHER_SLOT, new TestComponent());

        Assertions.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
        Assertions.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, OTHER_SLOT).count());

        SlotUtils.clearSlot(parent, TEST_SLOT);

        Assertions.assertEquals(0,
                SlotUtils.getElementsInSlot(parent, TEST_SLOT).count());
        Assertions.assertEquals(1,
                SlotUtils.getElementsInSlot(parent, OTHER_SLOT).count());
    }

    @Test
    void addToSlot_textNodeAsComponent_throws() {
        Text textNode = new Text("Text");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> SlotUtils.addToSlot(parent, TEST_SLOT, textNode));
    }

    @Test
    void setSlot_textNodeAsComponent_throws() {
        Text textNode = new Text("Text");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> SlotUtils.setSlot(parent, TEST_SLOT, textNode));
    }

    @Test
    void addToSlot_nullAsComponent_doesNotThrow() {
        SlotUtils.addToSlot(parent, TEST_SLOT, new TestComponent(), null);
    }

    @Test
    void setSlot_nullAsComponent_doesNotThrow() {
        SlotUtils.setSlot(parent, TEST_SLOT, new TestComponent(), null);
    }

    @Test
    void addToSlot_nullAsElement_doesNotThrow() {
        SlotUtils.addToSlot(parent, TEST_SLOT, new Element("div"), null);
    }

    @Test
    void setSlot_nullAsElement_doesNotThrow() {
        SlotUtils.setSlot(parent, TEST_SLOT, new Element("div"), null);
    }

    @Test
    void addToSlot_slotAttributeAddedInChild() {
        var slotComponent = new TestComponent();
        SlotUtils.addToSlot(parent, TEST_SLOT, slotComponent);
        Assertions.assertEquals(TEST_SLOT,
                slotComponent.getElement().getAttribute("slot"));
    }

    @Test
    void clearSlot_slotAttributeRemovedFromChild() {
        var slotComponent = new TestComponent();
        SlotUtils.addToSlot(parent, TEST_SLOT, slotComponent);
        SlotUtils.clearSlot(parent, TEST_SLOT);
        Assertions.assertNull(slotComponent.getElement().getAttribute("slot"));
    }
}
