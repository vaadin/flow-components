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
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class DialogSignalTest extends AbstractSignalsTest {
    private final Dialog dialog = new Dialog();

    @Test
    void bindWidth_unsupported() {
        var signal = new ValueSignal<>("600px");
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> dialog.bindWidth(signal));
    }

    @Test
    void bindHeight_unsupported() {
        var signal = new ValueSignal<>("600px");
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> dialog.bindHeight(signal));
    }

    @Test
    void bindVisible_visibilitySynchronized() {
        var visibleSignal = new ValueSignal<>(false);
        dialog.bindVisible(visibleSignal);
        ui.add(dialog);

        Assertions.assertFalse(dialog.isVisible());

        visibleSignal.set(true);
        Assertions.assertTrue(dialog.isVisible());
    }

    @Test
    void bindVisible_setVisibleWhileBound_throws() {
        var visibleSignal = new ValueSignal<>(false);
        dialog.bindVisible(visibleSignal);
        ui.add(dialog);

        Assertions.assertThrows(BindingActiveException.class,
                () -> dialog.setVisible(true));
    }

    @Test
    void bindVisible_strictModal_updatesModality() {
        var visibleSignal = new ValueSignal<>(false);
        dialog.setModality(ModalityMode.STRICT);
        dialog.bindVisible(visibleSignal);
        ui.add(dialog);
        dialog.open();

        Assertions.assertFalse(isServerSideModal(),
                "Dialog should not be modal when invisible");

        visibleSignal.set(true);
        Assertions.assertTrue(isServerSideModal(),
                "Dialog should be modal when visible");

        visibleSignal.set(false);
        Assertions.assertFalse(isServerSideModal(),
                "Dialog should not be modal when invisible");
    }

    private boolean isServerSideModal() {
        return dialog == ui.getUI().getInternals().getActiveModalComponent();
    }
}
