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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class DialogSignalTest extends AbstractSignalsUnitTest {
    private final Dialog dialog = new Dialog();

    @Test(expected = UnsupportedOperationException.class)
    public void bindWidth_unsupported() {
        var signal = new ValueSignal<>("600px");
        dialog.bindWidth(signal);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void bindHeight_unsupported() {
        var signal = new ValueSignal<>("600px");
        dialog.bindHeight(signal);
    }

    @Test
    public void bindVisible_visibilitySynchronized() {
        var visibleSignal = new ValueSignal<>(false);
        dialog.bindVisible(visibleSignal);
        ui.add(dialog);

        Assert.assertFalse(dialog.isVisible());

        visibleSignal.set(true);
        Assert.assertTrue(dialog.isVisible());
    }

    @Test(expected = BindingActiveException.class)
    public void bindVisible_setVisibleWhileBound_throws() {
        var visibleSignal = new ValueSignal<>(false);
        dialog.bindVisible(visibleSignal);
        ui.add(dialog);

        dialog.setVisible(true);
    }

    @Test
    public void bindVisible_strictModal_updatesModality() {
        var visibleSignal = new ValueSignal<>(false);
        dialog.setModality(ModalityMode.STRICT);
        dialog.bindVisible(visibleSignal);
        ui.add(dialog);
        dialog.open();

        Assert.assertFalse("Dialog should not be modal when invisible",
                isServerSideModal());

        visibleSignal.set(true);
        Assert.assertTrue("Dialog should be modal when visible",
                isServerSideModal());

        visibleSignal.set(false);
        Assert.assertFalse("Dialog should not be modal when invisible",
                isServerSideModal());
    }

    private boolean isServerSideModal() {
        return dialog == ui.getUI().getInternals().getActiveModalComponent();
    }
}
