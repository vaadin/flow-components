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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;
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
        sideNavItem = new SideNavItem("Item", "/path");
    }

    @Test
    public void changeLabel_labelChanged() {
        Assert.assertEquals("Item", sideNavItem.getLabel());
        sideNavItem.setLabel("Item Changed");
        Assert.assertEquals("Item Changed", sideNavItem.getLabel());
    }

    @Test
    public void setLabelToNull_labelIsNull() {
        sideNavItem.setLabel("Navigation test");
        sideNavItem.setLabel(null);

        Assert.assertNull(sideNavItem.getLabel());
    }

    @Test
    public void setEmptyLabel_labelIsEmpty() {
        sideNavItem.setLabel("");

        Assert.assertEquals("", sideNavItem.getLabel());
    }

    @Test
    public void setLabel_labelElementPresent() {
        sideNavItem.setLabel("Navigation test");

        Assert.assertTrue(sideNavItemHasLabelElement());
    }

    @Test
    public void setLabelAndUnsetLabel_labelElementRemoved() {
        sideNavItem.setLabel("Navigation test");
        sideNavItem.setLabel(null);

        Assert.assertFalse(sideNavItemHasLabelElement());
    }

    private boolean sideNavItemHasLabelElement() {
        return sideNavItem.getElement().getChildren()
                .anyMatch(this::isLabelElement);
    }

    private boolean isLabelElement(Element element) {
        return !element.hasAttribute("slot");
    }

    @Test
    public void createWithNoPath_pathNotSet() {
        final SideNavItem item = new SideNavItem("Test");

        Assert.assertNull(item.getPath());
    }

    @Test
    public void setNullStringPath_pathAttributeRemoved() {
        sideNavItem.setPath((String) null);

        Assert.assertFalse(sideNavItem.getElement().hasAttribute("path"));
        Assert.assertNull(sideNavItem.getPath());
    }

    @Test
    public void setNullComponentPath_pathAttributeRemoved() {
        sideNavItem.setPath((Class<? extends Component>) null);

        Assert.assertFalse(sideNavItem.getElement().hasAttribute("path"));
        Assert.assertNull(sideNavItem.getPath());
    }

    @Test
    public void setEmptyPath_returnsEmptyPath() {
        final SideNavItem item = new SideNavItem("Test");
        item.setPath("");

        Assert.assertEquals("", item.getPath());
        Assert.assertTrue(sideNavItem.getElement().hasAttribute("path"));
    }

    @Test
    public void setPathAsComponent_pathUpdated() {
        runWithMockRouter(TestRoute.class, () -> {
            sideNavItem.setPath(TestRoute.class);
            Assert.assertEquals("foo/bar", sideNavItem.getPath());
        });
    }

    @Test
    public void returnsExpectedPath() {
        Assert.assertEquals("path", sideNavItem.getPath());
    }

    @Test
    public void addSingleItem_itemAdded() {
        // one child for the label element
        Assert.assertEquals(1, sideNavItem.getElement().getChildCount());

        sideNavItem.addItem(new SideNavItem("Test"));

        Assert.assertEquals(2, sideNavItem.getElement().getChildCount());
    }

    @Test
    public void addSingleItem_itemHasCorrectSlot() {
        sideNavItem.addItem(new SideNavItem("Test"));

        Assert.assertEquals("children",
                sideNavItem.getElement().getChild(1).getAttribute("slot"));
    }

    @Test
    public void addTwoItemsAtOnce_itemsAdded() {
        // one child for the label element
        Assert.assertEquals(1, sideNavItem.getElement().getChildCount());

        sideNavItem.addItem(new SideNavItem("Test1"), new SideNavItem("Test2"));

        Assert.assertEquals(3, sideNavItem.getElement().getChildCount());
    }

    @Test
    public void noItems_addItemAsFirst_itemIsAdded() {
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
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(-1, testItem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noItems_addItemAtTooHighIndex_throws() {
        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(1, testItem);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addItemAtTooHighIndex_throws() {
        final List<SideNavItem> items = setupItems();

        final SideNavItem testItem = new SideNavItem("testItem");
        sideNavItem.addItemAtIndex(items.size() + 1, testItem);
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
    public void multipleItems_removeAll_allItemsRemoved() {
        setupItems();

        sideNavItem.removeAll();

        Assert.assertTrue(sideNavItem.getItems().isEmpty());
    }

    @Test
    public void removeAll_labelStillSet() {
        setupItems();

        sideNavItem.removeAll();

        Assert.assertFalse(sideNavItem.getLabel().isEmpty());
    }

    @Test
    public void removeAll_prefixAndSuffixStillSet() {
        setupItemsPrefixAndSuffix();

        sideNavItem.removeAll();

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

    @Test
    public void createWithPathAndPrefix_pathAndPrefixIsSet() {
        final Div prefixComponent = new Div();
        final SideNavItem item = new SideNavItem("Test item", "test-path",
                prefixComponent);

        Assert.assertEquals("test-path", item.getPath());
        Assert.assertEquals(prefixComponent, item.getPrefixComponent());
    }

    @Test
    public void createFromComponent_pathIsSet() {
        runWithMockRouter(TestRoute.class, () -> {
            SideNavItem item = new SideNavItem("test", TestRoute.class);
            Assert.assertEquals("foo/bar", item.getPath());
        });
    }

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

    @Test
    public void setPath_setQueryParameters_pathContainsParameters() {
        sideNavItem.setPath("path");

        QueryParameters queryParameters = new QueryParameters(Map.of("k1",
                List.of("v11", "v12"), "k2", List.of("v21", "v22")));
        sideNavItem.setQueryParameters(queryParameters);

        Assert.assertEquals("path?" + queryParameters.getQueryString(),
                sideNavItem.getPath());
    }

    @Test
    public void setPath_setQueryParameters_updateQueryParameters_pathIsUpdated() {
        sideNavItem.setPath("path");
        sideNavItem.setQueryParameters(
                new QueryParameters(Map.of("k1", List.of("v11", "v12"))));

        QueryParameters queryParameters = new QueryParameters(
                Map.of("k2", List.of("v21", "v22")));
        sideNavItem.setQueryParameters(queryParameters);

        Assert.assertEquals("path?" + queryParameters.getQueryString(),
                sideNavItem.getPath());
    }

    @Test
    public void setPath_setQueryParameters_setQueryParametersNull_parametersRemovedFromPath() {
        sideNavItem.setPath("path");
        sideNavItem.setQueryParameters(
                new QueryParameters(Map.of("k1", List.of("v11", "v12"))));

        sideNavItem.setQueryParameters(null);

        Assert.assertEquals("path", sideNavItem.getPath());
    }

    @Test
    public void createFromComponent_setQueryParameters_pathContainsParameters() {
        runWithMockRouter(TestRoute.class, () -> {
            SideNavItem item = new SideNavItem("test", TestRoute.class);

            QueryParameters queryParameters = new QueryParameters(Map.of("k1",
                    List.of("v11", "v12"), "k2", List.of("v21", "v22")));
            item.setQueryParameters(queryParameters);

            Assert.assertEquals("foo/bar?" + queryParameters.getQueryString(),
                    item.getPath());
        });
    }

    @Test
    public void createFromComponent_setQueryParameters_updateQueryParameters_pathIsUpdated() {
        runWithMockRouter(TestRoute.class, () -> {
            SideNavItem item = new SideNavItem("test", TestRoute.class);
            item.setQueryParameters(
                    new QueryParameters(Map.of("k1", List.of("v11", "v12"))));

            QueryParameters queryParameters = new QueryParameters(
                    Map.of("k2", List.of("v21", "v22")));
            item.setQueryParameters(queryParameters);

            Assert.assertEquals("foo/bar?" + queryParameters.getQueryString(),
                    item.getPath());
        });
    }

    @Test
    public void createFromComponent_setQueryParameters_setQueryParametersNull_parametersRemovedFromPath() {
        runWithMockRouter(TestRoute.class, () -> {
            SideNavItem item = new SideNavItem("test", TestRoute.class);
            item.setQueryParameters(
                    new QueryParameters(Map.of("k1", List.of("v11", "v12"))));

            item.setQueryParameters(null);

            Assert.assertEquals("foo/bar", item.getPath());
        });
    }

    @Test
    public void createFromComponentAndRouteParameters_pathContainsParameters() {
        runWithMockRouter(TestRouteWithRouteParams.class, () -> {
            SideNavItem item = new SideNavItem("test",
                    TestRouteWithRouteParams.class,
                    new RouteParameters(Map.of("k1", "v1", "k2", "v2")));

            Assert.assertEquals("foo/v1/v2/bar", item.getPath());
        });
    }

    @Test
    public void setPathFromComponentAndRouteParameters_pathContainsParameters() {
        runWithMockRouter(TestRouteWithRouteParams.class, () -> {
            SideNavItem item = new SideNavItem("test");
            item.setPath(TestRouteWithRouteParams.class,
                    new RouteParameters(Map.of("k1", "v1", "k2", "v2")));

            Assert.assertEquals("foo/v1/v2/bar", item.getPath());
        });
    }

    private void runWithMockRouter(Class<? extends Component> route,
                                   Runnable test) {
        Router router = mockRouter(route);
        try (MockedStatic<ComponentUtil> mockComponentUtil = Mockito
                .mockStatic(ComponentUtil.class)) {
            mockComponentUtil.when(() -> ComponentUtil.getRouter(Mockito.any()))
                    .thenReturn(router);
            test.run();
        }
    }

    @Test
    public void createWithNoPathAlias_pathAliasesEmpty() {
        final SideNavItem item = new SideNavItem("Test");

        Assert.assertEquals(0, item.getPathAliases().size());
        Assert.assertNull(item.getElement().getProperty("pathAliases"));
    }

    @Test
    public void addEmptyPathAlias_pathAliasAdded() {
        final SideNavItem item = new SideNavItem("Test");
        item.addPathAliases("");

        Assert.assertEquals(Set.of(""), item.getPathAliases());
        Assert.assertEquals("[\"\"]",
                item.getElement().getProperty("pathAliases"));
    }

    @Test
    public void addMultiplePathAliases_pathAliasesAdded() {
        final SideNavItem item = new SideNavItem("Test");
        item.addPathAliases("alias1", "alias2");

        Assert.assertEquals(Set.of("alias1", "alias2"), item.getPathAliases());
        Assert.assertTrue(Set
                .of("[\"alias1\",\"alias2\"]", "[\"alias2\",\"alias1\"]")
                .contains(item.getElement().getProperty("pathAliases")));
    }

    @Test
    public void addPathAlias_removePathAlias_pathAliasesEmpty() {
        final SideNavItem item = new SideNavItem("Test");
        item.addPathAliases("alias");
        item.removePathAliases("alias");

        Assert.assertEquals(0, item.getPathAliases().size());
        Assert.assertNull(item.getElement().getProperty("pathAliases"));
    }

    @Test
    public void addMultiplePathAliases_clearPathAliases_pathAliasesEmpty() {
        final SideNavItem item = new SideNavItem("Test");
        item.addPathAliases("alias1", "alias2");
        item.clearPathAliases();

        Assert.assertEquals(0, item.getPathAliases().size());
        Assert.assertNull(item.getElement().getProperty("pathAliases"));
    }

    @Test
    public void setPathAsComponent_pathAliasesAdded() {
        runWithMockRouter(TestRouteWithAliases.class, () -> {
            sideNavItem.setPath(TestRouteWithAliases.class);

            Assert.assertEquals(Set.of("foo/baz", "foo/qux"),
                    sideNavItem.getPathAliases());
            Assert.assertTrue(Set
                    .of("[\"foo/baz\",\"foo/qux\"]",
                            "[\"foo/qux\",\"foo/baz\"]")
                    .contains(sideNavItem.getElement()
                            .getProperty("pathAliases")));
        });
    }

    @Test
    public void setPathAsComponent_removePathAliasViaComponent_pathAliasesEmpty() {
        runWithMockRouter(TestRouteWithAliases.class, () -> {
            sideNavItem.setPath(TestRouteWithAliases.class);
            sideNavItem.removePathAliases(TestRouteWithAliases.class);

            Assert.assertEquals(0, sideNavItem.getPathAliases().size());
            Assert.assertNull(
                    sideNavItem.getElement().getProperty("pathAliases"));
        });
    }

    @Test
    public void setPathAsComponent_removePathAlias_pathAliasRemoved() {
        runWithMockRouter(TestRouteWithAliases.class, () -> {
            sideNavItem.setPath(TestRouteWithAliases.class);
            sideNavItem.removePathAliases("foo/baz");

            Assert.assertEquals(Set.of("foo/qux"),
                    sideNavItem.getPathAliases());
            Assert.assertEquals("[\"foo/qux\"]",
                    sideNavItem.getElement().getProperty("pathAliases"));
        });
    }

    @Test
    public void itemWithPathAndAliases_setNullAsComponent_pathAndAliasesRemoved() {
        runWithMockRouter(TestRouteWithAliases.class, () -> {
            sideNavItem.setPath(TestRouteWithAliases.class);
            sideNavItem.setPath((Class<Component>) null);

            Assert.assertNull(sideNavItem.getPath());
            Assert.assertEquals(0, sideNavItem.getPathAliases().size());
            Assert.assertNull(
                    sideNavItem.getElement().getProperty("pathAliases"));
        });
    }

    @Test
    public void withPathAlias_setQueryParameters_pathAliasDoesNotContainParameters() {
        sideNavItem.addPathAliases("pathAlias");

        QueryParameters queryParameters = new QueryParameters(Map.of("k1",
                List.of("v11", "v12"), "k2", List.of("v21", "v22")));
        sideNavItem.setQueryParameters(queryParameters);

        Assert.assertEquals(1, sideNavItem.getPathAliases().size());
        // TODO check, fails
        Assert.assertTrue(sideNavItem.getPathAliases().contains("pathAlias"));
        Assert.assertEquals("[\"pathAlias\"]",
                sideNavItem.getElement().getProperty("pathAliases"));
    }

    @Test
    public void setPathAsComponent_setQueryParameters_parameterAppliedToAliasTemplate() {
        runWithMockRouter(TestRouteWithAliases.class, () -> {
            sideNavItem.setPath(TestRouteWithAliases.class);

            QueryParameters queryParameters = new QueryParameters(
                    Map.of("foo", List.of("value")));
            sideNavItem.setQueryParameters(queryParameters);

            // TODO check, fails
            Assert.assertTrue(
                    sideNavItem.getPathAliases().contains("foo/value"));
        });
    }

    private Router mockRouter(Class<? extends Component> navigationTarget) {
        VaadinContext mockContext = Mockito.mock(VaadinContext.class);
        ApplicationRouteRegistry routeRegistry = ApplicationRouteRegistry
                .getInstance(mockContext);
        Router router = new Router(routeRegistry);

        RouteConfiguration routeConfiguration = RouteConfiguration
                .forRegistry(routeRegistry);
        routeConfiguration.setAnnotatedRoute(navigationTarget);
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

    @Route("foo/bar")
    private static class TestRoute extends Component {

    }

    @Route("foo/:k1/:k2/bar")
    private static class TestRouteWithRouteParams extends Component {

    }

    @Route("foo/bar")
    @RouteAlias("foo/baz")
    @RouteAlias("foo/qux")
    @RouteAlias("foo/:foo")
    private static class TestRouteWithAliases extends Component {

    }
}

