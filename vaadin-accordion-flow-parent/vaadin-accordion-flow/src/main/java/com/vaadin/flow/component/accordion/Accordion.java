package com.vaadin.flow.component.accordion;

/*
 * #%L
 * Vaadin Accordion for Vaadin 10
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
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
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.H1;

@Tag("div")
//@Tag("vaadin-accordion")
//@HtmlImport("frontend://bower_components/vaadin-accordion/src/vaadin-accordion.html")
public class Accordion extends Component {

    /**
     * Initializes a new Accordion.
     */
    public Accordion() {
        getElement().appendChild(new H1("Hello World!").getElement());
    }
}
