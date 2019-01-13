package com.vaadin.flow.component.customfield;

/*
 * #%L
 * Vaadin CustomField for Vaadin 10
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
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.H1;

@Tag("div")
//@Tag("vaadin-custom-field")
//@HtmlImport("frontend://bower_components/vaadin-custom-field/src/vaadin-custom-field.html")
public class CustomField extends Component {

    /**
     * Initializes a new CustomField.
     */
    public CustomField() {
        getElement().appendChild(new H1("Hello World!").getElement());
    }
}
