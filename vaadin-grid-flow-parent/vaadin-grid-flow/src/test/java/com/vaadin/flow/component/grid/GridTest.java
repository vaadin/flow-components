/*
 * Copyright 2000-2020 Vaadin Ltd.
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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.data.provider.DataProvider;

public class GridTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void dataViewForFaultyDataProvider_throwsException() {
        exceptionRule.expect(IllegalStateException.class);
                exceptionRule.expectMessage(
                        "Required ListDataProvider, but got 'AbstractBackEndDataProvider'. "
                                + "Use 'getDataView()' to get a generic DataView instance.");

        Grid<String> grid = new Grid<>();
        final GridListDataView<String> listDataView = grid
                .setDataSource(Arrays.asList("one", "two"));

        DataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> Arrays.asList("one").stream(),
                        query -> 1);

        grid.setDataProvider(dataProvider);

        grid.getListDataView();
    }

}
