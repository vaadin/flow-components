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
package com.vaadin.flow.component.orderedlayout.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import org.junit.Assert;
import org.junit.Test;

public class FlexComponentTest {

    @Test
    public void setFlexShrink() {
        TestComponent component = new TestComponent();
        Div div = new Div();
        component.add(div);
        component.setFlexShrink(2, div);

        Assert.assertEquals("should set flex-shrink",
                component.getFlexShrink(div), 2, 0);
    }

    @Test
    public void getFlexShrink_returnOneIfNotSet() {
        TestComponent component = new TestComponent();
        Div div = new Div();
        component.add(div);

        Assert.assertEquals("should return 1 if flex-shirk not set",
                component.getFlexShrink(div), 1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setFlexShrink_throwExceptionIfNegative() {
        TestComponent component = new TestComponent();
        Div div = new Div();
        component.add(div);
        component.setFlexShrink(-1, div);
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements FlexComponent {
    }
}
