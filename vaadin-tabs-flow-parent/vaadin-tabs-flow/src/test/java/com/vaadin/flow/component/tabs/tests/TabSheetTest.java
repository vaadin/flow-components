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
package com.vaadin.flow.component.tabs.tests;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.tests.MockUIExtension;

/**
 * @author Vaadin Ltd.
 */
class TabSheetTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private TabSheet tabSheet;
    private Tabs tabs;

    @BeforeEach
    void setup() {
        tabSheet = new TabSheet();
        tabs = (Tabs) tabSheet.getChildren().findFirst().get();

        ui.add(tabSheet);
    }

    @Test
    void tabsheet_tagName() {
        Assertions.assertEquals("vaadin-tabsheet",
                tabSheet.getElement().getTag());
    }

    @Test
    void addTab_assignsTabId() {
        var tab = tabSheet.add("Tab 0", new Span("Content 0"));
        ui.fakeClientCommunication();
        Assertions.assertTrue(tab.getId().isPresent());
    }

    @Test
    void addTab_assignsCustomTabId() {
        var content = new Span("Content 0");
        var tab = tabSheet.add("Tab 0", content);
        tab.setId("customId");

        ui.fakeClientCommunication();
        Assertions.assertEquals("customId",
                content.getElement().getAttribute("tab"));
    }

    @Test
    void addTab_assignsContentTab() {
        var content = new Span("Content 0");
        var tab = tabSheet.add("Tab 0", content);
        ui.fakeClientCommunication();
        Assertions.assertEquals(tab.getId().get(),
                content.getElement().getAttribute("tab"));
    }

    @Test
    void addTab_contentAdded() {
        var content = new Span("Content 0");
        tabSheet.add("Tab 0", content);
        ui.fakeClientCommunication();
        Assertions.assertTrue(content.getParent().isPresent());
    }

    @Test
    void addSameTabAgainWithNewContent_oldContentRemoved() {
        // Add a tab with content
        var content0 = new Span("Content 0");
        var tab0 = tabSheet.add("Tab 0", content0);
        ui.fakeClientCommunication();

        // Assert that the content is attached to the parent (the tab is
        // selected)
        Assertions.assertEquals(tabSheet, content0.getParent().get());

        // Add the same Tab instance again but with a new content component
        tabSheet.add(tab0, new Span("Content 0"));

        // Check that the old content is no longer attached to the parent
        Assertions.assertFalse(content0.getParent().isPresent());
    }

    @Test
    void addSameTabAgain_addedAsTheLastTab() {
        // Add a tab
        var tab0 = tabSheet.add("Tab 0", new Span("Content 0"));
        // Add another tab
        tabSheet.add("Tab 1", new Span("Content 1"));

        // Add the same Tab instance again
        tabSheet.add(tab0, new Span("Content 0"));

        // Check that the tab gets added as the last tab
        Assertions.assertEquals(1,
                tab0.getElement().getParent().indexOfChild(tab0.getElement()));
    }

    @Test
    void addSecondTab_contentNotAdded() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        var content1 = new Span("Content 1");
        tabSheet.add("Tab 1", content1);
        Assertions.assertFalse(content1.getParent().isPresent());
    }

    @Test
    void changeTab_contentAdded() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        var content1 = new Span("Content 1");
        tabSheet.add("Tab 1", content1);
        tabSheet.setSelectedIndex(1);
        ui.fakeClientCommunication();
        Assertions.assertTrue(content1.getParent().isPresent());
    }

    @Test
    void addSecondTab_contentDisabled() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        var content1 = new Span("Content 1");
        tabSheet.add("Tab 1", content1);
        Assertions.assertFalse(content1.isEnabled());
    }

    @Test
    void addTabs_tabCountCorrect() {
        Assertions.assertEquals(0, tabSheet.getTabCount());

        tabSheet.add("Tab 0", new Span("Content 0"));
        tabSheet.add("Tab 1", new Span("Content 1"));

        Assertions.assertEquals(2, tabSheet.getTabCount());
    }

    @Test
    void changeTab_contentEnabled() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        var content1 = new Span("Content 1");
        tabSheet.add("Tab 1", content1);
        tabSheet.setSelectedIndex(1);
        Assertions.assertTrue(content1.isEnabled());
    }

    @Test
    void changeTab_oldContentDisabled() {
        var content = new Span("Content 0");
        tabSheet.add("Tab 0", content);

        tabSheet.add("Tab 1", new Span("Content 1"));
        tabSheet.setSelectedIndex(1);
        Assertions.assertFalse(content.isEnabled());
    }

    @Test
    void addNullTab_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> tabSheet.add((Tab) null, new Span("Content 0")));
    }

    @Test
    void addNullContent_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> tabSheet.add("Tab 0", (Span) null));
    }

    @Test
    void addTextContent_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> tabSheet.add("Tab 0", new Text("Tab 0 content")));
    }

    @Test
    void changeTab_selectedChangeEvent() {
        var tab0 = tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));

        var listenerInvoked = new AtomicBoolean(false);
        tabSheet.addSelectedChangeListener(event -> {
            listenerInvoked.set(true);
            Assertions.assertEquals(tabSheet.getSelectedTab(),
                    event.getSelectedTab());
            Assertions.assertEquals(tabSheet, event.getSource());
            Assertions.assertEquals(tab1, event.getSelectedTab());
            Assertions.assertEquals(tab0, event.getPreviousTab());
        });
        tabSheet.setSelectedIndex(1);
        Assertions.assertTrue(listenerInvoked.get());
    }

    @Test
    void removeNullTab_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> tabSheet.remove((Tab) null));
    }

    @Test
    void removeNullContent_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> tabSheet.remove((Span) null));
    }

    @Test
    void removeTextContent_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> tabSheet.remove(new Text("Tab 0 content")));
    }

    @Test
    void removeTab_removesContent() {
        var content = new Span("Content 0");
        var tab = tabSheet.add("Tab 0", content);
        tabSheet.remove(tab);
        Assertions.assertFalse(content.getParent().isPresent());
    }

    @Test
    void removeTab_clearsSelection() {
        var content = new Span("Content 0");
        var tab = tabSheet.add("Tab 0", content);
        tabSheet.remove(tab);
        Assertions.assertEquals(-1, tabSheet.getSelectedIndex());
    }

    @Test
    void removeTab_selectedChangeEvent() {
        var tab0 = tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        var listenerInvoked = new AtomicBoolean(false);
        tabSheet.addSelectedChangeListener(event -> {
            listenerInvoked.set(true);
            Assertions.assertEquals(tabSheet.getSelectedTab(),
                    event.getSelectedTab());
            Assertions.assertEquals(tabSheet, event.getSource());
            Assertions.assertEquals(tab1, event.getSelectedTab());
            Assertions.assertEquals(tab0, event.getPreviousTab());
        });
        tabSheet.remove(tab0);
        Assertions.assertTrue(listenerInvoked.get());
    }

    @Test
    void removeTab_removesTab() {
        var tab = tabSheet.add("Tab 0", new Span("Content 0"));
        tabSheet.remove(tab);
        Assertions.assertFalse(tab.getParent().isPresent());
        Assertions.assertEquals(0, tabSheet.getTabCount());
    }

    @Test
    void removeContent_removesTab() {
        var content = new Span("Content 0");
        var tab = tabSheet.add("Tab 0", content);
        tabSheet.remove(content);
        Assertions.assertFalse(tab.getParent().isPresent());
    }

    @Test
    void removeNonExistentContent_doesNoteRemoveTab() {
        var tab = tabSheet.add("Tab 0", new Span("Content 0"));
        tabSheet.remove(new Span("Content 1"));
        Assertions.assertTrue(tab.getParent().isPresent());
    }

    @Test
    void addTab_initialSelection() {
        var tab = tabSheet.add("Tab 0", new Span("Content 0"));
        Assertions.assertEquals(0, tabSheet.getSelectedIndex());
        Assertions.assertEquals(tab, tabSheet.getSelectedTab());
    }

    @Test
    void setSelectedIndex_selection() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        tabSheet.setSelectedIndex(1);
        Assertions.assertEquals(1, tabSheet.getSelectedIndex());
        Assertions.assertEquals(tab1, tabSheet.getSelectedTab());
    }

    @Test
    void setSelectedTab_selection() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        tabSheet.setSelectedTab(tab1);
        Assertions.assertEquals(1, tabSheet.getSelectedIndex());
        Assertions.assertEquals(tab1, tabSheet.getSelectedTab());
    }

    @Test
    void setPrefix_hasPrefix() {
        var prefix = new Span("prefix");
        tabSheet.setPrefixComponent(prefix);
        Assertions.assertEquals(prefix, tabSheet.getPrefixComponent());
    }

    @Test
    void setTextAsPrefix_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> tabSheet.setPrefixComponent(new Text("Prefix")));
    }

    @Test
    void setSuffix_hasSuffix() {
        var suffix = new Span("suffix");
        tabSheet.setSuffixComponent(suffix);
        Assertions.assertEquals(suffix, tabSheet.getSuffixComponent());
    }

    @Test
    void setTextAsSuffix_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> tabSheet.setSuffixComponent(new Text("Suffix")));
    }

    @Test
    void addTab_addedAsLastTab() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        Assertions.assertEquals(1, tabs.indexOf(tab1));
    }

    @Test
    void addTabToNegativeIndex_addedAsLastTab() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add(new Tab("Tab 1"), new Span("Content 1"), -1);
        Assertions.assertEquals(1, tabs.indexOf(tab1));
    }

    @Test
    void addTabToEndIndex_addedAsLastTab() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add(new Tab("Tab 1"), new Span("Content 1"), 1);
        Assertions.assertEquals(1, tabs.indexOf(tab1));
    }

    @Test
    void addTabToStartIndex_addedAsFirstTab() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add(new Tab("Tab 1"), new Span("Content 1"), 0);
        Assertions.assertEquals(0, tabs.indexOf(tab1));
    }

    @Test
    void addTabToOverflowingIndex_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> tabSheet.add(new Tab("Tab 1"), new Span("Content 1"), 2));
    }

    @Test
    void removeNegativeIndex_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> tabSheet.remove(-1));
    }

    @Test
    void removeOverflowingIndex_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> tabSheet.remove(1));
    }

    @Test
    void removeFirstIndex_onlySecondTabRemains() {
        var tab0 = tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        tabSheet.remove(0);
        Assertions.assertEquals(tabs, tab1.getParent().get());
        Assertions.assertFalse(tab0.getParent().isPresent());
    }

    @Test
    void getTabAt_returnsTab() {
        var tab0 = tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        Assertions.assertEquals(tab0, tabSheet.getTabAt(0));
        Assertions.assertEquals(tab1, tabSheet.getTabAt(1));
    }

    @Test
    void getTabAtNegativeIndex_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> tabSheet.getTabAt(-1));
    }

    @Test
    void getTabAtOverflowingIndex_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> tabSheet.getTabAt(1));
    }

    @Test
    void indexOfTab_returnsIndex() {
        var tab0 = tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        Assertions.assertEquals(0, tabSheet.getIndexOf(tab0));
        Assertions.assertEquals(1, tabSheet.getIndexOf(tab1));
    }

    @Test
    void getIndexOfNull_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> tabSheet.getIndexOf(null));
    }

    @Test
    void getIndexOfNonAttachedTab_returnsMinusOne() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        Assertions.assertEquals(-1, tabSheet.getIndexOf(new Tab()));
    }

    @Test
    void unregisterSelectedChangeListenerOnEvent() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        tabSheet.add("Tab 1", new Span("Content 1"));

        var listenerInvokedCount = new AtomicInteger(0);
        tabSheet.addSelectedChangeListener(e -> {
            listenerInvokedCount.incrementAndGet();
            e.unregisterListener();
        });

        tabSheet.setSelectedIndex(1);
        // The listener should now be unregistered.
        tabSheet.setSelectedIndex(0);

        Assertions.assertEquals(1, listenerInvokedCount.get());
    }

    @Test
    void getTab_returnsTab() {
        var content0 = new Span("Content 0");
        var content1 = new Span("Content 1");
        var tab0 = tabSheet.add("Tab 0", content0);
        var tab1 = tabSheet.add("Tab 1", content1);
        Assertions.assertEquals(tab0, tabSheet.getTab(content0));
        Assertions.assertEquals(tab1, tabSheet.getTab(content1));
    }

    @Test
    void getTab_unknownComponent_returnsNull() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        Assertions.assertNull(tabSheet.getTab(new Span("Content Unknown")));
    }

    @Test
    void getTab_nullComponent_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        Assertions.assertThrows(NullPointerException.class,
                () -> tabSheet.getTab(null));
    }

    @Test
    void getComponent_returnsComponent() {
        var content0 = new Span("Content 0");
        var content1 = new Span("Content 1");
        var tab0 = tabSheet.add("Tab 0", content0);
        var tab1 = tabSheet.add("Tab 1", content1);
        Assertions.assertEquals(content0, tabSheet.getComponent(tab0));
        Assertions.assertEquals(content1, tabSheet.getComponent(tab1));
    }

    @Test
    void getComponent_unknownTab_returnsNull() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        Assertions.assertNull(tabSheet.getComponent(new Tab("Tab 1")));
    }

    @Test
    void getComponent_nullTab_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        Assertions.assertThrows(NullPointerException.class,
                () -> tabSheet.getComponent(null));
    }

    @Test
    void switchMultipleTabsBeforeClientResponse_onlyLastSelectedContentAttached() {
        var content0 = new Span("Content 0");
        var content1 = new Span("Content 1");
        var content2 = new Span("Content 2");
        tabSheet.add("Tab 0", content0);
        tabSheet.add("Tab 1", content1);
        tabSheet.add("Tab 2", content2);

        tabSheet.setSelectedIndex(1);
        tabSheet.setSelectedIndex(2);
        ui.fakeClientCommunication();

        Assertions.assertFalse(content1.getParent().isPresent());
        Assertions.assertTrue(content2.getParent().isPresent());
    }

    @Test
    void reuseTabContentInNewTabSheetInNewUI_contentAdded() {
        // Regression test for
        // https://github.com/vaadin/flow-components/issues/8875.
        // Tab contents are injected as beans, using a scope that allows them to
        // be reused between multiple UIs when reloading the page.
        var content = new Span("Content");
        tabSheet.add("Tab", content);
        ui.fakeClientCommunication();
        Assertions.assertEquals(tabSheet, content.getParent().orElseThrow());

        ui.replaceUI();
        tabSheet = new TabSheet();
        ui.add(tabSheet);
        tabSheet.add("Tab", content);
        ui.fakeClientCommunication();
        Assertions.assertEquals(tabSheet, content.getParent().orElseThrow());
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(TabSheet.class));
    }
}
