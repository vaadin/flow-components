
package com.vaadin.flow.component.listbox.demo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;
import com.vaadin.flow.component.listbox.demo.data.DepartmentData;
import com.vaadin.flow.component.listbox.demo.entity.Department;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * View for {@link ListBox} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-list-box")
public class ListBoxView extends DemoView {

    private static final String DATA_VIEW = "Data View";
    private static final String PRESENTATION = "Presentation";

    @Override
    public void initView() {
        basicDemo();// Basic usage
        disabledItem();
        multiSelection();
        dataViewRefreshItem();// Data View
        dataViewAddAndRemoveItem();
        dataViewFiltering();
        separatorDemo();// Presentation
        customOptions();
        usingTemplateRenderer();
        styling(); // Styling
    }

    private void basicDemo() {
        // begin-source-example
        // source-example-heading: Basic usage
        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Option one", "Option two", "Option three");
        listBox.setValue("Option one");
        // end-source-example

        addCard("Basic usage", listBox);
    }

    private void disabledItem() {
        // begin-source-example
        // source-example-heading: Disabled item
        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Option one", "Option two", "Option three");
        listBox.setValue("Option one");
        listBox.setItemEnabledProvider(item -> !"Option three".equals(item));
        // end-source-example

        addCard("Disabled item", listBox);
    }

    private void multiSelection() {
        // begin-source-example
        // source-example-heading: Multi select list box
        MultiSelectListBox<String> listBox = new MultiSelectListBox<>();
        listBox.setItems("Option one", "Option two", "Option three",
                "Option four");
        // end-source-example

        addCard("Multi select list box", listBox);
    }

    private void separatorDemo() {
        // begin-source-example
        // source-example-heading: Separators
        ListBox<DayOfWeek> listBox = new ListBox<>();
        listBox.setItems(DayOfWeek.values());
        listBox.setValue(DayOfWeek.MONDAY);
        listBox.addComponents(DayOfWeek.FRIDAY, new Hr());
        // end-source-example

        addCard(PRESENTATION, "Separators", listBox);
    }

    private void customOptions() {
        // begin-source-example
        // source-example-heading: Customizing the label
        ListBox<Employee> listBox = new ListBox<>();
        List<Employee> list = Arrays.asList(
                new Employee("Gabriella",
                        "https://randomuser.me/api/portraits/women/43.jpg"),
                new Employee("Rudi",
                        "https://randomuser.me/api/portraits/men/77.jpg"),
                new Employee("Hamsa",
                        "https://randomuser.me/api/portraits/men/35.jpg"),
                new Employee("Jacob",
                        "https://randomuser.me/api/portraits/men/76.jpg"));
        listBox.setItems(list);
        listBox.setValue(list.get(0));

        listBox.setRenderer(new ComponentRenderer<>(employee -> {
            Div text = new Div();
            text.setText(employee.getTitle());

            Image image = new Image();
            image.setWidth("21px");
            image.setHeight("21px");
            image.setSrc(employee.getImage());

            FlexLayout wrapper = new FlexLayout();
            text.getStyle().set("margin-left", "0.5em");
            wrapper.add(image, text);
            return wrapper;
        }));
        // end-source-example

        addCard(PRESENTATION, "Customizing the label", listBox);
    }

    private List<Department> getDepartments() {

        DepartmentData departmentData = new DepartmentData();
        return departmentData.getDepartments();
    }

    private void usingTemplateRenderer() {
        // begin-source-example
        // source-example-heading: Multi-line label
        ListBox<Department> listBox = new ListBox<>();
        List<Department> listOfDepartments = getDepartments();
        listBox.setItems(listOfDepartments);
        listBox.setValue(listOfDepartments.get(0));

        listBox.setRenderer(new ComponentRenderer<>(department -> {
            Div name = new Div();
            name.getStyle().set("font-weight", "bold");
            name.setText(department.getName());

            Div description = new Div();
            description.setText(department.getDescription());
            return new Div(name, description);
        }));
        // end-source-example

        addCard(PRESENTATION, "Multi-line label", listBox);
    }

    private void dataViewRefreshItem() {
        // begin-source-example
        // source-example-heading: Refresh Items
        MultiSelectListBox<Employee> multiSelectListBox = new MultiSelectListBox<>();
        Employee employee1 = new Employee("Employee One");
        Employee employee2 = new Employee("Employee Two");
        Employee employee3 = new Employee("Employee Three");
        ListBoxListDataView<Employee> dataView = multiSelectListBox
                .setItems(employee1, employee2, employee3);

        Button updateButton = new Button("Update second employee's name",
                click -> {
                    employee2.setTitle("Employee 2");
                    dataView.refreshItem(employee2);
                });
        // end-source-example

        addCard(DATA_VIEW, "Refresh Items", multiSelectListBox, updateButton);
    }

    private void dataViewAddAndRemoveItem() {
        // begin-source-example
        // source-example-heading: Add and Remove Item
        ListBox<Employee> listBox = new ListBox<>();
        List<Employee> employeeList = getEmployeeList();
        ListBoxListDataView<Employee> dataView = listBox.setItems(employeeList);
        AtomicInteger employeeCounter = new AtomicInteger(1);
        Button addButton = new Button("Add to Options",
                click -> dataView.addItem(new Employee(
                        "Employee " + (employeeCounter.incrementAndGet()))));
        Button removeButton = new Button("Remove from Options", click -> {
            int itemCount = dataView.getItemCount();
            if (itemCount > 0) {
                dataView.removeItem(dataView.getItem(itemCount - 1));
            }
        });
        // end-source-example

        HorizontalLayout layout = new HorizontalLayout(addButton, removeButton);
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);

        addCard(DATA_VIEW, "Add and Remove Item", layout, listBox);
    }

    private List<Employee> getEmployeeList() {
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(new Employee("Employee 1"));
        return employeeList;
    }

    private void dataViewFiltering() {
        // begin-source-example
        // source-example-heading: Filtering Items
        MultiSelectListBox<Integer> numbers = new MultiSelectListBox<>();
        ListBoxListDataView<Integer> numbersDataView = numbers.setItems(1, 2, 3,
                4, 5, 6, 7, 8, 9, 10);

        Button showOdds = new Button("Show Odds",
                click -> numbersDataView.setFilter(number -> number % 2 == 1));

        Button showEvens = new Button("Show Evens",
                click -> numbersDataView.setFilter(number -> number % 2 == 0));

        Button noFilter = new Button("Show All",
                click -> numbersDataView.removeFilters());
        // end-source-example
        HorizontalLayout buttonLayout = new HorizontalLayout(showOdds,
                showEvens, noFilter);
        buttonLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        addCard(DATA_VIEW, "Filtering Items", buttonLayout, numbers);
    }

    private <T> Set<T> createSet(T... items) {
        return new HashSet<>(Arrays.asList(items));
    }

    private void styling() {
        Div firstDiv = new Div();
        firstDiv.setText(
                "To read about styling you can read the related tutorial in");
        Anchor firstAnchor = new Anchor(
                "https://vaadin.com/docs/flow/theme/using-component-themes.html",
                "Using Component Themes");

        Div secondDiv = new Div();
        secondDiv.setText("To know about styling in html you can read the ");
        Anchor secondAnchor = new Anchor(
                "https://vaadin.com/components/vaadin-list-box/html-examples/list-box-styling-demos",
                "HTML Styling Demos");

        HorizontalLayout firstHorizontalLayout = new HorizontalLayout(firstDiv,
                firstAnchor);
        HorizontalLayout secondHorizontalLayout = new HorizontalLayout(
                secondDiv, secondAnchor);
        // begin-source-example
        // source-example-heading: Styling references

        // end-source-example
        addCard("Styling", "Styling references", firstHorizontalLayout,
                secondHorizontalLayout);
    }

    private static class Employee {
        private String title;
        private String image;

        public Employee() {
        }

        public Employee(String title) {
            this.title = title;
        }

        private Employee(String title, String image) {
            this.title = title;
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
