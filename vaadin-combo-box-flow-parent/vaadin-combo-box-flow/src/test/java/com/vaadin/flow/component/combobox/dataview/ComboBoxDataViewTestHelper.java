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

package com.vaadin.flow.component.combobox.dataview;

import java.lang.reflect.Method;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;

final class ComboBoxDataViewTestHelper {

    private ComboBoxDataViewTestHelper() {
    }

    static void setClientSideFilter(ComboBox<String> comboBox,
            String clientFilter) {
        try {
            // Reset the client filter on server side as though it's sent from
            // client
            Method setRequestedRangeMethod = ComboBox.class.getDeclaredMethod(
                    "setRequestedRange", int.class, int.class, String.class);
            setRequestedRangeMethod.setAccessible(true);
            setRequestedRangeMethod.invoke(comboBox, 0, comboBox.getPageSize(),
                    clientFilter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void fakeClientCommunication(UI ui) {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
