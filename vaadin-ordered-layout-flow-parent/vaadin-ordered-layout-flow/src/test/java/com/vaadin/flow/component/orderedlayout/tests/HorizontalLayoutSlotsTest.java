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
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.tests.MockUIExtension;

class HorizontalLayoutSlotsTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    private HorizontalLayout layout;

    @BeforeEach
    void setup() {
        layout = new HorizontalLayout();
        ui.add(layout);
    }

    @Test
    void addToStart_componentHasNoSlot() {
        Div div = new Div();
        layout.addToStart(div);
        Assertions.assertEquals(div, layout.getComponentAt(0));
        Assertions.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    void addToMiddle_componentHasMiddleSlot() {
        Div div = new Div();
        layout.addToMiddle(div);
        Assertions.assertEquals(div, layout.getComponentAt(0));
        Assertions.assertEquals("middle",
                div.getElement().getAttribute("slot"));
    }

    @Test
    void addToEnd_componentHasEndSlot() {
        Div div = new Div();
        layout.addToEnd(div);
        Assertions.assertEquals(div, layout.getComponentAt(0));
        Assertions.assertEquals("end", div.getElement().getAttribute("slot"));
    }

    @Test
    void addToMiddle_remove_componentHasNoSlot() {
        Div div = new Div();
        layout.addToMiddle(div);

        layout.remove(div);
        Assertions.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    void addToMiddle_removeAll_componentHasNoSlot() {
        Div div = new Div();
        layout.addToMiddle(div);

        layout.removeAll();
        Assertions.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    void addToMiddle_removeFromParent_componentHasNoSlot() {
        Div div = new Div();
        layout.addToMiddle(div);

        div.getElement().removeFromParent();
        Assertions.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    void addToEnd_remove_componentHasNoSlot() {
        Div div = new Div();
        layout.addToEnd(div);

        layout.remove(div);
        Assertions.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    void addToEnd_removeFromParent_componentHasNoSlot() {
        Div div = new Div();
        layout.addToEnd(div);

        div.getElement().removeFromParent();
        Assertions.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    void addToEnd_removeAll_componentHasNoSlot() {
        Div div = new Div();
        layout.addToEnd(div);

        layout.removeAll();
        Assertions.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    void addToStart_addToMiddle_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToStart(div1);

        Div div2 = new Div();
        layout.addToMiddle(div2);

        layout.replace(div1, div2);

        Assertions.assertEquals("middle",
                div1.getElement().getAttribute("slot"));
        Assertions.assertNull(div2.getElement().getAttribute("slot"));
    }

    @Test
    void addToMiddle_addToStart_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToMiddle(div1);

        Div div2 = new Div();
        layout.addToStart(div2);

        layout.replace(div1, div2);

        Assertions.assertNull(div1.getElement().getAttribute("slot"));
        Assertions.assertEquals("middle",
                div2.getElement().getAttribute("slot"));
    }

    @Test
    void addToStart_addToEnd_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToStart(div1);

        Div div2 = new Div();
        layout.addToEnd(div2);

        layout.replace(div1, div2);

        Assertions.assertEquals("end", div1.getElement().getAttribute("slot"));
        Assertions.assertNull(div2.getElement().getAttribute("slot"));
    }

    @Test
    void addToEnd_addToStart_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToEnd(div1);

        Div div2 = new Div();
        layout.addToStart(div2);

        layout.replace(div1, div2);

        Assertions.assertNull(div1.getElement().getAttribute("slot"));
        Assertions.assertEquals("end", div2.getElement().getAttribute("slot"));
    }

    @Test
    void addToMiddle_addToEnd_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToMiddle(div1);

        Div div2 = new Div();
        layout.addToEnd(div2);

        layout.replace(div1, div2);

        Assertions.assertEquals("end", div1.getElement().getAttribute("slot"));
        Assertions.assertEquals("middle",
                div2.getElement().getAttribute("slot"));
    }

    @Test
    void addToEnd_addToMiddle_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToEnd(div1);

        Div div2 = new Div();
        layout.addToMiddle(div2);

        layout.replace(div1, div2);

        Assertions.assertEquals("middle",
                div1.getElement().getAttribute("slot"));
        Assertions.assertEquals("end", div2.getElement().getAttribute("slot"));
    }

    @Test
    void addToMiddle_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToMiddle(div1);

        Div div2 = new Div();

        layout.replace(div1, div2);

        Assertions.assertNull(div1.getElement().getAttribute("slot"));
        Assertions.assertEquals("middle",
                div2.getElement().getAttribute("slot"));
    }

    @Test
    void addToEnd_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToEnd(div1);

        Div div2 = new Div();

        layout.replace(div1, div2);

        Assertions.assertNull(div1.getElement().getAttribute("slot"));
        Assertions.assertEquals("end", div2.getElement().getAttribute("slot"));
    }

    @Test
    void addToEnd_addToMiddle_addToStart_indexesAreInSlotsOrder() {
        Div div1 = new Div();
        layout.addToEnd(div1);

        Div div2 = new Div();
        layout.addToMiddle(div2);

        Div div3 = new Div();
        layout.addToStart(div3);

        Assertions.assertEquals(div3, layout.getComponentAt(0));
        Assertions.assertEquals(div2, layout.getComponentAt(1));
        Assertions.assertEquals(div1, layout.getComponentAt(2));
    }

    @Test
    void addToEnd_addToMiddle_add_indexesAreInSlotsOrder() {
        Div div1 = new Div();
        layout.addToEnd(div1);

        Div div2 = new Div();
        layout.addToMiddle(div2);

        Div div3 = new Div();
        layout.add(div3);

        Assertions.assertEquals(div3, layout.getComponentAt(0));
        Assertions.assertEquals(div2, layout.getComponentAt(1));
        Assertions.assertEquals(div1, layout.getComponentAt(2));
    }

    @Test
    void addComponentAtIndex_added_componentSlotIsSet() {
        Div div1 = new Div();
        layout.addToMiddle(div1);

        Div div2 = new Div();
        layout.addToMiddle(div2);

        Div div3 = new Div();
        layout.addComponentAtIndex(1, div3);

        Assertions.assertEquals(div3, layout.getComponentAt(1));
        Assertions.assertEquals("middle",
                div1.getElement().getAttribute("slot"));
    }

    @Test
    void addComponentAtIndex_movedToStart_componentSlotRemoved() {
        Div div1 = new Div();
        layout.addToStart(div1);

        Div div2 = new Div();
        layout.addToMiddle(div2);

        layout.addComponentAtIndex(0, div2);

        Assertions.assertEquals(div2, layout.getComponentAt(0));
        Assertions.assertNull(div2.getElement().getAttribute("slot"));
    }

    @Test
    void addComponentAtIndex_movedFromStart_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToStart(div1);

        Div div2 = new Div();
        layout.addToMiddle(div2);

        Div div3 = new Div();
        layout.addToEnd(div3);

        layout.addComponentAtIndex(1, div1);
        Assertions.assertEquals("middle",
                div1.getElement().getAttribute("slot"));

        layout.addComponentAtIndex(2, div1);
        Assertions.assertEquals("end", div1.getElement().getAttribute("slot"));
    }

    @Test
    void addComponentAtIndex_indexEqualsChildCount_addedToLatestSlot_start1() {
        Div addedAtIndex = new Div();

        layout.addComponentAtIndex(layout.getComponentCount(), addedAtIndex);
        Assertions.assertNull(addedAtIndex.getElement().getAttribute("slot"));
    }

    @Test
    void addComponentAtIndex_indexEqualsChildCount_addedToLatestSlot_start2() {
        layout.addToStart(new Div());

        Div addedAtIndex = new Div();
        layout.addComponentAtIndex(layout.getComponentCount(), addedAtIndex);
        Assertions.assertNull(addedAtIndex.getElement().getAttribute("slot"));
    }

    @Test
    void addComponentAtIndex_indexEqualsChildCount_addedToLatestSlot_middle() {
        layout.addToMiddle(new Div());

        Div addedAtIndex = new Div();
        layout.addComponentAtIndex(layout.getComponentCount(), addedAtIndex);
        Assertions.assertEquals("middle",
                addedAtIndex.getElement().getAttribute("slot"));
    }

    @Test
    void addComponentAtIndex_indexEqualsChildCount_addedToLatestSlot_end() {
        layout.addToEnd(new Div());

        Div addedAtIndex = new Div();
        layout.addComponentAtIndex(layout.getComponentCount(), addedAtIndex);
        Assertions.assertEquals("end",
                addedAtIndex.getElement().getAttribute("slot"));
    }

    @Test
    void add_sameComponentAddedTwice_doesNotThrow() {
        Div div1 = new Div();
        Div div2 = new Div();

        layout.add(div1);
        layout.add(div1, div2);

        Assertions.assertEquals(div1, layout.getComponentAt(0));
        Assertions.assertEquals(div2, layout.getComponentAt(1));
    }

    @Test
    void add_sameComponentAddedTwice_changeOrder_doesNotThrow() {
        Div div1 = new Div();
        Div div2 = new Div();

        layout.add(div1);
        layout.add(div2, div1);

        Assertions.assertEquals(div2, layout.getComponentAt(0));
        Assertions.assertEquals(div1, layout.getComponentAt(1));
    }

    @Test
    void addToMiddle_sameComponentAddedTwice_doesNotThrow() {
        Div div1 = new Div();
        Div div2 = new Div();

        layout.addToMiddle(div1);
        layout.addToMiddle(div1, div2);

        Assertions.assertEquals(div1, layout.getComponentAt(0));
        Assertions.assertEquals(div2, layout.getComponentAt(1));
    }

    @Test
    void addToMiddle_sameComponentAddedTwice_changeOrder_doesNotThrow() {
        Div div1 = new Div();
        Div div2 = new Div();

        layout.addToMiddle(div1);
        layout.addToMiddle(div2, div1);

        Assertions.assertEquals(div2, layout.getComponentAt(0));
        Assertions.assertEquals(div1, layout.getComponentAt(1));
    }

    @Test
    void addComponentAtIndex_firstComponentAdded() {
        Div div1 = new Div();
        layout.addComponentAtIndex(0, div1);

        Assertions.assertEquals(div1, layout.getComponentAt(0));
    }

    @Test
    void addToMiddle_textNodeAsComponent_throws() {
        Text textNode = new Text("Text");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> layout.addToMiddle(textNode));
    }

    @Test
    void addToEnd_textNodeAsComponent_throws() {
        Text textNode = new Text("Text");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> layout.addToEnd(textNode));
    }

    @Test
    void addToMiddle_withNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> layout.addToMiddle((Component) null));
    }

    @Test
    void addToEnd_withNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> layout.addToEnd((Component) null));
    }

    @Test
    void addToMiddle_withAnyNullValue_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> layout.addToMiddle(new Div(), (Component) null));
    }

    @Test
    void addToEnd_withAnyNullValue_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> layout.addToEnd(new Div(), (Component) null));
    }
}
