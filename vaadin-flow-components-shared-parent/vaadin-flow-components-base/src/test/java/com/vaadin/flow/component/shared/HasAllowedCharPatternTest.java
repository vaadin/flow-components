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
package com.vaadin.flow.component.shared;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

public class HasAllowedCharPatternTest {

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void initialValue() {
        Assert.assertEquals(component.getAllowedCharPattern(), "");
    }

    @Test
    public void changeValue() {
        component.setAllowedCharPattern("[-+\\d]");
        Assert.assertEquals(
                component.getElement().getProperty("allowedCharPattern"),
                "[-+\\d]");

        component.setAllowedCharPattern(null);
        Assert.assertEquals(
                component.getElement().getProperty("allowedCharPattern"), "");
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements HasAllowedCharPattern {
    }
}
