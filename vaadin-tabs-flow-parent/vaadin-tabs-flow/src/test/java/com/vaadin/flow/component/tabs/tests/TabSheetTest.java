/*
 * Copyright 2022 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;

/**
 * @author Vaadin Ltd.
 */
public class TabSheetTest {

    private TabSheet tabSheet;

    @Before
    public void setup() {
        tabSheet = new TabSheet();
    }

    @Test
    public void tabsheet_tagName() {
        Assert.assertEquals("vaadin-tabsheet", tabSheet.getElement().getTag());
    }

    @Test
    public void addTab_assignsTabId() {
        var tab = tabSheet.add("Tab 0", new Span("Content 0"));
        Assert.assertTrue(tab.getId().isPresent());
    }

    @Test
    public void addTab_assignsContentTab() {
        var content = new Span("Content 0");
        var tab = tabSheet.add("Tab 0", content);
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
    }

    @Test
    public void setSelectedTab_selection() {
        tabSheet.add("Tab 0", new Span("Content 0"));
        var tab1 = tabSheet.add("Tab 1", new Span("Content 1"));
        tabSheet.setSelectedTab(tab1);
        Assert.assertEquals(1, tabSheet.getSelectedIndex());
        Assert.assertEquals(tab1, tabSheet.getSelectedTab());
    }

}
