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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.server.VaadinSession;

/**
 * Unit tests for the Dialog.
 */
public class DialogTest {

    private UI ui = new UI();

    @Before
    public void setup() {
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
        Span span1 = new Span("Text 1");
        Span span2 = new Span("Text 2");
        Span span3 = new Span("Text 3");

        Dialog dialog = new Dialog(span1, span2);
        dialog.setWidth("200px");
        dialog.setHeight("100px");

        List<Component> children = dialog.getChildren()
                .collect(Collectors.toList());
        Assert.assertEquals(2, children.size());
        Assert.assertTrue(children.contains(span1));
        Assert.assertTrue(children.contains(span2));

        dialog.add(span3);
        children = dialog.getChildren().collect(Collectors.toList());
        Assert.assertEquals(3, children.size());
        Assert.assertTrue(children.contains(span1));
        Assert.assertTrue(children.contains(span2));
        Assert.assertTrue(children.contains(span3));

        dialog.remove(span2);
        children = dialog.getChildren().collect(Collectors.toList());
        Assert.assertEquals(2, children.size());
        Assert.assertTrue(children.contains(span1));
        Assert.assertTrue(children.contains(span3));

        span1.getElement().removeFromParent();
        children = dialog.getChildren().collect(Collectors.toList());
        Assert.assertEquals(1, children.size());
        Assert.assertTrue(children.contains(span3));

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

        Assert.assertFalse("draggable is false by default",
                dialog.getElement().getProperty("draggable", false));
    }

    @Test
    public void setDraggable_dialogCanBeDraggable() {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);

        Assert.assertTrue("draggable can be set to true",
                dialog.getElement().getProperty("draggable", false));
    }

    @Test
    public void draggedEvent_topLeftPropertiesSynced() {
        Dialog dialog = new Dialog();

        // Emulate a drag event
        ComponentUtil.fireEvent(dialog,
                new Dialog.DialogDraggedEvent(dialog, true, "20", "10"));

        Assert.assertEquals("20", dialog.getLeft());
        Assert.assertEquals("10", dialog.getTop());
    }

    @Test
    public void resizeEvent_widthHeightTopLeftPropertiesSynced() {
        Dialog dialog = new Dialog();

        // Emulate a resize event
        ComponentUtil.fireEvent(dialog, new Dialog.DialogResizeEvent(dialog,
                true, "200", "100", "10", "20"));

        Assert.assertEquals("200", dialog.getWidth());
        Assert.assertEquals("100", dialog.getHeight());
        Assert.assertEquals("10", dialog.getLeft());
        Assert.assertEquals("20", dialog.getTop());
    }

    @Test
    public void isResizable_falseByDefault() {
        Dialog dialog = new Dialog();

        Assert.assertFalse("resizable is false by default",
                dialog.getElement().getProperty("resizable", false));
    }

    @Test
    public void setResizable_dialogCanBeResizable() {
        Dialog dialog = new Dialog();
        dialog.setResizable(true);

        Assert.assertTrue("resizable can be set to true",
                dialog.getElement().getProperty("resizable", false));
    }

    @Test
    public void isModal_trueByDefault() {
        Dialog dialog = new Dialog();

        // Element's api "modeless" acts inverted to Flow's api "modal":
        // modeless is false and modal is true by default
        Assert.assertTrue("modal is true by default",
                !dialog.getElement().getProperty("modeless", false));
    }

    @Test
    public void getRole_defaultDialog() {
        Dialog dialog = new Dialog();

        Assert.assertEquals("dialog", dialog.getRole());
        Assert.assertEquals("dialog", dialog.getOverlayRole());
        Assert.assertEquals("dialog", dialog.getElement().getProperty("role"));
    }

    @Test
    public void setOverlayRole_getOverlayRole() {
        Dialog dialog = new Dialog();
        dialog.setOverlayRole("alertdialog");

        Assert.assertEquals("alertdialog", dialog.getRole());
        Assert.assertEquals("alertdialog", dialog.getOverlayRole());
        Assert.assertEquals("alertdialog",
                dialog.getElement().getProperty("role"));
    }

    @Test(expected = NullPointerException.class)
    public void setOverlayRole_null_throws() {
        Dialog dialog = new Dialog();
        dialog.setOverlayRole(null);
    }

    @Test
    public void setRole_getRole() {
        Dialog dialog = new Dialog();
        dialog.setRole("alertdialog");

        Assert.assertEquals("alertdialog", dialog.getRole());
        Assert.assertEquals("alertdialog", dialog.getOverlayRole());
        Assert.assertEquals("alertdialog",
                dialog.getElement().getProperty("role"));
    }

    @Test(expected = NullPointerException.class)
    public void setRole_null_throws() {
        Dialog dialog = new Dialog();
        dialog.setRole(null);
    }

    @Test
    public void setModal_dialogCanBeModeless() {
        Dialog dialog = new Dialog();
        dialog.setModal(false);

        // Element's api "modeless" acts inverted to Flow's api "modal":
        // modeless is false and modal is true by default
        Assert.assertFalse("modal can be set to false",
                !dialog.getElement().getProperty("modeless", false));
    }

    private void addDivAtIndex(int index) {
        Dialog dialog = new Dialog();

        Div div = new Div();
        dialog.addComponentAtIndex(index, div);
    }

    @Test
    public void dialogHasStyle() {
        Dialog dialog = new Dialog();
        Assert.assertTrue(dialog instanceof HasStyle);
    }

    @Test
    public void elementAddedToHeaderOrFooter_elementShouldHaveDialogAsParent() {
        Dialog dialog = new Dialog();
        Span content = new Span("content");
        dialog.getHeader().add(content);

        Assert.assertTrue(content.getParent().isPresent());
        Assert.assertEquals(content.getParent().get(), dialog);

        Span secondContent = new Span("second_content");
        Span thirdContent = new Span("third_content");

        dialog.getHeader().add(secondContent, thirdContent);

        Assert.assertTrue(secondContent.getParent().isPresent());
        Assert.assertEquals(secondContent.getParent().get(), dialog);

        Assert.assertTrue(thirdContent.getParent().isPresent());
        Assert.assertEquals(thirdContent.getParent().get(), dialog);

        Span fourthContent = new Span("fourth_content");
        dialog.getHeader().addComponentAsFirst(fourthContent);

        Assert.assertTrue(fourthContent.getParent().isPresent());
        Assert.assertEquals(fourthContent.getParent().get(), dialog);

        Span fifthContent = new Span("fifth_content");
        dialog.getHeader().addComponentAtIndex(2, fifthContent);

        Assert.assertTrue(fifthContent.getParent().isPresent());
        Assert.assertEquals(fifthContent.getParent().get(), dialog);
    }

    @Test
    public void elementRemovedFromHeaderOrFooter_elementShouldNotHaveDialogAsParent() {
        Dialog dialog = new Dialog();
        Span content = new Span("content");
        Span secondContent = new Span("second_content");
        Span thirdContent = new Span("third_content");

        dialog.getHeader().add(content, secondContent, thirdContent);

        dialog.getHeader().remove(content);

        Assert.assertFalse(content.getParent().isPresent());

        dialog.getHeader().remove(secondContent, thirdContent);
        Assert.assertFalse(secondContent.getParent().isPresent());
        Assert.assertFalse(thirdContent.getParent().isPresent());
    }

    @Test
    public void allElementsRemovedFromHeader_elementsShouldNotHaveDialogAsParents() {
        Dialog dialog = new Dialog();
        Span content = new Span("content");
        Span secondContent = new Span("second_content");
        Span thirdContent = new Span("third_content");

        dialog.getHeader().add(content, secondContent, thirdContent);

        dialog.getHeader().removeAll();

        Assert.assertFalse(content.getParent().isPresent());
        Assert.assertFalse(secondContent.getParent().isPresent());
        Assert.assertFalse(thirdContent.getParent().isPresent());
    }

    @Test
    public void allElementsRemovedFromFooter_elementsShouldNotHaveDialogAsParents() {
        Dialog dialog = new Dialog();
        Span content = new Span("content");
        Span secondContent = new Span("second_content");
        Span thirdContent = new Span("third_content");

        dialog.getFooter().add(content, secondContent, thirdContent);

        dialog.getFooter().removeAll();

        Assert.assertFalse(content.getParent().isPresent());
        Assert.assertFalse(secondContent.getParent().isPresent());
        Assert.assertFalse(thirdContent.getParent().isPresent());
    }

    @Test(expected = NullPointerException.class)
    public void callAddToHeaderOrFooter_withNull_shouldThrowError() {
        Dialog dialog = new Dialog();
        dialog.getHeader().add((Component) null);
    }

    @Test(expected = NullPointerException.class)
    public void callAddToHeaderOrFooter_withAnyNullValue_shouldThrowError() {
        Dialog dialog = new Dialog();
        dialog.getHeader().add(new Span("content"), null);
    }

    @Test
    public void unregisterOpenedChangeListenerOnEvent() {
        var dialog = new Dialog();

        var listenerInvokedCount = new AtomicInteger(0);
        dialog.addOpenedChangeListener(e -> {
            listenerInvokedCount.incrementAndGet();
            e.unregisterListener();
        });

        dialog.open();
        dialog.close();

        Assert.assertEquals(1, listenerInvokedCount.get());
    }

    @Test
    public void createDialogWithTitle() {
        String title = "Title";

        var dialog = new Dialog(title);
        Assert.assertEquals(title, dialog.getHeaderTitle());

        Span content = new Span("content");
        Span secondContent = new Span("second_content");
        Span thirdContent = new Span("third_content");

        var dialogWithComponents = new Dialog(title, content, secondContent,
                thirdContent);
        Assert.assertEquals(title, dialogWithComponents.getHeaderTitle());
        Assert.assertEquals(content,
                dialogWithComponents.getChildren().toList().get(0));
        Assert.assertEquals(secondContent,
                dialogWithComponents.getChildren().toList().get(1));
        Assert.assertEquals(thirdContent,
                dialogWithComponents.getChildren().toList().get(2));
    }

    @Test
    public void open_autoAttachedInBeforeClientResponse() {
        Dialog dialog = new Dialog();
        dialog.open();

        fakeClientResponse();
        Assert.assertNotNull(dialog.getElement().getParent());
    }

    @Test
    public void open_close_notAutoAttachedInBeforeClientResponse() {
        Dialog dialog = new Dialog();
        dialog.open();
        dialog.close();

        fakeClientResponse();
        Assert.assertNull(dialog.getElement().getParent());
    }

    @Test
    public void position_setTopLeft_positionIsDefined() {
        Dialog dialog = new Dialog();
        dialog.setTop("10px");
        dialog.setLeft("20px");

        Assert.assertEquals("10px", dialog.getTop());
        Assert.assertEquals("20px", dialog.getLeft());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addClosedListener_listenerInvokedOnClose() {
        Dialog dialog = new Dialog();
        ComponentEventListener<Dialog.ClosedEvent> listener = Mockito
                .mock(ComponentEventListener.class);
        dialog.addClosedListener(listener);

        Element element = dialog.getElement();
        dialog.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(new DomEvent(element, "closed",
                        JacksonUtils.createObjectNode()));

        Mockito.verify(listener, Mockito.times(1))
                .onComponentEvent(Mockito.any(Dialog.ClosedEvent.class));
    }

    private void fakeClientResponse() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
