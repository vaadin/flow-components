package com.vaadin.flow.component.grid;

import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Test for Grid constructors with DataProvider and Collection as parameter
 *
 * @author Vaadin Ltd.
 */
public class GridDataProviderConstructorTest {

    transient List<String> dummyData = Arrays.asList("item1","item2","item3","item4");

    @Test
    public void constructorBackEndDataProvider() {
        Grid<String> gridBackendDataProvider = new Grid<>(
                new StringBackendDataProvider());
        Assert.assertEquals(dummyData.get(0),
                gridBackendDataProvider.getLazyDataView().getItem(0));
        Assert.assertEquals(dummyData.get(1),
                gridBackendDataProvider.getLazyDataView().getItem(1));
        Assert.assertEquals(dummyData.get(2),
                gridBackendDataProvider.getLazyDataView().getItem(2));
        Assert.assertEquals(dummyData.get(3),
                gridBackendDataProvider.getLazyDataView().getItem(3));
    }

    @Test
    public void constructorGenericDataProvider() {
        Grid<String> gridGenericDataProvider = new Grid<>(
                new StringGenericDataProvider());
        Assert.assertEquals(dummyData.get(0),
                gridGenericDataProvider.getGenericDataView().getItem(0));
        Assert.assertEquals(dummyData.get(1),
                gridGenericDataProvider.getGenericDataView().getItem(1));
        Assert.assertEquals(dummyData.get(2),
                gridGenericDataProvider.getGenericDataView().getItem(2));
        Assert.assertEquals(dummyData.get(3),
                gridGenericDataProvider.getGenericDataView().getItem(3));
    }

    @Test
    public void constructorInMemoryDataProvider() {
        Grid<String> gridInMemoryDataProvider = new Grid<>(
                new StringInMemoryDataProvider());
        Assert.assertEquals(dummyData.get(0),
                gridInMemoryDataProvider.getGenericDataView().getItem(0));
        Assert.assertEquals(dummyData.get(1),
                gridInMemoryDataProvider.getGenericDataView().getItem(1));
        Assert.assertEquals(dummyData.get(2),
                gridInMemoryDataProvider.getGenericDataView().getItem(2));
        Assert.assertEquals(dummyData.get(3),
                gridInMemoryDataProvider.getGenericDataView().getItem(3));
    }

    @Test
    public void constructorListDataProvider() {
        Grid<String> gridListDataProvider = new Grid<>(
                new ListDataProvider<String>(dummyData));
        Assert.assertEquals(dummyData.get(0),
                gridListDataProvider.getListDataView().getItem(0));
        Assert.assertEquals(dummyData.get(1),
                gridListDataProvider.getListDataView().getItem(1));
        Assert.assertEquals(dummyData.get(2),
                gridListDataProvider.getListDataView().getItem(2));
        Assert.assertEquals(dummyData.get(3),
                gridListDataProvider.getListDataView().getItem(3));
    }

    @Test
    public void constructorCollection() {
        Grid<String> gridCollection = new Grid<>(dummyData);
        Assert.assertEquals(dummyData.get(0),
                gridCollection.getGenericDataView().getItem(0));
        Assert.assertEquals(dummyData.get(1),
                gridCollection.getGenericDataView().getItem(1));
        Assert.assertEquals(dummyData.get(2),
                gridCollection.getGenericDataView().getItem(2));
        Assert.assertEquals(dummyData.get(3),
                gridCollection.getGenericDataView().getItem(3));
    }

    private final class StringBackendDataProvider extends AbstractDataProvider<String, Void>
            implements BackEndDataProvider<String, Void> {

        @Override
        public void setSortOrders(List<QuerySortOrder> list) {
            return;
        }

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public int size(Query<String, Void> query) {
            return 4;
        }

        @Override
        public Stream<String> fetch(Query<String, Void> query) {
            query.getPage();
            query.getPageSize();
            return dummyData.subList(query.getOffset(),
                    query.getOffset() + query.getLimit()).stream();
        }

    }

    private final class StringInMemoryDataProvider extends AbstractDataProvider<String, SerializablePredicate<String>>
            implements InMemoryDataProvider<String> {

        @Override
        public SerializablePredicate<String> getFilter() {
            return null;
        }

        @Override
        public void setFilter(
                SerializablePredicate<String> serializablePredicate) {

        }

        @Override
        public SerializableComparator<String> getSortComparator() {
            return null;
        }

        @Override
        public void setSortComparator(
                SerializableComparator<String> serializableComparator) {
            return;
        }

        @Override
        public int size(Query<String, SerializablePredicate<String>> query) {
            return 4;
        }

        @Override
        public Stream<String> fetch(
                Query<String, SerializablePredicate<String>> query) {
            query.getPage();
            query.getPageSize();
            return dummyData.subList(query.getOffset(),
                    query.getOffset() + query.getLimit()).stream();
        }

    }

    private final class StringGenericDataProvider extends AbstractDataProvider<String, Void>
            implements DataProvider<String, Void> {

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public int size(Query<String, Void> query) {
            return 4;
        }

        @Override
        public Stream<String> fetch(Query<String, Void> query) {
            query.getPage();
            query.getPageSize();
            return dummyData.subList(query.getOffset(),
                    query.getOffset() + query.getLimit()).stream();
        }

    }
}
