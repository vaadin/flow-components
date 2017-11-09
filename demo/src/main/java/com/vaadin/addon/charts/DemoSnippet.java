package com.vaadin.addon.charts;

import com.vaadin.ui.Component;
import com.vaadin.ui.Tag;
import com.vaadin.ui.common.HtmlImport;

@Tag("demo-snippet")
@HtmlImport("frontend://src/demo-snippet.html")
public class DemoSnippet extends Component {

    public void setSource(String source) {
        getElement().setProperty("_markdown", source);
    }
}
