
package com.vaadin.flow.component.grid.contextmenu;

import com.vaadin.flow.component.contextmenu.MenuManager;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableRunnable;

public class GridSubMenuTest {

    private MenuManager menuManager = Mockito.mock(MenuManager.class);

    private class TestContextMenu extends GridSubMenu<Object> {

        public TestContextMenu() {
            super(Mockito.mock(GridMenuItem.class),
                    Mockito.mock(SerializableRunnable.class));
        }

        @Override
        protected MenuManager<GridContextMenu<Object>, GridMenuItem<Object>, GridSubMenu<Object>> createMenuManager() {
            return menuManager;
        }
    }

    @Test
    public void addTextItem_delegateToMenuManager() {
        TestContextMenu menu = new TestContextMenu();
        menu.addItem("foo", null);

        Mockito.verify(menuManager).addItem("foo");
    }

    @Test
    public void addComponentItem_delegateToMenuManager() {
        TestContextMenu menu = new TestContextMenu();
        Component component = Mockito.mock(Component.class);
        menu.addItem(component, null);

        Mockito.verify(menuManager).addItem(component);
    }
}
