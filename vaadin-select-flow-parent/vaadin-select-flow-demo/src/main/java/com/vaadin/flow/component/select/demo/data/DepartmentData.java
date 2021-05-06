package com.vaadin.flow.component.select.demo.data;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.select.demo.entity.Department;

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
