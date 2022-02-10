package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route("vaadin-grid/grid-details-row")
public class GridDetailsRowPage extends Div {

    private Grid<Person> grid = new Grid<>();
    private List<Person> items = new ArrayList<>();

    private int nbUpdates;
    private Person person3 = new Person("Person 3", 2);
    private Person person4 = new Person("Person 4", 1111);

    public GridDetailsRowPage() {

        items.add(new Person("Person 1", 99));
        items.add(new Person("Person 2", 1));
        items.add(person3);
        items.add(person4);

        ListDataProvider<Person> ldp = new ListDataProvider<>(items);
        grid.setDataProvider(ldp);
        grid.setSelectionMode(SelectionMode.NONE);
        grid.addColumn(Person::getFirstName).setHeader("name");
        grid.setItemDetailsRenderer(new ComponentRenderer<>(
                item -> new Button(item.getFirstName())));

        add(grid, new Button("click to open details",
                e -> setFirstAndSecondItemsVisible()));
        Button updatePerson3 = new Button("update and refresh person 3", e -> {
            nbUpdates++;
            person3.setFirstName("Person 3 - updates " + nbUpdates);
            grid.getDataProvider().refreshItem(person3);
        });
        updatePerson3.setId("update-button");
        add(updatePerson3);

        Button removeButton = new Button("remove person 4", e -> {
            items.remove(person4);
            grid.getDataProvider().refreshAll();
        });
        removeButton.setId("remove-button");
        add(removeButton);
        setFirstAndSecondItemsVisible();
    }

    public void setFirstAndSecondItemsVisible() {
        grid.setDetailsVisible(items.get(0), true);
        grid.setDetailsVisible(items.get(1), true);
    }
}
