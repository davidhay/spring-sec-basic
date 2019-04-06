package com.howtodoinjava.rest;


import com.howtodoinjava.rest.dao.EmployeeDAO;
import com.howtodoinjava.rest.model.Employee;
import com.howtodoinjava.rest.model.Employees;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(includeFilters = @ComponentScan.Filter(classes = EnableWebSecurity.class))
public class SpringBootDemoApplicationTest {
    private static String PATH_EMPLOYEES = "/employees/";
    private static String PATH_MESSAGE = "/message";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @MockBean
    private EmployeeDAO dao;

    @Before
    public void setUp() throws Exception {
        Employees employees = new Employees();
        employees.getEmployeeList().add(new Employee(1, "f1", "l1", "e1@test.com"));
        employees.getEmployeeList().add(new Employee(2, "f2", "l2", "e2@test.com"));
        employees.getEmployeeList().add(new Employee(3, "f3", "l3", "e3@test.com"));
        when(dao.getAllEmployees()).thenReturn(employees);
    }

    @Test
    public void getAuthorized() throws Exception {
        this.mockMvc.perform(get(PATH_EMPLOYEES).with(httpBasic("admin", "password")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.employees[0]").exists())
                .andExpect(jsonPath("$.employees[1]").exists())
                .andExpect(jsonPath("$.employees[2]").exists())
                .andExpect(jsonPath("$.employees[0].firstName").value("f1"))
                .andExpect(jsonPath("$.employees[1].lastName").value("l2"))
                .andExpect(jsonPath("$.employees[2].email").value("e3@test.com"))
                .andExpect(jsonPath("$.employees[3]").doesNotExist());
    }

    @Test
    public void getUnauthorized() throws Exception {
        this.mockMvc.perform(get(PATH_EMPLOYEES))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, equalTo("Basic realm=\"DavidRealm\"")));
    }

    @Test
    public void getMessage() throws Exception {
        this.mockMvc.perform(get(PATH_MESSAGE))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }
}
