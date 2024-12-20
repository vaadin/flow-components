/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.card;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.dom.Element;

/**
 *
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-card")
// @NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.7.0-alpha2")
// @JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/card", version = "24.7.0-alpha2")
@JsModule("@vaadin/card/src/vaadin-card.js")
public class Card extends Component
        implements HasSize, HasStyle,
        HasThemeVariant<CardVariant> {

    /**
     * Default constructor. Creates an empty card.
     */
    public Card() {
    }

    /**
     * Adds the given components as children of this component.
     *
     * @param components
     *            the components to add
     */
    private void add(Component... components) {
        assert components != null;
        for (Component component : components) {
            assert component != null;
            getElement().appendChild(component.getElement());
        }
    }

    /**
     * Removes the given child components from this component.
     *
     * @param components
     *            The components to remove.
     * @throws IllegalArgumentException
     *             if any of the components is not a child of this component.
     */
    protected void remove(Component... components) {
        for (Component component : components) {
            if (getElement().equals(component.getElement().getParent())) {
                component.getElement().removeAttribute("slot");
                getElement().removeChild(component.getElement());
            } else {
                throw new IllegalArgumentException("The given component ("
                        + component + ") is not a child of this component");
            }
        }
    }

    /**
     * Removes all contents from this component except elements in
     * {@code exclusion} array. This includes child components, text content as
     * well as child elements that have been added directly to this component
     * using the {@link Element} API.
     *
     * @see Card#removeAll()
     */
    private void removeAll(Element... exclusion) {
        Set<Element> toExclude = Stream.of(exclusion)
                .collect(Collectors.toSet());
        Predicate<Element> filter = toExclude::contains;
        getElement().getChildren().filter(filter.negate())
                .forEach(child -> child.removeAttribute("slot"));
        getElement().removeAllChildren();
        getElement().appendChild(exclusion);
    }
}
