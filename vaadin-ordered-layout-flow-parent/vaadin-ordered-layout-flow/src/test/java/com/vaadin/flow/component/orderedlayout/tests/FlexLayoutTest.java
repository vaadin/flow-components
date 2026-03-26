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
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

class FlexLayoutTest {

    @Test
    void replace_nullToComponent_appendAsResult() {
        FlexLayout layout = new FlexLayout();
        layout.add(new Span());
        Div div = new Div();
        layout.replace(null, div);
        Assertions.assertEquals(div, layout.getComponentAt(1));
    }

    @Test
    void replace_componentToNull_removeAsResult() {
        FlexLayout layout = new FlexLayout();
        layout.add(new Span());
        Div div = new Div();
        layout.add(div);
        layout.replace(div, null);
        Assertions.assertEquals(1, layout.getComponentCount());
    }

    @Test
    void replace_keepAlignmentSelf() {
        FlexLayout layout = new FlexLayout();
        Div div = new Div();
        layout.add(div);
        layout.setAlignSelf(Alignment.END, div);

        Span span = new Span();
        layout.replace(div, span);
        Assertions.assertEquals(Alignment.END, layout.getAlignSelf(span));
    }

    @Test
    void replace_keepFlexGrow() {
        FlexLayout layout = new FlexLayout();
        Div div = new Div();
        layout.add(div);
        layout.setFlexGrow(1.1d, div);

        Span span = new Span();
        layout.replace(div, span);
        Assertions.assertEquals(1.1d, layout.getFlexGrow(span),
                Double.MIN_VALUE);
    }

    @Test
    void testFlexLayout_setAndUnsetAlignContent() {
        FlexLayout layout = new FlexLayout();
        FlexLayout.ContentAlignment contentAlignment = FlexLayout.ContentAlignment.CENTER;
        layout.setAlignContent(contentAlignment);

        Assertions.assertEquals(contentAlignment, layout.getAlignContent(),
                "should set align-content");

        layout.setAlignContent(null);
        Assertions.assertEquals(FlexLayout.ContentAlignment.STRETCH,
                layout.getAlignContent(),
                "should return stretch if no align-content set");
    }

    @Test
    void testFlexLayout_setAndRemoveFlexBasis() {
        FlexLayout layout = new FlexLayout();
        Div div = new Div();
        layout.add(div);
        layout.setFlexBasis("10px", div);

        Assertions.assertEquals("10px", layout.getFlexBasis(div),
                "should set flex-basis");

        layout.setFlexBasis(null, div);
        Assertions.assertNull(layout.getFlexBasis(div),
                "should remove flex-basis from component");
    }

    @Test
    void testFlexLayout_setAndUnsetFlexDirection() {
        FlexLayout layout = new FlexLayout();
        FlexLayout.FlexDirection direction = FlexLayout.FlexDirection.ROW_REVERSE;
        layout.setFlexDirection(direction);

        Assertions.assertEquals(direction, layout.getFlexDirection(),
                "should set flex-direction");

        layout.setFlexDirection(null);
        Assertions.assertEquals(FlexLayout.FlexDirection.ROW,
                layout.getFlexDirection(),
                "should return row if no flex-direction set");
    }

    @Test
    void testFlexLayout_setAndUnsetOrder() {
        FlexLayout layout = new FlexLayout();
        Div div = new Div();
        layout.add(div);

        Assertions.assertEquals(0, layout.getOrder(div),
                "should return 0 if no order set");

        layout.setOrder(1, div);
        Assertions.assertEquals(1, layout.getOrder(div), "should set order");

        layout.setOrder(0, div);
        Assertions.assertEquals(0, layout.getOrder(div), "should unset order");
    }
}
