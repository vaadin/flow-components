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
package com.vaadin.flow.component.splitlayout.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutI18n;

class SplitLayoutI18nTest {

    private SplitLayout splitLayout;
    private SplitLayoutI18n i18n;

    @BeforeEach
    void setup() {
        splitLayout = new SplitLayout();
        i18n = new SplitLayoutI18n();
    }

    @Test
    void getI18n_returnsNull() {
        Assertions.assertNull(splitLayout.getI18n());
    }

    @Test
    void setI18n_getI18n() {
        splitLayout.setI18n(i18n);
        Assertions.assertSame(i18n, splitLayout.getI18n());
    }

    @Test
    void setI18n_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> splitLayout.setI18n(null));
    }

    @Test
    void setSeparator_getSeparator() {
        Assertions.assertNull(i18n.getSeparator());
        i18n.setSeparator("Resize separator");
        Assertions.assertEquals("Resize separator", i18n.getSeparator());
    }

    @Test
    void separatorSetter_returnsI18n() {
        Assertions.assertSame(i18n, i18n.setSeparator("foo"));
    }

    @Test
    void setI18n_propertySet() {
        splitLayout.setI18n(i18n.setSeparator("Resize separator"));
        Assertions.assertEquals("{\"separator\":\"Resize separator\"}",
                splitLayout.getElement().getProperty("i18n"));
    }
}
