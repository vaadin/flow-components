/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

import java.util.List;
import java.util.stream.Collectors;

@Element("vaadin-rich-text-editor")
public class RichTextEditorElement extends TestBenchElement {

    public TestBenchElement getEditor() {
        return $("div").attributeContains("class", "ql-editor").first();
    }

    public List getTitles() {
        TestBenchElement toolbar = $("div").attributeContains("part", "toolbar")
                .first();
        List<TestBenchElement> buttonTooltips = toolbar
                .$("button + vaadin-tooltip").all();
        return buttonTooltips.stream()
                .map(tooltip -> tooltip.getProperty("text"))
                .collect(Collectors.toList());
    }

}
