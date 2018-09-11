package com.vaadin.flow.component.applayout;

/*
 * #%L
 * Vaadin App Layout for Vaadin 10
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

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.RouterLayout;

/**
 * Convenience class for using an AppLayout as a parent layout in a Flow application.
 * Basic usage involves extending this class and implementing
 * the {@code configure} method.
 *
 * @see AbstractAppRouterLayout#configure(AppLayout)
 */
public abstract class AbstractAppRouterLayout implements RouterLayout {

    private AppLayout appLayout = new AppLayout();

    protected AbstractAppRouterLayout() {
        configure(getAppLayout());
    }

    /**
     * This hook is called when this router layout is being constructed
     * and provides an opportunity to configure the AppLayout in use.
     *
     * @param appLayout
     */
    protected abstract void configure(AppLayout appLayout);

    /**
     * This hook is called when a navigation is being made into a route
     * which has this router layout as its parent layout.
     *
     * @param route
     * @param content
     */
    protected void onNavigate(String route, HasElement content) {
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        final String target = UI.getCurrent().getRouter().getUrl(
                content.getElement().getComponent().get().getClass());

        onNavigate(target, content);

        getAppLayout().getMenuItemTargetingRoute(target)
                .ifPresent(getAppLayout()::selectMenuItem);
        getAppLayout().setContent(content.getElement());
    }

    @Override
    public Element getElement() {
        return getAppLayout().getElement();
    }

    /**
     * Returns an application layout instance
     */
    public AppLayout getAppLayout() {
        return appLayout;
    }
}
