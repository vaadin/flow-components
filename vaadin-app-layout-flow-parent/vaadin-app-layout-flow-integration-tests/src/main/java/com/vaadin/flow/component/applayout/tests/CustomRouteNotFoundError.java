package com.vaadin.flow.component.applayout.tests;

import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RouteNotFoundError;

@ParentLayout(AppRouterLayout.class)
public class CustomRouteNotFoundError extends RouteNotFoundError {
}
