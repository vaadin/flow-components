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
