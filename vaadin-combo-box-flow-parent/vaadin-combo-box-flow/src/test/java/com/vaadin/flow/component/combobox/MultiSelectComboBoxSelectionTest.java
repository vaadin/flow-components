package com.vaadin.flow.component.combobox;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiSelectComboBoxSelectionTest {

    MultiSelectComboBox<String> comboBox;
    private MultiSelectionListener<MultiSelectComboBox<String>, String> selectionListenerSpy;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        comboBox = new MultiSelectComboBox<>();
        comboBox.setItems("1", "2", "3", "4", "5");
        selectionListenerSpy = Mockito.mock(MultiSelectionListener.class);
        comboBox.addSelectionListener(selectionListenerSpy);
    }

    @Test
    public void isSelected() {
        comboBox.select("2", "3");

        Assert.assertTrue(comboBox.isSelected("2"));
        Assert.assertTrue(comboBox.isSelected("3"));

        Assert.assertFalse(comboBox.isSelected("1"));
        Assert.assertFalse(comboBox.isSelected("4"));
        Assert.assertFalse(comboBox.isSelected("5"));
        Assert.assertFalse(comboBox.isSelected("99"));
    }

    @Test(expected = NullPointerException.class)
    public void isNullSelected_throws() {
        comboBox.isSelected(null);
    }

    @Test
    public void getSelectedItems() {
        comboBox.select("2", "3");

        Assert.assertEquals(Set.of("2", "3"), comboBox.getSelectedItems());
    }

    @Test
    public void setValue_updatesSelectionAndTriggersSelectionListener() {
        comboBox.setValue(Set.of("2", "3"));

        Assert.assertEquals(Set.of("2", "3"), comboBox.getValue());
        Assert.assertEquals(Set.of("2", "3"), comboBox.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setValue_setExistingValue_noChanges() {
        comboBox.setValue(Set.of("2", "3"));
        Mockito.reset(selectionListenerSpy);
        comboBox.setValue(Set.of("2", "3"));

        Assert.assertEquals(Set.of("2", "3"), comboBox.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setValue_setDifferentValue_selectionChanged() {
        comboBox.setValue(Set.of("2", "3"));
        Mockito.reset(selectionListenerSpy);
        comboBox.setValue(Set.of("1", "2"));

        Assert.assertEquals(Set.of("1", "2"), comboBox.getValue());
        Assert.assertEquals(Set.of("1", "2"), comboBox.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void changeSelection_updatesSelectionAndValueAndTriggersSelectionListener() {
        comboBox.select("1", "2", "3");
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);

        comboBox.deselect("2", "3");
        Assert.assertEquals(Set.of("1"), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of("1"), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);

        comboBox.deselectAll();
        Assert.assertEquals(Set.of(), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of(), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateSelection_updatesSelectionAndValueAndTriggersSelectionListener() {
        comboBox.updateSelection(Set.of("1", "2", "3"), Collections.emptySet());
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);

        comboBox.updateSelection(Collections.emptySet(), Set.of("2", "3"));
        Assert.assertEquals(Set.of("1"), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of("1"), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void selectExistingItems_noChanges() {
        comboBox.select("1", "2", "3");
        Mockito.reset(selectionListenerSpy);

        comboBox.select();
        comboBox.select("1");
        comboBox.select("1", "2");
        comboBox.select("1", "2", "3");
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deselectUnselectedItems_noChanges() {
        comboBox.select("1", "2", "3");
        Mockito.reset(selectionListenerSpy);

        comboBox.deselect();
        comboBox.deselect("4", "5");
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }

    @Test
    public void emptySelection_deselectAll_noChanges() {
        comboBox.deselectAll();
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }

    @Test
    public void selectItem_changeDataProvider_selectionIsReset() {
        AtomicReference<String> capture = new AtomicReference<>();
        comboBox.addValueChangeListener(
                event -> capture.set(getItemsString(event.getValue())));

        comboBox.setValue("1");

        Assert.assertEquals("1", capture.get());
        Assert.assertEquals(Set.of("1"), comboBox.getValue());

        comboBox.setItems("Foo", "Bar");

        Assert.assertEquals(Collections.emptySet(), comboBox.getValue());
        Assert.assertEquals("", capture.get());
    }

    @Test
    public void selectItems_removeItemFromDataSource_refreshAll_removedItemsAreDeselected() {
        List<String> items = new ArrayList<>();
        items.add("Foo");
        items.add("Bar");

        comboBox.setItems(items);

        AtomicReference<String> capture = new AtomicReference<>();
        comboBox.addValueChangeListener(
                event -> capture.set(getItemsString(event.getValue())));

        comboBox.setValue("Foo", "Bar");

        Assert.assertEquals(getItemsString(Set.of("Foo", "Bar")),
                capture.get());
        Assert.assertEquals(Set.of("Foo", "Bar"), comboBox.getValue());

        items.add("Baz");
        items.remove(0);
        comboBox.getListDataView().refreshAll();

        Assert.assertEquals(Set.of("Bar"), comboBox.getValue());
        Assert.assertEquals("Bar", capture.get());
    }

    @Test
    public void selectItem_setItemLabelGenerator_selectionIsRetained() {
        AtomicReference<String> capture = new AtomicReference<>();
        comboBox.addValueChangeListener(
                event -> capture.set(getItemsString(event.getValue())));

        comboBox.setValue("1");

        Assert.assertEquals("1", capture.get());
        Assert.assertEquals(Set.of("1"), comboBox.getValue());

        comboBox.setItemLabelGenerator(item -> item + " (Updated)");

        Assert.assertEquals("1", capture.get());
        Assert.assertEquals(Set.of("1"), comboBox.getValue());
    }

    @Test
    public void useLazyLoading_setValue_refreshAll_valueIsReset() {
        List<String> items = List.of("Foo", "Bar");

        comboBox.setItems(new AbstractBackEndDataProvider<String, String>() {
            @Override
            protected Stream<String> fetchFromBackEnd(
                    Query<String, String> query) {
                return items.stream().skip(query.getOffset())
                        .limit(query.getLimit());
            }

            @Override
            protected int sizeInBackEnd(Query<String, String> query) {
                return (int) fetchFromBackEnd(query).count();
            }
        });

        AtomicReference<String> capture = new AtomicReference<>();
        comboBox.addValueChangeListener(
                event -> capture.set(getItemsString(event.getValue())));

        comboBox.setValue("Foo");

        Assert.assertEquals(Set.of("Foo"), comboBox.getValue());
        Assert.assertEquals("Foo", capture.get());

        comboBox.getLazyDataView().refreshAll();

        Assert.assertEquals(Collections.emptySet(), comboBox.getValue());
        Assert.assertEquals("", capture.get());
    }

    private String getItemsString(Set<String> itemSet) {
        return itemSet.stream().sorted().collect(Collectors.joining(", "));
    }
}