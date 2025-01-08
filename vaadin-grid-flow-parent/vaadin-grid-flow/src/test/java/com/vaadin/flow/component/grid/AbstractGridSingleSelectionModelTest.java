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
package com.vaadin.flow.component.grid;

import java.util.Objects;
import java.util.Set;
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

    private final TestEntity entity1 = new TestEntity(1, "Name");
    private final TestEntity entity2 = new TestEntity(2, "Name");
    private final TestEntity entity3 = new TestEntity(3, "Name");

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        grid = new Grid<>();
        dataProviderWithIdentityProvider = new CallbackDataProvider<>(
                query -> Stream.of(entity1, entity2, entity3), query -> 3,
                TestEntity::getId);
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

    @Test
    public void selectFromClient_withItemSelectableProvider_preventsSelection() {
        grid.setItems(dataProviderWithIdentityProvider);
        grid.setItemSelectableProvider(item -> item.getId() != entity1.id);

        GridSelectionModel<TestEntity> selectionModel = grid
                .getSelectionModel();

        // prevent client selection of non-selectable item
        selectionModel.selectFromClient(entity1);
        Assert.assertEquals(Set.of(), selectionModel.getSelectedItems());

        // allow client selection of selectable item
        selectionModel.selectFromClient(entity2);
        Assert.assertEquals(Set.of(entity2), selectionModel.getSelectedItems());
    }

    @Test
    public void deselectFromClient_withItemSelectableProvider_preventsDeselection() {
        grid.setItems(dataProviderWithIdentityProvider);
        grid.setItemSelectableProvider(item -> item.getId() != entity1.id);

        GridSelectionModel<TestEntity> selectionModel = grid
                .getSelectionModel();

        // prevent client deselection of non-selectable item
        selectionModel.select(entity1);
        selectionModel.deselectFromClient(entity1);
        Assert.assertEquals(Set.of(entity1), selectionModel.getSelectedItems());

        // allow client deselection of selectable item
        selectionModel.select(entity2);
        selectionModel.deselectFromClient(entity2);
        Assert.assertEquals(Set.of(), selectionModel.getSelectedItems());
    }

    @Test
    public void select_withItemSelectableProvider_allowsSelection() {
        grid.setItems(dataProviderWithIdentityProvider);
        grid.setItemSelectableProvider(item -> item.getId() != entity1.id);

        GridSelectionModel<TestEntity> selectionModel = grid
                .getSelectionModel();

        // allow programmatic selection of any item
        selectionModel.select(entity1);
        Assert.assertEquals(Set.of(entity1), selectionModel.getSelectedItems());

        selectionModel.select(entity2);
        Assert.assertEquals(Set.of(entity2), selectionModel.getSelectedItems());
    }

    @Test
    public void deselect_withItemSelectableProvider_allowsDeselection() {
        grid.setItems(dataProviderWithIdentityProvider);
        grid.setItemSelectableProvider(item -> item.getId() != entity1.id);

        GridSelectionModel<TestEntity> selectionModel = grid
                .getSelectionModel();

        // allow programmatic deselection of any item
        selectionModel.select(entity1);
        selectionModel.deselect(entity1);
        Assert.assertEquals(Set.of(), selectionModel.getSelectedItems());

        selectionModel.select(entity2);
        selectionModel.select(entity2);
        Assert.assertEquals(Set.of(entity2), selectionModel.getSelectedItems());
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
