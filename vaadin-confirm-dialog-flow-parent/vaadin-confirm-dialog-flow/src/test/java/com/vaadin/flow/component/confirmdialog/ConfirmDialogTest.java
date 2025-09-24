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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;

public class ConfirmDialogTest {

    @Test
    public void setAriaDescribedBy() {
        var confirmDialog = new ConfirmDialog();
        confirmDialog.setAriaDescribedBy("aria-describedby");

        Assert.assertTrue(confirmDialog.getAriaDescribedBy().isPresent());
        Assert.assertEquals("aria-describedby",
                confirmDialog.getAriaDescribedBy().get());

        confirmDialog.setAriaDescribedBy(null);
        Assert.assertTrue(confirmDialog.getAriaDescribedBy().isEmpty());
    }

    @Test
    public void setWidth_getWidth() {
        var confirmDialog = new ConfirmDialog();
        confirmDialog.setWidth("100px");

        Assert.assertEquals("100px", confirmDialog.getWidth());
        Assert.assertEquals("100px",
                confirmDialog.getElement().getProperty("width"));

        confirmDialog.setWidth(null);
        Assert.assertNull(confirmDialog.getWidth());
        Assert.assertNull(confirmDialog.getElement().getProperty("width"));
    }

    @Test
    public void setHeight_getHeight() {
        var confirmDialog = new ConfirmDialog();
        confirmDialog.setHeight("100px");

        Assert.assertEquals("100px", confirmDialog.getHeight());
        Assert.assertEquals("100px",
                confirmDialog.getElement().getProperty("height"));

        confirmDialog.setHeight(null);
        Assert.assertNull(confirmDialog.getHeight());
        Assert.assertNull(confirmDialog.getElement().getProperty("height"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addClosedListener_listenerInvokedOnClose() {
        ConfirmDialog dialog = new ConfirmDialog();
        ComponentEventListener<ConfirmDialog.ClosedEvent> listener = Mockito
                .mock(ComponentEventListener.class);
        dialog.addClosedListener(listener);

        Element element = dialog.getElement();
        dialog.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(new DomEvent(element, "closed",
                        JacksonUtils.createObjectNode()));

        Mockito.verify(listener, Mockito.times(1))
                .onComponentEvent(Mockito.any(ConfirmDialog.ClosedEvent.class));
    }
}
