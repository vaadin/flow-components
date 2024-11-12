/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.contextmenu;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.internal.StateNode;

/**
 * Unit tests for MenuItem.
 */
public class MenuItemTest {

    private ContextMenu contextMenu;
    private MenuItem item;

    @Before
    public void setup() {
        contextMenu = new ContextMenu();
        item = contextMenu.addItem("");
    }

    @Test(expected = IllegalStateException.class)
    public void nonCheckable_setChecked_throws() {
        item.setChecked(true);
    }

    @Test
    public void setCheckable_setChecked_isChecked() {
        item.setCheckable(true);
        Assert.assertFalse(item.isChecked());
        item.setChecked(true);
        Assert.assertTrue(item.isChecked());
    }

    @Test
    public void checked_setUnCheckable_unChecks() {
        item.setCheckable(true);
        item.setChecked(true);

        item.setCheckable(false);
        Assert.assertFalse(item.isCheckable());
    }

    @Test
    public void implementsHasAriaLabel() {
        Assert.assertTrue(item instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        item.setAriaLabel("aria-label");
        Assert.assertTrue(item.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", item.getAriaLabel().get());

        item.setAriaLabel(null);
        Assert.assertTrue(item.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        item.setAriaLabelledBy("aria-labelledby");
        Assert.assertTrue(item.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby", item.getAriaLabelledBy().get());

        item.setAriaLabelledBy(null);
        Assert.assertTrue(item.getAriaLabelledBy().isEmpty());
    }

    @Test
    public void disableOnClick_click_componentIsDisabled() {
        AtomicBoolean itemIsEnabled = new AtomicBoolean(true);

        item = contextMenu.addItem("foo",
                event -> itemIsEnabled.set(event.getSource().isEnabled()));
        item.setDisableOnClick(true);
        clickMenuItem(item);

        Assert.assertFalse(itemIsEnabled.get());
    }

    @Test
    public void disableOnClick_clickRevertsDisabled_componentIsEnabled() {
        item = contextMenu.addItem("foo",
                event -> event.getSource().setEnabled(true));
        item.setDisableOnClick(true);
        clickMenuItem(item);
        Assert.assertTrue(item.isEnabled());
    }

    @Test
    public void initDisableOnClick_onlyCalledOnceForServerRoundTrip() {
        SerializableRunnable contentReset = Mockito
                .mock(SerializableRunnable.class);
        item = Mockito.mock(MenuItem.class,
                Mockito.withSettings().useConstructor(contextMenu, contentReset)
                        .defaultAnswer(Mockito.CALLS_REAL_METHODS));

        Element element = Mockito.mock(Element.class);
        Mockito.when(item.getElement()).thenReturn(element);
        Mockito.when(element.executeJs(Mockito.anyString()))
                .thenReturn(Mockito.mock(PendingJavaScriptInvocation.class));
        Mockito.when(element.getComponent()).thenReturn(Optional.of(item));
        Mockito.when(element.getNode()).thenReturn(new StateNode());

        item.setDisableOnClick(true);
        item.setDisableOnClick(false);
        item.setDisableOnClick(true);

        Mockito.verify(element, Mockito.times(1)).executeJs(
                "window.Vaadin.Flow.disableOnClick.initDisableOnClick($0)");
    }

    private static void clickMenuItem(MenuItem menuItem) {
        ComponentUtil.fireEvent(menuItem, new ClickEvent<>(menuItem, false, 0,
                0, 0, 0, 0, 0, false, false, false, false));
    }
}
