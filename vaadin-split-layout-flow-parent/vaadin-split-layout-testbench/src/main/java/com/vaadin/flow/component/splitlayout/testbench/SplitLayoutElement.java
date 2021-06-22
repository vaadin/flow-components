package com.vaadin.flow.component.splitlayout.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-split-layout&gt;</code>
 * element.
 */
@Element("vaadin-split-layout")
public class SplitLayoutElement extends TestBenchElement {

    public TestBenchElement getSplitter() {
        return getPropertyElement("$", "splitter");
    }

    public TestBenchElement getPrimaryComponent() {
        return (TestBenchElement) executeScript(
                "return arguments[0].children[0]", this);
    }

    public TestBenchElement getSecondaryComponent() {
        return (TestBenchElement) executeScript(
                "return arguments[0].children[1]", this);
    }
}
