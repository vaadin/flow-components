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
package com.vaadin.flow.component.textfield.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.textfield.Autocapitalize;
import com.vaadin.flow.component.textfield.HasAutocapitalize;

class HasAutocapitalizeTest {

    @Tag("div")
    public static class HasAutocapitalizeComponent extends Component
            implements HasAutocapitalize {

    }

    @Test
    void defaultValue() {
        HasAutocapitalizeComponent c = new HasAutocapitalizeComponent();
        Autocapitalize autocapitalize = c.getAutocapitalize();
        Assertions.assertNull(autocapitalize);
    }

    @Test
    void emptyValue() {
        HasAutocapitalizeComponent c = new HasAutocapitalizeComponent();
        c.getElement().setAttribute("autocapitalize", "");
        Autocapitalize autocapitalize = c.getAutocapitalize();
        Assertions.assertEquals(Autocapitalize.SENTENCES, autocapitalize);
    }

    @Test
    void noCapitalization() {
        HasAutocapitalizeComponent c = new HasAutocapitalizeComponent();
        c.setAutocapitalize(Autocapitalize.NONE);
        Assertions.assertEquals(Autocapitalize.NONE, c.getAutocapitalize());
    }

    @Test
    void sentencesCapitalization() {
        HasAutocapitalizeComponent c = new HasAutocapitalizeComponent();
        c.setAutocapitalize(Autocapitalize.SENTENCES);
        Assertions.assertEquals(Autocapitalize.SENTENCES,
                c.getAutocapitalize());
    }

    @Test
    void wordsCapitalization() {
        HasAutocapitalizeComponent c = new HasAutocapitalizeComponent();
        c.setAutocapitalize(Autocapitalize.WORDS);
        Assertions.assertEquals(Autocapitalize.WORDS, c.getAutocapitalize());
    }

    @Test
    void charsCapitalization() {
        HasAutocapitalizeComponent c = new HasAutocapitalizeComponent();
        c.setAutocapitalize(Autocapitalize.CHARACTERS);
        Assertions.assertEquals(Autocapitalize.CHARACTERS,
                c.getAutocapitalize());
    }
}
