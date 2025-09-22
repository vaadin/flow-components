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
package com.vaadin.flow.component.popover;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.VaadinSession;

/**
 * @author Vaadin Ltd.
 */
public class PopoverAutoAddTest {
    private UI ui = new UI();
    private VaadinSession session;

    @Before
    public void setup() {
        UI.setCurrent(ui);

        session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
        VaadinSession.setCurrent(session);
        Mockito.when(session.getErrorHandler()).thenReturn(event -> {
            throw new RuntimeException(event.getThrowable());
        });
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void setTarget_autoAdded() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);

        fakeClientResponse();
        Assert.assertEquals(ui.getElement(), popover.getElement().getParent());
    }

    @Test
    public void setTarget_clearTarget_autoRemoved() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);
        fakeClientResponse();

        popover.setTarget(null);

        fakeClientResponse();
        Assert.assertNull(popover.getElement().getParent());
    }

    @Test
    public void setTarget_detachTarget_autoRemoved() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);
        fakeClientResponse();

        ui.remove(target);

        fakeClientResponse();
        Assert.assertNull(popover.getElement().getParent());
    }

    @Test
    public void setTarget_removeAll_noException() {
        Div target = new Div();
        Popover popover = new Popover();
        popover.setTarget(target);
        ui.add(target);

        ui.removeAll();

        Assert.assertNull(popover.getElement().getParent());
        Assert.assertEquals(0, ui.getChildren().count());
    }

    @Test
    public void setTarget_changeTarget_notAutoRemoved() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);
        fakeClientResponse();

        Div other = new Div();
        ui.add(other);
        popover.setTarget(other);
        fakeClientResponse();

        Assert.assertEquals(ui.getElement(), popover.getElement().getParent());
    }

    @Test
    public void setTarget_changeTarget_detachOldTarget_notAutoRemoved() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);
        fakeClientResponse();

        Div other = new Div();
        ui.add(other);
        popover.setTarget(other);
        fakeClientResponse();

        ui.remove(target);
        fakeClientResponse();
        Assert.assertEquals(ui.getElement(), popover.getElement().getParent());
    }

    @Test
    public void setTarget_changeToDetachedTarget_autoRemoved() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);
        fakeClientResponse();

        Div other = new Div();
        popover.setTarget(other);
        fakeClientResponse();

        Assert.assertNull(popover.getElement().getParent());
    }

    @Test
    public void setTarget_changeUI_autoAdded() {
        Popover popover = new Popover();
        Div target = new Div();
        popover.setTarget(target);
        ui.add(target);
        fakeClientResponse();

        // Create a new UI and move the component to it (@PreserveOnRefresh)
        ui = new UI();
        UI.setCurrent(ui);
        ui.getInternals().setSession(session);
        target.getElement().removeFromTree(false);
        ui.add(target);

        fakeClientResponse();
        Assert.assertEquals(ui.getElement(), popover.getElement().getParent());
    }

    @Test
    public void setTarget_openModal_popoverIsAttachedToUi() {
        Div target = new Div();
        Popover popover = new Popover();
        popover.setTarget(target);
        ui.add(target);

        Div modalElement = new Div();
        ui.setChildComponentModal(modalElement, true);
        fakeClientResponse();

        Assert.assertEquals(ui, popover.getParent().orElseThrow());
    }

    private void fakeClientResponse() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
