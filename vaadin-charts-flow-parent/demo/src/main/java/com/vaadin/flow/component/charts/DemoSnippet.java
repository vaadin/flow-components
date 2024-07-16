/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

@Tag("demo-snippet")
@HtmlImport("frontend://src/demo-snippet.html")
public class DemoSnippet extends Component {

    public void setSource(String source) {
        getElement().setProperty("_markdown", source == null ? "No source" : source);
    }
}
