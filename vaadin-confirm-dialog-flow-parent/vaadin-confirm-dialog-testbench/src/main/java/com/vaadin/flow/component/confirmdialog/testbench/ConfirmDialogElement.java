/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.confirmdialog.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-confirm-dialog")
public class ConfirmDialogElement extends TestBenchElement {
    public TestBenchElement getConfirmButton() {
        return getPropertyElement("_confirmButton");
    }

    public TestBenchElement getRejectButton() {
        return getPropertyElement("_rejectButton");
    }

    public TestBenchElement getCancelButton() {
        return getPropertyElement("_cancelButton");
    }

    public String getMessageText() {
        return getPropertyString("message");
    }

    public String getHeaderText() {
        return getPropertyString("header");
    }
}
