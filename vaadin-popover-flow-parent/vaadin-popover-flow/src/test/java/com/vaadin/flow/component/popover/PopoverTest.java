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
 *
 */
package com.vaadin.flow.component.popover;

import java.util.Collections;
import java.util.Set;
import elemental.json.JsonArray;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.Text;

/**
 * @author Vaadin Ltd.
 */
public class PopoverTest {
    private Popover popover;

    @Before
    public void setup() {
        popover = new Popover();
    }

    @Test
    public void setFor_getFor() {
        popover.setFor("target-id");
        Assert.assertEquals(popover.getFor(), "target-id");
    }

    @Test
    public void setTarget_getTarget() {
        Div target = new Div();
        popover.setTarget(target);
        Assert.assertEquals(popover.getTarget(), target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setTarget_textNodeAsComponent_throws() {
        Text textNode = new Text("Text");
        popover.setTarget(textNode);
    }

    @Test
    public void setPosition_getPosition() {
        popover.setPosition(PopoverPosition.END);
        Assert.assertEquals("end",
                popover.getElement().getProperty("position"));
        Assert.assertEquals(PopoverPosition.END, popover.getPosition());
    }

    @Test
    public void defaultPosition_equalsNull() {
        Assert.assertEquals(null, popover.getPosition());
    }

    @Test
    public void setFocusDelay_getFocusDelay() {
        Assert.assertEquals(0, popover.getFocusDelay());

        popover.setFocusDelay(1000);
        Assert.assertEquals(1000, popover.getFocusDelay());
        Assert.assertEquals(1000,
                popover.getElement().getProperty("focusDelay", 0));
    }

    @Test
    public void setHoverDelay_getHoverDelay() {
        Assert.assertEquals(0, popover.getHoverDelay());

        popover.setHoverDelay(1000);
        Assert.assertEquals(1000, popover.getHoverDelay());
        Assert.assertEquals(1000,
                popover.getElement().getProperty("hoverDelay", 0));
    }

    @Test
    public void setHideDelay_getHideDelay() {
        Assert.assertEquals(0, popover.getHideDelay());

        popover.setHideDelay(1000);
        Assert.assertEquals(1000, popover.getHideDelay());
        Assert.assertEquals(1000,
                popover.getElement().getProperty("hideDelay", 0));
    }

    @Test
    public void getTrigger_defaultValue_click() {
        Assert.assertEquals(Set.of(PopoverTrigger.CLICK), popover.getTrigger());
    }

    @Test
    public void getTriggerProperty_defaultValue_click() {
        JsonArray jsonArray = (JsonArray) popover.getElement()
                .getPropertyRaw("trigger");
        Assert.assertEquals(1, jsonArray.length());
        Assert.assertEquals("click", jsonArray.get(0).asString());
    }

    @Test
    public void setTrigger_getTrigger() {
        popover.setTrigger(Set.of(PopoverTrigger.HOVER, PopoverTrigger.FOCUS));
        Assert.assertEquals(Set.of(PopoverTrigger.HOVER, PopoverTrigger.FOCUS),
                popover.getTrigger());
    }

    @Test
    public void setTrigger_getTriggerProperty() {
        popover.setTrigger(Set.of(PopoverTrigger.HOVER, PopoverTrigger.FOCUS));
        JsonArray jsonArray = (JsonArray) popover.getElement()
                .getPropertyRaw("trigger");
        Assert.assertEquals(2, jsonArray.length());
        Assert.assertEquals("hover", jsonArray.get(0).asString());
        Assert.assertEquals("focus", jsonArray.get(1).asString());
    }

    @Test
    public void setTrigger_null_getTrigger_emptySet() {
        popover.setTrigger(null);
        Assert.assertEquals(Collections.emptySet(), popover.getTrigger());
    }

    @Test
    public void setTrigger_null_getTriggerProperty_emptyArray() {
        popover.setTrigger(null);
        JsonArray jsonArray = (JsonArray) popover.getElement()
                .getPropertyRaw("trigger");
        Assert.assertEquals(0, jsonArray.length());
    }

    @Test
    public void setTrigger_empty_getTrigger_emptySet() {
        popover.setTrigger(Collections.emptySet());
        Assert.assertEquals(Collections.emptySet(), popover.getTrigger());
    }

    @Test
    public void setTrigger_empty_getTriggerProperty_emptyArray() {
        popover.setTrigger(Collections.emptySet());
        JsonArray jsonArray = (JsonArray) popover.getElement()
                .getPropertyRaw("trigger");
        Assert.assertEquals(0, jsonArray.length());
    }
}
