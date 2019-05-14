package com.vaadin.flow.component.select.data;

import com.vaadin.flow.component.select.entity.Department;

import java.util.ArrayList;
import java.util.List;

public class DepartmentData {

    private final static List<Department> DEPARTMENT_LIST = createDepartmentList();

    private static List<Department> createDepartmentList() {
        List<Department> departmentList = new ArrayList<>();

        departmentList = new ArrayList<>();
        departmentList.add(new Department(1, "Product"));
        departmentList.add(new Department(2, "Service"));
        departmentList.add(new Department(1, "HR"));
        departmentList.add(new Department(1, "Accounting"));

        return departmentList;
    }

    public List<Department> getDepartments() {
        return DEPARTMENT_LIST;
    }
}
