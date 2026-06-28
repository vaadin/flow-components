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

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.breadcrumbs.Breadcrumbs;
import com.vaadin.flow.component.breadcrumbs.Breadcrumbs.Mode;
import com.vaadin.flow.component.breadcrumbs.BreadcrumbsFeatureFlagProvider;
import com.vaadin.flow.component.breadcrumbs.BreadcrumbsItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.DynamicPageTitle;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.LocationChangeEvent;
import com.vaadin.flow.router.NavigationTrigger;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PageTitleContext;
import com.vaadin.flow.router.PageTitleGenerator;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouteParent;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.router.internal.AfterNavigationHandler;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

class BreadcrumbsModeTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            BreadcrumbsFeatureFlagProvider.BREADCRUMBS_COMPONENT);

    @AfterEach
    void clearCurrentService() {
        // installRouter sets the current service for title resolution; clear it
        // so it does not leak into other tests.
        VaadinService.setCurrent(null);
    }

    @Test
    void defaultConstructor_modeIsRouter() {
        Assertions.assertEquals(Mode.ROUTER, new Breadcrumbs().getMode());
    }

    @Test
    void modeConstructor_modeIsSet() {
        Assertions.assertEquals(Mode.MANUAL,
                new Breadcrumbs(Mode.MANUAL).getMode());
    }

    @Test
    void manualMode_mutatingMethods_doNotThrow() {
        var breadcrumbs = new Breadcrumbs(Mode.MANUAL);
        var home = new BreadcrumbsItem("Home");
        var page = new BreadcrumbsItem("Page");

        Assertions.assertDoesNotThrow(() -> breadcrumbs.add(home));
        Assertions.assertDoesNotThrow(() -> breadcrumbs.remove(home));
        Assertions.assertDoesNotThrow(breadcrumbs::removeAll);
        Assertions.assertDoesNotThrow(() -> breadcrumbs.add(home));
        Assertions.assertDoesNotThrow(() -> breadcrumbs.replace(home, page));
        Assertions.assertDoesNotThrow(
                () -> breadcrumbs.addComponentAsFirst(home));
        Assertions.assertDoesNotThrow(
                () -> breadcrumbs.addComponentAtIndex(0, page));
    }

    @Test
    void routerMode_mutatingMethods_throw() {
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        var home = new BreadcrumbsItem("Home");
        var page = new BreadcrumbsItem("Page");

        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.add(home));
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.remove(home));
        Assertions.assertThrows(IllegalStateException.class,
                breadcrumbs::removeAll);
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.replace(home, page));
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.addComponentAsFirst(home));
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.addComponentAtIndex(0, page));
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.bindChildren(null, null));
    }

    @Test
    void getChildren_worksInBothModes() {
        var manual = new Breadcrumbs(Mode.MANUAL);
        manual.add(new BreadcrumbsItem("Home"));
        Assertions.assertEquals(1, manual.getChildren().count());

        var router = new Breadcrumbs(Mode.ROUTER);
        Assertions.assertEquals(0, router.getChildren().count());
    }

    @Test
    void manualModeWithChildren_setModeRouter_clearsChildrenAndReappliesGuard() {
        var breadcrumbs = new Breadcrumbs(Mode.MANUAL);
        breadcrumbs.add(new BreadcrumbsItem("Home"),
                new BreadcrumbsItem("Page"));

        // MANUAL -> ROUTER clears manually-added children without throwing,
        // exercising the updateChildrenInternal(List.of()) bypass.
        breadcrumbs.setMode(Mode.ROUTER);
        Assertions.assertEquals(0, breadcrumbs.getChildren().count());

        // The bypass flag is reset afterwards, so a guarded add throws again.
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.add(new BreadcrumbsItem("Other")));
    }

    @Test
    void setModeSameValue_isNoOp_childrenUnchanged() {
        var breadcrumbs = new Breadcrumbs(Mode.MANUAL);
        var item = new BreadcrumbsItem("Home");
        breadcrumbs.add(item);
        breadcrumbs.setMode(Mode.MANUAL);
        Assertions.assertEquals(1, breadcrumbs.getChildren().count());
        Assertions.assertEquals(item,
                breadcrumbs.getChildren().findFirst().orElse(null));
    }

    @Test
    void setMode_withActiveChildrenBinding_throws() {
        var breadcrumbs = new Breadcrumbs(Mode.MANUAL);
        var itemSignal = new ValueSignal<>("Home");
        var listSignal = new ValueSignal<>(List.of(itemSignal));
        breadcrumbs.bindChildren(listSignal,
                signal -> new BreadcrumbsItem(signal.peek()));

        // A binding controls the children and cannot be removed; switching
        // modes would clear them, so setMode must fail.
        Assertions.assertThrows(IllegalStateException.class,
                () -> breadcrumbs.setMode(Mode.ROUTER));
    }

    // ROUTER-MODE LISTENER

    @Test
    void routerMode_attach_registersExactlyOneAfterNavigationListener() {
        installRouter();
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);

        Assertions.assertEquals(0, afterNavigationListeners().size());
        ui.add(breadcrumbs);
        Assertions.assertEquals(1, afterNavigationListeners().size());
    }

    @Test
    void manualMode_attach_doesNotRegisterListener() {
        installRouter();
        var breadcrumbs = new Breadcrumbs(Mode.MANUAL);

        ui.add(breadcrumbs);
        Assertions.assertEquals(0, afterNavigationListeners().size());
    }

    @Test
    void routerMode_detach_unregistersListener() {
        installRouter();
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        ui.add(breadcrumbs);
        Assertions.assertEquals(1, afterNavigationListeners().size());

        ui.remove(breadcrumbs);
        Assertions.assertEquals(0, afterNavigationListeners().size());
    }

    @Test
    void routerMode_afterNavigationEvent_buildsTrailFromRouteHierarchy() {
        installRouter(HomeView.class, CustomersView.class, AcmeView.class);
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        ui.add(breadcrumbs);

        fireAfterNavigation(breadcrumbs,
                List.of(new AcmeView(), new CustomersView(), new HomeView()));

        var items = breadcrumbs.getChildren().map(BreadcrumbsItem.class::cast)
                .toList();
        Assertions.assertEquals(3, items.size());

        var routeConfiguration = routeConfiguration();
        Assertions.assertEquals("Home", items.get(0).getText());
        Assertions.assertEquals(routeConfiguration.getUrl(HomeView.class,
                RouteParameters.empty()), items.get(0).getPath());

        Assertions.assertEquals("Customers", items.get(1).getText());
        Assertions.assertEquals(routeConfiguration.getUrl(CustomersView.class,
                RouteParameters.empty()), items.get(1).getPath());

        // The last (current) item carries no path.
        Assertions.assertEquals("Acme", items.get(2).getText());
        Assertions.assertNull(items.get(2).getPath());
    }

    @Test
    void routerMode_currentViewHasDynamicTitle_usedForLastItem() {
        installRouter(HomeView.class, CustomersView.class,
                DynamicAcmeView.class);
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        ui.add(breadcrumbs);

        fireAfterNavigation(breadcrumbs, List.of(new DynamicAcmeView(),
                new CustomersView(), new HomeView()));

        var items = breadcrumbs.getChildren().map(BreadcrumbsItem.class::cast)
                .toList();
        Assertions.assertEquals(3, items.size());
        // Dynamic title wins over the class @PageTitle ("Acme Static").
        Assertions.assertEquals("Acme Dynamic", items.get(2).getText());
        Assertions.assertNull(items.get(2).getPath());
    }

    @Test
    void routerMode_currentItemTitle_resolvedWithQueryParameters() {
        installRouter(HomeView.class, QueryTitleView.class);
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        ui.add(breadcrumbs);

        fireAfterNavigation(breadcrumbs,
                List.of(new QueryTitleView(), new HomeView()),
                QueryParameters.of("product", "5"));

        var items = breadcrumbs.getChildren().map(BreadcrumbsItem.class::cast)
                .toList();
        Assertions.assertEquals(2, items.size());
        // The current item's instance-free title generator sees the query
        // parameters of the current navigation.
        Assertions.assertEquals("Product 5", items.get(1).getText());
    }

    @Test
    void routerMode_ancestorTitle_resolvedWithoutQueryParameters() {
        installRouter(HomeView.class, QueryTitleView.class,
                PlainLeafView.class);
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        ui.add(breadcrumbs);

        fireAfterNavigation(breadcrumbs, List.of(new PlainLeafView(),
                new QueryTitleView(), new HomeView()),
                QueryParameters.of("product", "5"));

        var items = breadcrumbs.getChildren().map(BreadcrumbsItem.class::cast)
                .toList();
        Assertions.assertEquals(3, items.size());
        // The ancestor's title generator is resolved without query parameters,
        // so it falls back to the no-parameter label.
        Assertions.assertEquals("Product", items.get(1).getText());
        Assertions.assertEquals("Leaf", items.get(2).getText());
    }

    @Test
    void routerMode_lateCallbackAfterDetach_isNoOp() {
        installRouter(HomeView.class, CustomersView.class, AcmeView.class);
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        ui.add(breadcrumbs);

        // Capture the registered listener, then detach.
        var listener = afterNavigationListeners().get(0);
        ui.remove(breadcrumbs);

        // A stray late callback after detach must be a no-op.
        listener.afterNavigation(afterNavigationEvent(
                List.of(new AcmeView(), new CustomersView(), new HomeView())));
        Assertions.assertEquals(0, breadcrumbs.getChildren().count());
    }

    @Test
    void setModeRouter_onAttachedManualInstance_registersListenerAndBuildsTrail() {
        installRouter(HomeView.class, CustomersView.class, AcmeView.class);
        // Navigate so the initial rebuild has a current target to resolve.
        ui.getUI().getInternals().updateRouterState(
                new com.vaadin.flow.router.RouterState(new Location("acme"),
                        RouteParameters.empty(), List.of(new AcmeView(),
                                new CustomersView(), new HomeView()),
                        AcmeView.class));

        var breadcrumbs = new Breadcrumbs(Mode.MANUAL);
        ui.add(breadcrumbs);
        Assertions.assertEquals(0, afterNavigationListeners().size());

        breadcrumbs.setMode(Mode.ROUTER);

        Assertions.assertEquals(1, afterNavigationListeners().size());
        // The initial rebuild built the trail from the current router state.
        Assertions.assertEquals(3, breadcrumbs.getChildren().count());
    }

    @Test
    void setModeManual_onAttachedRouterInstance_unregistersListenerAndClearsTrail() {
        installRouter(HomeView.class, CustomersView.class, AcmeView.class);
        var breadcrumbs = new Breadcrumbs(Mode.ROUTER);
        ui.add(breadcrumbs);
        fireAfterNavigation(breadcrumbs,
                List.of(new AcmeView(), new CustomersView(), new HomeView()));
        Assertions.assertEquals(3, breadcrumbs.getChildren().count());
        Assertions.assertEquals(1, afterNavigationListeners().size());

        breadcrumbs.setMode(Mode.MANUAL);

        Assertions.assertEquals(0, afterNavigationListeners().size());
        Assertions.assertEquals(0, breadcrumbs.getChildren().count());
    }

    // TEST HELPERS

    private List<AfterNavigationHandler> afterNavigationListeners() {
        // UI stores after-navigation listeners under the internal handler type,
        // so the listener must be queried with that exact key.
        return ui.getUI().getInternals()
                .getListeners(AfterNavigationHandler.class);
    }

    private RouteConfiguration routeConfiguration() {
        return RouteConfiguration.forRegistry(
                ui.getUI().getInternals().getRouter().getRegistry());
    }

    @SafeVarargs
    private void installRouter(Class<? extends Component>... routes) {
        VaadinContext mockContext = Mockito.mock(VaadinContext.class);
        ApplicationRouteRegistry routeRegistry = ApplicationRouteRegistry
                .getInstance(mockContext);

        RouteConfiguration routeConfiguration = RouteConfiguration
                .forRegistry(routeRegistry);
        for (Class<? extends Component> route : routes) {
            routeConfiguration.setAnnotatedRoute(route);
        }

        // A spy over a real Router runs the real resolvePageTitle (used to
        // resolve item titles) while getRegistry() is stubbed to return the
        // application registry directly, avoiding Router#getRegistry()'s
        // session-scoped wrapping (which would delegate to a parent registry
        // that the mock service cannot provide). doReturn avoids calling the
        // real getRegistry during stubbing.
        Router router = Mockito.spy(new Router(routeRegistry));
        Mockito.doReturn(routeRegistry).when(router).getRegistry();
        Mockito.when(ui.getService().getRouter()).thenReturn(router);
        // Title resolution reads the current service's instantiator.
        VaadinService.setCurrent(ui.getService());
    }

    private void fireAfterNavigation(Breadcrumbs breadcrumbs,
            List<? extends Component> leafFirstChain) {
        fireAfterNavigation(breadcrumbs, leafFirstChain,
                QueryParameters.empty());
    }

    private void fireAfterNavigation(Breadcrumbs breadcrumbs,
            List<? extends Component> leafFirstChain,
            QueryParameters queryParameters) {
        afterNavigationListeners().forEach(listener -> listener.afterNavigation(
                afterNavigationEvent(leafFirstChain, queryParameters)));
    }

    private AfterNavigationEvent afterNavigationEvent(
            List<? extends Component> leafFirstChain) {
        return afterNavigationEvent(leafFirstChain, QueryParameters.empty());
    }

    private AfterNavigationEvent afterNavigationEvent(
            List<? extends Component> leafFirstChain,
            QueryParameters queryParameters) {
        UI realUI = ui.getUI();
        Router router = realUI.getInternals().getRouter();
        List<HasElement> chain = List.copyOf(leafFirstChain);
        LocationChangeEvent locationChangeEvent = new LocationChangeEvent(
                router, realUI, NavigationTrigger.PROGRAMMATIC,
                new Location("acme", queryParameters), chain);
        return new AfterNavigationEvent(locationChangeEvent,
                RouteParameters.empty());
    }

    // TEST VIEWS — @RouteParent chain Home -> Customers -> Acme

    @Route("home")
    @PageTitle("Home")
    public static class HomeView extends Div {
    }

    @Route("customers")
    @RouteParent(HomeView.class)
    @PageTitle("Customers")
    public static class CustomersView extends Div {
    }

    @Route("acme")
    @RouteParent(CustomersView.class)
    @PageTitle("Acme")
    public static class AcmeView extends Div {
    }

    @Route("acme")
    @RouteParent(CustomersView.class)
    @PageTitle("Acme Static")
    public static class DynamicAcmeView extends Div implements HasDynamicTitle {
        @Override
        public String getPageTitle() {
            return "Acme Dynamic";
        }
    }

    // Resolves its title via an instance-free PageTitleGenerator that reads the
    // "product" query parameter, falling back to "Product" when it is absent.
    @Route("query-title")
    @RouteParent(HomeView.class)
    @DynamicPageTitle(ProductTitleGenerator.class)
    public static class QueryTitleView extends Div {
    }

    @Route("leaf")
    @RouteParent(QueryTitleView.class)
    @PageTitle("Leaf")
    public static class PlainLeafView extends Div {
    }

    public static class ProductTitleGenerator implements PageTitleGenerator {
        @Override
        public String generatePageTitle(PageTitleContext context) {
            List<String> product = context.queryParameters()
                    .getParameters("product");
            return product.isEmpty() ? "Product" : "Product " + product.get(0);
        }
    }
}
