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
package com.vaadin.flow.component.popover;

import static org.mockito.Mockito.times;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.server.VaadinService;

import tools.jackson.databind.node.ArrayNode;

/**
 * @author Vaadin Ltd.
 */
class PopoverTest {
    private Popover popover;

    @BeforeEach
    void setup() {
        popover = new Popover();
    }

    @Test
    void setFor_getFor() {
        popover.setFor("target-id");
        Assertions.assertEquals("target-id", popover.getFor());
    }

    @Test
    void setTarget_getTarget() {
        Div target = new Div();
        popover.setTarget(target);
        Assertions.assertEquals(popover.getTarget(), target);
    }

    @Test
    void setTarget_textNodeAsComponent_throws() {
        Text textNode = new Text("Text");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> popover.setTarget(textNode));
    }

    @Test
    void setPosition_getPosition() {
        popover.setPosition(PopoverPosition.END);
        Assertions.assertEquals("end",
                popover.getElement().getProperty("position"));
        Assertions.assertEquals(PopoverPosition.END, popover.getPosition());
    }

    @Test
    void defaultPosition_equalsNull() {
        Assertions.assertEquals(null, popover.getPosition());
    }

    @Test
    void setWidth_widthPropertyUpdated() {
        popover.setWidth("200px");
        Assertions.assertEquals("200px",
                popover.getElement().getProperty("width", ""));

        popover.setWidth(null);
        Assertions.assertEquals("",
                popover.getElement().getProperty("width", ""));
    }

    @Test
    void setHeight_heightPropertyUpdated() {
        popover.setHeight("200px");
        Assertions.assertEquals("200px",
                popover.getElement().getProperty("height", ""));

        popover.setHeight(null);
        Assertions.assertEquals("",
                popover.getElement().getProperty("height", ""));
    }

    @Test
    void setFocusDelay_getFocusDelay() {
        Assertions.assertEquals(0, popover.getFocusDelay());

        popover.setFocusDelay(1000);
        Assertions.assertEquals(1000, popover.getFocusDelay());
        Assertions.assertEquals(1000,
                popover.getElement().getProperty("focusDelay", 0));
    }

    @Test
    void setHoverDelay_getHoverDelay() {
        Assertions.assertEquals(0, popover.getHoverDelay());

        popover.setHoverDelay(1000);
        Assertions.assertEquals(1000, popover.getHoverDelay());
        Assertions.assertEquals(1000,
                popover.getElement().getProperty("hoverDelay", 0));
    }

    @Test
    void setHideDelay_getHideDelay() {
        Assertions.assertEquals(0, popover.getHideDelay());

        popover.setHideDelay(1000);
        Assertions.assertEquals(1000, popover.getHideDelay());
        Assertions.assertEquals(1000,
                popover.getElement().getProperty("hideDelay", 0));
    }

    @Test
    void isOpenOnClick_trueByDefault() {
        Assertions.assertTrue(popover.isOpenOnClick());
    }

    @Test
    void isOpenOnFocus_falseByDefault() {
        Assertions.assertFalse(popover.isOpenOnFocus());
    }

    @Test
    void isOpenOnHover_falseByDefault() {
        Assertions.assertFalse(popover.isOpenOnHover());
    }

    @Test
    void setOpenOnClick_isOpenOnClick() {
        popover.setOpenOnClick(false);
        Assertions.assertFalse(popover.isOpenOnClick());

        popover.setOpenOnClick(true);
        Assertions.assertTrue(popover.isOpenOnClick());
    }

    @Test
    void setOpenOnFocus_isOpenOnFocus() {
        popover.setOpenOnFocus(true);
        Assertions.assertTrue(popover.isOpenOnFocus());

        popover.setOpenOnFocus(false);
        Assertions.assertFalse(popover.isOpenOnFocus());
    }

    @Test
    void setOpenOnHover_isOpenOnHover() {
        popover.setOpenOnHover(true);
        Assertions.assertTrue(popover.isOpenOnHover());

        popover.setOpenOnHover(false);
        Assertions.assertFalse(popover.isOpenOnHover());
    }

    @Test
    void getTriggerProperty_defaultValue_click() {
        ArrayNode jsonArray = (ArrayNode) popover.getElement()
                .getPropertyRaw("trigger");
        Assertions.assertEquals(1, jsonArray.size());
        Assertions.assertEquals("click", jsonArray.get(0).asString());
    }

    @Test
    void setOpenOnClick_triggerPropertyUpdated() {
        popover.setOpenOnClick(false);

        ArrayNode jsonArray = (ArrayNode) popover.getElement()
                .getPropertyRaw("trigger");
        Assertions.assertEquals(0, jsonArray.size());
    }

    @Test
    void setOpenOnFocus_triggerPropertyUpdated() {
        popover.setOpenOnFocus(true);

        ArrayNode jsonArray = (ArrayNode) popover.getElement()
                .getPropertyRaw("trigger");
        Assertions.assertEquals(2, jsonArray.size());
        Assertions.assertEquals("click", jsonArray.get(0).asString());
        Assertions.assertEquals("focus", jsonArray.get(1).asString());
    }

    @Test
    void setOpenOnHover_triggerPropertyUpdated() {
        popover.setOpenOnHover(true);

        ArrayNode jsonArray = (ArrayNode) popover.getElement()
                .getPropertyRaw("trigger");
        Assertions.assertEquals(2, jsonArray.size());
        Assertions.assertEquals("click", jsonArray.get(0).asString());
        Assertions.assertEquals("hover", jsonArray.get(1).asString());
    }

    @Test
    void setAriaLabel_getAriaLabel() {
        popover.setAriaLabel("aria-label");
        Assertions.assertTrue(popover.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", popover.getAriaLabel().get());

        popover.setAriaLabel(null);
        Assertions.assertTrue(popover.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy_getAriaLabelledBy() {
        popover.setAriaLabelledBy("aria-labelledby");
        Assertions.assertTrue(popover.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                popover.getAriaLabelledBy().get());

        popover.setAriaLabelledBy(null);
        Assertions.assertTrue(popover.getAriaLabelledBy().isEmpty());
    }

    @Test
    void getRole_defaultDialog() {
        Popover popover = new Popover();

        Assertions.assertEquals("dialog", popover.getRole());
        Assertions.assertEquals("dialog", popover.getOverlayRole());
        Assertions.assertEquals("dialog",
                popover.getElement().getProperty("role"));
    }

    @Test
    void setOverlayRole_getOverlayRole() {
        popover.setOverlayRole("alertdialog");

        Assertions.assertEquals("alertdialog", popover.getRole());
        Assertions.assertEquals("alertdialog", popover.getOverlayRole());
        Assertions.assertEquals("alertdialog",
                popover.getElement().getProperty("role"));
    }

    @Test
    void setOverlayRole_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> popover.setOverlayRole(null));
    }

    @Test
    void setRole_getRole() {
        popover.setRole("alertdialog");

        Assertions.assertEquals("alertdialog", popover.getRole());
        Assertions.assertEquals("alertdialog", popover.getOverlayRole());
        Assertions.assertEquals("alertdialog",
                popover.getElement().getProperty("role"));
    }

    @Test
    void setRole_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> popover.setRole(null));
    }

    @Test
    void setModal_isModal() {
        Assertions.assertFalse(popover.isModal());
        Assertions
                .assertFalse(popover.getElement().getProperty("modal", false));

        popover.setModal(true);
        Assertions.assertTrue(popover.isModal());
        Assertions.assertTrue(popover.getElement().getProperty("modal", false));
    }

    @Test
    void setBackdropVisible_isBackdropVisible() {
        Assertions.assertFalse(popover.isBackdropVisible());
        Assertions.assertFalse(
                popover.getElement().getProperty("withBackdrop", false));

        popover.setBackdropVisible(true);
        Assertions.assertTrue(popover.isBackdropVisible());
        Assertions.assertTrue(
                popover.getElement().getProperty("withBackdrop", false));
    }

    @Test
    void setModalAndBackdropVisible() {
        popover.setModal(true, true);
        Assertions.assertTrue(popover.isModal());
        Assertions.assertTrue(popover.isBackdropVisible());

        popover.setModal(false, false);
        Assertions.assertFalse(popover.isModal());
        Assertions.assertFalse(popover.isBackdropVisible());
    }

    @Test
    void setAutofocus_isAutofocus() {
        Assertions.assertFalse(popover.isAutofocus());
        Assertions.assertFalse(
                popover.getElement().getProperty("autofocus", false));

        popover.setAutofocus(true);
        Assertions.assertTrue(popover.isAutofocus());
        Assertions.assertTrue(
                popover.getElement().getProperty("autofocus", false));
    }

    @Test
    void popoverWithContent() {
        Div content = new Div();
        Popover popoverWithContent = new Popover(content);
        Assertions.assertEquals(1, popoverWithContent.getChildren().count());
        Assertions.assertSame(content,
                popoverWithContent.getChildren().findFirst().get());
    }

    @Test
    void testSetDefaultFocusDelay_threadSafety() {
        testStaticSettersThreadsSafety(
                () -> Popover.setDefaultFocusDelay(1000));
    }

    @Test
    void testSetDefaultHoverDelay_threadSafety() {
        testStaticSettersThreadsSafety(
                () -> Popover.setDefaultHoverDelay(1000));
    }

    @Test
    void testSetDefaultHideDelay_threadSafety() {
        testStaticSettersThreadsSafety(() -> Popover.setDefaultHideDelay(1000));
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(Popover.class));
    }

    private void testStaticSettersThreadsSafety(Runnable tester) {
        // Reset the static state for each test
        Popover.uiInitListenerRegistered.set(false);
        final VaadinService current = Mockito.mock(VaadinService.class);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        final CountDownLatch latch = new CountDownLatch(10);
        final Runnable runnable = () -> {
            VaadinService.setCurrent(current);
            latch.countDown();
            for (int i = 0; i < 100000; i++) {
                tester.run();
            }
        };
        final var list = IntStream.range(0, 10)
                .mapToObj(it -> executorService.submit(runnable)).toList();
        for (var future : list) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();
        Mockito.verify(current, times(1)).addUIInitListener(Mockito.any());
    }
}
