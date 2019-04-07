package com.ealanta.rest.dao;

import com.ealanta.rest.model.Employee;
import com.ealanta.rest.model.Employees;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeDAO {

    private final Employees emp = new Employees();

    public EmployeeDAO(){
        List<Employee> emps = emp.getEmployeeList();
        emps.add(new Employee(1, "Bruce", "Wayne", "bruce@wayne.com"));
        emps.add(new Employee(2, "Dick", "Grayson", "robin@wayne.com"));
        emps.add(new Employee(3, "Alfred", "Pennyworth", "titanic@wayne.com"));
    }

    public Employees getAllEmployees() {
        return emp;
    }

}
