package com.howtodoinjava.rest;


import com.howtodoinjava.rest.model.Employees;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootDemoApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class SpringBootDemoApplicationIntegrationTest {
    @Autowired
    private TestRestTemplate template;

    private static final String PATH_EMPLOYEES = "/employees/";
    private static final String USERNAME="admin";
    private static final String PASSWORD="password";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void getUnauthorized() throws Exception {
        ResponseEntity<Employees> response = template.getForEntity(PATH_EMPLOYEES, Employees.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
        List<String> authenticate = response.getHeaders().get(HttpHeaders.WWW_AUTHENTICATE);
        assertThat(authenticate, hasSize(1));
        String auth = authenticate.get(0);
        System.out.println(auth);
        assertThat(auth, equalTo("Basic realm=\"DavidRealm\""));
    }

    @Test
    public void getAuthorised() throws Exception {
        ResponseEntity<Employees> response = template.withBasicAuth(USERNAME, PASSWORD).getForEntity(PATH_EMPLOYEES, Employees.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        List<String> authenticate = response.getHeaders().get(HttpHeaders.WWW_AUTHENTICATE);
        assertThat(authenticate, is(nullValue()));

        assertThat(response.getBody().getEmployeeList(), hasSize(3));
    }
}
