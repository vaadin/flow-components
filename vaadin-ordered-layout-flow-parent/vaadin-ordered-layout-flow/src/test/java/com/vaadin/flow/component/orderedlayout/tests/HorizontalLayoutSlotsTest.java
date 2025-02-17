/*
 * Copyright 2000-2025 Vaadin Ltd.
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
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class HorizontalLayoutSlotsTest {
    private HorizontalLayout layout;
    private UI ui;

    @Before
    public void setup() {
        layout = new HorizontalLayout();
        ui = new UI();
        UI.setCurrent(ui);
        ui.add(layout);
    }

    @Test
    public void addToStart_componentHasNoSlot() {
        Div div = new Div();
        layout.addToStart(div);
        Assert.assertEquals(div, layout.getComponentAt(0));
        Assert.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    public void addToMiddle_componentHasMiddleSlot() {
        Div div = new Div();
        layout.addToMiddle(div);
        Assert.assertEquals(div, layout.getComponentAt(0));
        Assert.assertEquals(div.getElement().getAttribute("slot"), "middle");
    }

    @Test
    public void addToEnd_componentHasEndSlot() {
        Div div = new Div();
        layout.addToEnd(div);
        Assert.assertEquals(div, layout.getComponentAt(0));
        Assert.assertEquals(div.getElement().getAttribute("slot"), "end");
    }

    @Test
    public void addToMiddle_remove_componentHasNoSlot() {
        Div div = new Div();
        layout.addToMiddle(div);

        layout.remove(div);
        Assert.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    public void addToMiddle_removeAll_componentHasNoSlot() {
        Div div = new Div();
        layout.addToMiddle(div);

        layout.removeAll();
        Assert.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    public void addToMiddle_removeFromParent_componentHasNoSlot() {
        Div div = new Div();
        layout.addToMiddle(div);

        div.getElement().removeFromParent();
        Assert.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    public void addToEnd_remove_componentHasNoSlot() {
        Div div = new Div();
        layout.addToEnd(div);

        layout.remove(div);
        Assert.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    public void addToEnd_removeFromParent_componentHasNoSlot() {
        Div div = new Div();
        layout.addToEnd(div);

        div.getElement().removeFromParent();
        Assert.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    public void addToEnd_removeAll_componentHasNoSlot() {
        Div div = new Div();
        layout.addToEnd(div);

        layout.removeAll();
        Assert.assertNull(div.getElement().getAttribute("slot"));
    }

    @Test
    public void addToStart_addToMiddle_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToStart(div1);

        Div div2 = new Div();
        layout.addToMiddle(div2);

        layout.replace(div1, div2);

        Assert.assertEquals(div1.getElement().getAttribute("slot"), "middle");
        Assert.assertNull(div2.getElement().getAttribute("slot"));
    }

    @Test
    public void addToMiddle_addToStart_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToMiddle(div1);

        Div div2 = new Div();
        layout.addToStart(div2);

        layout.replace(div1, div2);

        Assert.assertNull(div1.getElement().getAttribute("slot"));
        Assert.assertEquals(div2.getElement().getAttribute("slot"), "middle");
    }

    @Test
    public void addToStart_addToEnd_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToStart(div1);

        Div div2 = new Div();
        layout.addToEnd(div2);

        layout.replace(div1, div2);

        Assert.assertEquals(div1.getElement().getAttribute("slot"), "end");
        Assert.assertNull(div2.getElement().getAttribute("slot"));
    }

    @Test
    public void addToEnd_addToStart_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToEnd(div1);

        Div div2 = new Div();
        layout.addToStart(div2);

        layout.replace(div1, div2);

        Assert.assertNull(div1.getElement().getAttribute("slot"));
        Assert.assertEquals(div2.getElement().getAttribute("slot"), "end");
    }

    @Test
    public void addToMiddle_addToEnd_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToMiddle(div1);

        Div div2 = new Div();
        layout.addToEnd(div2);

        layout.replace(div1, div2);

        Assert.assertEquals(div1.getElement().getAttribute("slot"), "end");
        Assert.assertEquals(div2.getElement().getAttribute("slot"), "middle");
    }

    @Test
    public void addToEnd_addToMiddle_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToEnd(div1);

        Div div2 = new Div();
        layout.addToMiddle(div2);

        layout.replace(div1, div2);

        Assert.assertEquals(div1.getElement().getAttribute("slot"), "middle");
        Assert.assertEquals(div2.getElement().getAttribute("slot"), "end");
    }

    @Test
    public void addToMiddle_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToMiddle(div1);

        Div div2 = new Div();

        layout.replace(div1, div2);

        Assert.assertNull(div1.getElement().getAttribute("slot"));
        Assert.assertEquals(div2.getElement().getAttribute("slot"), "middle");
    }

    @Test
    public void addToEnd_replace_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToEnd(div1);

        Div div2 = new Div();

        layout.replace(div1, div2);

        Assert.assertNull(div1.getElement().getAttribute("slot"));
        Assert.assertEquals(div2.getElement().getAttribute("slot"), "end");
    }

    @Test
    public void addToSlots_indexesAreSlotsOrder() {
        Div div1 = new Div();
        layout.addToEnd(div1);

        Div div2 = new Div();
        layout.addToMiddle(div2);

        Div div3 = new Div();
        layout.addToStart(div3);

        Assert.assertEquals(div3, layout.getComponentAt(0));
        Assert.assertEquals(div2, layout.getComponentAt(1));
        Assert.assertEquals(div1, layout.getComponentAt(2));
    }

    @Test
    public void addComponentAtIndex_added_componentSlotIsSet() {
        Div div1 = new Div();
        layout.addToMiddle(div1);

        Div div2 = new Div();
        layout.addToMiddle(div2);

        Div div3 = new Div();
        layout.addComponentAtIndex(1, div3);

        Assert.assertEquals(div3, layout.getComponentAt(1));
        Assert.assertEquals(div1.getElement().getAttribute("slot"), "middle");
    }

    @Test
    public void addComponentAtIndex_movedToStart_componentSlotRemoved() {
        Div div1 = new Div();
        layout.addToStart(div1);

        Div div2 = new Div();
        layout.addToMiddle(div2);

        layout.addComponentAtIndex(0, div2);

        Assert.assertEquals(div2, layout.getComponentAt(0));
        Assert.assertNull(div2.getElement().getAttribute("slot"));
    }

    @Test
    public void addComponentAtIndex_movedFromStart_componentSlotUpdated() {
        Div div1 = new Div();
        layout.addToStart(div1);

        Div div2 = new Div();
        layout.addToMiddle(div2);

        Div div3 = new Div();
        layout.addToEnd(div3);

        layout.addComponentAtIndex(1, div1);
        Assert.assertEquals(div1.getElement().getAttribute("slot"), "middle");

        layout.addComponentAtIndex(2, div1);
        Assert.assertEquals(div1.getElement().getAttribute("slot"), "end");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addToMiddle_textNodeAsComponent_throws() {
        Text textNode = new Text("Text");
        layout.addToMiddle(textNode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addToEnd_textNodeAsComponent_throws() {
        Text textNode = new Text("Text");
        layout.addToEnd(textNode);
    }

    @Test(expected = NullPointerException.class)
    public void addToMiddle_withNull_throws() {
        layout.addToMiddle((Component) null);
    }

    @Test(expected = NullPointerException.class)
    public void addToEnd_withNull_throws() {
        layout.addToEnd((Component) null);
    }

    @Test(expected = NullPointerException.class)
    public void addToMiddle_withAnyNullValue_throws() {
        layout.addToMiddle(new Div(), (Component) null);
    }

    @Test(expected = NullPointerException.class)
    public void addToEnd_withAnyNullValue_throws() {
        layout.addToEnd(new Div(), (Component) null);
    }
}
