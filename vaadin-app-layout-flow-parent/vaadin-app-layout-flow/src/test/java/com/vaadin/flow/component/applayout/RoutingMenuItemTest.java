package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.UI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UI.class)
public class RoutingMenuItemTest {

    private RoutingMenuItem sut;

    @Before
    public void setUp() {
        sut = new RoutingMenuItem("Home", "");
    }

    @Test
    public void setRoute() {
        // Isolate Flow's UI class for testing.
        PowerMockito.mockStatic(UI.class);
        UI ui = Mockito.mock(UI.class);
        BDDMockito.given(UI.getCurrent()).willReturn(ui);

        click(sut);

        // Verify that clicking causes a UI navigation to home.
        Mockito.verify(ui, times(1)).navigate("");

        sut.setRoute("Admin");
        click(sut);

        // Verify that clicking causes a UI navigation to the updated route.
        Mockito.verify(ui, times(1)).navigate("Admin");
    }

    @Test(expected = NullPointerException.class)
    public void setRoute_null() {
        sut.setRoute(null);
    }

    private void click(MenuItem menuItem) {
        menuItem.getListener().onComponentEvent(new MenuItemClickEvent(menuItem, false));
    }
}
