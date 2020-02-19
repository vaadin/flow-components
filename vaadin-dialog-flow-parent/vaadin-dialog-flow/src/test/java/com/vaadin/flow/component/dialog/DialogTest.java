/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

/**
 * Unit tests for the Dialog.
 */
public class DialogTest {

    private UI ui = new UI();

    @Before
    public void setUp() {
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void createDialogWithComponents_componentsArePartOfGetChildren() {
        Label label1 = new Label("Label 1");
        Label label2 = new Label("Label 2");
        Label label3 = new Label("Label 3");

        Dialog dialog = new Dialog(label1, label2);
        dialog.setWidth("200px");
        dialog.setHeight("100px");

        List<Component> children = dialog.getChildren()
                .collect(Collectors.toList());
        Assert.assertEquals(2, children.size());
        Assert.assertThat(children, CoreMatchers.hasItems(label1, label2));

        dialog.add(label3);
        children = dialog.getChildren().collect(Collectors.toList());
        Assert.assertEquals(3, children.size());
        Assert.assertThat(children,
                CoreMatchers.hasItems(label1, label2, label3));

        dialog.remove(label2);
        children = dialog.getChildren().collect(Collectors.toList());
        Assert.assertEquals(2, children.size());
        Assert.assertThat(children, CoreMatchers.hasItems(label1, label3));

        label1.getElement().removeFromParent();
        children = dialog.getChildren().collect(Collectors.toList());
        Assert.assertEquals(1, children.size());
        Assert.assertThat(children, CoreMatchers.hasItems(label3));

        dialog.removeAll();
        children = dialog.getChildren().collect(Collectors.toList());
        Assert.assertEquals(0, children.size());

        Assert.assertEquals(dialog.getWidth(), "200px");
        Assert.assertEquals(dialog.getHeight(), "100px");
    }

    @Test(expected = IllegalStateException.class)
    public void setOpened_noUi() {
        UI.setCurrent(null);
        Dialog dialog = new Dialog();
        dialog.setOpened(true);
    }

    @Test
    public void addDialogCloseActionListener_dialogClosed_JavaScriptIsScheduled() {
        Dialog dialog = new Dialog();

        dialog.addDialogCloseActionListener(event -> {
        });

        Assert.assertTrue(flushInvocations().isEmpty());

        dialog.open();

        assertInvocations(3);
    }

    @Test
    public void addDialogCloseActionListener_dialogOpened_JavaScriptIsScheduled() {
        Dialog dialog = new Dialog();

        dialog.open();

        Assert.assertEquals(2, flushInvocations().size());

        dialog.addDialogCloseActionListener(event -> {
        });

        assertInvocations(1);
    }

    @Test
    public void addDialogCloseActionListener_removeListener_dialogIsOpened_noJSAfterReopen() {
        Dialog dialog = new Dialog();

        Registration registration = dialog
                .addDialogCloseActionListener(event -> {
                });

        dialog.open();

        registration.remove();

        dialog.close();

        dialog.open();

        Assert.assertEquals(2, flushInvocations().size());
    }

    @Test
    public void addDialogCloseActionListener_removeListener_dialogIsClosed_noJSAfterReopen() {
        Dialog dialog = new Dialog();

        Registration registration = dialog
                .addDialogCloseActionListener(event -> {
                });

        registration.remove();

        dialog.open();

        Assert.assertEquals(2, flushInvocations().size());
    }

    @Test
    public void addDialogCloseActionListener_dialogIsClosing_JavaScriptIsRescheduled() {
        Dialog dialog = new Dialog();

        dialog.addDialogCloseActionListener(event -> {
        });

        dialog.open();

        flushInvocations();

        dialog.close();

        dialog.open();

        assertInvocations(1);
    }

    @Test
    public void addDialogCloseActionListener_dialogClosed_severalListenersAreAdded_onlyOneJavaScriptIsScheduled() {
        Dialog dialog = new Dialog();

        dialog.addDialogCloseActionListener(event -> {
        });

        dialog.addDialogCloseActionListener(event -> {
        });

        dialog.open();

        assertInvocations(3);
    }

    @Test
    public void addDialogCloseActionListener_dialogClosed_twoListenersAreAddedAndOneRemoved_onlyOneJavaScriptIsScheduled() {
        Dialog dialog = new Dialog();

        dialog.addDialogCloseActionListener(event -> {
        });

        Registration registration = dialog
                .addDialogCloseActionListener(event -> {
                });

        dialog.open();

        registration.remove();

        assertInvocations(3);
    }

    @Test
    public void addDialogCloseActionListener_dialogClosed_twoListenersAreAddedAndOneIsRemoved_oneJavaScriptIsScheduledAfterReopen() {
        Dialog dialog = new Dialog();

        dialog.addDialogCloseActionListener(event -> {
        });

        Registration registration = dialog
                .addDialogCloseActionListener(event -> {
                });

        dialog.open();

        registration.remove();

        flushInvocations();

        dialog.close();

        dialog.open();

        assertInvocations(1);
    }

    @Test
    public void addDialogCloseActionListener_dialogClosed_twoListenersAreAddedAndOneIsRemovedAfterClose_oneJavaScriptIsScheduledAfterReopen() {
        Dialog dialog = new Dialog();

        dialog.addDialogCloseActionListener(event -> {
        });

        Registration registration = dialog
                .addDialogCloseActionListener(event -> {
                });

        dialog.open();

        flushInvocations();

        dialog.close();

        registration.remove();

        dialog.open();

        assertInvocations(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addComponentAtIndex_negativeIndex() {
        addDivAtIndex(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addComponentAtIndex_indexIsBiggerThanChildrenCount() {
        addDivAtIndex(1);
    }

    @Test
    public void isDraggable_falseByDefault() {
        Dialog dialog = new Dialog();

        Assert.assertFalse("draggable is false by default", dialog.getElement().getProperty("draggable", false));
    }

    @Test
    public void setDraggable_dialogCanBeDraggable() {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);

        Assert.assertTrue("draggable can be set to true", dialog.getElement().getProperty("draggable", false));
    }

    @Test
    public void isResizable_falseByDefault() {
        Dialog dialog = new Dialog();

        Assert.assertFalse("resizable is false by default", dialog.getElement().getProperty("resizable", false));
    }

    @Test
    public void setResizable_dialogCanBeResizable() {
        Dialog dialog = new Dialog();
        dialog.setResizable(true);

        Assert.assertTrue("resizable can be set to true", dialog.getElement().getProperty("resizable", false));
    }

    @Test
    public void isModal_trueByDefault() {
        Dialog dialog = new Dialog();

        // Element's api "modeless" acts inverted to Flow's api "modal": modeless is false and modal is true by default 
        Assert.assertTrue("modal is true by default", !dialog.getElement().getProperty("modeless", false));
    }

    @Test
    public void setModal_dialogCanBeModeless() {
        Dialog dialog = new Dialog();
        dialog.setModal(false);

        // Element's api "modeless" acts inverted to Flow's api "modal": modeless is false and modal is true by default 
        Assert.assertFalse("modal can be set to false", !dialog.getElement().getProperty("modeless", false));
    }

    private void addDivAtIndex(int index) {
        Dialog dialog = new Dialog();

        Div div = new Div();
        dialog.addComponentAtIndex(index, div);
    }

    private List<PendingJavaScriptInvocation> flushInvocations() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations();
    }

    private void assertInvocations(int expectedInvocations) {
        List<PendingJavaScriptInvocation> invocations = flushInvocations();

        Assert.assertEquals(expectedInvocations, invocations.size());
    }
}
