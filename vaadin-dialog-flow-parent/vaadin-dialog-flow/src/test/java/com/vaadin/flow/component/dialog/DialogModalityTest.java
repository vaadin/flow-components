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
package com.vaadin.flow.component.dialog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

public class DialogModalityTest {
    private final UI ui = new UI();
    private final Dialog dialog = new Dialog();

    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
    }

    @After
    public void teardown() {
        UI.setCurrent(null);
    }

    @Test
    public void defaults() {
        Assert.assertTrue(dialog.isModal());
        Assert.assertEquals(ModalityMode.VISUAL, dialog.getModality());
        Assert.assertFalse(dialog.getElement().getProperty("modeless", false));
    }

    @Test
    public void setModal_isModal() {
        dialog.setModal(true);
        Assert.assertTrue(dialog.isModal());
        Assert.assertEquals(ModalityMode.STRICT, dialog.getModality());
        Assert.assertFalse(dialog.getElement().getProperty("modeless", false));

        dialog.setModal(false);
        Assert.assertFalse(dialog.isModal());
        Assert.assertEquals(ModalityMode.MODELESS, dialog.getModality());
        Assert.assertTrue(dialog.getElement().getProperty("modeless", false));
    }

    @Test
    public void setModality_getModality() {
        dialog.setModality(ModalityMode.STRICT);
        Assert.assertTrue(dialog.isModal());
        Assert.assertEquals(ModalityMode.STRICT, dialog.getModality());
        Assert.assertFalse(dialog.getElement().getProperty("modeless", false));

        dialog.setModality(ModalityMode.VISUAL);
        Assert.assertTrue(dialog.isModal());
        Assert.assertEquals(ModalityMode.VISUAL, dialog.getModality());
        Assert.assertFalse(dialog.getElement().getProperty("modeless", false));

        dialog.setModality(ModalityMode.MODELESS);
        Assert.assertFalse(dialog.isModal());
        Assert.assertEquals(ModalityMode.MODELESS, dialog.getModality());
        Assert.assertTrue(dialog.getElement().getProperty("modeless", false));
    }

    @Test
    public void strictModal() {
        // Auto added
        dialog.setModality(ModalityMode.STRICT);
        dialog.open();
        fakeClientResponse();
        Assert.assertTrue(isServerSideModal());

        // Manually added
        dialog.close();
        fakeClientResponse();
        Assert.assertFalse(isServerSideModal());

        ui.add(dialog);
        dialog.open();
        Assert.assertTrue(isServerSideModal());
    }

    @Test
    public void strictModal_onlyModalWhenOpened() {
        // Regression test to verify that setting modality or visibility does
        // not make the dialog modal before it is opened
        dialog.setModality(ModalityMode.MODELESS);
        dialog.setVisible(false);

        ui.add(dialog);

        dialog.setModality(ModalityMode.STRICT);
        Assert.assertFalse(isServerSideModal());

        dialog.setVisible(true);
        Assert.assertFalse(isServerSideModal());

        dialog.open();
        Assert.assertTrue(isServerSideModal());
    }

    @Test
    public void strictModal_toggleVisibility() {
        dialog.setModality(ModalityMode.STRICT);

        ui.add(dialog);
        dialog.open();
        Assert.assertTrue(isServerSideModal());

        dialog.setVisible(false);
        Assert.assertFalse(isServerSideModal());

        dialog.setVisible(true);
        Assert.assertTrue(isServerSideModal());
    }

    @Test
    public void strictModal_toggleModality() {
        ui.add(dialog);
        dialog.setModality(ModalityMode.STRICT);
        dialog.open();
        Assert.assertTrue(isServerSideModal());

        dialog.setModality(ModalityMode.MODELESS);
        Assert.assertFalse(isServerSideModal());

        dialog.setModality(ModalityMode.STRICT);
        Assert.assertTrue(isServerSideModal());
    }

    @Test
    public void strictModal_toggleOpened() {
        ui.add(dialog);
        dialog.setModality(ModalityMode.STRICT);
        dialog.open();
        Assert.assertTrue(isServerSideModal());

        dialog.close();
        Assert.assertFalse(isServerSideModal());

        dialog.open();
        Assert.assertTrue(isServerSideModal());
    }

    @Test
    public void strictModal_toggleAttached() {
        ui.add(dialog);
        dialog.setModality(ModalityMode.STRICT);
        dialog.open();
        Assert.assertTrue(isServerSideModal());

        dialog.removeFromParent();
        Assert.assertFalse(isServerSideModal());

        ui.add(dialog);
        Assert.assertTrue(isServerSideModal());
    }

    @Test
    public void strictModal_openSecondDialog_addedToFirstDialog() {
        dialog.setModality(ModalityMode.STRICT);
        ui.add(dialog);
        dialog.open();

        // Auto-added dialog should be added to current modal
        Dialog secondDialog = new Dialog();
        secondDialog.open();
        fakeClientResponse();

        Assert.assertEquals(dialog, secondDialog.getParent().orElse(null));
    }

    @Test
    public void visualModal() {
        // Auto added
        dialog.setModality(ModalityMode.VISUAL);
        dialog.open();
        fakeClientResponse();
        Assert.assertFalse(isServerSideModal());

        // Manually added
        dialog.close();
        fakeClientResponse();
        Assert.assertFalse(isServerSideModal());

        ui.add(dialog);
        dialog.open();
        Assert.assertFalse(isServerSideModal());
    }

    @Test
    public void visualModal_openSecondDialog_addedToUI() {
        dialog.setModality(ModalityMode.VISUAL);
        ui.add(dialog);
        dialog.open();

        Dialog secondDialog = new Dialog();
        secondDialog.open();
        fakeClientResponse();

        Assert.assertEquals(ui, secondDialog.getParent().orElse(null));
    }

    @Test
    public void modeless() {
        // Auto added
        dialog.setModality(ModalityMode.MODELESS);
        dialog.open();
        fakeClientResponse();
        Assert.assertFalse(isServerSideModal());

        // Manually added
        dialog.close();
        fakeClientResponse();
        Assert.assertFalse(isServerSideModal());

        ui.add(dialog);
        dialog.open();
        Assert.assertFalse(isServerSideModal());
    }

    private boolean isServerSideModal() {
        return dialog == ui.getInternals().getActiveModalComponent();
    }

    private void fakeClientResponse() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
