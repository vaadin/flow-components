/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.demo.data;

import com.vaadin.flow.component.grid.demo.entity.Task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaskData {
    private List<Task> TASK_LIST = createTaskList();

    private List<Task> createTaskList() {
        List<Task> taskList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        taskList.add(new Task(1, "Grid demos",
                LocalDate.parse("01/01/2019", formatter)));
        taskList.add(new Task(1, "Checkbox demos",
                LocalDate.parse("02/01/2019", formatter)));
        taskList.add(new Task(1, "Date Picker demos",
                LocalDate.parse("03/01/2019", formatter)));
        taskList.add(new Task(1, "Radio Button demos",
                LocalDate.parse("04/01/2019", formatter)));
        taskList.add(new Task(1, "Text Field demos",
                LocalDate.parse("05/01/2019", formatter)));
        taskList.add(new Task(1, "Time Picker demos",
                LocalDate.parse("06/01/2019", formatter)));
        taskList.add(new Task(1, "Dialog demos ",
                LocalDate.parse("07/01/2019", formatter)));

        return taskList;
    }

    public List<Task> getTasks() {
        return TASK_LIST;
    }
}
