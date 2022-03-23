package com.vaadin.flow.component.richtexteditor.testbench;

/*
 * #%L
 * Vaadin Rich Text Editor Testbench API
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
        List<TestBenchElement> buttons = toolbar.$("button").all();
        return buttons.stream().map(btn -> btn.getAttribute("title"))
                .collect(Collectors.toList());
    }

}
