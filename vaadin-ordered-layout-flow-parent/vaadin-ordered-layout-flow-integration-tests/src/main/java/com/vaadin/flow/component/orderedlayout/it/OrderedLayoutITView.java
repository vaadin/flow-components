/*
 * Copyright 2000-2025 Vaadin Ltd.
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
