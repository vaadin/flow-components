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
package com.vaadin.flow.component.breadcrumb.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.breadcrumb.BreadcrumbItem;

class BreadcrumbItemTest {

    @Tag("div")
    private static class TestIcon extends Component {
    }

    @Test
    void constructor_withLabel_setsLabel() {
        BreadcrumbItem item = new BreadcrumbItem("Home");
        assertEquals("Home", item.getLabel());
    }

    @Test
    void constructor_withLabel_hasNullPath() {
        BreadcrumbItem item = new BreadcrumbItem("Home");
        assertNull(item.getPath());
    }

    @Test
    void constructor_withLabelAndPath_setsLabelAndPath() {
        BreadcrumbItem item = new BreadcrumbItem("Docs", "/docs");
        assertEquals("Docs", item.getLabel());
        assertEquals("/docs", item.getPath());
    }

    @Test
    void setPath_updatesElementProperty() {
        BreadcrumbItem item = new BreadcrumbItem("Home");
        item.setPath("/new-path");
        assertEquals("/new-path", item.getPath());
    }

    @Test
    void setPath_null_removesPath() {
        BreadcrumbItem item = new BreadcrumbItem("Home", "/path");
        item.setPath(null);
        assertNull(item.getPath());
    }

    @Test
    void getPath_returnsCurrentPath() {
        BreadcrumbItem item = new BreadcrumbItem("Home", "/home");
        assertEquals("/home", item.getPath());
        item.setPath("/other");
        assertEquals("/other", item.getPath());
    }

    @Test
    void setLabel_updatesTextContent() {
        BreadcrumbItem item = new BreadcrumbItem("Old");
        item.setLabel("New");
        assertEquals("New", item.getLabel());
    }

    @Test
    void getLabel_returnsCurrentLabel() {
        BreadcrumbItem item = new BreadcrumbItem("Home");
        assertEquals("Home", item.getLabel());
    }

    @Test
    void setLabel_null_removesLabel() {
        BreadcrumbItem item = new BreadcrumbItem("Home");
        item.setLabel(null);
        assertNull(item.getLabel());
    }

    @Test
    void setCurrent_true_setsCurrentProperty() {
        BreadcrumbItem item = new BreadcrumbItem("Home");
        item.setCurrent(true);
        assertTrue(item.isCurrent());
    }

    @Test
    void setCurrent_false_unsetsCurrentProperty() {
        BreadcrumbItem item = new BreadcrumbItem("Home");
        item.setCurrent(true);
        item.setCurrent(false);
        assertFalse(item.isCurrent());
    }

    @Test
    void isCurrent_defaultIsFalse() {
        BreadcrumbItem item = new BreadcrumbItem("Home");
        assertFalse(item.isCurrent());
    }

    @Test
    void asCurrent_setsCurrent_andReturnsSameInstance() {
        BreadcrumbItem item = new BreadcrumbItem("Home");
        BreadcrumbItem result = item.asCurrent();
        assertTrue(item.isCurrent());
        assertSame(item, result);
    }

    @Test
    void setPrefixComponent_addsComponentWithPrefixSlot() {
        BreadcrumbItem item = new BreadcrumbItem("Home");
        TestIcon icon = new TestIcon();
        item.setPrefixComponent(icon);
        assertEquals("prefix",
                icon.getElement().getAttribute("slot"));
    }

    @Test
    void getPrefixComponent_returnsSetComponent() {
        BreadcrumbItem item = new BreadcrumbItem("Home");
        TestIcon icon = new TestIcon();
        item.setPrefixComponent(icon);
        assertSame(icon, item.getPrefixComponent());
    }

    @Test
    void constructor_withLabelPathAndPrefix_setsAllProperties() {
        TestIcon icon = new TestIcon();
        BreadcrumbItem item = new BreadcrumbItem("Projects", "/projects",
                icon);
        assertEquals("Projects", item.getLabel());
        assertEquals("/projects", item.getPath());
        assertSame(icon, item.getPrefixComponent());
    }

    @Test
    void constructor_withPrefixAndPath_setsPathAndPrefix() {
        TestIcon icon = new TestIcon();
        BreadcrumbItem item = new BreadcrumbItem(icon, "/");
        assertEquals("/", item.getPath());
        assertSame(icon, item.getPrefixComponent());
        assertNull(item.getLabel());
    }
}
