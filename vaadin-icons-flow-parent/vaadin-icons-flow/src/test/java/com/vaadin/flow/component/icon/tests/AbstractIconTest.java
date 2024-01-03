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
package com.vaadin.flow.component.icon.tests;

import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.dom.ElementConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AbstractIconTest {
    private AbstractIcon abstractIcon;

    @Before
    public void init() {
        abstractIcon = new AbstractIcon() {
            @Override
            public void setColor(String color) {
                this.getStyle().setColor(color);
            }

            @Override
            public String getColor() {
                return this.getStyle().get("color");
            }
        };
    }

    @Test
    public void setSize() {
        abstractIcon.setSize("100px");

        Assert.assertEquals("100px",
                abstractIcon.getStyle().get(ElementConstants.STYLE_HEIGHT));
        Assert.assertEquals("100px",
                abstractIcon.getStyle().get(ElementConstants.STYLE_WIDTH));

        abstractIcon.setSize(null);

        Assert.assertNull(
                abstractIcon.getStyle().get(ElementConstants.STYLE_HEIGHT));
        Assert.assertNull(
                abstractIcon.getStyle().get(ElementConstants.STYLE_WIDTH));
    }
}
