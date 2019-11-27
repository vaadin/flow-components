package com.vaadin.flow.component.listbox.demo.data;

import com.vaadin.flow.component.listbox.demo.entity.Department;

import java.util.ArrayList;
import java.util.List;

public class DepartmentData {

    private final static List<Department> DEPARTMENT_LIST = createDepartmentList();

    private static List<Department> createDepartmentList() {
        List<Department> departmentList = new ArrayList<>();

        departmentList.add(new Department(1, "Product",
                "Development and maintenance of the official software products"));
        departmentList.add(new Department(2, "Services",
                "Customer consulting projects and service product delivery and development"));
        departmentList.add(
                new Department(3, "HR", "Employee well-being and development"));
        departmentList.add(new Department(4, "Accounting",
                "Finance, billing and reporting"));

        return departmentList;
    }

    public List<Department> getDepartments() {
        return DEPARTMENT_LIST;
    }
}
