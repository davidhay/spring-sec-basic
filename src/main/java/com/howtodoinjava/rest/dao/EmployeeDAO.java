package com.howtodoinjava.rest.dao;

import com.howtodoinjava.rest.model.Employee;
import com.howtodoinjava.rest.model.Employees;
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
