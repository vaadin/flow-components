
package com.vaadin.flow.component.grid;

import java.util.*;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.dom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;

public class AbstractGridMultiSelectionModelTest {

    private Set<String> selected;
    private Set<String> deselected;
    private Grid<String> grid;
    private DataCommunicatorTest.MockUI ui;

    @Before
    public void init() {
        selected = new HashSet<>();
        deselected = new HashSet<>();
        grid = new Grid<String>() {
            @Override
            void doClientSideSelection(Set items) {
                selected.addAll(items);
            }

            @Override
            void doClientSideDeselection(Set<String> items) {
                deselected.addAll(items);
            }

            @Override
            boolean isInActiveRange(String item) {
                // Updates are sent only for items loaded by client
                return true;
            }
        };

        ui = new DataCommunicatorTest.MockUI();
        ui.add(grid);
    }

    @Test
    public void select_singleItemSignature_sendToClientSide() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.select("foo");
        Assert.assertEquals(1, selected.size());
        Assert.assertEquals("foo", selected.iterator().next());
    }

    @Test
    public void select_singleItemSignature_selectFormClient_dontSendToClientSide() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.getSelectionModel().selectFromClient("foo");
        Assert.assertEquals(0, selected.size());
    }

    @Test
    public void deselect_singleItemSignature_sendToClientSide() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.select("foo");

        grid.deselect("foo");
        Assert.assertEquals(1, deselected.size());
        Assert.assertEquals("foo", deselected.iterator().next());
    }

    @Test
    public void singleItemSignature_deselectFormClient_dontSendToClientSide() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.select("foo");

        grid.getSelectionModel().deselectFromClient("foo");
        Assert.assertEquals(0, deselected.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void select_usesDataProviderIdentify() {
        Grid<TestEntity> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.MULTI);

        CallbackDataProvider<TestEntity, Void> dataProviderWithIdentityProvider = new CallbackDataProvider<>(
                query -> Stream.of(new TestEntity(1, "Name"),
                        new TestEntity(2, "Name"), new TestEntity(3, "Name")),
                query -> 3, TestEntity::getId);
        grid.setItems(dataProviderWithIdentityProvider);

        SelectionListener<Grid<TestEntity>, TestEntity> selectionListenerMock = Mockito
                .mock(SelectionListener.class);
        GridSelectionModel<TestEntity> selectionModel = grid
                .getSelectionModel();
        selectionModel.addSelectionListener(selectionListenerMock);

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
    @SuppressWarnings("unchecked")
    public void selectFromClient_usesDataProviderIdentify() {
        Grid<TestEntity> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.MULTI);

        CallbackDataProvider<TestEntity, Void> dataProviderWithIdentityProvider = new CallbackDataProvider<>(
                query -> Stream.of(new TestEntity(1, "Name"),
                        new TestEntity(2, "Name"), new TestEntity(3, "Name")),
                query -> 3, TestEntity::getId);
        grid.setItems(dataProviderWithIdentityProvider);

        SelectionListener<Grid<TestEntity>, TestEntity> selectionListenerMock = Mockito
                .mock(SelectionListener.class);
        GridSelectionModel<TestEntity> selectionModel = grid
                .getSelectionModel();
        selectionModel.addSelectionListener(selectionListenerMock);

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
    public void isSelectAllCheckboxVisible_withInMemoryDataProviderAndDefaultVisibilityMode_visible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(true, false, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);
    }

    @Test
    public void isSelectAllCheckboxVisible_withDefinedSizeLazyDataProviderAndDefaultVisibilityMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);
    }

    @Test
    public void isSelectAllCheckboxVisible_withUnknownSizeLazyDataProviderAndDefaultVisibilityMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, true, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);
    }

    @Test
    public void isSelectAllCheckboxVisible_withInMemoryDataProviderAndVisibleMode_visible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(true, false, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
    }

    @Test
    public void isSelectAllCheckboxVisible_withDefinedSizeLazyDataProviderAndVisibleMode_visible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, false, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
    }

    @Test
    public void isSelectAllCheckboxVisible_withUnknownSizeLazyDataProviderAndVisibleMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, true, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
    }

    @Test
    public void isSelectAllCheckboxVisible_withInMemoryDataProviderAndHiddenMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(true, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    public void isSelectAllCheckboxVisible_withDefinedSizeLazyDataProviderAndHiddenMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    public void isSelectAllCheckboxVisible_withUnknownSizeLazyDataProviderAndHiddenMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, true, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    public void selectFromClient_inMemoryDataProviderWithDefaultVisibility_updatesCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                true, false, true, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);
    }

    @Test
    public void selectFromClient_lazyDefinedSizeDataProviderWithDefaultVisibility_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);

    }

    @Test
    public void selectFromClient_lazyUnknownSizeDataProviderWithDefaultVisibility_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, true, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);

    }

    @Test
    public void selectFromClient_inMemoryDataProviderWithVisible_updatesCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                true, false, true, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
    }

    @Test
    public void selectFromClient_lazyDefinedSizeDataProviderWithVisible_updatesCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, false, false, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);

    }

    @Test
    public void selectFromClient_lazyUnknownSizeDataProviderWithVisible_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, true, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);

    }

    @Test
    public void selectFromClient_inMemoryDataProviderWithHidden_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                true, false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    public void selectFromClient_lazyDefinedSizeDataProviderWithHidden_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);

    }

    @Test
    public void selectFromClient_lazyUnknownSizeDataProviderWithHidden_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, true, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    public void select_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // select first
        grid.getSelectionModel().select("foo");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // select second, which equals all selected
        grid.getSelectionModel().select("bar");
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void selectFromClient_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // select first
        grid.getSelectionModel().selectFromClient("foo");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // select second, which equals all selected
        grid.getSelectionModel().selectFromClient("bar");
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void deselect_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // start with all selected
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // deselect first
        grid.getSelectionModel().deselect("foo");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // deselect second, which equals none selected
        grid.getSelectionModel().deselect("bar");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void deselectFromClient_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // start with all selected
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // deselect first
        grid.getSelectionModel().deselectFromClient("foo");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // deselect second, which equals none selected
        grid.getSelectionModel().deselectFromClient("bar");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void selectAll_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void clientSelectAll_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        ((AbstractGridMultiSelectionModel<String>) grid.getSelectionModel())
                .clientSelectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void deselectAll_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // start with all selected
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        grid.getSelectionModel().deselectAll();
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void clientDeselectAll_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // start with all selected
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        ((AbstractGridMultiSelectionModel<String>) grid.getSelectionModel())
                .clientDeselectAll();
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void updateSelection_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // Select all
        grid.asMultiSelect().updateSelection(
                new HashSet<>(Arrays.asList("foo", "bar")), new HashSet<>());
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // Deselect single
        grid.asMultiSelect().updateSelection(new HashSet<>(),
                new HashSet<>(Collections.singletonList("foo")));
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // Deselect all
        grid.asMultiSelect().updateSelection(new HashSet<>(),
                new HashSet<>(Collections.singletonList("bar")));
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    private void verifySelectAllCheckboxVisibilityInMultiSelectMode(
            boolean inMemory, boolean unknownItemCount,
            boolean expectedVisibility,
            GridMultiSelectionModel.SelectAllCheckboxVisibility visibility) {
        customiseMultiSelectGridAndDataProvider(inMemory, unknownItemCount,
                visibility, false);

        boolean selectAllCheckboxVisible = ((GridMultiSelectionModel<String>) grid
                .getSelectionModel()).isSelectAllCheckboxVisible();

        Assert.assertEquals(
                "Unexpected select all checkbox visibility in multi-select mode",
                expectedVisibility, selectAllCheckboxVisible);
    }

    private void verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
            boolean inMemory, boolean unknownItemCount,
            boolean expectedSizeQuery, boolean expectedCheckboxStateUpdate,
            GridMultiSelectionModel.SelectAllCheckboxVisibility visibility) {
        DataProvider<String, ?> dataProvider = customiseMultiSelectGridAndDataProvider(
                inMemory, unknownItemCount, visibility, true);

        fakeClientCommunication();

        Mockito.reset(dataProvider);

        Mockito.when(dataProvider.isInMemory()).thenReturn(inMemory);

        grid.getSelectionModel().selectFromClient("foo");

        Mockito.verify(dataProvider, Mockito.times(expectedSizeQuery ? 1 : 0))
                .size(Mockito.any(Query.class));

        grid.getSelectionModel().selectFromClient("bar");

        Mockito.verify(dataProvider, Mockito.times(expectedSizeQuery ? 2 : 0))
                .size(Mockito.any(Query.class));

        Assert.assertEquals(expectedCheckboxStateUpdate ? "true" : "false",
                getGridSelectionColumn(grid).getElement()
                        .getProperty("selectAll"));
    }

    private DataProvider<String, ?> customiseMultiSelectGridAndDataProvider(
            boolean inMemory, boolean unknownItemCount,
            GridMultiSelectionModel.SelectAllCheckboxVisibility visibility,
            boolean mockDataProvider) {
        grid.setSelectionMode(SelectionMode.MULTI);

        GridMultiSelectionModel<String> selectionModel = (GridMultiSelectionModel<String>) grid
                .getSelectionModel();
        selectionModel.setSelectAllCheckboxVisibility(visibility);

        DataProvider<String, ?> dataProvider;

        if (inMemory) {
            dataProvider = getInMemoryDataProvider();
        } else if (unknownItemCount) {
            dataProvider = getUnknownItemCountLazyDataProvider();
        } else {
            dataProvider = getDefinedSizeLazyDataProvider();
        }

        if (mockDataProvider) {
            dataProvider = Mockito.spy(dataProvider);
        }

        grid.setDataProvider(dataProvider);
        grid.getDataCommunicator().setDefinedSize(!unknownItemCount);
        return dataProvider;
    }

    private DataProvider<String, Void> getDefinedSizeLazyDataProvider() {
        return DataProvider.fromCallbacks(
                query -> Stream.of("foo", "bar").skip(query.getOffset())
                        .limit(query.getLimit()),
                query -> (int) Stream.of("foo", "bar").skip(query.getOffset())
                        .limit(query.getLimit()).count());
    }

    private DataProvider<String, Void> getUnknownItemCountLazyDataProvider() {
        return DataProvider.fromCallbacks(query -> Stream.of("foo", "bar")
                .skip(query.getOffset()).limit(query.getLimit()), query -> {
                    Assert.fail("Unexpected size query call");
                    return 0;
                });
    }

    private DataProvider<String, ?> getInMemoryDataProvider() {
        return DataProvider.ofItems("foo", "bar");
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    private <T> GridSelectionColumn getGridSelectionColumn(Grid<T> grid) {
        Component child = grid.getChildren().findFirst().orElseThrow(
                () -> new IllegalStateException("Grid does not have a child"));
        if (!(child instanceof GridSelectionColumn)) {
            throw new IllegalStateException(
                    "First Grid child is not a GridSelectionColumn");
        }
        return (GridSelectionColumn) child;
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
