package com.ealanta.rest;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootDemoApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class SpringBootDemoApplicationIntegrationTest {

    private static final String PATH_EMPLOYEES = "/employees/";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "2112";
    private static final String BAD_PASSWORD = "BAD_PASSWORD";

    @LocalServerPort
    int randomServerPort;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSuccessfulLogin() throws Exception {
        Context ctx = new Context();

        HttpResponse response = ctx.login(USERNAME, PASSWORD);

        Header[] headers = response.getAllHeaders();
        Arrays.stream(headers).forEach(header ->
                                               System.out.printf("HEADER %s%n", header));
        Assert.assertEquals(HttpStatus.FOUND.value(), response.getStatusLine().getStatusCode());
        Header location = response.getFirstHeader(HttpHeaders.LOCATION);
        Assert.assertEquals("http://localhost:" + randomServerPort + "/home", location.getValue());
        checkSessionCookie(ctx.cookieStore);
    }

    @Test
    public void testSuccessfulLoginThenAccessProtected() throws Exception {
        Context ctx = new Context();

        HttpResponse loginResponse = ctx.login(USERNAME, PASSWORD);

        HttpGet employees = new HttpGet("http://localhost:" + randomServerPort + "/employees/");
        HttpResponse employeeResponse = ctx.client.execute(employees);

        Assert.assertEquals(HttpStatus.OK.value(), employeeResponse.getStatusLine().getStatusCode());

        ObjectMapper mapper = new ObjectMapper();

        String rawJSON = StreamUtils.copyToString(employeeResponse.getEntity().getContent(), Charset.defaultCharset());
        System.out.println(rawJSON);

        JsonNode node = mapper.readTree(rawJSON);

        Assert.assertEquals("Bruce", node.at("/employees/0/firstName").asText());
        Assert.assertEquals("Grayson", node.at("/employees/1/lastName").asText());
        Assert.assertEquals("titanic@wayne.com", node.at("/employees/2/email").asText());
        Assert.assertTrue(node.at("/employees/3").isMissingNode());
    }


    @Test
    public void performUnsuccessfulLogin() throws Exception {
        Context ctx = new Context();
        HttpResponse response = ctx.login(USERNAME, BAD_PASSWORD);
        Header[] headers = response.getAllHeaders();
        Arrays.stream(headers).forEach(header ->
                                               System.out.printf("HEADER %s%n", header));
        Assert.assertEquals(HttpStatus.FOUND.value(), response.getStatusLine().getStatusCode());
        Header location = response.getFirstHeader(HttpHeaders.LOCATION);
        Assert.assertEquals("http://localhost:" + randomServerPort + "/login?error=true", location.getValue());
        checkSessionCookie(ctx.cookieStore);
    }

    private void checkSessionCookie(BasicCookieStore cookieStore) throws Exception {
        List<Cookie> cookies = cookieStore.getCookies();
        Assert.assertEquals(1, cookies.size());
        Cookie cookie = cookies.get(0);
        Assert.assertEquals("JSESSIONID", cookie.getName());
        Assert.assertEquals("/", cookie.getPath());
        Date expiry = cookie.getExpiryDate();
        Assert.assertNull(expiry);
        Assert.assertEquals(false, cookie.isSecure());
        Assert.assertEquals(false, cookie.isPersistent());
        Assert.assertEquals(false, cookie.isExpired(new Date()));

    }

    class Context {
        public final BasicCookieStore cookieStore;
        public final CloseableHttpClient client;

        public Context() {
            cookieStore = new BasicCookieStore();
            client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        }

        public HttpResponse login(String username, String password) throws Exception {
            List<NameValuePair> postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair("username", username));
            postParameters.add(new BasicNameValuePair("password", password));

            //Build the server URI together with the parameters you wish to pass
            URIBuilder uriBuilder = new URIBuilder("http://localhost:" + randomServerPort + "/login");
            uriBuilder.addParameters(postParameters);

            HttpPost loginReq = new HttpPost(uriBuilder.build());
            return client.execute(loginReq);
        }
    }
}

