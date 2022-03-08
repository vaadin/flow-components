package com.vaadin.flow.component.orderedlayout.it;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-ordered-layout/ordered-layout-tests")
public class OrderedLayoutITView extends Div {

    public OrderedLayoutITView() {
        VerticalLayout verticalLayoutWithSpacing = new VerticalLayout();
        verticalLayoutWithSpacing.setId("vl-spacing");
        verticalLayoutWithSpacing.add(
                AbstractLayout.createToggleThemeCheckbox(
                        verticalLayoutWithSpacing, "spacing-xs"),
                AbstractLayout.createToggleThemeCheckbox(
                        verticalLayoutWithSpacing, "spacing-s"),
                AbstractLayout.createToggleThemeCheckbox("spacing",
                        verticalLayoutWithSpacing::setSpacing, true),
                AbstractLayout.createToggleThemeCheckbox(
                        verticalLayoutWithSpacing, "spacing-l"),
                AbstractLayout.createToggleThemeCheckbox(
                        verticalLayoutWithSpacing, "spacing-xl"));

        HorizontalLayout horizontalLayoutWithSpacing = new HorizontalLayout();
        horizontalLayoutWithSpacing.setId("hl-spacing");
        horizontalLayoutWithSpacing.add(
                AbstractLayout.createToggleThemeCheckbox(
                        horizontalLayoutWithSpacing, "spacing-xs"),
                AbstractLayout.createToggleThemeCheckbox(
                        horizontalLayoutWithSpacing, "spacing-s"),
                AbstractLayout.createToggleThemeCheckbox("spacing",
                        horizontalLayoutWithSpacing::setSpacing, true),
                AbstractLayout.createToggleThemeCheckbox(
                        horizontalLayoutWithSpacing, "spacing-l"),
                AbstractLayout.createToggleThemeCheckbox(
                        horizontalLayoutWithSpacing, "spacing-xl"));

        FlexLayout flayout = new FlexLayout();
        flayout.setId("flex-layout");
        NativeButton noWrap = new NativeButton("no-wrap",
                e -> flayout.setFlexWrap(FlexLayout.FlexWrap.NOWRAP));
        NativeButton wrap = new NativeButton("wrap",
                e -> flayout.setFlexWrap(FlexLayout.FlexWrap.WRAP));
        NativeButton wrapReverse = new NativeButton("wrap-reverse",
                e -> flayout.setFlexWrap(FlexLayout.FlexWrap.WRAP_REVERSE));
        NativeButton flexWrapDisplay = new NativeButton("no-flex-wrap");
        flexWrapDisplay.setId("flex-wrap-display");
        NativeButton getFlexWrap = new NativeButton("Get flex-wrap button",
                e -> flexWrapDisplay
                        .setText(String.valueOf(flayout.getFlexWrap())));
        noWrap.setId("no-wrap");
        wrap.setId("wrap");
        wrapReverse.setId("wrap-reverse");
        getFlexWrap.setId("wrap-btn");
        flayout.add(noWrap, wrap, wrapReverse, getFlexWrap, flexWrapDisplay);

        add(verticalLayoutWithSpacing, horizontalLayoutWithSpacing, flayout);
    }
}
