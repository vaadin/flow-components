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
}
