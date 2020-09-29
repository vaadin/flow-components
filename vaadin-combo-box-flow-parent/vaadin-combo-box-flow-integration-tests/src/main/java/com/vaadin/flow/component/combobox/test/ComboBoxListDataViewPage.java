package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.component.combobox.demo.entity.Person;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;

@Route("combobox-list-data-view-page")
public class ComboBoxListDataViewPage extends Div {

    public static final String ITEM_COUNT = "itemCount";
    public static final String ITEM_DATA = "itemData";
    public static final String ITEM_SELECT = "itemSelect";
    public static final String SHOW_ITEM_DATA = "showItemData";
    public static final String SHOW_NEXT_DATA = "showNextData";
    public static final String SHOW_PREVIOUS_DATA = "showPreviousData";
    public static final String AGE_FILTER = "ageFilter";
    public static final String REMOVE_ITEM = "removeItem";
    public static final String REVERSE_SORTING = "reverseSorting";

    public ComboBoxListDataViewPage() {
        List<Person> personList = generatePersonItems();
        ComboBox<Person> comboBox = new ComboBox<>();
        comboBox.setAllowCustomValue(true);
        comboBox.setItemLabelGenerator(Person::getFirstName);

        final ComboBoxListDataView<Person> dataView = comboBox
                .setItems(personList);

        // Add custom item to collection
        comboBox.addCustomValueSetListener(event -> {
            Person newPerson = new Person();
            newPerson.setFirstName(event.getDetail());
            dataView.addItem(newPerson);
            comboBox.setValue(newPerson);
        });

        Span count = new Span(Integer.toString(dataView.getItemCount()));
        count.setId(ITEM_COUNT);
        Span itemData = new Span("Item: ");
        itemData.setId(ITEM_DATA);

        // Create data controls
        // For selecting an item to manipulate through the data view
        IntegerField itemSelect = new IntegerField("Target item");
        itemSelect.setId(ITEM_SELECT);
        NativeButton selectItemOnIndex = new NativeButton("Select item",
                event -> comboBox
                        .setValue(dataView.getItem(itemSelect.getValue())));
        selectItemOnIndex.setId("selectItemOnIndex");
        NativeButton showItemData = new NativeButton("Person data",
                event -> itemData.setText("Item: " + dataView
                        .getItem(itemSelect.getValue()).getFirstName()));
        showItemData.setId(SHOW_ITEM_DATA);

        // Navigation
        NativeButton showNextData = new NativeButton("Next person",
                event -> itemData.setText("Item: " + dataView
                        .getNextItem(dataView.getItem(itemSelect.getValue()))
                        .get().getFirstName()));
        showNextData.setId(SHOW_NEXT_DATA);

        NativeButton showPreviousData = new NativeButton("Previous person",
                event -> itemData.setText("Item: " + dataView
                        .getPreviousItem(
                                dataView.getItem(itemSelect.getValue()))
                        .get().getFirstName()));

        // Remove item
        NativeButton removePerson = new NativeButton("Remove person", event -> {
            dataView.removeItem(comboBox.getValue());
            comboBox.setValue(null);
        });
        removePerson.setId(REMOVE_ITEM);
        showPreviousData.setId(SHOW_PREVIOUS_DATA);

        // Filter by Age
        IntegerField filterByAge = new IntegerField("Age filter",
                event -> dataView.setFilter(person -> event.getValue() == null
                        || event.getValue().equals(person.getAge())));
        filterByAge.setId(AGE_FILTER);

        // Sorting
        NativeButton reverseSorting = new NativeButton("Reverse sorting",
                event -> dataView.setSortOrder(Person::getFirstName,
                        SortDirection.DESCENDING));
        reverseSorting.setId(REVERSE_SORTING);

        // Item count listener
        dataView.addItemCountChangeListener(event -> {
            count.setText(Integer.toString(event.getItemCount()));
            showPreviousData.setEnabled(itemSelect.getValue() > 0);
            showNextData.setEnabled(
                    itemSelect.getValue() < event.getItemCount() - 1);
        });

        itemSelect.setValue(0);
        showPreviousData.setEnabled(false);
        itemSelect.addValueChangeListener(event -> {
            if (event.getValue() >= dataView.getItemCount()) {
                itemData.setText("Action: Item outside of data rage of [0,"
                        + (dataView.getItemCount() - 1)
                        + "]. Resetting to previous");
                itemSelect.setValue(event.getOldValue());
                return;
            }
            showPreviousData.setEnabled(event.getValue() > 0);
            showNextData
                    .setEnabled(event.getValue() < dataView.getItemCount() - 1);
        });

        add(comboBox, itemSelect, filterByAge, reverseSorting,
                selectItemOnIndex, showItemData, showNextData, showPreviousData,
                removePerson, count, itemData);
    }

    private List<Person> generatePersonItems() {
        return IntStream.range(1, 251)
                .mapToObj(index -> new Person(index, "Person " + index, "lastName",
                        index % 100, new Person.Address(), "1234567890"))
                .collect(Collectors.toList());
    }
}
