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
package com.vaadin.flow.component.confirmdialog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class ConfirmDialogSignalTest extends AbstractSignalsTest {
    private ConfirmDialog dialog;

    @BeforeEach
    void setUp() {
        dialog = new ConfirmDialog();
        ui.add(dialog);
    }

    @Test
    void bindWidth_widthSynchronized() {
        var signal = new ValueSignal<>("400px");

        dialog.bindWidth(signal);
        Assertions.assertEquals("400px", dialog.getWidth());

        signal.set("500px");
        Assertions.assertEquals("500px", dialog.getWidth());
    }

    @Test
    void bindWidth_detachAndReattach_widthSynchronizedWhenAttached() {
        var signal = new ValueSignal<>("400px");

        dialog.bindWidth(signal);
        Assertions.assertEquals("400px", dialog.getWidth());

        dialog.removeFromParent();

        signal.set("500px");
        Assertions.assertEquals("400px", dialog.getWidth());

        ui.add(dialog);
        Assertions.assertEquals("500px", dialog.getWidth());
    }

    @Test
    void bindWidth_setWidthWhileBound_throws() {
        var signal = new ValueSignal<>("400px");

        dialog.bindWidth(signal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> dialog.setWidth("500px"));
    }

    @Test
    void bindWidth_bindWidthWhileBound_throws() {
        var signal = new ValueSignal<>("400px");

        dialog.bindWidth(signal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> dialog.bindWidth(new ValueSignal<>("500px")));
    }

    @Test
    void bindHeight_heightSynchronized() {
        var signal = new ValueSignal<>("400px");

        dialog.bindHeight(signal);
        Assertions.assertEquals("400px", dialog.getHeight());

        signal.set("500px");
        Assertions.assertEquals("500px", dialog.getHeight());
    }

    @Test
    void bindHeight_detachAndReattach_heightSynchronizedWhenAttached() {
        var signal = new ValueSignal<>("400px");

        dialog.bindHeight(signal);
        Assertions.assertEquals("400px", dialog.getHeight());

        dialog.removeFromParent();

        signal.set("500px");
        Assertions.assertEquals("400px", dialog.getHeight());

        ui.add(dialog);
        Assertions.assertEquals("500px", dialog.getHeight());
    }

    @Test
    void bindHeight_setHeightWhileBound_throws() {
        var signal = new ValueSignal<>("400px");

        dialog.bindHeight(signal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> dialog.setHeight("500px"));
    }

    @Test
    void bindHeight_bindHeightWhileBound_throws() {
        var signal = new ValueSignal<>("400px");

        dialog.bindHeight(signal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> dialog.bindHeight(new ValueSignal<>("500px")));
    }

}
