package com.vaadin.flow.component.combobox.test.data;

import com.vaadin.flow.component.combobox.test.entity.Department;

import java.util.ArrayList;
import java.util.List;

public class DepartmentData {

    private final static List<Department> DEPARTMENT_LIST = createDepartmentList();

    private static List<Department> createDepartmentList() {
        List<Department> departmentList = new ArrayList<>();

        departmentList = new ArrayList<>();
        departmentList.add(new Department(1, "Product"));
        departmentList.add(new Department(2, "Service"));
        departmentList.add(new Department(3, "HR"));
        departmentList.add(new Department(4, "Accounting"));

        return departmentList;
    }

    public List<Department> getDepartments() {
        return DEPARTMENT_LIST;
    }
}
