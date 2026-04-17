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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.breadcrumb.Breadcrumb;
import com.vaadin.flow.component.breadcrumb.Breadcrumb.BreadcrumbI18n;
import com.vaadin.flow.component.breadcrumb.Breadcrumb.NavigateEvent;
import com.vaadin.flow.component.breadcrumb.BreadcrumbItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.shared.Registration;

import tools.jackson.databind.node.ObjectNode;

class BreadcrumbTest {

    private Breadcrumb breadcrumb;

    @BeforeEach
    void setUp() {
        breadcrumb = new Breadcrumb();
    }

    @Test
    void addItem_appendsBothItemsAsChildren() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home", "/");
        BreadcrumbItem item2 = new BreadcrumbItem("Docs", "/docs");

        breadcrumb.addItem(item1, item2);

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(2, items.size());
        assertEquals(item1, items.get(0));
        assertEquals(item2, items.get(1));
    }

    @Test
    void getItems_returnsAddedItemsInOrder() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home", "/");
        BreadcrumbItem item2 = new BreadcrumbItem("Products", "/products");
        BreadcrumbItem item3 = new BreadcrumbItem("Details", "/details");

        breadcrumb.addItem(item1);
        breadcrumb.addItem(item2);
        breadcrumb.addItem(item3);

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(3, items.size());
        assertEquals("Home", items.get(0).getLabel());
        assertEquals("Products", items.get(1).getLabel());
        assertEquals("Details", items.get(2).getLabel());
    }

    @Test
    void addItemAsFirst_insertsBeforeExistingItems() {
        BreadcrumbItem existing = new BreadcrumbItem("Docs", "/docs");
        breadcrumb.addItem(existing);

        BreadcrumbItem first = new BreadcrumbItem("Home", "/");
        breadcrumb.addItemAsFirst(first);

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(2, items.size());
        assertEquals(first, items.get(0));
        assertEquals(existing, items.get(1));
    }

    @Test
    void addItemAtIndex_insertsAtCorrectPosition() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home", "/");
        BreadcrumbItem item2 = new BreadcrumbItem("Details", "/details");
        breadcrumb.addItem(item1, item2);

        BreadcrumbItem middle = new BreadcrumbItem("Products", "/products");
        breadcrumb.addItemAtIndex(1, middle);

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(3, items.size());
        assertEquals(item1, items.get(0));
        assertEquals(middle, items.get(1));
        assertEquals(item2, items.get(2));
    }

    @Test
    void remove_removesItemFromChildren() {
        BreadcrumbItem item1 = new BreadcrumbItem("Home", "/");
        BreadcrumbItem item2 = new BreadcrumbItem("Docs", "/docs");
        breadcrumb.addItem(item1, item2);

        breadcrumb.remove(item1);

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(1, items.size());
        assertEquals(item2, items.get(0));
    }

    @Test
    void removeAll_removesAllBreadcrumbItems() {
        breadcrumb.addItem(new BreadcrumbItem("Home", "/"),
                new BreadcrumbItem("Docs", "/docs"),
                new BreadcrumbItem("Page", "/page"));

        breadcrumb.removeAll();

        assertTrue(breadcrumb.getItems().isEmpty());
    }

    @Test
    void setItems_varargs_replacesExistingItems() {
        breadcrumb.addItem(new BreadcrumbItem("Old", "/old"));

        BreadcrumbItem item1 = new BreadcrumbItem("New1", "/new1");
        BreadcrumbItem item2 = new BreadcrumbItem("New2", "/new2");
        breadcrumb.setItems(item1, item2);

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(2, items.size());
        assertEquals(item1, items.get(0));
        assertEquals(item2, items.get(1));
    }

    @Test
    void setItems_list_acceptsList() {
        breadcrumb.addItem(new BreadcrumbItem("Old", "/old"));

        BreadcrumbItem item1 = new BreadcrumbItem("New1", "/new1");
        breadcrumb.setItems(List.of(item1));

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(1, items.size());
        assertEquals(item1, items.get(0));
    }

    @Test
    void setSeparator_addsSeparatorWithSlotAttribute() {
        Span separator = new Span("/");
        breadcrumb.setSeparator(separator);

        assertEquals(separator, breadcrumb.getSeparator());
        assertEquals("separator", separator.getElement().getAttribute("slot"));
    }

    @Test
    void getSeparator_returnsSetSeparator() {
        Span separator = new Span(">");
        breadcrumb.setSeparator(separator);

        assertEquals(separator, breadcrumb.getSeparator());
    }

    @Test
    void setSeparator_null_removesExistingSeparator() {
        Span separator = new Span("/");
        breadcrumb.setSeparator(separator);
        assertEquals(separator, breadcrumb.getSeparator());

        breadcrumb.setSeparator(null);
        assertNull(breadcrumb.getSeparator());
    }

    @Test
    void getSeparator_returnsNullWhenNoSeparatorSet() {
        assertNull(breadcrumb.getSeparator());
    }

    @Test
    void setI18n_pushesJsonToElement() {
        BreadcrumbI18n i18n = new BreadcrumbI18n()
                .setNavigationLabel("Breadcrumb");
        breadcrumb.setI18n(i18n);

        ObjectNode json = (ObjectNode) breadcrumb.getElement()
                .getPropertyRaw("i18n");
        assertNotNull(json);
        assertEquals("Breadcrumb", json.get("navigationLabel").asText());
    }

    @Test
    void getI18n_returnsPreviouslySetI18n() {
        BreadcrumbI18n i18n = new BreadcrumbI18n()
                .setNavigationLabel("Breadcrumb").setOverflow("More");
        breadcrumb.setI18n(i18n);

        BreadcrumbI18n result = breadcrumb.getI18n();
        assertSame(i18n, result);
        assertEquals("Breadcrumb", result.getNavigationLabel());
        assertEquals("More", result.getOverflow());
    }

    @Test
    void getI18n_returnsNullWhenNotSet() {
        assertNull(breadcrumb.getI18n());
    }

    @Test
    void i18n_fluentSettersReturnInstance() {
        BreadcrumbI18n i18n = new BreadcrumbI18n();
        BreadcrumbI18n result = i18n.setNavigationLabel("Nav");
        assertSame(i18n, result);

        result = i18n.setOverflow("More");
        assertSame(i18n, result);
    }

    @Test
    void i18n_isSerializable() throws Exception {
        BreadcrumbI18n i18n = new BreadcrumbI18n()
                .setNavigationLabel("Breadcrumb").setOverflow("More");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ObjectOutputStream(baos).writeObject(i18n);

        BreadcrumbI18n deserialized = (BreadcrumbI18n) new ObjectInputStream(
                new ByteArrayInputStream(baos.toByteArray())).readObject();

        assertEquals("Breadcrumb", deserialized.getNavigationLabel());
        assertEquals("More", deserialized.getOverflow());
    }

    @Test
    void i18n_nullFieldsOmittedFromJson() {
        BreadcrumbI18n i18n = new BreadcrumbI18n()
                .setNavigationLabel("Breadcrumb");
        breadcrumb.setI18n(i18n);

        ObjectNode json = (ObjectNode) breadcrumb.getElement()
                .getPropertyRaw("i18n");
        assertTrue(json.has("navigationLabel"));
        assertTrue(!json.has("overflow"),
                "Null overflow field should be omitted from JSON");
    }

    @Test
    void addNavigateListener_returnsRegistration() {
        Registration registration = breadcrumb
                .addNavigateListener(event -> {
                });
        assertInstanceOf(Registration.class, registration);
    }

    @Test
    void navigateEvent_triggersListener() {
        AtomicReference<NavigateEvent> eventRef = new AtomicReference<>();
        breadcrumb.addNavigateListener(eventRef::set);

        ComponentUtil.fireEvent(breadcrumb,
                new NavigateEvent(breadcrumb, false, "/test", false));

        assertNotNull(eventRef.get());
    }

    @Test
    void navigateEvent_carriesCorrectPath() {
        AtomicReference<NavigateEvent> eventRef = new AtomicReference<>();
        breadcrumb.addNavigateListener(eventRef::set);

        ComponentUtil.fireEvent(breadcrumb,
                new NavigateEvent(breadcrumb, false, "/products", false));

        assertEquals("/products", eventRef.get().getPath());
    }

    @Test
    void navigateEvent_carriesCorrectCurrentFlag() {
        AtomicReference<NavigateEvent> eventRef = new AtomicReference<>();
        breadcrumb.addNavigateListener(eventRef::set);

        ComponentUtil.fireEvent(breadcrumb,
                new NavigateEvent(breadcrumb, false, "/current", true));

        assertTrue(eventRef.get().isCurrent());
    }

    @Test
    void navigateListener_removingRegistrationStopsFiring() {
        AtomicReference<NavigateEvent> eventRef = new AtomicReference<>();
        Registration registration = breadcrumb
                .addNavigateListener(eventRef::set);

        registration.remove();

        ComponentUtil.fireEvent(breadcrumb,
                new NavigateEvent(breadcrumb, false, "/test", false));

        assertNull(eventRef.get());
    }

    // --- Auto-trail tests ---

    @PageTitle("Home")
    @Tag("div")
    private static class HomeView extends Component {
    }

    @PageTitle("Products")
    @Tag("div")
    private static class ProductsView extends Component {
    }

    @Tag("div")
    private static class DynamicTitleView extends Component
            implements HasDynamicTitle {
        @Override
        public String getPageTitle() {
            return "Dynamic Title";
        }
    }

    @Tag("div")
    private static class PlainView extends Component {
    }

    private AfterNavigationEvent createMockEvent(HasElement... chain) {
        AfterNavigationEvent event = mock(AfterNavigationEvent.class);
        when(event.getActiveChain()).thenReturn(List.of(chain));
        return event;
    }

    @Test
    void afterNavigation_noExplicitItems_buildsItemsFromRouteChain() {
        HomeView home = new HomeView();
        ProductsView products = new ProductsView();

        breadcrumb.afterNavigation(createMockEvent(home, products));

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(2, items.size());
        assertEquals("Home", items.get(0).getLabel());
        assertEquals("Products", items.get(1).getLabel());
    }

    @Test
    void afterNavigation_usesPageTitleAnnotation() {
        HomeView home = new HomeView();

        breadcrumb.afterNavigation(createMockEvent(home));

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(1, items.size());
        assertEquals("Home", items.get(0).getLabel());
    }

    @Test
    void afterNavigation_usesHasDynamicTitle() {
        DynamicTitleView view = new DynamicTitleView();

        breadcrumb.afterNavigation(createMockEvent(view));

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(1, items.size());
        assertEquals("Dynamic Title", items.get(0).getLabel());
    }

    @Test
    void afterNavigation_fallsBackToSimpleClassName() {
        PlainView view = new PlainView();

        breadcrumb.afterNavigation(createMockEvent(view));

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(1, items.size());
        assertEquals("PlainView", items.get(0).getLabel());
    }

    @Test
    void afterNavigation_lastItemIsMarkedAsCurrent() {
        HomeView home = new HomeView();
        ProductsView products = new ProductsView();

        breadcrumb.afterNavigation(createMockEvent(home, products));

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertFalse(items.get(0).isCurrent());
        assertTrue(items.get(1).isCurrent());
    }

    @Test
    void addItem_disablesAutoMode() {
        breadcrumb.addItem(new BreadcrumbItem("Explicit", "/explicit"));

        HomeView home = new HomeView();
        breadcrumb.afterNavigation(createMockEvent(home));

        // Should still have the explicit item, not auto-generated ones
        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(1, items.size());
        assertEquals("Explicit", items.get(0).getLabel());
    }

    @Test
    void setItems_varargs_disablesAutoMode() {
        breadcrumb.setItems(new BreadcrumbItem("A", "/a"),
                new BreadcrumbItem("B", "/b"));

        HomeView home = new HomeView();
        breadcrumb.afterNavigation(createMockEvent(home));

        // Should still have the explicit items
        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(2, items.size());
        assertEquals("A", items.get(0).getLabel());
        assertEquals("B", items.get(1).getLabel());
    }

    @Test
    void setItems_list_disablesAutoMode() {
        breadcrumb.setItems(List.of(new BreadcrumbItem("X", "/x")));

        HomeView home = new HomeView();
        breadcrumb.afterNavigation(createMockEvent(home));

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(1, items.size());
        assertEquals("X", items.get(0).getLabel());
    }

    @Test
    void afterNavigation_replacesAutoItemsOnSubsequentCalls() {
        HomeView home = new HomeView();
        breadcrumb.afterNavigation(createMockEvent(home));
        assertEquals(1, breadcrumb.getItems().size());

        ProductsView products = new ProductsView();
        breadcrumb.afterNavigation(createMockEvent(home, products));

        List<BreadcrumbItem> items = breadcrumb.getItems();
        assertEquals(2, items.size());
        assertEquals("Home", items.get(0).getLabel());
        assertEquals("Products", items.get(1).getLabel());
    }
}
