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

import com.vaadin.flow.component.html.Div;

class ConfirmDialogChildrenTest {

    private ConfirmDialog dialog;

    @BeforeEach
    void setup() {
        dialog = new ConfirmDialog();
    }

    @Test
    void add_removeAll_componentIsRemoved() {
        Div div = new Div();
        dialog.add(div);

        dialog.removeAll();

        Assertions.assertNull(div.getElement().getParent());
    }

    @Test
    void setText_removeAll_componentIsRemoved() {
        Div div = new Div();
        dialog.setText(div);

        dialog.removeAll();

        Assertions.assertNull(div.getElement().getParent());
    }

    @Test
    void setHeader_removeAll_componentIsNotRemoved() {
        Div div = new Div();
        dialog.setHeader(div);

        dialog.removeAll();

        Assertions.assertEquals(dialog.getElement(),
                div.getElement().getParent());
    }

    @Test
    void setConfirmButton_removeAll_componentIsNotRemoved() {
        Div div = new Div();
        dialog.setConfirmButton(div);

        dialog.removeAll();

        Assertions.assertEquals(dialog.getElement(),
                div.getElement().getParent());
    }

    @Test
    void setCancelButton_removeAll_componentIsNotRemoved() {
        Div div = new Div();
        dialog.setCancelButton(div);

        dialog.removeAll();

        Assertions.assertEquals(dialog.getElement(),
                div.getElement().getParent());
    }

    @Test
    void setRejectButton_removeAll_componentIsNotRemoved() {
        Div div = new Div();
        dialog.setRejectButton(div);

        dialog.removeAll();

        Assertions.assertEquals(dialog.getElement(),
                div.getElement().getParent());
    }
}
