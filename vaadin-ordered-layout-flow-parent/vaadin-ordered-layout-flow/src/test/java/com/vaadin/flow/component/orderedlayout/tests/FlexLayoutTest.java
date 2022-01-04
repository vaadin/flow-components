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
package com.vaadin.flow.component.orderedlayout.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import org.junit.Assert;
import org.junit.Test;

public class FlexLayoutTest {

    @Test
    public void replace_nullToComponent_appendAsResult() {
        FlexLayout layout = new FlexLayout();
        layout.add(new Label());
        Div div = new Div();
        layout.replace(null, div);
        Assert.assertEquals(div, layout.getComponentAt(1));
    }

    @Test
    public void replace_componentToNull_removeAsResult() {
        FlexLayout layout = new FlexLayout();
        layout.add(new Label());
        Div div = new Div();
        layout.add(div);
        layout.replace(div, null);
        Assert.assertEquals(1, layout.getComponentCount());
    }

    @Test
    public void replace_keepAlignmentSelf() {
        FlexLayout layout = new FlexLayout();
        Div div = new Div();
        layout.add(div);
        layout.setAlignSelf(Alignment.END, div);

        Label label = new Label();
        layout.replace(div, label);
        Assert.assertEquals(Alignment.END, layout.getAlignSelf(label));
    }

    @Test
    public void replace_keepFlexGrow() {
        FlexLayout layout = new FlexLayout();
        Div div = new Div();
        layout.add(div);
        layout.setFlexGrow(1.1d, div);

        Label label = new Label();
        layout.replace(div, label);
        Assert.assertEquals(1.1d, layout.getFlexGrow(label), Double.MIN_VALUE);
    }

    @Test
    public void testFlexLayout_setAndUnsetAlignContent() {
        FlexLayout layout = new FlexLayout();
        FlexLayout.ContentAlignment contentAlignment = FlexLayout.ContentAlignment.CENTER;
        layout.setAlignContent(contentAlignment);

        Assert.assertEquals("should set align-content",
                layout.getAlignContent(), contentAlignment);

        layout.setAlignContent(null);
        Assert.assertEquals("should return stretch if no align-content set",
                layout.getAlignContent(), FlexLayout.ContentAlignment.STRETCH);
    }

    @Test
    public void testFlexLayout_setAndRemoveFlexBasis() {
        FlexLayout layout = new FlexLayout();
        Div div = new Div();
        layout.add(div);
        layout.setFlexBasis("10px", div);

        Assert.assertEquals("should set flex-basis", layout.getFlexBasis(div),
                "10px");

        layout.setFlexBasis(null, div);
        Assert.assertNull("should remove flex-basis from component",
                layout.getFlexBasis(div));
    }

    @Test
    public void testFlexLayout_setAndUnsetFlexDirection() {
        FlexLayout layout = new FlexLayout();
        FlexLayout.FlexDirection direction = FlexLayout.FlexDirection.ROW_REVERSE;
        layout.setFlexDirection(direction);

        Assert.assertEquals("should set flex-direction",
                layout.getFlexDirection(layout), direction);

        layout.setFlexDirection(null);
        Assert.assertEquals("should return row if no flex-direction set",
                layout.getFlexDirection(layout), FlexLayout.FlexDirection.ROW);
    }

    @Test
    public void testFlexLayout_setFlexShrink() {
        FlexLayout layout = new FlexLayout();
        Div div = new Div();
        layout.add(div);
        layout.setFlexShrink(2, div);

        Assert.assertEquals("should set flex-shrink", layout.getFlexShrink(div),
                2, 0);
    }

    @Test
    public void testFlexLayout_getFlexShrink_returnOneIfNotSet() {
        FlexLayout layout = new FlexLayout();
        Div div = new Div();
        layout.add(div);

        Assert.assertEquals("should return 1 if flex-shirk not set",
                layout.getFlexShrink(div), 1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFlexLayout_setFlexShrink_throwExceptionIfNegative() {
        FlexLayout layout = new FlexLayout();
        Div div = new Div();
        layout.add(div);
        layout.setFlexShrink(-1, div);
    }

    @Test
    public void testFlexLayout_setAndUnsetOrder() {
        FlexLayout layout = new FlexLayout();
        Div div = new Div();
        layout.add(div);

        Assert.assertEquals("should return 0 if no order set",
                layout.getOrder(div), 0);

        layout.setOrder(1, div);
        Assert.assertEquals("should set order", layout.getOrder(div), 1);

        layout.setOrder(0, div);
        Assert.assertEquals("should unset order", layout.getOrder(div), 0);
    }
}
