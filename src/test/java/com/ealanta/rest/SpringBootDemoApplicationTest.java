package com.ealanta.rest;


import com.ealanta.rest.dao.EmployeeDAO;
import com.ealanta.rest.model.Employee;
import com.ealanta.rest.model.Employees;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    public void performSuccessfulLogin() throws Exception {
        MvcResult result = this.mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.ALL)
                        .content("username=admin&password=2112"))
                        .andExpect(authenticated().withAuthenticationName("admin")
                                           .withAuthentication(this::checkAuthentication).withRoles("USER","ADMIN","TEST"))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/home"))
                        //.andDo(print())
                        .andReturn();

    }

    @Test
    @WithMockUser(username = "bob", roles = {"ADMIN","USER"} )
    public void canLogoutIfLoggedIn() throws Exception {
        MvcResult result = this.mockMvc.perform(logout())
                        .andExpect(unauthenticated())
                                   .andExpect(status().is3xxRedirection())
                                   .andExpect(redirectedUrl("/login?logout=true"))
                                   .andDo(print())
                                   .andReturn();

    }

    @Test
    public void cannotLogoutIfNotLoggedIn() throws Exception {
        MvcResult result = this.mockMvc.perform(logout())
                                   .andExpect(unauthenticated())
                                   .andExpect(status().is3xxRedirection())
                                   .andExpect(redirectedUrl("/login?logout=true"))
                                   .andDo(print())
                                   .andReturn();
    }

    @Test
    public void redirectedToLoginIfNotLoggedIn() throws Exception {
        MvcResult result = this.mockMvc.perform(
                get("/home")
                        .accept(MediaType.ALL)
        )
                        .andExpect(unauthenticated())
                        .andExpect(status().isFound())
                        .andExpect(redirectedUrl("http://localhost/login"))
                        //.andDo(print())
                        .andReturn();

        DefaultSavedRequest req = (DefaultSavedRequest)result.getRequest().getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
        Assert.assertEquals("GET",req.getMethod());
        Assert.assertEquals("/home",req.getRequestURI());
    }

    @Test
    @WithMockUser(username = "bob", roles = {"ADMIN","USER"} )
    public void testCanAccessProtectedResourcesIfLoggedIn() throws Exception {
        MvcResult result = this.mockMvc.perform(
                get("/employees/")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        )
                                   .andExpect(authenticated().withRoles("USER","ADMIN").withUsername("bob"))
                                   .andExpect(status().isOk()).andDo(print())
                                   .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                                   .andExpect(jsonPath(".employees[0]").exists())
                                   .andExpect(jsonPath(".employees[1]").exists())
                                   .andExpect(jsonPath(".employees[2]").exists())
                                   .andExpect(jsonPath(".employees[3]").doesNotExist())
                                   .andReturn();
    }

    @Test
    public void performUnSuccessfulLogin1() throws Exception {
        MvcResult result = this.mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.ALL)
                        .content("username=admin&password=BOB"))
                                   .andExpect(unauthenticated())
                                   .andExpect(status().isFound())
                                   .andExpect(redirectedUrl("/login?error=true"))
                                   //.andDo(print())
                                   .andReturn();

        BadCredentialsException ex = (BadCredentialsException)result.getRequest().getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        Assert.assertEquals("Bad credentials",ex.getMessage());

    }

    public void checkAuthentication(Authentication auth){
        System.out.printf("AUTHORITIES %s %n",auth.getAuthorities());
    }

    @Test
    public void getMessage() throws Exception {
        this.mockMvc.perform(get(PATH_MESSAGE))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }
}
