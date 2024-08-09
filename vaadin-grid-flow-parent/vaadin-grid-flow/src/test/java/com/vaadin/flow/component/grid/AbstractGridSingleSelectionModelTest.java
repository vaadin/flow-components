/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import java.util.Objects;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.selection.SelectionListener;

public class AbstractGridSingleSelectionModelTest {
    private Grid<TestEntity> grid;
    private CallbackDataProvider<TestEntity, Void> dataProviderWithIdentityProvider;
    private SelectionListener<Grid<TestEntity>, TestEntity> selectionListenerMock;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        grid = new Grid<>();
        dataProviderWithIdentityProvider = new CallbackDataProvider<>(
                query -> Stream.of(new TestEntity(1, "Name"),
                        new TestEntity(2, "Name"), new TestEntity(3, "Name")),
                query -> 3, TestEntity::getId);
        selectionListenerMock = Mockito.mock(SelectionListener.class);
        grid.getSelectionModel().addSelectionListener(selectionListenerMock);
    }

    @Test
    public void select_usesDataProviderIdentify() {
        grid.setItems(dataProviderWithIdentityProvider);
        GridSelectionModel<TestEntity> selectionModel = grid
                .getSelectionModel();
        // Select initial item
        selectionModel.select(new TestEntity(1, "joseph"));
        // Select item with different equals value, but same identity in data
        // provider
        selectionModel.select(new TestEntity(1, "Joseph"));

        // Second select should not result in a selection change
        Mockito.verify(selectionListenerMock, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @Test
    public void select_setNullClearsSelection() {
        grid.setItems(dataProviderWithIdentityProvider);
        GridSelectionModel<TestEntity> selectionModel = grid
                .getSelectionModel();
        // Select initial item
        selectionModel.select(new TestEntity(1, "joseph"));
        // Select null
        selectionModel.select(null);

        // Second select should result in a selection change
        Mockito.verify(selectionListenerMock, Mockito.times(2))
                .selectionChange(Mockito.any());
        Assert.assertEquals(0, selectionModel.getSelectedItems().size());
    }

    @Test
    public void selectFromClient_usesDataProviderIdentify() {
        grid.setItems(dataProviderWithIdentityProvider);
        GridSelectionModel<TestEntity> selectionModel = grid
                .getSelectionModel();
        // Select initial item
        selectionModel.selectFromClient(new TestEntity(1, "joseph"));
        // Select item with different equals value, but same identity in data
        // provider
        selectionModel.selectFromClient(new TestEntity(1, "Joseph"));

        // Second select should not result in a selection change
        Mockito.verify(selectionListenerMock, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @Test
    public void selectFromClient_setNullClearsSelection() {
        grid.setItems(dataProviderWithIdentityProvider);
        GridSelectionModel<TestEntity> selectionModel = grid
                .getSelectionModel();
        // Select initial item
        selectionModel.selectFromClient(new TestEntity(1, "joseph"));
        // Select null
        selectionModel.selectFromClient(null);

        // Second select should result in a selection change
        Mockito.verify(selectionListenerMock, Mockito.times(2))
                .selectionChange(Mockito.any());
        Assert.assertEquals(0, selectionModel.getSelectedItems().size());
    }

    @Test
    public void isSelected_usesDataProviderIdentify() {
        grid.setItems(dataProviderWithIdentityProvider);
        GridSelectionModel<TestEntity> selectionModel = grid
                .getSelectionModel();
        // Select item
        selectionModel.select(new TestEntity(1, "joseph"));
        // Item with same ID, but different hash code should be reported as
        // selected
        Assert.assertTrue(
                selectionModel.isSelected(new TestEntity(1, "Joseph")));
    }

    public static class TestEntity {
        private int id;
        private String name;

        public TestEntity(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        // equals and hashCode are intentionally implemented differently from
        // the identifier getter for the data provider. We want to make sure
        // that the selection model uses the data provider identity, rather than
        // the equals implementation
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            TestEntity that = (TestEntity) o;
            return id == that.id && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
    }
}
