package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.startup.RouteRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UI.class)
public class AbstractAppRouterLayoutTest {

    public class TestAppRouterLayout extends AbstractAppRouterLayout {

        @Override
        protected void configure(AppLayout appLayout) {
            events.add("Configured");
        }

        @Override
        protected void onNavigate(String route, HasElement content) {
            events.add("Navigate to " + route);
        }
    }

    @Route("route1")
    public class Route1 extends Div {
    }

    @Route("route2")
    public class Route2 extends Div {
    }

    private List<String> events = new ArrayList<>();

    private AbstractAppRouterLayout sut;

    @Before
    public void setup() {
        sut = new TestAppRouterLayout();
    }

    @Test
    public void init() {
        Assert.assertEquals(1, events.size());

        // Ensure configure() called
        Assert.assertEquals("Configured", events.get(0));
    }

    @Test
    public void showRouterLayoutContent() {
        setupFlowRouting();

        RoutingMenuItem route1MenuItem = new RoutingMenuItem("Route 1", "route1");
        sut.getAppLayout().addMenuItem(route1MenuItem);
        sut.getAppLayout().addMenuItem(new RoutingMenuItem("Dummy", "dummy"));

        Route1 route1 = new Route1();
        sut.showRouterLayoutContent(route1);

        // Ensure onNavigate() called
        Assert.assertEquals("Navigate to route1", events.get(events.size() - 1));

        // Ensure matching menu item is selected if present
        Assert.assertEquals(route1MenuItem, sut.getAppLayout().getSelectedMenuItem());
        Assert.assertEquals(route1.getElement(), sut.getAppLayout().getContent());

        sut.showRouterLayoutContent(new Route2());

        // Ensure selected menu item remains unchanged if route does not match
        Assert.assertEquals(route1MenuItem, sut.getAppLayout().getSelectedMenuItem());
    }

    private void setupFlowRouting() {
        PowerMockito.mockStatic(UI.class);
        UI ui = Mockito.mock(UI.class);

        RouteRegistry registry = Mockito.mock(RouteRegistry.class);
        Router router = Mockito.spy(new Router(registry));

        Mockito.doReturn("route1").when(router).getUrl(Route1.class);
        Mockito.doReturn("route2").when(router).getUrl(Route2.class);

        BDDMockito.given(UI.getCurrent()).willReturn(ui);
        Mockito.when(ui.getRouter()).thenReturn(router);
    }
}