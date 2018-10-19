package com.vaadin.flow.component.applayout;

/*
 * #%L
 * Vaadin App Layout
 * %%
 * Copyright (C) 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouteNotFoundError;

/**
 * Convenience class for using an AppLayout as a parent layout in a Flow application.
 * Basic usage involves extending this class and implementing
 * the {@code configure} method.
 *
 * @see AbstractAppRouterLayout#configure(AppLayout, AppLayoutMenu)
 */
public abstract class AbstractAppRouterLayout implements RouterLayout {

    private AppLayout appLayout = new AppLayout();

    private AppLayoutMenu appLayoutMenu = appLayout.createMenu();

    protected AbstractAppRouterLayout() {
        configure(getAppLayout(), getAppLayoutMenu());
    }

    /**
     * This hook is called when this router layout is being constructed
     * and provides an opportunity to configure the AppLayout in use.
     *
     * @param appLayout {@link AppLayout} parent layout
     * @param appLayoutMenu {@link AppLayoutMenu} to configure.
     */
    protected abstract void configure(AppLayout appLayout,
        AppLayoutMenu appLayoutMenu);

    /**
     * This hook is called before a navigation is being made into a route
     * which has this router layout as its parent layout.
     *
     * @param route route that is being navigated to
     * @param content  {@link HasElement} the content component being added
     */
    protected void beforeNavigate(String route, HasElement content) {
    }

    /**
     * This hook is called after a navigation is made into a route
     * which has this router layout as its parent layout.
     *
     * @param route route navigated to
     * @param content  {@link HasElement} the content component added
     */
    protected void afterNavigate(String route, HasElement content) {
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        Component component = content.getElement().getComponent().get();
        String target = null;
        if (component instanceof RouteNotFoundError) {
            getAppLayoutMenu().selectMenuItem(null);
        } else {
            target = UI.getCurrent().getRouter()
                    .getUrl(component.getClass());

            getAppLayoutMenu().getMenuItemTargetingRoute(target)
                    .ifPresent(getAppLayoutMenu()::selectMenuItem);
        }
        beforeNavigate(target, content);
        getAppLayout().setContent(content.getElement());
        afterNavigate(target, content);
    }

    @Override
    public Element getElement() {
        return getAppLayout().getElement();
    }

    /**
     * @return {@link AppLayout} parent layout
     */
    public AppLayout getAppLayout() {
        return appLayout;
    }

    /**
     *
     * @return {@link AppLayoutMenu} which will be updated on navigation.
     */
    public AppLayoutMenu getAppLayoutMenu() {
        return appLayoutMenu;
    }
}
