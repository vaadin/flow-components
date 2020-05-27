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

package com.vaadin.flow.component.select.data;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.ListDataProvider;

public class SelectListDataViewTest {

    private final String[] items = new String[] { "one", "two", "three",
            "four" };

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void dataViewWithItem_rowOutsideSetRequested_exceptionThrown() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage(
                "Given index 7 is outside of the accepted range '0 - 3'");

        Select<String> select = new Select<>();
        SelectListDataView<String> dataView = select.setDataProvider(items);

        dataView.getItemOnIndex(7);
    }

    @Test
    public void dataViewWithItem_negativeRowRequested_exceptionThrown() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage(
                "Given index -7 is outside of the accepted range '0 - 3'");

        Select<String> select = new Select<>();
        SelectListDataView<String> dataView = select.setDataProvider(items);

        dataView.getItemOnIndex(-7);
    }

    @Test
    public void dataViewWithoutItems_exceptionThrown() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage("Requested index 5 on empty data.");

        Select<String> select = new Select<>();
        SelectListDataView<String> dataView = select
                .setDataProvider(new ListDataProvider<>(new ArrayList<>()));

        dataView.getItemOnIndex(5);
    }

    @Test
    public void selectItemOnIndex_correctItemSelected() {
        Select<String> select = new Select<>();
        SelectListDataView<String> dataView = select.setDataProvider(items);

        dataView.selectItem(1);

        Assert.assertEquals("Unexpected item selected", items[1],
                select.getValue());
    }

    @Test
    public void selectNextItem_correctItemSelected() {
        Select<String> select = new Select<>();
        SelectListDataView<String> dataView = select.setDataProvider(items);

        dataView.selectItem(1);
        final Optional<String> nextItem = dataView.selectNextItem();

        Assert.assertTrue("No next item found.", nextItem.isPresent());
        Assert.assertEquals("Unexpected item selected as next", items[2],
                select.getValue());
        Assert.assertEquals("Unexpected item selected as next", items[2],
                nextItem.get());
    }

    @Test
    public void selectPreviousItem_correctItemSelected() {
        Select<String> select = new Select<>();
        SelectListDataView<String> dataView = select.setDataProvider(items);

        dataView.selectItem(1);
        final Optional<String> previousItem = dataView.selectPreviousItem();

        Assert.assertTrue("No previous item found.", previousItem.isPresent());
        Assert.assertEquals("Unexpected item selected as previous", items[0],
                select.getValue());
        Assert.assertEquals("Unexpected item selected as previous", items[0],
                previousItem.get());
    }

}
