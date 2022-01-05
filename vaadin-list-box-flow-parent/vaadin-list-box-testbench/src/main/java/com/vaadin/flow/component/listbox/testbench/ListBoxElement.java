/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.listbox.testbench;

import com.vaadin.testbench.HasLabel;

import com.vaadin.testbench.HasSelectByText;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A TestBench element representing a <code>&lt;vaadin-list-box&gt;</code>
 * element.
 */
@Element("vaadin-list-box")
public class ListBoxElement extends TestBenchElement
        implements HasLabel, HasSelectByText {

    @Override
    public void selectByText(String text) {
        getItems().filter(e -> Objects.equals(text, e.getText())).findFirst()
                .orElseThrow(NoSuchElementException::new).click();
    }

    @Override
    public String getSelectedText() {
        return getItems().filter(i -> i.getAttribute("selected") != null)
                .findFirst().map(WebElement::getText).orElse(null);
    }

    public List<String> getOptions() {
        return getItems().map(WebElement::getText).collect(Collectors.toList());
    }

    private Stream<WebElement> getItems() {
        return findElements(By.tagName("vaadin-item")).stream();
    }
}
