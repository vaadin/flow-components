/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;
import elemental.json.JsonArray;
import elemental.json.impl.JsonUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.Element;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class SideNavItemTest {

    private SideNavItem sideNavItem;

    @Before
    public void setup() {
        sideNavItem = new SideNavItem("Item", "path");
    }

    // CONSTRUCTOR TESTS

    @Test
    public void allConstructorsArePresent() {
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
    public void returnsExpectedLabel() {
        Assert.assertEquals("Item", sideNavItem.getLabel());
    }

    @Test
    public void changeLabel_labelChanged() {
        sideNavItem.setLabel("Item Changed");
        Assert.assertEquals("Item Changed", sideNavItem.getLabel());
    }

    @Test
    public void setLabelToNull_labelElementRemoved() {
        sideNavItem.setLabel(null);

        Assert.assertNull(sideNavItem.getLabel());
        Assert.assertFalse(sideNavItemHasLabelElement());
    }

    @Test
    public void setEmptyLabel_labelIsEmpty() {
        sideNavItem.setLabel("");

        Assert.assertEquals("", sideNavItem.getLabel());
    }

    // PATH AND ALIAS TESTS

    @Test
    public void returnsExpectedPathAndAliases() {
        assertPath("path");
        assertPathAliases(Collections.emptySet());
    }

    @Test
    public void createWithNoPath_pathNotSet() {
        sideNavItem = new SideNavItem("Test");

        assertPath(null);
    }

    @Test
    public void setNullStringPath_pathAttributeRemoved() {
        sideNavItem.setPath((String) null);

        assertPath(null);
    }

    @Test
    public void setNullComponentPath_pathAttributeRemoved() {
        sideNavItem.setPath((Class<? extends Component>) null);

        assertPath(null);
    }

    @Test
    public void setEmptyPath_returnsEmptyPath() {
        sideNavItem.setPath("");

        assertPath("");
    }

    @Test
    public void setPathWithoutAliasesAsComponent_onlyPathUpdated() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRoute.class);

            assertPath("foo/bar");
            assertPathAliases(Collections.emptySet());
        }, TestRoute.class);
    }

    @Test
    public void createFromComponent_pathIsSet() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test", TestRoute.class);
            assertPath("foo/bar");
        }, TestRoute.class);
    }

    @Test
    public void setEmptyStringAsPathAlias_pathAliasAdded() {
        sideNavItem.setPathAliases(Set.of(""));

        assertPathAliases(Set.of(""));
    }

    @Test
    public void setMultiplePathAliases_pathAliasesAdded() {
        sideNavItem.setPathAliases(Set.of("alias1", "alias2"));

        assertPathAliases(Set.of("alias1", "alias2"));
    }

    @Test
    public void setMultiplePathAliases_setEmptyPathAliases_pathAliasesEmpty() {
        sideNavItem.setPathAliases(Set.of("alias1", "alias2"));
        sideNavItem.setPathAliases(Collections.emptySet());

        assertPathAliases(Collections.emptySet());
    }

    @Test
    public void setMultiplePathAliases_setPathAliasesNull_pathAliasesEmpty() {
        sideNavItem.setPathAliases(Set.of("alias1", "alias2"));
        sideNavItem.setPathAliases(null);

        assertPathAliases(Collections.emptySet());
    }

    @Test
    public void setPathAsComponent_setOtherPathAsComponent_pathAndAliasesUpdated() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithAliases.class);
            sideNavItem.setPath(OtherTestRouteWithAliases.class);

            assertPath("bar/foo");
            assertPathAliases(Set.of("baz/foo"));
        }, TestRouteWithAliases.class, OtherTestRouteWithAliases.class);
    }

    @Test
    public void setPathAsComponent_setNullAsComponent_pathAndAliasesRemoved() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithAliases.class);
            sideNavItem.setPath((Class<Component>) null);

            assertPath(null);
            assertPathAliases(Collections.emptySet());
        }, TestRouteWithAliases.class);
    }

    @Test
    public void createWithPathAndPrefix_pathAndPrefixIsSet() {
        final Div prefixComponent = new Div();
        sideNavItem = new SideNavItem("Test item", "test-path",
                prefixComponent);

        assertPath("test-path");
        Assert.assertEquals(prefixComponent, sideNavItem.getPrefixComponent());
    }

    // EXPAND AND COLLAPSE TESTS

    @Test
    public void isCollapsedByDefault() {
        Assert.assertFalse(sideNavItem.isExpanded());
    }

    @Test
    public void setExpanded_isExpanded() {
        sideNavItem.setExpanded(true);

        Assert.assertTrue(sideNavItem.isExpanded());
    }

    @Test
    public void expandAndCollapse_isCollapsed() {
        sideNavItem.setExpanded(true);
        sideNavItem.setExpanded(false);

        Assert.assertFalse(sideNavItem.isExpanded());
    }

    // CHILDREN TESTS

    @Test
    public void hasCorrectNumberOfChildren() {
        // one child for the label element
        Assert.assertEquals(1, sideNavItem.getElement().getChildCount());
    }

    @Test
    public void addSingleItem_itemAdded() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItem(testItem);

        Assert.assertEquals(2, sideNavItem.getElement().getChildCount());
        Assert.assertEquals("children",
                sideNavItem.getElement().getChild(1).getAttribute("slot"));
        Assert.assertEquals(1, sideNavItem.getItems().size());
        Assert.assertEquals(testItem, sideNavItem.getItems().get(0));
    }

    @Test
    public void addTwoItemsAtOnce_itemsAdded() {
        final SideNavItem testItem1 = new SideNavItem("testItem1");
        final SideNavItem testItem2 = new SideNavItem("testItem2");

        sideNavItem.addItem(testItem1, testItem2);

        Assert.assertEquals(2, sideNavItem.getItems().size());
        Assert.assertEquals(testItem1, sideNavItem.getItems().get(0));
        Assert.assertEquals(testItem2, sideNavItem.getItems().get(1));
    }

    @Test
    public void addItemAsFirst_itemIsAdded() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAsFirst(testItem);

        Assert.assertEquals(1, sideNavItem.getItems().size());
        Assert.assertEquals(testItem, sideNavItem.getItems().get(0));
    }

    @Test
    public void multipleItems_addItemAsFirst_itemIsFirst() {
        List<SideNavItem> initialItems = setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAsFirst(testItem);

        Assert.assertEquals(initialItems.size() + 1,
                sideNavItem.getItems().size());
        Assert.assertEquals(testItem, sideNavItem.getItems().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addItemAtNegativeIndex_throws() {
        sideNavItem.addItemAtIndex(-1, new SideNavItem("testItem"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void noItems_addItemAtTooHighIndex_throws() {
        sideNavItem.addItemAtIndex(1, new SideNavItem("testItem"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addItemAtTooHighIndex_throws() {
        final List<SideNavItem> items = setupItems();

        sideNavItem.addItemAtIndex(items.size() + 1,
                new SideNavItem("testItem"));
    }

    @Test
    public void noItems_addItemAtIndexZero_itemIsAdded() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(0, testItem);

        Assert.assertEquals(1, sideNavItem.getItems().size());
        Assert.assertEquals(testItem, sideNavItem.getItems().get(0));
    }

    @Test
    public void multipleItems_addItemAtIndex_itemIsAdded() {
        final List<SideNavItem> items = setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(2, testItem);

        Assert.assertEquals(items.size() + 1, sideNavItem.getItems().size());
        Assert.assertEquals(testItem, sideNavItem.getItems().get(2));
    }

    @Test
    public void multipleItems_addItemAtIndex_itemHasCorrectSlot() {
        setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(2, testItem);

        Assert.assertEquals("children",
                testItem.getElement().getAttribute("slot"));
    }

    @Test
    public void multipleItemsPrefixAndSuffix_addItemAtIndex_addedItemHasCorrectPosition() {
        final List<SideNavItem> items = setupItemsPrefixAndSuffix();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(2, testItem);

        Assert.assertEquals(items.size() + 1, sideNavItem.getItems().size());
        Assert.assertEquals(testItem, sideNavItem.getItems().get(2));
    }

    @Test
    public void multipleItemsPrefixAndSuffix_addItemAtHigherIndex_addedItemHasCorrectPosition() {
        final List<SideNavItem> items = setupItemsPrefixAndSuffix();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(3, testItem);

        Assert.assertEquals(items.size() + 1, sideNavItem.getItems().size());
        Assert.assertEquals(testItem, sideNavItem.getItems().get(3));
    }

    @Test
    public void multipleItems_addItemAtLastIndex_itemIsAppended() {
        final List<SideNavItem> items = setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(sideNavItem.getItems().size(), testItem);

        Assert.assertEquals(items.size() + 1, sideNavItem.getItems().size());
        Assert.assertEquals(testItem,
                sideNavItem.getItems().get(sideNavItem.getItems().size() - 1));
    }

    @Test
    public void multipleItems_removeAll_onlyItemsRemoved() {
        setupItemsPrefixAndSuffix();

        sideNavItem.removeAll();

        Assert.assertTrue(sideNavItem.getItems().isEmpty());
        Assert.assertNotNull(sideNavItem.getLabel());
        Assert.assertNotNull(sideNavItem.getPrefixComponent());
        Assert.assertNotNull(sideNavItem.getSuffixComponent());
    }

    @Test
    public void removeSingleItem_itemRemoved() {
        final List<SideNavItem> sideNavItems = setupItems();

        sideNavItem.remove(sideNavItems.get(2));

        Assert.assertEquals(sideNavItems.size() - 1,
                sideNavItem.getItems().size());
        Assert.assertFalse(
                sideNavItem.getItems().contains(sideNavItems.get(2)));
    }

    @Test
    public void removeTwoItems_bothItemsRemoved() {
        final List<SideNavItem> sideNavItems = setupItems();

        sideNavItem.remove(sideNavItems.get(1), sideNavItems.get(2));

        Assert.assertEquals(sideNavItems.size() - 2,
                sideNavItem.getItems().size());
        Assert.assertFalse(
                sideNavItem.getItems().contains(sideNavItems.get(1)));
        Assert.assertFalse(
                sideNavItem.getItems().contains(sideNavItems.get(2)));
    }

    @Test
    public void removeUnknownItem_nothingHappens() {
        final List<SideNavItem> sideNavItems = setupItems();

        sideNavItem.remove(new SideNavItem("Foreign item"));

        Assert.assertEquals(sideNavItem.getItems(), sideNavItems);
    }

    // QUERY PARAMETERS TESTS

    @Test
    public void setQueryParameters_pathContainsParameters() {
        QueryParameters queryParameters = new QueryParameters(Map.of("k1",
                List.of("v11", "v12"), "k2", List.of("v21", "v22")));
        sideNavItem.setQueryParameters(queryParameters);

        assertPath("path?" + queryParameters.getQueryString());
    }

    @Test
    public void createFromComponent_setQueryParameters_pathContainsParameters() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test", TestRoute.class);

            QueryParameters queryParameters = new QueryParameters(Map.of("k1",
                    List.of("v11", "v12"), "k2", List.of("v21", "v22")));
            sideNavItem.setQueryParameters(queryParameters);

            assertPath("foo/bar?" + queryParameters.getQueryString());
        }, TestRoute.class);
    }

    @Test
    public void setQueryParameters_updateQueryParameters_pathIsUpdated() {
        sideNavItem.setQueryParameters(
                new QueryParameters(Map.of("k1", List.of("v11", "v12"))));

        QueryParameters queryParameters = new QueryParameters(
                Map.of("k2", List.of("v21", "v22")));
        sideNavItem.setQueryParameters(queryParameters);

        assertPath("path?" + queryParameters.getQueryString());
    }

    @Test
    public void setQueryParameters_setQueryParametersNull_parametersRemovedFromPath() {
        sideNavItem.setQueryParameters(
                new QueryParameters(Map.of("k1", List.of("v11", "v12"))));

        sideNavItem.setQueryParameters(null);

        assertPath("path");
    }

    @Test
    public void createFromComponent_setQueryParameters_updateQueryParameters_pathIsUpdated() {
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
    public void createFromComponent_setQueryParameters_setQueryParametersNull_parametersRemovedFromPath() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test", TestRoute.class);
            sideNavItem.setQueryParameters(
                    new QueryParameters(Map.of("k1", List.of("v11", "v12"))));

            sideNavItem.setQueryParameters(null);

            assertPath("foo/bar");
        }, TestRoute.class);
    }

    @Test
    public void setPathAlias_setQueryParameters_pathAliasDoesNotContainParameters() {
        sideNavItem.setPathAliases(Set.of("pathAlias"));

        QueryParameters queryParameters = new QueryParameters(Map.of("k1",
                List.of("v11", "v12"), "k2", List.of("v21", "v22")));
        sideNavItem.setQueryParameters(queryParameters);

        assertPathAliases(Set.of("pathAlias"));
    }

    @Test
    public void setPathAsComponent_setQueryParameters_pathAliasDoesNotContainParameters() {
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
    public void createFromComponentWithRouteParameters_pathContainsParameters() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test",
                    TestRouteWithRouteParams.class,
                    new RouteParameters(Map.of("k1", "v1", "k2", "v2")));

            assertPath("foo/v1/v2/bar");
        }, TestRouteWithRouteParams.class);
    }

    @Test
    public void setPathAndRouteParametersAsComponent_pathContainsParameters() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithRouteParams.class,
                    new RouteParameters(Map.of("k1", "v1", "k2", "v2")));

            assertPath("foo/v1/v2/bar");
        }, TestRouteWithRouteParams.class);
    }

    @Test
    public void setPathAndRouteParametersAsComponent_aliasesWithMatchingParamsUpdated() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithAliases.class, new RouteParameters(
                    Map.of("key1", "value1", "key2", "value2")));

            assertPathAliases(Set.of("foo/baz", "foo/qux", "foo/value1/bar",
                    "foo/value1/value1/bar", "foo/value1/value2/bar"));
        }, TestRouteWithAliases.class);
    }

    @Test
    public void createFromComponentWithRouteParameters_aliasesWithMatchingParamsUpdated() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test", TestRouteWithAliases.class,
                    new RouteParameters(
                            Map.of("key1", "value1", "key2", "value2")));

            assertPathAliases(Set.of("foo/baz", "foo/qux", "foo/value1/bar",
                    "foo/value1/value1/bar", "foo/value1/value2/bar"));
        }, TestRouteWithAliases.class);
    }

    @Test
    public void setPathAndRouteParametersAsComponent_aliasWithMissingParamNotAdded() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithAliases.class,
                    new RouteParameters(Map.of("key1", "value1")));

            assertPathAliases(Set.of("foo/baz", "foo/qux", "foo/value1/bar",
                    "foo/value1/value1/bar"));
        }, TestRouteWithAliases.class);
    }

    @Test
    public void createFromComponentWithRouteParameters_aliasWithMissingParamNotAdded() {
        runWithMockRouter(() -> {
            sideNavItem = new SideNavItem("test", TestRouteWithAliases.class,
                    new RouteParameters(Map.of("key1", "value1")));

            assertPathAliases(Set.of("foo/baz", "foo/qux", "foo/value1/bar",
                    "foo/value1/value1/bar"));
        }, TestRouteWithAliases.class);
    }

    @Test(expected = NotFoundException.class)
    public void setPathAsComponentWithMissingRouteParameter_throws() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithRouteParams.class,
                    new RouteParameters(Map.of("k1", "v1")));
        }, TestRouteWithRouteParams.class);
    }

    @Test
    public void setPathAsComponent_aliasWithMissingParameterNotAdded() {
        runWithMockRouter(() -> {
            sideNavItem.setPath(TestRouteWithAliases.class);

            assertPath("foo/bar");
            assertPathAliases(Set.of("foo/baz", "foo/qux"));
        }, TestRouteWithAliases.class);
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
            Assert.assertNull(sideNavItem.getPath());
            Assert.assertFalse(sideNavItem.getElement().hasAttribute("path"));
        } else {
            Assert.assertEquals(expectedPath, sideNavItem.getPath());
            Assert.assertEquals(expectedPath,
                    sideNavItem.getElement().getAttribute("path"));
        }
    }

    private void assertPathAliases(Set<String> expectedAliases) {
        Assert.assertEquals(expectedAliases, sideNavItem.getPathAliases());
        if (expectedAliases.isEmpty()) {
            Assert.assertFalse(
                    sideNavItem.getElement().hasProperty("pathAliases"));
        } else {
            String aliasesProperty = sideNavItem.getElement()
                    .getProperty("pathAliases");
            Assert.assertNotNull(aliasesProperty);
            JsonArray actualAliasesArray = JsonUtil.parse(aliasesProperty);
            Set<String> actualAliasesSet = new HashSet<>();
            for (int i = 0; i < actualAliasesArray.length(); i++) {
                actualAliasesSet.add(actualAliasesArray.getString(i));
            }
            Assert.assertEquals(expectedAliases, actualAliasesSet);
        }
    }

    @Route("foo/bar")
    private static class TestRoute extends Component {

    }

    @Route("foo/:k1/:k2/bar")
    private static class TestRouteWithRouteParams extends Component {

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
