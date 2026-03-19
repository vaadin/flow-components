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
package com.vaadin.flow.component.sidenav.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;

import tools.jackson.databind.node.ArrayNode;

class SideNavItemTest {

    private SideNavItem sideNavItem;

    @BeforeEach
    void setup() {
        sideNavItem = new SideNavItem("Item", "path");
    }

    // CONSTRUCTOR TESTS

    @Test
    void allConstructorsArePresent() {
        sideNavItem = new SideNavItem("Label");
        sideNavItem = new SideNavItem("Label", "path");
        sideNavItem = new SideNavItem("Label", "path", new Div());
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("Label", TestRoute.class);
            sideNavItem = new SideNavItem("Label", TestRoute.class,
                    RouteParameters.empty());
            sideNavItem = new SideNavItem("Label", TestRoute.class, new Div());
            sideNavItem = new SideNavItem("Label", TestRoute.class,
                    RouteParameters.empty(), new Div());
        }, TestRoute.class);
    }

    // LABEL TESTS

    @Test
    void returnsExpectedLabel() {
        Assertions.assertEquals("Item", sideNavItem.getLabel());
    }

    @Test
    void changeLabel_labelChanged() {
        sideNavItem.setLabel("Item Changed");
        Assertions.assertEquals("Item Changed", sideNavItem.getLabel());
    }

    @Test
    void setLabelToNull_labelElementRemoved() {
        sideNavItem.setLabel(null);

        Assertions.assertNull(sideNavItem.getLabel());
        Assertions.assertFalse(sideNavItemHasLabelElement());
    }

    @Test
    void setEmptyLabel_labelIsEmpty() {
        sideNavItem.setLabel("");

        Assertions.assertEquals("", sideNavItem.getLabel());
    }

    // PATH AND ALIAS TESTS

    @Test
    void returnsExpectedPathAndAliases() {
        assertPath("path");
        assertPathAliases(Collections.emptySet());
    }

    @Test
    void createWithNoPath_pathNotSet() {
        sideNavItem = new SideNavItem("Test");

        assertPath(null);
    }

    @Test
    void setNullStringPath_pathAttributeRemoved() {
        sideNavItem.setPath((String) null);

        assertPath(null);
    }

    @Test
    void setNullComponentPath_pathAttributeRemoved() {
        sideNavItem.setPath((Class<? extends Component>) null);

        assertPath(null);
    }

    @Test
    void setEmptyPath_returnsEmptyPath() {
        sideNavItem.setPath("");

        assertPath("");
    }

    @Test
    void setPathWithoutAliasesAsComponent_onlyPathUpdated() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRoute.class);

            assertPath("foo/bar");
            assertPathAliases(Collections.emptySet());
        }, TestRoute.class);
    }

    @Test
    void createFromComponent_pathIsSet() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test", TestRoute.class);
            assertPath("foo/bar");
        }, TestRoute.class);
    }

    @Test
    void setEmptyStringAsPathAlias_pathAliasAdded() {
        sideNavItem.setPathAliases(Set.of(""));

        assertPathAliases(Set.of(""));
    }

    @Test
    void setMultiplePathAliases_pathAliasesAdded() {
        sideNavItem.setPathAliases(Set.of("alias1", "alias2"));

        assertPathAliases(Set.of("alias1", "alias2"));
    }

    @Test
    void setMultiplePathAliases_setEmptyPathAliases_pathAliasesEmpty() {
        sideNavItem.setPathAliases(Set.of("alias1", "alias2"));
        sideNavItem.setPathAliases(Collections.emptySet());

        assertPathAliases(Collections.emptySet());
    }

    @Test
    void setMultiplePathAliases_setPathAliasesNull_pathAliasesEmpty() {
        sideNavItem.setPathAliases(Set.of("alias1", "alias2"));
        sideNavItem.setPathAliases(null);

        assertPathAliases(Collections.emptySet());
    }

    @Test
    void setPathAsComponent_setOtherPathAsComponent_pathAndAliasesUpdated() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithAliases.class);
            sideNavItem.setPath(OtherTestRouteWithAliases.class);

            assertPath("bar/foo");
            assertPathAliases(Set.of("baz/foo"));
        }, TestRouteWithAliases.class, OtherTestRouteWithAliases.class);
    }

    @Test
    void setPathAsComponent_setNullAsComponent_pathAndAliasesRemoved() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithAliases.class);
            sideNavItem.setPath((Class<Component>) null);

            assertPath(null);
            assertPathAliases(Collections.emptySet());
        }, TestRouteWithAliases.class);
    }

    @Test
    void createWithPathAndPrefix_pathAndPrefixIsSet() {
        final Div prefixComponent = new Div();
        sideNavItem = new SideNavItem("Test item", "test-path",
                prefixComponent);

        assertPath("test-path");
        Assertions.assertEquals(prefixComponent,
                sideNavItem.getPrefixComponent());
    }

    // TOOLTIP TESTS
    @Test
    void implementsHasTooltip() {
        sideNavItem = new SideNavItem("Test item", "test-path");
        Assertions.assertTrue(sideNavItem instanceof HasTooltip);
    }

    @Test
    void tooltipTextIsSet() {
        sideNavItem = new SideNavItem("Test item", "test-path");
        sideNavItem.setTooltipText("Test tooltip text");
        Assertions.assertNotNull(sideNavItem.getTooltip());
        Assertions.assertEquals("Test tooltip text",
                sideNavItem.getTooltip().getText());
    }

    // EXPAND AND COLLAPSE TESTS

    @Test
    void isCollapsedByDefault() {
        Assertions.assertFalse(sideNavItem.isExpanded());
    }

    @Test
    void setExpanded_isExpanded() {
        sideNavItem.setExpanded(true);

        Assertions.assertTrue(sideNavItem.isExpanded());
    }

    @Test
    void expandAndCollapse_isCollapsed() {
        sideNavItem.setExpanded(true);
        sideNavItem.setExpanded(false);

        Assertions.assertFalse(sideNavItem.isExpanded());
    }

    // CHILDREN TESTS

    @Test
    void hasCorrectNumberOfChildren() {
        // one child for the label element
        Assertions.assertEquals(1, sideNavItem.getElement().getChildCount());
    }

    @Test
    void addSingleItem_itemAdded() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItem(testItem);

        Assertions.assertEquals(2, sideNavItem.getElement().getChildCount());
        Assertions.assertEquals("children",
                sideNavItem.getElement().getChild(1).getAttribute("slot"));
        Assertions.assertEquals(1, sideNavItem.getItems().size());
        Assertions.assertEquals(testItem, sideNavItem.getItems().get(0));
    }

    @Test
    void addTwoItemsAtOnce_itemsAdded() {
        final SideNavItem testItem1 = new SideNavItem("testItem1");
        final SideNavItem testItem2 = new SideNavItem("testItem2");

        sideNavItem.addItem(testItem1, testItem2);

        Assertions.assertEquals(2, sideNavItem.getItems().size());
        Assertions.assertEquals(testItem1, sideNavItem.getItems().get(0));
        Assertions.assertEquals(testItem2, sideNavItem.getItems().get(1));
    }

    @Test
    void addItemAsFirst_itemIsAdded() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAsFirst(testItem);

        Assertions.assertEquals(1, sideNavItem.getItems().size());
        Assertions.assertEquals(testItem, sideNavItem.getItems().get(0));
    }

    @Test
    void multipleItems_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAsFirst(testItem);

        Assertions.assertEquals(initialItems.size() + 1,
                sideNavItem.getItems().size());
        Assertions.assertEquals(testItem, sideNavItem.getItems().get(0));
    }

    @Test
    void addItemAtNegativeIndex_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> sideNavItem.addItemAtIndex(-1,
                        new SideNavItem("testItem")));
    }

    @Test
    void noItems_addItemAtTooHighIndex_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> sideNavItem.addItemAtIndex(1,
                        new SideNavItem("testItem")));
    }

    @Test
    void addItemAtTooHighIndex_throws() {
        final List<SideNavItem> items = setupItems();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> sideNavItem.addItemAtIndex(items.size() + 1,
                        new SideNavItem("testItem")));
    }

    @Test
    void noItems_addItemAtIndexZero_itemIsAdded() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(0, testItem);

        Assertions.assertEquals(1, sideNavItem.getItems().size());
        Assertions.assertEquals(testItem, sideNavItem.getItems().get(0));
    }

    @Test
    void multipleItems_addItemAtIndex_itemIsAdded() {
        final List<SideNavItem> items = setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(2, testItem);

        Assertions.assertEquals(items.size() + 1,
                sideNavItem.getItems().size());
        Assertions.assertEquals(testItem, sideNavItem.getItems().get(2));
    }

    @Test
    void multipleItems_addItemAtIndex_itemHasCorrectSlot() {
        setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(2, testItem);

        Assertions.assertEquals("children",
                testItem.getElement().getAttribute("slot"));
    }

    @Test
    void multipleItemsPrefixAndSuffix_addItemAtIndex_addedItemHasCorrectPosition() {
        final List<SideNavItem> items = setupItemsPrefixAndSuffix();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(2, testItem);

        Assertions.assertEquals(items.size() + 1,
                sideNavItem.getItems().size());
        Assertions.assertEquals(testItem, sideNavItem.getItems().get(2));
    }

    @Test
    void multipleItemsPrefixAndSuffix_addItemAtHigherIndex_addedItemHasCorrectPosition() {
        final List<SideNavItem> items = setupItemsPrefixAndSuffix();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(3, testItem);

        Assertions.assertEquals(items.size() + 1,
                sideNavItem.getItems().size());
        Assertions.assertEquals(testItem, sideNavItem.getItems().get(3));
    }

    @Test
    void multipleItems_addItemAtLastIndex_itemIsAppended() {
        final List<SideNavItem> items = setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(sideNavItem.getItems().size(), testItem);

        Assertions.assertEquals(items.size() + 1,
                sideNavItem.getItems().size());
        Assertions.assertEquals(testItem,
                sideNavItem.getItems().get(sideNavItem.getItems().size() - 1));
    }

    @Test
    void multipleItems_removeAll_onlyItemsRemoved() {
        setupItemsPrefixAndSuffix();

        sideNavItem.removeAll();

        Assertions.assertTrue(sideNavItem.getItems().isEmpty());
        Assertions.assertNotNull(sideNavItem.getLabel());
        Assertions.assertNotNull(sideNavItem.getPrefixComponent());
        Assertions.assertNotNull(sideNavItem.getSuffixComponent());
    }

    @Test
    void removeSingleItem_itemRemoved() {
        final List<SideNavItem> sideNavItems = setupItems();

        sideNavItem.remove(sideNavItems.get(2));

        Assertions.assertEquals(sideNavItems.size() - 1,
                sideNavItem.getItems().size());
        Assertions.assertFalse(
                sideNavItem.getItems().contains(sideNavItems.get(2)));
    }

    @Test
    void removeTwoItems_bothItemsRemoved() {
        final List<SideNavItem> sideNavItems = setupItems();

        sideNavItem.remove(sideNavItems.get(1), sideNavItems.get(2));

        Assertions.assertEquals(sideNavItems.size() - 2,
                sideNavItem.getItems().size());
        Assertions.assertFalse(
                sideNavItem.getItems().contains(sideNavItems.get(1)));
        Assertions.assertFalse(
                sideNavItem.getItems().contains(sideNavItems.get(2)));
    }

    @Test
    void removeUnknownItem_nothingHappens() {
        final List<SideNavItem> sideNavItems = setupItems();

        sideNavItem.remove(new SideNavItem("Foreign item"));

        Assertions.assertEquals(sideNavItem.getItems(), sideNavItems);
    }

    // QUERY PARAMETERS TESTS

    @Test
    void setQueryParameters_pathContainsParameters() {
        QueryParameters queryParameters = new QueryParameters(Map.of("k1",
                List.of("v11", "v12"), "k2", List.of("v21", "v22")));
        sideNavItem.setQueryParameters(queryParameters);

        assertPath("path?" + queryParameters.getQueryString());
    }

    @Test
    void createFromComponent_setQueryParameters_pathContainsParameters() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test", TestRoute.class);

            QueryParameters queryParameters = new QueryParameters(Map.of("k1",
                    List.of("v11", "v12"), "k2", List.of("v21", "v22")));
            sideNavItem.setQueryParameters(queryParameters);

            assertPath("foo/bar?" + queryParameters.getQueryString());
        }, TestRoute.class);
    }

    @Test
    void setQueryParameters_updateQueryParameters_pathIsUpdated() {
        sideNavItem.setQueryParameters(
                new QueryParameters(Map.of("k1", List.of("v11", "v12"))));

        QueryParameters queryParameters = new QueryParameters(
                Map.of("k2", List.of("v21", "v22")));
        sideNavItem.setQueryParameters(queryParameters);

        assertPath("path?" + queryParameters.getQueryString());
    }

    @Test
    void setQueryParameters_setQueryParametersNull_parametersRemovedFromPath() {
        sideNavItem.setQueryParameters(
                new QueryParameters(Map.of("k1", List.of("v11", "v12"))));

        sideNavItem.setQueryParameters(null);

        assertPath("path");
    }

    @Test
    void createFromComponent_setQueryParameters_updateQueryParameters_pathIsUpdated() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test", TestRoute.class);
            sideNavItem.setQueryParameters(
                    new QueryParameters(Map.of("k1", List.of("v11", "v12"))));

            QueryParameters queryParameters = new QueryParameters(
                    Map.of("k2", List.of("v21", "v22")));
            sideNavItem.setQueryParameters(queryParameters);

            assertPath("foo/bar?" + queryParameters.getQueryString());
        }, TestRoute.class);
    }

    @Test
    void createFromComponent_setQueryParameters_setQueryParametersNull_parametersRemovedFromPath() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test", TestRoute.class);
            sideNavItem.setQueryParameters(
                    new QueryParameters(Map.of("k1", List.of("v11", "v12"))));

            sideNavItem.setQueryParameters(null);

            assertPath("foo/bar");
        }, TestRoute.class);
    }

    @Test
    void setPathAlias_setQueryParameters_pathAliasDoesNotContainParameters() {
        sideNavItem.setPathAliases(Set.of("pathAlias"));

        QueryParameters queryParameters = new QueryParameters(Map.of("k1",
                List.of("v11", "v12"), "k2", List.of("v21", "v22")));
        sideNavItem.setQueryParameters(queryParameters);

        assertPathAliases(Set.of("pathAlias"));
    }

    @Test
    void setPathAsComponent_setQueryParameters_pathAliasDoesNotContainParameters() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithAliases.class);

            QueryParameters queryParameters = new QueryParameters(Map.of("k1",
                    List.of("v11", "v12"), "k2", List.of("v21", "v22")));
            sideNavItem.setQueryParameters(queryParameters);

            assertPathAliases(Set.of("foo/baz", "foo/qux"));
        }, TestRouteWithAliases.class);
    }

    // ROUTE PARAMETERS TESTS

    @Test
    void createFromComponentWithRouteParameters_pathContainsParameters() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test",
                    TestRouteWithRouteParams.class,
                    new RouteParameters(Map.of("k1", "v1", "k2", "v2")));

            assertPath("foo/v1/v2/bar");
        }, TestRouteWithRouteParams.class);
    }

    @Test
    void setPathAndRouteParametersAsComponent_pathContainsParameters() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithRouteParams.class,
                    new RouteParameters(Map.of("k1", "v1", "k2", "v2")));

            assertPath("foo/v1/v2/bar");
        }, TestRouteWithRouteParams.class);
    }

    @Test
    void setPathAndRouteParametersAsComponent_aliasesWithMatchingParamsUpdated() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithAliases.class, new RouteParameters(
                    Map.of("key1", "value1", "key2", "value2")));

            assertPathAliases(Set.of("foo/baz", "foo/qux", "foo/value1/bar",
                    "foo/value1/value1/bar", "foo/value1/value2/bar"));
        }, TestRouteWithAliases.class);
    }

    @Test
    void createFromComponentWithRouteParameters_aliasesWithMatchingParamsUpdated() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test", TestRouteWithAliases.class,
                    new RouteParameters(
                            Map.of("key1", "value1", "key2", "value2")));

            assertPathAliases(Set.of("foo/baz", "foo/qux", "foo/value1/bar",
                    "foo/value1/value1/bar", "foo/value1/value2/bar"));
        }, TestRouteWithAliases.class);
    }

    @Test
    void setPathAndRouteParametersAsComponent_aliasWithMissingParamNotAdded() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithAliases.class,
                    new RouteParameters(Map.of("key1", "value1")));

            assertPathAliases(Set.of("foo/baz", "foo/qux", "foo/value1/bar",
                    "foo/value1/value1/bar"));
        }, TestRouteWithAliases.class);
    }

    @Test
    void createFromComponentWithRouteParameters_aliasWithMissingParamNotAdded() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test", TestRouteWithAliases.class,
                    new RouteParameters(Map.of("key1", "value1")));

            assertPathAliases(Set.of("foo/baz", "foo/qux", "foo/value1/bar",
                    "foo/value1/value1/bar"));
        }, TestRouteWithAliases.class);
    }

    @Test
    void setPathAsComponentWithMissingRouteParameter_throws() {
        Assertions.assertThrows(NotFoundException.class,
                () -> runWithMockRouter(() -> {
                    sideNavItem.setPath(TestRouteWithRouteParams.class,
                            new RouteParameters(Map.of("k1", "v1")));
                }, TestRouteWithRouteParams.class));
    }

    @Test
    void setPathAsComponent_aliasWithMissingParameterNotAdded() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithAliases.class);

            assertPath("foo/bar");
            assertPathAliases(Set.of("foo/baz", "foo/qux"));
        }, TestRouteWithAliases.class);
    }

    @Test
    void createFromComponentWithHasUrlParameter_pathContainsParameters() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test",
                    TestRouteWithHasUrlParameter.class, "bar/baz");

            assertPath("foo/bar/baz");
        }, TestRouteWithHasUrlParameter.class);
    }

    @Test
    void setPathAsComponentWithHasUrlParameter_pathContainsParameters() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithHasUrlParameter.class, "bar/baz");

            assertPath("foo/bar/baz");
        }, TestRouteWithHasUrlParameter.class);
    }

    @Test
    void setTarget_hasTarget() {
        sideNavItem.setTarget("_blank");
        Assertions.assertEquals("_blank",
                sideNavItem.getElement().getProperty("target"));
        Assertions.assertEquals("_blank", sideNavItem.getTarget());
    }

    @Test
    void targetDefined_setToNull_noTarget() {
        sideNavItem.setTarget("_blank");
        sideNavItem.setTarget(null);
        Assertions.assertFalse(sideNavItem.getElement().hasProperty("target"));
        Assertions.assertNull(sideNavItem.getTarget());
    }

    @Test
    void isMatchNested_falseByDefault() {
        Assertions.assertFalse(sideNavItem.isMatchNested());
    }

    @Test
    void setMatchNested_isMatchNested() {
        sideNavItem.setMatchNested(true);
        Assertions.assertTrue(
                sideNavItem.getElement().getProperty("matchNested", false));
        Assertions.assertTrue(sideNavItem.isMatchNested());

        sideNavItem.setMatchNested(false);
        Assertions.assertFalse(
                sideNavItem.getElement().getProperty("matchNested", false));
        Assertions.assertFalse(sideNavItem.isMatchNested());
    }

    @Test
    void isRouterIgnore_falseByDefault() {
        Assertions.assertFalse(sideNavItem.isRouterIgnore());
    }

    @Test
    void setRouterIgnore_hasRouterIgnore() {
        sideNavItem.setRouterIgnore(true);
        Assertions.assertTrue(
                sideNavItem.getElement().getProperty("routerIgnore", false));
        Assertions.assertTrue(sideNavItem.isRouterIgnore());

        sideNavItem.setRouterIgnore(false);
        Assertions.assertFalse(
                sideNavItem.getElement().getProperty("routerIgnore", false));
        Assertions.assertFalse(sideNavItem.isRouterIgnore());
    }

    @Test
    void setOpenInNewBrowserTab_targetBlankDefinedOnProperty() {
        // call setOpenInNewTab and check that getTarget returns "_blank"
        sideNavItem.setOpenInNewBrowserTab(true);
        Assertions.assertEquals("_blank",
                sideNavItem.getElement().getProperty("target"));
        Assertions.assertTrue(sideNavItem.isOpenInNewBrowserTab());
    }

    @Test
    void openInNewBrowserTabDefined_setOpenInNewBrowserTabToFalse() {
        sideNavItem.setOpenInNewBrowserTab(true);
        sideNavItem.setOpenInNewBrowserTab(false);
        Assertions.assertFalse(sideNavItem.getElement().hasProperty("target"));
        Assertions.assertFalse(sideNavItem.isOpenInNewBrowserTab());
    }

    private boolean sideNavItemHasLabelElement() {
        return sideNavItem.getElement().getChildren()
                .anyMatch(this::isLabelElement);
    }

    private boolean isLabelElement(Element element) {
        return !element.hasAttribute("slot");
    }

    @SafeVarargs
    private void runWithMockRouter(Runnable test,
            Class<? extends Component>... routes) {
        Router router = mockRouter(routes);
        try (MockedStatic<ComponentUtil> mockComponentUtil = Mockito
                .mockStatic(ComponentUtil.class)) {
            mockComponentUtil.when(() -> ComponentUtil.getRouter(Mockito.any()))
                    .thenReturn(router);
            test.run();
        }
    }

    @SafeVarargs
    private Router mockRouter(Class<? extends Component>... navigationTargets) {
        VaadinContext mockContext = Mockito.mock(VaadinContext.class);
        ApplicationRouteRegistry routeRegistry = ApplicationRouteRegistry
                .getInstance(mockContext);
        Router router = new Router(routeRegistry);

        RouteConfiguration routeConfiguration = RouteConfiguration
                .forRegistry(routeRegistry);
        for (Class<? extends Component> navigationTarget : navigationTargets) {
            routeConfiguration.setAnnotatedRoute(navigationTarget);
        }
        return router;
    }

    private List<SideNavItem> setupItems() {
        List<SideNavItem> items = new ArrayList<>();

        addNavItem("Item1", "http://localhost:8080/item1", items);
        addNavItem("Item2", "http://localhost:8080/item2", items);
        addNavItem("Item3", "http://localhost:8080/item3", items);
        addNavItem("Item4", "http://localhost:8080/item4", items);

        return items;
    }

    private List<SideNavItem> setupItemsPrefixAndSuffix() {
        List<SideNavItem> items = new ArrayList<>();

        addNavItem("Item1", "http://localhost:8080/item1", items);
        sideNavItem.setPrefixComponent(new Div());
        addNavItem("Item2", "http://localhost:8080/item2", items);
        addNavItem("Item3", "http://localhost:8080/item3", items);
        sideNavItem.setSuffixComponent(new Div());
        addNavItem("Item4", "http://localhost:8080/item4", items);

        return items;
    }

    private void addNavItem(String Item1, String url, List<SideNavItem> items) {
        SideNavItem item = new SideNavItem(Item1, url);
        items.add(item);
        sideNavItem.addItem(item);
    }

    private void assertPath(String expectedPath) {
        if (expectedPath == null) {
            Assertions.assertNull(sideNavItem.getPath());
            Assertions
                    .assertFalse(sideNavItem.getElement().hasAttribute("path"));
        } else {
            Assertions.assertEquals(expectedPath, sideNavItem.getPath());
            Assertions.assertEquals(expectedPath,
                    sideNavItem.getElement().getAttribute("path"));
        }
    }

    private void assertPathAliases(Set<String> expectedAliases) {
        Assertions.assertEquals(expectedAliases, sideNavItem.getPathAliases());
        if (expectedAliases.isEmpty()) {
            Assertions.assertFalse(
                    sideNavItem.getElement().hasProperty("pathAliases"));
        } else {
            ArrayNode actualAliasesArray = (ArrayNode) sideNavItem.getElement()
                    .getPropertyRaw("pathAliases");
            Assertions.assertNotNull(actualAliasesArray);
            Set<String> actualAliasesSet = new HashSet<>();
            for (int i = 0; i < actualAliasesArray.size(); i++) {
                actualAliasesSet.add(actualAliasesArray.get(i).asString());
            }
            Assertions.assertEquals(expectedAliases, actualAliasesSet);
        }
    }

    @Route("foo/bar")
    private static class TestRoute extends Component {

    }

    @Route("foo/:k1/:k2/bar")
    private static class TestRouteWithRouteParams extends Component {

    }

    @Route("foo")
    private static class TestRouteWithHasUrlParameter extends Component
            implements HasUrlParameter<String> {
        @Override
        public void setParameter(BeforeEvent event, String parameter) {

        }
    }

    @Route("foo/bar")
    @RouteAlias("foo/baz")
    @RouteAlias("foo/qux")
    @RouteAlias("foo/:key1/bar")
    @RouteAlias("foo/:key1/:key1/bar")
    @RouteAlias("foo/:key1/:key2/bar")
    private static class TestRouteWithAliases extends Component {

    }

    @Route("bar/foo")
    @RouteAlias("baz/foo")
    private static class OtherTestRouteWithAliases extends Component {

    }
}
