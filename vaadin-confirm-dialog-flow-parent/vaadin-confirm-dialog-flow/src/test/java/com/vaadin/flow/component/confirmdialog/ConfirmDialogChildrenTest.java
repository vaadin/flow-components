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
package com.vaadin.flow.component.confirmdialog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.VaadinSession;

public class ConfirmDialogChildrenTest {

    private UI ui = new UI();
    private ConfirmDialog dialog;

    @Before
    public void setup() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);

        dialog = new ConfirmDialog();
        ui.add(dialog);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void add_removeAll_componentIsRemoved() {
        Div div = new Div();
        dialog.add(div);

        dialog.removeAll();

        Assert.assertNull(div.getElement().getParent());
    }

    @Test
    public void setText_removeAll_componentIsRemoved() {
        Div div = new Div();
        dialog.setText(div);

        dialog.removeAll();

        Assert.assertNull(div.getElement().getParent());
    }

    @Test
    public void setHeader_removeAll_componentIsNotRemoved() {
        Div div = new Div();
        dialog.setHeader(div);

        dialog.removeAll();

        Assert.assertEquals(dialog.getElement(), div.getElement().getParent());
    }

    @Test
    public void setConfirmButton_removeAll_componentIsNotRemoved() {
        Div div = new Div();
        dialog.setConfirmButton(div);

        dialog.removeAll();

        Assert.assertEquals(dialog.getElement(), div.getElement().getParent());
    }

    @Test
    public void setCancelButton_removeAll_componentIsNotRemoved() {
        Div div = new Div();
        dialog.setCancelButton(div);

        dialog.removeAll();

        Assert.assertEquals(dialog.getElement(), div.getElement().getParent());
    }

    @Test
    public void setRejectButton_removeAll_componentIsNotRemoved() {
        Div div = new Div();
        dialog.setRejectButton(div);

        dialog.removeAll();

        Assert.assertEquals(dialog.getElement(), div.getElement().getParent());
    }
}
