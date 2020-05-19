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
package com.vaadin.flow.component.checkbox.dataview;

import com.vaadin.flow.data.provider.DataController;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SizeChangeListener;
import com.vaadin.flow.shared.Registration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public class CheckboxGroupListDataViewTest {

    private final static Collection<String> ITEMS = Arrays.asList(
            "first", "middle", "last");

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private DataProvider DATA_PROVIDER;

    private DataController<String> dataController;

    private CheckboxGroupListDataView<String> dataView;

    @Before
    public void init() {
        DATA_PROVIDER = DataProvider.ofCollection(ITEMS);
        dataController = new DataControllerStub();
        dataView = new CheckboxGroupListDataView<>(dataController);
    }

    @Test
    public void getItemOnIndex_correctIndex_itemFound() {
        Assert.assertEquals("Wrong item returned for index","first",
                dataView.getItemOnIndex(0));
    }

    @Test
    public void getItemOnIndex_negativeIndex_throwsException() {
        exceptionRule.expect(IndexOutOfBoundsException.class);
        exceptionRule.expectMessage("Expected zero or greater index, but was given: -1");
        dataView.getItemOnIndex(-1);
    }

    @Test
    public void getItemOnIndex_emptyDataSet_throwsException() {
        DATA_PROVIDER = DataProvider.ofItems();
        exceptionRule.expect(IndexOutOfBoundsException.class);
        exceptionRule.expectMessage("Item requested on an empty data set");
        dataView.getItemOnIndex(0);
    }

    @Test
    public void getItemOnIndex_indexOutsideOfSize_throwsException() {
        exceptionRule.expect(IndexOutOfBoundsException.class);
        dataView.getItemOnIndex(ITEMS.size());
    }

    private class DataControllerStub implements DataController<String> {

        @Override
        public DataProvider<String, ?> getDataProvider() { return DATA_PROVIDER; }

        @Override
        public Registration addSizeChangeListener(SizeChangeListener listener) { return null; }

        @Override
        public int getDataSize() { return DATA_PROVIDER.size(new Query<>()); }

        @Override
        public Stream<String> getAllItems() { return DATA_PROVIDER.fetch(new Query<>()); }
    }
}
