package com.vaadin.flow.component.grid;

import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.selection.SelectionListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Objects;
import java.util.stream.Stream;

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

    public static class TestEntity {
        private final int id;
        private final String name;

        public TestEntity(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
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
