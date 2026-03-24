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
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.tests.MockUIExtension;

class DialogModalityTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final Dialog dialog = new Dialog();

    @Test
    void defaults() {
        Assertions.assertTrue(dialog.isModal());
        Assertions.assertEquals(ModalityMode.VISUAL, dialog.getModality());
        Assertions.assertFalse(
                dialog.getElement().getProperty("modeless", false));
    }

    @Test
    void setModal_isModal() {
        dialog.setModal(true);
        Assertions.assertTrue(dialog.isModal());
        Assertions.assertEquals(ModalityMode.STRICT, dialog.getModality());
        Assertions.assertFalse(
                dialog.getElement().getProperty("modeless", false));

        dialog.setModal(false);
        Assertions.assertFalse(dialog.isModal());
        Assertions.assertEquals(ModalityMode.MODELESS, dialog.getModality());
        Assertions
                .assertTrue(dialog.getElement().getProperty("modeless", false));
    }

    @Test
    void setModality_getModality() {
        dialog.setModality(ModalityMode.STRICT);
        Assertions.assertTrue(dialog.isModal());
        Assertions.assertEquals(ModalityMode.STRICT, dialog.getModality());
        Assertions.assertFalse(
                dialog.getElement().getProperty("modeless", false));

        dialog.setModality(ModalityMode.VISUAL);
        Assertions.assertTrue(dialog.isModal());
        Assertions.assertEquals(ModalityMode.VISUAL, dialog.getModality());
        Assertions.assertFalse(
                dialog.getElement().getProperty("modeless", false));

        dialog.setModality(ModalityMode.MODELESS);
        Assertions.assertFalse(dialog.isModal());
        Assertions.assertEquals(ModalityMode.MODELESS, dialog.getModality());
        Assertions
                .assertTrue(dialog.getElement().getProperty("modeless", false));
    }

    @Test
    void strictModal() {
        // Auto added
        dialog.setModality(ModalityMode.STRICT);
        dialog.open();
        ui.fakeClientCommunication();
        Assertions.assertTrue(isServerSideModal());

        // Manually added
        dialog.close();
        ui.fakeClientCommunication();
        Assertions.assertFalse(isServerSideModal());

        ui.add(dialog);
        dialog.open();
        Assertions.assertTrue(isServerSideModal());
    }

    @Test
    void strictModal_onlyModalWhenOpened() {
        // Regression test to verify that setting modality or visibility does
        // not make the dialog modal before it is opened
        dialog.setModality(ModalityMode.MODELESS);
        dialog.setVisible(false);

        ui.add(dialog);

        dialog.setModality(ModalityMode.STRICT);
        Assertions.assertFalse(isServerSideModal());

        dialog.setVisible(true);
        Assertions.assertFalse(isServerSideModal());

        dialog.open();
        Assertions.assertTrue(isServerSideModal());
    }

    @Test
    void strictModal_toggleVisibility() {
        dialog.setModality(ModalityMode.STRICT);

        ui.add(dialog);
        dialog.open();
        Assertions.assertTrue(isServerSideModal());

        dialog.setVisible(false);
        Assertions.assertFalse(isServerSideModal());

        dialog.setVisible(true);
        Assertions.assertTrue(isServerSideModal());
    }

    @Test
    void strictModal_toggleModality() {
        ui.add(dialog);
        dialog.setModality(ModalityMode.STRICT);
        dialog.open();
        Assertions.assertTrue(isServerSideModal());

        dialog.setModality(ModalityMode.MODELESS);
        Assertions.assertFalse(isServerSideModal());

        dialog.setModality(ModalityMode.STRICT);
        Assertions.assertTrue(isServerSideModal());
    }

    @Test
    void strictModal_toggleOpened() {
        ui.add(dialog);
        dialog.setModality(ModalityMode.STRICT);
        dialog.open();
        Assertions.assertTrue(isServerSideModal());

        dialog.close();
        Assertions.assertFalse(isServerSideModal());

        dialog.open();
        Assertions.assertTrue(isServerSideModal());
    }

    @Test
    void strictModal_toggleAttached() {
        ui.add(dialog);
        dialog.setModality(ModalityMode.STRICT);
        dialog.open();
        Assertions.assertTrue(isServerSideModal());

        dialog.removeFromParent();
        Assertions.assertFalse(isServerSideModal());

        ui.add(dialog);
        Assertions.assertTrue(isServerSideModal());
    }

    @Test
    void strictModal_openSecondDialog_addedToFirstDialog() {
        dialog.setModality(ModalityMode.STRICT);
        ui.add(dialog);
        dialog.open();

        // Auto-added dialog should be added to current modal
        Dialog secondDialog = new Dialog();
        secondDialog.open();
        ui.fakeClientCommunication();

        Assertions.assertEquals(dialog, secondDialog.getParent().orElse(null));
    }

    @Test
    void visualModal() {
        // Auto added
        dialog.setModality(ModalityMode.VISUAL);
        dialog.open();
        ui.fakeClientCommunication();
        Assertions.assertFalse(isServerSideModal());

        // Manually added
        dialog.close();
        ui.fakeClientCommunication();
        Assertions.assertFalse(isServerSideModal());

        ui.add(dialog);
        dialog.open();
        Assertions.assertFalse(isServerSideModal());
    }

    @Test
    void visualModal_openSecondDialog_addedToUI() {
        dialog.setModality(ModalityMode.VISUAL);
        ui.add(dialog);
        dialog.open();

        Dialog secondDialog = new Dialog();
        secondDialog.open();
        ui.fakeClientCommunication();

        Assertions.assertEquals(ui.getUI(),
                secondDialog.getParent().orElse(null));
    }

    @Test
    void modeless() {
        // Auto added
        dialog.setModality(ModalityMode.MODELESS);
        dialog.open();
        ui.fakeClientCommunication();
        Assertions.assertFalse(isServerSideModal());

        // Manually added
        dialog.close();
        ui.fakeClientCommunication();
        Assertions.assertFalse(isServerSideModal());

        ui.add(dialog);
        dialog.open();
        Assertions.assertFalse(isServerSideModal());
    }

    private boolean isServerSideModal() {
        return dialog == ui.getUI().getInternals().getActiveModalComponent();
    }

}
