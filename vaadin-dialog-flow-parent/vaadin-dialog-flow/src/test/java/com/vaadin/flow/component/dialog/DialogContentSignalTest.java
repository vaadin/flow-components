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
package com.vaadin.flow.component.dialog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class DialogContentSignalTest extends AbstractSignalsTest {

    private Dialog dialog;

    @BeforeEach
    void setup() {
        dialog = new Dialog();
        UI.getCurrent().add(dialog);
    }

    @Test
    void textBindingActive_addComponentAtIndex_throws() {
        dialog.getElement().bindText(new ValueSignal<>("text"));

        var component = new Span();
        Assertions.assertThrows(BindingActiveException.class,
                () -> dialog.addComponentAtIndex(0, component));
        Assertions.assertFalse(component.isAttached());
    }

    @Test
    void textBindingActive_getContentAdd_throws() {
        dialog.getElement().bindText(new ValueSignal<>("text"));

        var component = new Span();
        Assertions.assertThrows(BindingActiveException.class,
                () -> dialog.getContent().add(component));
        Assertions.assertFalse(component.isAttached());
    }
}
