package com.vaadin.flow.component.applayout;

/*
 * #%L
 * Vaadin App Layout
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.dom.Element;

import java.util.Objects;

/**
 * Server-side component for the {@code <vaadin-app-layout>} element.
 * Provides a quick and easy way to get a common application layout.
 */
@Tag("vaadin-app-layout")
@HtmlImport("frontend://bower_components/vaadin-app-layout/src/vaadin-app-layout.html")
public class AppLayout extends Component {

    private Element branding;
    private Element content;
    private Element menu;

    /**
     * Sets the component into branding area
     *
     * @param branding {@link Component} to set into branding area
     */
    public void setBranding(Component branding) {
        setBranding(toElement(branding));
    }

    /**
     * Sets the element into branding area
     *
     * @param branding {@link Element} to set into branding area
     */
    public void setBranding(Element branding) {
        Objects.requireNonNull(branding, "Branding cannot be null");

        removeBranding();

        this.branding = branding;
        branding.setAttribute("slot", "branding");

        getElement().appendChild(branding);
    }

    /**
     * Clears the branding area
     */
    public void removeBranding() {
        remove(this.branding);
        this.branding = null;
    }

    /**
     * Returns the {@link Element}
     */
    public Element getContent() {
        return content;
    }

    /**
     * Sets the displayed content.
     *
     * @param content {@link Component} to display in the content area
     */
    public void setContent(Component content) {
        setContent(toElement(content));
    }

    /**
     * Sets the displayed content.
     *
     * @param content {@link Element} to display in the content area
     */
    public void setContent(Element content) {
        Objects.requireNonNull(content, "Content cannot be null");

        removeContent();

        this.content = content;
        getElement().appendChild(content);
    }

    /**
     * Removes the displayed content.
     */
    public void removeContent() {
        remove(this.content);
        this.content = null;
    }

    /**
     * @return {@link Element} displayed at the content area.
     */
    public Element getMenu() {
        return menu;
    }

    /**
     * Sets the component to be placed in the menu slot.
     *
     * @param menu {@link HasElement} to placed in the menu slot.
     */
    public void setMenu(HasElement menu) {
        setMenu(toElement(menu));
    }

    /**
     * Sets the element to be placed in the menu slot.
     *
     * @param menu {@link Element} to placed in the menu slot.
     */
    public void setMenu(Element menu) {
        Objects.requireNonNull(menu, "Menu cannot be null");

        removeMenu();
        this.menu = menu;
        menu.setAttribute("slot", "menu");
        getElement().appendChild(menu);
    }

    /**
     * Creates a new empty AppLayoutMenu and sets it as the menu for this AppLayout instance.
     *
     * @return {@link AppLayoutMenu} created.
     */
    public AppLayoutMenu createMenu() {
        final AppLayoutMenu menu = new AppLayoutMenu();
        setMenu(menu);
        return menu;
    }

    /**
     * Remove the menu.
     */
    public void removeMenu() {
        remove(this.menu);
        this.menu = null;
    }

    private void remove(Element element) {
        if (element != null) {
            element.removeFromParent();
        }
    }

    private static Element toElement(HasElement hasElement) {
        return hasElement != null ? hasElement.getElement() : null;
    }
}
