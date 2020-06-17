package com.vaadin.flow.component.treegrid.demo.data;

import com.vaadin.flow.component.treegrid.demo.entity.Department;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DepartmentData {
    private static final List<Department> DEPARTMENT_LIST = createDepartmentList();

    private static List<Department> createDepartmentList() {
        List<Department> departmentList = new ArrayList<>();

        departmentList
                .add(new Department(1, "Product Development", null, "Päivi"));
        departmentList.add(
                new Department(11, "Flow", departmentList.get(0), "Pekka"));
        departmentList.add(new Department(111, "Flow Core",
                departmentList.get(1), "Pekka"));
        departmentList.add(new Department(111, "Flow Components",
                departmentList.get(1), "Gilberto"));
        departmentList.add(
                new Department(12, "Design", departmentList.get(0), "Pekka"));
        departmentList.add(
                new Department(13, "DJO", departmentList.get(0), "Thomas"));
        departmentList.add(
                new Department(14, "Component", departmentList.get(0), "Tomi"));
        departmentList.add(new Department(2, "HR", null, "Anne"));
        departmentList.add(
                new Department(21, "Office", departmentList.get(7), "Anu"));
        departmentList.add(
                new Department(22, "Employee", departmentList.get(7), "Minna"));
        departmentList.add(new Department(3, "Marketing", null, "Niko"));
        departmentList.add(
                new Department(31, "Growth", departmentList.get(10), "Ömer"));
        departmentList.add(new Department(32, "Demand Generation",
                departmentList.get(10), "Marcus"));
        departmentList.add(new Department(33, "Product Marketing",
                departmentList.get(10), "Pekka"));
        departmentList.add(new Department(34, "Brand Experience",
                departmentList.get(10), "Eero"));

        return departmentList;

    }

    public List<Department> getDepartments() {
        return DEPARTMENT_LIST;
    }

    public List<Department> getRootDepartments() {
        return DEPARTMENT_LIST.stream()
                .filter(department -> department.getParent() == null)
                .collect(Collectors.toList());
    }

    public List<Department> getChildDepartments(Department parent) {
        return DEPARTMENT_LIST.stream().filter(
                department -> Objects.equals(department.getParent(), parent))
                .collect(Collectors.toList());
    }

}
