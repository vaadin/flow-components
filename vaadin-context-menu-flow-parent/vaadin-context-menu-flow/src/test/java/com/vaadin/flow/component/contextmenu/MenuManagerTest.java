/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.shared.Registration;

@SuppressWarnings("unchecked")
public class MenuManagerTest {

    private MenuManager<ContextMenu, MenuItem, SubMenu> manager;
    private ContextMenu menu = Mockito.mock(ContextMenu.class);
    private SerializableRunnable reset = Mockito
            .mock(SerializableRunnable.class);

    private SerializableBiFunction<ContextMenu, SerializableRunnable, MenuItem> factory = Mockito
            .mock(SerializableBiFunction.class);
    private MenuItem parent = Mockito.mock(MenuItem.class);

    private List<Component> addedComponents = new ArrayList<>();

    private static class TestMenuItem extends MenuItem {

        public TestMenuItem(ContextMenu contextMenu,
                SerializableRunnable contentReset) {
            super(contextMenu, contentReset);
        }

        @Override
        protected <T extends ComponentEvent<?>> Registration addListener(
                Class<T> eventType, ComponentEventListener<T> listener) {
            return getEventBus().addListener(eventType, listener);
        }

    }

    @Before
    public void setUp() {
        manager = new MenuManager<ContextMenu, MenuItem, SubMenu>(menu, reset,
                factory, MenuItem.class, parent) {
            @Override
            public void add(Component... components) {
                Stream.of(components).forEach(addedComponents::add);
            }
        };
    }

    @Test
    public void addItem_text_createItemUsingFactory_setText_addToItem() {
        MenuItem item = Mockito.mock(MenuItem.class);
        Mockito.when(factory.apply(menu, reset)).thenReturn(item);
        manager.addItem("foo");

        Mockito.verify(item).setText("foo");

        Assert.assertEquals(addedComponents.size(), 1);
        Assert.assertEquals(item, addedComponents.get(0));
    }

    @Test
    public void addItem_component_createItemUsingFactory_addComponent_addToItem() {
        MenuItem item = Mockito.mock(MenuItem.class);
        Mockito.when(factory.apply(menu, reset)).thenReturn(item);
        Component component = Mockito.mock(Component.class);
        manager.addItem(component);

        Mockito.verify(item).add(component);

        Assert.assertEquals(addedComponents.size(), 1);
        Assert.assertEquals(item, addedComponents.get(0));
    }

    @Test
    public void addItem_textWithListener_createItemUsingFactory_setText_addToItemAndAddListener() {
        TestMenuItem item = Mockito.mock(TestMenuItem.class);
        Mockito.when(factory.apply(menu, reset)).thenReturn(item);
        ComponentEventListener listener = Mockito
                .mock(ComponentEventListener.class);
        manager.addItem("foo", listener);

        Mockito.verify(item).setText("foo");
        Mockito.verify(item).addListener(ClickEvent.class, listener);

        Assert.assertEquals(addedComponents.size(), 1);
        Assert.assertEquals(item, addedComponents.get(0));
    }

    @Test
    public void addItem_textNullListener_createItemUsingFactory_setText_addToItemAndNoListenerAdded() {
        TestMenuItem item = Mockito.mock(TestMenuItem.class);
        Mockito.when(factory.apply(menu, reset)).thenReturn(item);
        manager.addItem("foo", null);

        Mockito.verify(item).setText("foo");
        Mockito.verifyNoMoreInteractions(item);

        Assert.assertEquals(addedComponents.size(), 1);
        Assert.assertEquals(item, addedComponents.get(0));
    }

    @Test
    public void addItem_componentWithListener_createItemUsingFactory_addComponent_addToItemAndAddListener() {
        TestMenuItem item = Mockito.mock(TestMenuItem.class);
        Mockito.when(factory.apply(menu, reset)).thenReturn(item);
        Component component = Mockito.mock(Component.class);
        ComponentEventListener listener = Mockito
                .mock(ComponentEventListener.class);
        manager.addItem(component, listener);

        Mockito.verify(item).add(component);
        Mockito.verify(item).addListener(ClickEvent.class, listener);

        Assert.assertEquals(addedComponents.size(), 1);
        Assert.assertEquals(item, addedComponents.get(0));
    }

    @Test
    public void addItem_componentNullListener_createItemUsingFactory_addComponent_addToItemAndAddListener() {
        TestMenuItem item = Mockito.mock(TestMenuItem.class);
        Mockito.when(factory.apply(menu, reset)).thenReturn(item);
        Component component = Mockito.mock(Component.class);
        manager.addItem(component, null);

        Mockito.verify(item).add(component);
        Mockito.verifyNoMoreInteractions(item);

        Assert.assertEquals(addedComponents.size(), 1);
        Assert.assertEquals(item, addedComponents.get(0));
    }

    @Test
    public void addComponents_componentsAreAdded_resetIsCalled() {
        Component component1 = Mockito.mock(Component.class);
        Component component2 = Mockito.mock(Component.class);

        manager = new MenuManager<ContextMenu, MenuItem, SubMenu>(menu, reset,
                factory, MenuItem.class, parent);

        manager.add(component1, component2);

        List<Component> children = manager.getChildren()
                .collect(Collectors.toList());

        Assert.assertEquals(2, children.size());
        Assert.assertEquals(component1, children.get(0));
        Assert.assertEquals(component2, children.get(1));

        Mockito.verify(reset).run();
    }

    @Test(expected = IllegalStateException.class)
    public void addComponents_parentIsCheckable_throws() {
        Mockito.when(parent.isCheckable()).thenReturn(true);
        manager = new MenuManager<ContextMenu, MenuItem, SubMenu>(menu, reset,
                factory, MenuItem.class, parent);

        manager.add(Mockito.mock(Component.class));
    }

    @Test
    public void removeComponents_componentsAreRemoved_resetIsCalled() {
        Component component1 = Mockito.mock(Component.class);
        Component component2 = Mockito.mock(Component.class);

        manager = new MenuManager<ContextMenu, MenuItem, SubMenu>(menu, reset,
                factory, MenuItem.class, parent);

        manager.add(component1, component2);

        manager.remove(component1, component2);

        List<Component> children = manager.getChildren()
                .collect(Collectors.toList());

        Assert.assertTrue(children.isEmpty());

        // one from add , one from remove
        Mockito.verify(reset, Mockito.times(2)).run();
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeComponents_componentIsNotChild_resetIsNotCalled() {
        Component component1 = Mockito.mock(Component.class);

        manager = new MenuManager<ContextMenu, MenuItem, SubMenu>(menu, reset,
                factory, MenuItem.class, parent);

        manager.remove(component1);
    }

    @Test
    public void removeAll_componentsAreRemoved_resetIsCalled() {
        Component component1 = Mockito.mock(Component.class);
        Component component2 = Mockito.mock(Component.class);

        manager = new MenuManager<ContextMenu, MenuItem, SubMenu>(menu, reset,
                factory, MenuItem.class, parent);

        manager.add(component1, component2);

        manager.removeAll();

        List<Component> children = manager.getChildren()
                .collect(Collectors.toList());

        Assert.assertTrue(children.isEmpty());

        // one from add , one from remove
        Mockito.verify(reset, Mockito.times(2)).run();
    }

    @Test
    public void addComponentAtIndex_componentInserted_resetIsCalled() {
        Component component1 = Mockito.mock(Component.class);
        Component component2 = Mockito.mock(Component.class);
        Component component3 = Mockito.mock(Component.class);

        manager = new MenuManager<ContextMenu, MenuItem, SubMenu>(menu, reset,
                factory, MenuItem.class, parent);

        manager.add(component1, component2);
        manager.addComponentAtIndex(1, component3);

        List<Component> children = manager.getChildren()
                .collect(Collectors.toList());

        Assert.assertEquals(3, children.size());
        Assert.assertEquals(component1, children.get(0));
        Assert.assertEquals(component3, children.get(1));
        Assert.assertEquals(component2, children.get(2));

        Mockito.verify(reset, Mockito.times(2)).run();
    }

    @Test(expected = IllegalStateException.class)
    public void addComponentAtIndex_parentIsCheckable_throws() {
        Component component = Mockito.mock(Component.class);

        manager = new MenuManager<ContextMenu, MenuItem, SubMenu>(menu, reset,
                factory, MenuItem.class, parent);

        Mockito.when(parent.isCheckable()).thenReturn(true);

        manager.addComponentAtIndex(0, component);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addComponentAtIndex_negariveIndex_throws() {
        manager = new MenuManager<ContextMenu, MenuItem, SubMenu>(menu, reset,
                factory, MenuItem.class, parent);

        manager.addComponentAtIndex(-1, Mockito.mock(Component.class));
    }

    @Test
    public void getItems_onlyItemsAreReturned() {
        Component component = Mockito.mock(Component.class);

        manager = new MenuManager<ContextMenu, MenuItem, SubMenu>(menu, reset,
                factory, MenuItem.class, parent);

        TestMenuItem item = Mockito.mock(TestMenuItem.class);
        Mockito.when(factory.apply(menu, reset)).thenReturn(item);

        manager.addItem("foo");
        manager.add(component);
        manager.addItem("bar");

        Assert.assertEquals(2, manager.getItems().size());
    }

}
