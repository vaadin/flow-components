package com.vaadin.flow.component.applayout;

import org.junit.Before;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;

public abstract class MockTest {

    protected RouteRegistry registry;

    @Before
    public final void setupRoutingMocks() {
        // Isolate Flow's UI and routing mechanism for testing
        Mockito.mockStatic(UI.class);
        Mockito.mockStatic(VaadinService.class);

        UI ui = Mockito.mock(UI.class);
        VaadinService vaadinService = Mockito.mock(VaadinService.class);
        registry = Mockito.mock(RouteRegistry.class);
        Router router = Mockito.spy(new Router(registry));

        BDDMockito.given(UI.getCurrent()).willReturn(ui);
        BDDMockito.given(VaadinService.getCurrent()).willReturn(vaadinService);

        Mockito.when(ui.getRouter()).thenReturn(router);
        Mockito.when(vaadinService.getRouter()).thenReturn(router);

        final Answer<?> routeResolver = invocationOnMock -> {
            Class<?> routeClass = (Class<?>) invocationOnMock.getArguments()[0];
            Route route = routeClass.getDeclaredAnnotation(Route.class);
            return route.value();
        };

        // Let Flow resolve route URLs
        Mockito.doAnswer(routeResolver).when(registry)
                .getTargetUrl(Mockito.any(Class.class));
    }

}
