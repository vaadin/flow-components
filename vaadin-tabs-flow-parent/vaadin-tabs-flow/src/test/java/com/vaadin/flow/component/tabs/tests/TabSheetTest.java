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

package com.vaadin.flow.component.tabs.tests;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.internal.UIInternals;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.server.VaadinSession;

/**
 * @author Vaadin Ltd.
 */
public class TabSheetTest {

    private TabSheet tabSheet;
    private Tabs tabs;

    private UI ui;

    @Before
    public void setup() {
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui = new UI();
        ui.getInternals().setSession(session);

        tabSheet = new TabSheet();
        tabs = (Tabs) tabSheet.getChildren().findFirst().get();

        ui.getElement().appendChild(tabSheet.getElement());
    }

    @Test
    public void tabsheet_tagName() {
        Assert.assertEquals("vaadin-tabsheet", tabSheet.getElement().getTag());
    }

    @Test
    public void addTab_assignsTabId() {
        var tab = tabSheet.add("Tab 0", new Span("Content 0"));
        flushBeforeClientResponse();
        Assert.assertTrue(tab.getId().isPresent());
    }

    @Test
    public void addTab_assignsCustomTabId() {
        var content = new Span("Content 0");
        var tab = tabSheet.add("Tab 0", content);
        tab.setId("customId");

        flushBeforeClientResponse();
        Assert.assertEquals("customId",
                content.getElement().getAttribute("tab"));
    }

    @Test
    public void addTab_assignsContentTab() {
        var content = new Span("Content 0");
        var tab = tabSheet.add("Tab 0", content);
        flushBeforeClientResponse();
        Assert.assertEquals(tab.getId().get(),
                content.getElement().getAttribute("tab"));
    }

    @Test
    public void addTab_contentAdded() {
        var content = new Span("Content 0");
        tabSheet.add("Tab 0", content);
        Assert.assertTrue(content.getParent().isPresent());
    }

    @Test
    public void addSameTabAgainWithNewContent_oldContentRemoved() {
        // Add a tab with content
        var content0 = new Span("Content 0");
        var tab0 = tabSheet.add("Tab 0", content0);

        // Assert that the content is attached to the parent (the tab is
        // selected)
        Assert.assertEquals(tabSheet, content0.getParent().get());

        // Add the same Tab instance again but with a new content component
        tabSheet.add(tab0, new Span("Content 0"));

        // Check that the old content is no longer attached to the parent
        Assert.assertFalse(content0.getParent().isPresent());
    }

    @Test
    public void addSameTabAgain_addedAsTheLastTab() {
        // Add a tab
        var tab0 = tabSheet.add("Tab 0", new Span("Content 0"));
        // Add another tab
        tabSheet.add("Tab 1", new Span("Content 1"));

        // Add the same Tab instance again
        tabSheet.add(tab0, new Span("Content 0"));

        // Check that the tab gets added as the last tab
        Assert.assertEquals(1,
                tab0.getElement().getParent().indexOfChild(tab0.getElement()));
    }

    @Test
    public void addSecondTab_contentNotAdded() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        var content1 = new Span("Content 1");
        tabSheet.add("Tab 1", content1);
        Assert.assertFalse(content1.getParent().isPresent());
    }

    @Test
    public void changeTab_contentAdded() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        var content1 = new Span("Content 1");
        tabSheet.add("Tab 1", content1);
        tabSheet.setSelectedIndex(1);
        Assert.assertTrue(content1.getParent().isPresent());
    }

    @Test
    public void addSecondTab_contentDisabled() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        var content1 = new Span("Content 1");
        tabSheet.add("Tab 1", content1);
        Assert.assertFalse(content1.isEnabled());
    }

    @Test
    public void changeTab_contentEnabled() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        var content1 = new Span("Content 1");
        tabSheet.add("Tab 1", content1);
        tabSheet.setSelectedIndex(1);
        Assert.assertTrue(content1.isEnabled());
    }

    @Test
    public void changeTab_oldContentDisabled() {
        var content = new Span("Content 0");
        tabSheet.add("Tab 0", content);

        tabSheet.add("Tab 1", new Span("Content 1"));
        tabSheet.setSelectedIndex(1);
        Assert.assertFalse(content.isEnabled());
    }

    @Test(expected = NullPointerException.class)
    public void addNullTab_throws() {
        tabSheet.add((Tab) null, new Span("Content 0"));
    }

    @Test(expected = NullPointerException.class)
    public void addNullContent_throws() {
        tabSheet.add("Tab 0", (Span) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTextContent_throws() {
        tabSheet.add("Tab 0", new Text("Tab 0 content"));
    }

    @Test
    public void changeTab_selectedChangeEvent() {
        var tab0 = tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));

        var listenerInvoked = new AtomicBoolean(false);
        tabSheet.addSelectedChangeListener(event -> {
            listenerInvoked.set(true);
            Assert.assertEquals(tabSheet.getSelectedTab(),
                    event.getSelectedTab());
            Assert.assertEquals(tabSheet, event.getSource());
            Assert.assertEquals(tab1, event.getSelectedTab());
            Assert.assertEquals(tab0, event.getPreviousTab());
        });
        tabSheet.setSelectedIndex(1);
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test(expected = NullPointerException.class)
    public void removeNullTab_throws() {
        tabSheet.remove((Tab) null);
    }

    @Test(expected = NullPointerException.class)
    public void removeNullContent_throws() {
        tabSheet.remove((Span) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeTextContent_throws() {
        tabSheet.remove(new Text("Tab 0 content"));
    }

    @Test
    public void removeTab_removesContent() {
        var content = new Span("Content 0");
        var tab = tabSheet.add("Tab 0", content);
        tabSheet.remove(tab);
        Assert.assertFalse(content.getParent().isPresent());
    }

    @Test
    public void removeTab_clearsSelection() {
        var content = new Span("Content 0");
        var tab = tabSheet.add("Tab 0", content);
        tabSheet.remove(tab);
        Assert.assertEquals(-1, tabSheet.getSelectedIndex());
    }

    @Test
    public void removeTab_selectedChangeEvent() {
        var tab0 = tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        var listenerInvoked = new AtomicBoolean(false);
        tabSheet.addSelectedChangeListener(event -> {
            listenerInvoked.set(true);
            Assert.assertEquals(tabSheet.getSelectedTab(),
                    event.getSelectedTab());
            Assert.assertEquals(tabSheet, event.getSource());
            Assert.assertEquals(tab1, event.getSelectedTab());
            Assert.assertEquals(tab0, event.getPreviousTab());
        });
        tabSheet.remove(tab0);
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void removeTab_removesTab() {
        var tab = tabSheet.add("Tab 0", new Span("Content 0"));
        tabSheet.remove(tab);
        Assert.assertFalse(tab.getParent().isPresent());
    }

    @Test
    public void removeContent_removesTab() {
        var content = new Span("Content 0");
        var tab = tabSheet.add("Tab 0", content);
        tabSheet.remove(content);
        Assert.assertFalse(tab.getParent().isPresent());
    }

    @Test
    public void removeNonExistentContent_doesNoteRemoveTab() {
        var tab = tabSheet.add("Tab 0", new Span("Content 0"));
        tabSheet.remove(new Span("Content 1"));
        Assert.assertTrue(tab.getParent().isPresent());
    }

    @Test
    public void addTab_initialSelection() {
        var tab = tabSheet.add("Tab 0", new Span("Content 0"));
        Assert.assertEquals(0, tabSheet.getSelectedIndex());
        Assert.assertEquals(tab, tabSheet.getSelectedTab());
    }

    @Test
    public void setSelectedIndex_selection() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        tabSheet.setSelectedIndex(1);
        Assert.assertEquals(1, tabSheet.getSelectedIndex());
        Assert.assertEquals(tab1, tabSheet.getSelectedTab());
        Assert.assertEquals(1,
                tabSheet.getElement().getProperty("selected", 0));
    }

    @Test
    public void setSelectedTab_selection() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        tabSheet.setSelectedTab(tab1);
        Assert.assertEquals(1, tabSheet.getSelectedIndex());
        Assert.assertEquals(tab1, tabSheet.getSelectedTab());
        Assert.assertEquals(1,
                tabSheet.getElement().getProperty("selected", 0));
    }

    @Test
    public void setPrefix_hasPrefix() {
        var prefix = new Span("prefix");
        tabSheet.setPrefixComponent(prefix);
        Assert.assertEquals(prefix, tabSheet.getPrefixComponent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setTextAsPrefix_throws() {
        tabSheet.setPrefixComponent(new Text("Prefix"));
    }

    @Test
    public void setSuffix_hasSuffix() {
        var suffix = new Span("suffix");
        tabSheet.setSuffixComponent(suffix);
        Assert.assertEquals(suffix, tabSheet.getSuffixComponent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setTextAsSuffix_throws() {
        tabSheet.setSuffixComponent(new Text("Suffix"));
    }

    @Test
    public void addThemeVariants_hasThemeVariants() {
        tabSheet.addThemeVariants(TabSheetVariant.LUMO_TABS_CENTERED,
                TabSheetVariant.LUMO_BORDERED);
        Assert.assertTrue(tabSheet.getThemeName()
                .contains(TabSheetVariant.LUMO_TABS_CENTERED.getVariantName()));
        Assert.assertTrue(tabSheet.getThemeName()
                .contains(TabSheetVariant.LUMO_BORDERED.getVariantName()));
    }

    @Test
    public void addTab_addedAsLastTab() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        Assert.assertEquals(1, tabs.indexOf(tab1));
    }

    @Test
    public void addTabToNegativeIndex_addedAsLastTab() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add(new Tab("Tab 1"), new Span("Content 1"), -1);
        Assert.assertEquals(1, tabs.indexOf(tab1));
    }

    @Test
    public void addTabToEndIndex_addedAsLastTab() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add(new Tab("Tab 1"), new Span("Content 1"), 1);
        Assert.assertEquals(1, tabs.indexOf(tab1));
    }

    @Test
    public void addTabToStartIndex_addedAsFirstTab() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add(new Tab("Tab 1"), new Span("Content 1"), 0);
        Assert.assertEquals(0, tabs.indexOf(tab1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTabToOverflowingIndex_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        tabSheet.add(new Tab("Tab 1"), new Span("Content 1"), 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeNegativeIndex_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        tabSheet.remove(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeOverflowingIndex_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        tabSheet.remove(1);
    }

    @Test
    public void removeFirstIndex_onlySecondTabRemains() {
        var tab0 = tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        tabSheet.remove(0);
        Assert.assertEquals(tabs, tab1.getParent().get());
        Assert.assertFalse(tab0.getParent().isPresent());
    }

    @Test
    public void getTabAt_returnsTab() {
        var tab0 = tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        Assert.assertEquals(tab0, tabSheet.getTabAt(0));
        Assert.assertEquals(tab1, tabSheet.getTabAt(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTabAtNegativeIndex_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        tabSheet.getTabAt(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTabAtOverflowingIndex_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        tabSheet.getTabAt(1);
    }

    @Test
    public void indexOfTab_returnsIndex() {
        var tab0 = tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        Assert.assertEquals(0, tabSheet.getIndexOf(tab0));
        Assert.assertEquals(1, tabSheet.getIndexOf(tab1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIndexOfNull_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        tabSheet.getIndexOf(null);
    }

    @Test
    public void getIndexOfNonAttachedTab_returnsMinusOne() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        Assert.assertEquals(-1, tabSheet.getIndexOf(new Tab()));
    }

    @Test
    public void selectTabFromTabs_selectedUpdated() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        tabSheet.add("Tab 1", new Span("Content 1"));
        tabs.setSelectedIndex(1);
        Assert.assertEquals(1,
                tabSheet.getElement().getProperty("selected", 0));
    }

    @Test
    public void unregisterSelectedChangeListenerOnEvent() {
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

        Assert.assertEquals(1, listenerInvokedCount.get());
    }

    @Test
    public void getTab_returnsTab() {
        var content0 = new Span("Content 0");
        var content1 = new Span("Content 1");
        var tab0 = tabSheet.add("Tab 0", content0);
        var tab1 = tabSheet.add("Tab 1", content1);
        Assert.assertEquals(tab0, tabSheet.getTab(content0));
        Assert.assertEquals(tab1, tabSheet.getTab(content1));
    }

    @Test
    public void getTab_unknownComponent_returnsNull() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        Assert.assertNull(tabSheet.getTab(new Span("Content Unknown")));
    }

    @Test(expected = NullPointerException.class)
    public void getTab_nullComponent_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        tabSheet.getTab(null);
    }

    @Test
    public void getComponent_returnsComponent() {
        var content0 = new Span("Content 0");
        var content1 = new Span("Content 1");
        var tab0 = tabSheet.add("Tab 0", content0);
        var tab1 = tabSheet.add("Tab 1", content1);
        Assert.assertEquals(content0, tabSheet.getComponent(tab0));
        Assert.assertEquals(content1, tabSheet.getComponent(tab1));
    }

    @Test
    public void getComponent_unknownTab_returnsNull() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        Assert.assertNull(tabSheet.getComponent(new Tab("Tab 1")));
    }

    @Test(expected = NullPointerException.class)
    public void getComponent_nullTab_throws() {
        tabSheet.add("Tab 0", new Span("Content 0"));

        tabSheet.getComponent(null);
    }

    private void flushBeforeClientResponse() {
        UIInternals internals = ui.getInternals();
        internals.getStateTree().runExecutionsBeforeClientResponse();
    }
}
