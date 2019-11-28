package com.vaadin.flow.component.textfield.testbench;

/*
 * Copyright 2000-2018 Vaadin Ltd.
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

import com.vaadin.testbench.HasPropertySettersGetters;

/**
 * Implement by elements which support helper text
 */
public interface HasHelperText extends HasPropertySettersGetters {

    /**
     * Gets the helper text for the element.
     *
     * @return the helper text or an empty string if there is no helper text
     */
    default public String getHelperText() {
        String ret = getPropertyString("helperText");
        if (ret == null) {
            return "";
        } else {
            return ret;
        }
    }
}
