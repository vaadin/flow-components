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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.tests.MockUIExtension;

/**
 * Unit tests for the Dialog.
 */
class DialogTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Test
    void createDialogWithComponents_componentsArePartOfGetChildren() {
        Span span1 = new Span("Text 1");
        Span span2 = new Span("Text 2");
        Span span3 = new Span("Text 3");

        Dialog dialog = new Dialog(span1, span2);
        dialog.setWidth("200px");
        dialog.setHeight("100px");

        List<Component> children = dialog.getChildren()
                .collect(Collectors.toList());
        Assertions.assertEquals(2, children.size());
        Assertions.assertTrue(children.contains(span1));
        Assertions.assertTrue(children.contains(span2));

        dialog.add(span3);
        children = dialog.getChildren().collect(Collectors.toList());
        Assertions.assertEquals(3, children.size());
        Assertions.assertTrue(children.contains(span1));
        Assertions.assertTrue(children.contains(span2));
        Assertions.assertTrue(children.contains(span3));

        dialog.remove(span2);
        children = dialog.getChildren().collect(Collectors.toList());
        Assertions.assertEquals(2, children.size());
        Assertions.assertTrue(children.contains(span1));
        Assertions.assertTrue(children.contains(span3));

        span1.getElement().removeFromParent();
        children = dialog.getChildren().collect(Collectors.toList());
        Assertions.assertEquals(1, children.size());
        Assertions.assertTrue(children.contains(span3));

        dialog.removeAll();
        children = dialog.getChildren().collect(Collectors.toList());
        Assertions.assertEquals(0, children.size());

        Assertions.assertEquals("200px", dialog.getWidth());
        Assertions.assertEquals("100px", dialog.getHeight());
    }

    @Test
    void setOpened_noUi() {
        ui.clearUI();
        Dialog dialog = new Dialog();
        Assertions.assertThrows(IllegalStateException.class,
                () -> dialog.setOpened(true));
    }

    @Test
    void addComponentAtIndex_negativeIndex() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> addDivAtIndex(-1));
    }

    @Test
    void addComponentAtIndex_indexIsBiggerThanChildrenCount() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> addDivAtIndex(1));
    }

    @Test
    void isDraggable_falseByDefault() {
        Dialog dialog = new Dialog();

        Assertions.assertFalse(
                dialog.getElement().getProperty("draggable", false),
                "draggable is false by default");
    }

    @Test
    void setDraggable_dialogCanBeDraggable() {
        Dialog dialog = new Dialog();
        dialog.setDraggable(true);

        Assertions.assertTrue(
                dialog.getElement().getProperty("draggable", false),
                "draggable can be set to true");
    }

    @Test
    void isKeepInViewport_falseByDefault() {
        Dialog dialog = new Dialog();

        Assertions.assertFalse(dialog.isKeepInViewport(),
                "keepInViewport is false by default");
        Assertions.assertFalse(
                dialog.getElement().getProperty("keepInViewport", false),
                "keepInViewport property is false by default");
    }

    @Test
    void setKeepInViewport_updatesProperty() {
        Dialog dialog = new Dialog();
        dialog.setKeepInViewport(true);

        Assertions.assertTrue(dialog.isKeepInViewport(),
                "keepInViewport can be set to true");
        Assertions.assertTrue(
                dialog.getElement().getProperty("keepInViewport", false),
                "keepInViewport property is true");
    }

    @Test
    void draggedEvent_topLeftPropertiesSynced() {
        Dialog dialog = new Dialog();

        // Emulate a drag event
        ComponentUtil.fireEvent(dialog,
                new Dialog.DialogDraggedEvent(dialog, true, "20", "10"));

        Assertions.assertEquals("20", dialog.getLeft());
        Assertions.assertEquals("10", dialog.getTop());
    }

    @Test
    void resizeEvent_widthHeightTopLeftPropertiesSynced() {
        Dialog dialog = new Dialog();

        // Emulate a resize event
        ComponentUtil.fireEvent(dialog, new Dialog.DialogResizeEvent(dialog,
                true, "200", "100", "10", "20"));

        Assertions.assertEquals("200", dialog.getWidth());
        Assertions.assertEquals("100", dialog.getHeight());
        Assertions.assertEquals("10", dialog.getLeft());
        Assertions.assertEquals("20", dialog.getTop());
    }

    @Test
    void isResizable_falseByDefault() {
        Dialog dialog = new Dialog();

        Assertions.assertFalse(
                dialog.getElement().getProperty("resizable", false),
                "resizable is false by default");
    }

    @Test
    void setResizable_dialogCanBeResizable() {
        Dialog dialog = new Dialog();
        dialog.setResizable(true);

        Assertions.assertTrue(
                dialog.getElement().getProperty("resizable", false),
                "resizable can be set to true");
    }

    @Test
    void isFocusTrap_trueByDefault() {
        Dialog dialog = new Dialog();
        Assertions.assertTrue(dialog.isFocusTrap(),
                "focusTrap is true by default");
        Assertions.assertFalse(
                dialog.getElement().getProperty("noFocusTrap", false),
                "noFocusTrap property is false by default");
    }

    @Test
    void setFocusTrap_dialogFocusTrapCanBeDisabled() {
        Dialog dialog = new Dialog();
        dialog.setFocusTrap(false);
        Assertions.assertFalse(dialog.isFocusTrap(),
                "focusTrap can be set to false");
        Assertions.assertTrue(
                dialog.getElement().getProperty("noFocusTrap", false),
                "noFocusTrap property is true when focus trap is disabled");
    }

    @Test
    void setFocusTrap_dialogFocusTrapCanBeReEnabled() {
        Dialog dialog = new Dialog();
        dialog.setFocusTrap(false);
        dialog.setFocusTrap(true);
        Assertions.assertTrue(dialog.isFocusTrap(),
                "focusTrap can be re-enabled");
        Assertions.assertFalse(
                dialog.getElement().getProperty("noFocusTrap", false),
                "noFocusTrap property is false when focus trap is enabled");
    }

    @Test
    void getRole_defaultDialog() {
        Dialog dialog = new Dialog();

        Assertions.assertEquals("dialog", dialog.getRole());
        Assertions.assertEquals("dialog", dialog.getOverlayRole());
        Assertions.assertEquals("dialog",
                dialog.getElement().getProperty("role"));
    }

    @Test
    void setOverlayRole_getOverlayRole() {
        Dialog dialog = new Dialog();
        dialog.setOverlayRole("alertdialog");

        Assertions.assertEquals("alertdialog", dialog.getRole());
        Assertions.assertEquals("alertdialog", dialog.getOverlayRole());
        Assertions.assertEquals("alertdialog",
                dialog.getElement().getProperty("role"));
    }

    @Test
    void setOverlayRole_null_throws() {
        Dialog dialog = new Dialog();
        Assertions.assertThrows(NullPointerException.class,
                () -> dialog.setOverlayRole(null));
    }

    @Test
    void setRole_getRole() {
        Dialog dialog = new Dialog();
        dialog.setRole("alertdialog");

        Assertions.assertEquals("alertdialog", dialog.getRole());
        Assertions.assertEquals("alertdialog", dialog.getOverlayRole());
        Assertions.assertEquals("alertdialog",
                dialog.getElement().getProperty("role"));
    }

    @Test
    void setRole_null_throws() {
        Dialog dialog = new Dialog();
        Assertions.assertThrows(NullPointerException.class,
                () -> dialog.setRole(null));
    }

    private void addDivAtIndex(int index) {
        Dialog dialog = new Dialog();

        Div div = new Div();
        dialog.addComponentAtIndex(index, div);
    }

    @Test
    void dialogHasStyle() {
        Dialog dialog = new Dialog();
        Assertions.assertTrue(dialog instanceof HasStyle);
    }

    @Test
    void elementAddedToHeaderOrFooter_elementShouldHaveDialogAsParent() {
        Dialog dialog = new Dialog();
        Span content = new Span("content");
        dialog.getHeader().add(content);

        Assertions.assertTrue(content.getParent().isPresent());
        Assertions.assertEquals(content.getParent().get(), dialog);

        Span secondContent = new Span("second_content");
        Span thirdContent = new Span("third_content");

        dialog.getHeader().add(secondContent, thirdContent);

        Assertions.assertTrue(secondContent.getParent().isPresent());
        Assertions.assertEquals(secondContent.getParent().get(), dialog);

        Assertions.assertTrue(thirdContent.getParent().isPresent());
        Assertions.assertEquals(thirdContent.getParent().get(), dialog);

        Span fourthContent = new Span("fourth_content");
        dialog.getHeader().addComponentAsFirst(fourthContent);

        Assertions.assertTrue(fourthContent.getParent().isPresent());
        Assertions.assertEquals(fourthContent.getParent().get(), dialog);

        Span fifthContent = new Span("fifth_content");
        dialog.getHeader().addComponentAtIndex(2, fifthContent);

        Assertions.assertTrue(fifthContent.getParent().isPresent());
        Assertions.assertEquals(fifthContent.getParent().get(), dialog);
    }

    @Test
    void elementRemovedFromHeaderOrFooter_elementShouldNotHaveDialogAsParent() {
        Dialog dialog = new Dialog();
        Span content = new Span("content");
        Span secondContent = new Span("second_content");
        Span thirdContent = new Span("third_content");

        dialog.getHeader().add(content, secondContent, thirdContent);

        dialog.getHeader().remove(content);

        Assertions.assertFalse(content.getParent().isPresent());

        dialog.getHeader().remove(secondContent, thirdContent);
        Assertions.assertFalse(secondContent.getParent().isPresent());
        Assertions.assertFalse(thirdContent.getParent().isPresent());
    }

    @Test
    void allElementsRemovedFromHeader_elementsShouldNotHaveDialogAsParents() {
        Dialog dialog = new Dialog();
        Span content = new Span("content");
        Span secondContent = new Span("second_content");
        Span thirdContent = new Span("third_content");

        dialog.getHeader().add(content, secondContent, thirdContent);

        dialog.getHeader().removeAll();

        Assertions.assertFalse(content.getParent().isPresent());
        Assertions.assertFalse(secondContent.getParent().isPresent());
        Assertions.assertFalse(thirdContent.getParent().isPresent());
    }

    @Test
    void allElementsRemovedFromFooter_elementsShouldNotHaveDialogAsParents() {
        Dialog dialog = new Dialog();
        Span content = new Span("content");
        Span secondContent = new Span("second_content");
        Span thirdContent = new Span("third_content");

        dialog.getFooter().add(content, secondContent, thirdContent);

        dialog.getFooter().removeAll();

        Assertions.assertFalse(content.getParent().isPresent());
        Assertions.assertFalse(secondContent.getParent().isPresent());
        Assertions.assertFalse(thirdContent.getParent().isPresent());
    }

    @Test
    void callAddToHeaderOrFooter_withNull_shouldThrowError() {
        Dialog dialog = new Dialog();
        Assertions.assertThrows(NullPointerException.class,
                () -> dialog.getHeader().add((Component) null));
    }

    @Test
    void callAddToHeaderOrFooter_withAnyNullValue_shouldThrowError() {
        Dialog dialog = new Dialog();
        Assertions.assertThrows(NullPointerException.class,
                () -> dialog.getHeader().add(new Span("content"), null));
    }

    @Test
    void unregisterOpenedChangeListenerOnEvent() {
        var dialog = new Dialog();

        var listenerInvokedCount = new AtomicInteger(0);
        dialog.addOpenedChangeListener(e -> {
            listenerInvokedCount.incrementAndGet();
            e.unregisterListener();
        });

        dialog.open();
        dialog.close();

        Assertions.assertEquals(1, listenerInvokedCount.get());
    }

    @Test
    void createDialogWithTitle() {
        String title = "Title";

        var dialog = new Dialog(title);
        Assertions.assertEquals(title, dialog.getHeaderTitle());

        Span content = new Span("content");
        Span secondContent = new Span("second_content");
        Span thirdContent = new Span("third_content");

        var dialogWithComponents = new Dialog(title, content, secondContent,
                thirdContent);
        Assertions.assertEquals(title, dialogWithComponents.getHeaderTitle());
        Assertions.assertEquals(content,
                dialogWithComponents.getChildren().toList().get(0));
        Assertions.assertEquals(secondContent,
                dialogWithComponents.getChildren().toList().get(1));
        Assertions.assertEquals(thirdContent,
                dialogWithComponents.getChildren().toList().get(2));
    }

    @Test
    void open_autoAttachedInBeforeClientResponse() {
        Dialog dialog = new Dialog();
        dialog.open();

        ui.fakeClientCommunication();
        Assertions.assertNotNull(dialog.getElement().getParent());
    }

    @Test
    void open_close_notAutoAttachedInBeforeClientResponse() {
        Dialog dialog = new Dialog();
        dialog.open();
        dialog.close();

        ui.fakeClientCommunication();
        Assertions.assertNull(dialog.getElement().getParent());
    }

    @Test
    void position_setTopLeft_positionIsDefined() {
        Dialog dialog = new Dialog();
        dialog.setTop("10px");
        dialog.setLeft("20px");

        Assertions.assertEquals("10px", dialog.getTop());
        Assertions.assertEquals("20px", dialog.getLeft());
    }

    @SuppressWarnings("unchecked")
    @Test
    void addClosedListener_listenerInvokedOnClose() {
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

    @Test
    void bindChildren_throwsUnsupportedOperationException() {
        Dialog dialog = new Dialog();
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> dialog.bindChildren(null, null));
    }
}
