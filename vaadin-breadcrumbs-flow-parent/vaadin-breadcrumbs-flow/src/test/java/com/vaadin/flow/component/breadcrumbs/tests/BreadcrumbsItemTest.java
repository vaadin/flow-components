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
package com.vaadin.flow.component.breadcrumbs.tests;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.breadcrumbs.BreadcrumbsItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;

class BreadcrumbsItemTest {

    // CONSTRUCTOR TESTS

    @Test
    void allConstructorsArePresent() {
        new BreadcrumbsItem("Text");
        new BreadcrumbsItem("Text", "/path");
        new BreadcrumbsItem("Text", "/path", new Div());
        runWithMockRouter(() -> {
            new BreadcrumbsItem("Text", TestRoute.class);
            new BreadcrumbsItem("Text", TestRoute.class,
                    RouteParameters.empty());
            new BreadcrumbsItem("Text", TestRoute.class, new Div());
            new BreadcrumbsItem("Text", TestRoute.class,
                    RouteParameters.empty(), new Div());
        }, TestRoute.class);
    }

    @Test
    void createWithText_textSetAndNoPath() {
        var item = new BreadcrumbsItem("Home");

        Assertions.assertEquals("Home", item.getText());
        Assertions.assertNull(item.getPath());
        Assertions.assertFalse(item.getElement().hasAttribute("path"));
    }

    @Test
    void createWithPath_pathAttributeSet() {
        var item = new BreadcrumbsItem("Docs", "/docs");

        Assertions.assertEquals("/docs", item.getPath());
        Assertions.assertEquals("/docs",
                item.getElement().getAttribute("path"));
    }

    @Test
    void createWithViewAndPrefix_pathAndPrefixSet() {
        runWithMockRouter(() -> {
            var homeIcon = new Div();
            var item = new BreadcrumbsItem("Home", TestRoute.class, homeIcon);

            Assertions.assertEquals("foo/bar", item.getPath());
            Assertions.assertEquals(homeIcon, item.getPrefixComponent());
        }, TestRoute.class);
    }

    // PATH TESTS

    @Test
    void setStringPath_pathAttributeSet() {
        var item = new BreadcrumbsItem("Docs");
        item.setPath("/docs");

        Assertions.assertEquals("/docs", item.getPath());
        Assertions.assertEquals("/docs",
                item.getElement().getAttribute("path"));
    }

    @Test
    void setNullStringPath_pathAttributeRemoved() {
        var item = new BreadcrumbsItem("Docs", "/docs");
        item.setPath((String) null);

        Assertions.assertNull(item.getPath());
        Assertions.assertFalse(item.getElement().hasAttribute("path"));
    }

    @Test
    void setViewPath_pathAttributeSetToViewUrl() {
        runWithMockRouter(() -> {
            var item = new BreadcrumbsItem("Foo");
            item.setPath(TestRoute.class);

            Assertions.assertEquals("foo/bar", item.getPath());
        }, TestRoute.class);
    }

    @Test
    void setViewPathWithRouteParameters_pathAttributeSetToParameterisedUrl() {
        runWithMockRouter(() -> {
            var item = new BreadcrumbsItem("Foo");
            item.setPath(TestRouteWithRouteParams.class,
                    new RouteParameters(Map.of("k1", "v1", "k2", "v2")));

            Assertions.assertEquals("foo/v1/v2/bar", item.getPath());
        }, TestRouteWithRouteParams.class);
    }

    @Test
    void setNullViewPath_pathAttributeRemoved() {
        var item = new BreadcrumbsItem("Docs", "/docs");
        item.setPath((Class<? extends Component>) null);

        Assertions.assertNull(item.getPath());
        Assertions.assertFalse(item.getElement().hasAttribute("path"));
    }

    // PREFIX TESTS

    @Test
    void setPrefixComponent_getPrefixComponent() {
        var item = new BreadcrumbsItem("Home");
        var prefix = new Div();
        item.setPrefixComponent(prefix);

        Assertions.assertEquals(prefix, item.getPrefixComponent());
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

    @Route("foo/bar")
    private static class TestRoute extends Component {
    }

    @Route("foo/:k1/:k2/bar")
    private static class TestRouteWithRouteParams extends Component {
    }
}
