/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.icon.demo;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.FontIcon;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd
 */
@Route("vaadin-icons/font-icons")
@NpmPackage(value = "@fortawesome/fontawesome-free", version = "6.4.2")
@CssImport("@fortawesome/fontawesome-free/css/all.min.css")
public class FontIconPage extends Div {

    public FontIconPage() {
        add(FontAwesomeIcons.USER.create());
    }

    enum FontAwesomeIcons {
        CAMERA("fa-camera"), USER("fa-user");

        private String iconClassName;

        FontAwesomeIcons(String iconClassName) {
            this.iconClassName = iconClassName;
        }

        public FontIcon create() {
            return new FontIcon("fa-solid", iconClassName);
        }
    }

}
