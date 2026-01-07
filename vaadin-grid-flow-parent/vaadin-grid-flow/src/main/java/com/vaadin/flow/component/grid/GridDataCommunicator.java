/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.grid;

import com.vaadin.flow.data.provider.ArrayUpdater;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.dom.Element;

/**
 * WARNING: This class is for internal use only.
 */
class GridDataCommunicator<T> extends DataCommunicator<T> {
    public GridDataCommunicator(Element element,
            CompositeDataGenerator<T> dataGenerator,
            ArrayUpdater arrayUpdater) {
        super(dataGenerator, arrayUpdater, data -> element
                .callJsFunction("$connector.updateFlatData", data),
                element.getNode());
    }

    @Override
    public void refreshViewport() {
        super.refreshViewport();
    }
}
