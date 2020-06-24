package com.vaadin.flow.component.richtexteditor.testbench;

/*
 * #%L
 * Vaadin Rich Text Editor Testbench API
 * %%
 * Copyright (C) 2018 Vaadin Ltd
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
    TestBenchElement toolbar = $("div").attributeContains("part", "toolbar").first();
    List<TestBenchElement> buttons = toolbar.$("button").all();
    return buttons.stream().map(btn -> btn.getAttribute("title")).collect(Collectors.toList());
  }

}
