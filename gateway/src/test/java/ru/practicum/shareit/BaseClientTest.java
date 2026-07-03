package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BaseClientTest {
    private static final String SERVER_URL = "http://localhost:9090";
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private MockRestServiceServer server;
    private TestClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri(SERVER_URL)
                .build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new TestClient(restTemplate);
    }

    // Проверка GET-запроса без пользователя
    @Test
    void getShouldSendRequestWithoutUser() {
        expect(HttpMethod.GET, "/users");

        assertEquals(HttpStatus.OK, client.getRequest("/users").getStatusCode());

        server.verify();
    }

    // Проверка GET-запроса с пользователем
    @Test
    void getShouldSendRequestWithUser() {
        expectWithUser(HttpMethod.GET, "/items", 1L);

        assertEquals(HttpStatus.OK, client.getRequest("/items", 1L).getStatusCode());

        server.verify();
    }

    // Проверка GET-запроса с параметрами
    @Test
    void getShouldSendRequestWithParameters() {
        expectWithUser(HttpMethod.GET, "/bookings?state=ALL", 1L);

        ResponseEntity<Object> response = client.getRequest(
                "/bookings?state={state}",
                1L,
                Map.of("state", "ALL")
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        server.verify();
    }

    // Проверка POST-запроса без пользователя
    @Test
    void postShouldSendRequestWithoutUser() {
        expect(HttpMethod.POST, "/users");

        assertEquals(HttpStatus.OK, client.postRequest("/users", "Пользователь").getStatusCode());

        server.verify();
    }

    // Проверка POST-запроса с пользователем
    @Test
    void postShouldSendRequestWithUser() {
        expectWithUser(HttpMethod.POST, "/items", 1L);

        assertEquals(HttpStatus.OK, client.postRequest("/items", 1L, "Вещь").getStatusCode());

        server.verify();
    }

    // Проверка PUT-запроса
    @Test
    void putShouldSendRequest() {
        expectWithUser(HttpMethod.PUT, "/items/2", 1L);

        assertEquals(HttpStatus.OK, client.putRequest("/items/2", 1L, "Вещь").getStatusCode());

        server.verify();
    }

    // Проверка PATCH-запроса без пользователя
    @Test
    void patchShouldSendRequestWithoutUser() {
        expect(HttpMethod.PATCH, "/users/1");

        assertEquals(HttpStatus.OK, client.patchRequest("/users/1", "Пользователь").getStatusCode());

        server.verify();
    }

    // Проверка PATCH-запроса с пользователем
    @Test
    void patchShouldSendRequestWithUser() {
        expectWithUser(HttpMethod.PATCH, "/bookings/1", 1L);

        assertEquals(HttpStatus.OK, client.patchRequest("/bookings/1", 1L).getStatusCode());

        server.verify();
    }

    // Проверка DELETE-запроса без пользователя
    @Test
    void deleteShouldSendRequestWithoutUser() {
        expect(HttpMethod.DELETE, "/users/1");

        assertEquals(HttpStatus.OK, client.deleteRequest("/users/1").getStatusCode());

        server.verify();
    }

    // Проверка DELETE-запроса с пользователем
    @Test
    void deleteShouldSendRequestWithUser() {
        expectWithUser(HttpMethod.DELETE, "/items/2", 1L);

        assertEquals(HttpStatus.OK, client.deleteRequest("/items/2", 1L).getStatusCode());

        server.verify();
    }

    // Проверка возврата ошибки сервера с телом ответа
    @Test
    void clientShouldReturnErrorWithBody() {
        server.expect(requestTo(SERVER_URL + "/users/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"Ошибка\"}"));

        assertEquals(HttpStatus.NOT_FOUND, client.getRequest("/users/999").getStatusCode());

        server.verify();
    }

    // Проверка возврата ответа без тела
    @Test
    void clientShouldReturnResponseWithoutBody() {
        server.expect(requestTo(SERVER_URL + "/empty"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_MODIFIED));

        assertEquals(HttpStatus.NOT_MODIFIED, client.getRequest("/empty").getStatusCode());

        server.verify();
    }

    // Проверка возврата ответа с телом
    @Test
    void clientShouldReturnResponseWithBody() {
        server.expect(requestTo(SERVER_URL + "/redirect"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"Ошибка\"}"));

        assertEquals(HttpStatus.FOUND, client.getRequest("/redirect").getStatusCode());

        server.verify();
    }

    private void expect(HttpMethod method, String path) {
        server.expect(requestTo(SERVER_URL + path))
                .andExpect(method(method))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
    }

    private void expectWithUser(HttpMethod method, String path, Long userId) {
        server.expect(requestTo(SERVER_URL + path))
                .andExpect(method(method))
                .andExpect(header(USER_ID_HEADER, String.valueOf(userId)))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
    }

    private static class TestClient extends BaseClient {
        TestClient(RestTemplate rest) {
            super(rest);
        }

        ResponseEntity<Object> getRequest(String path) {
            return get(path);
        }

        ResponseEntity<Object> getRequest(String path, long userId) {
            return get(path, userId);
        }

        ResponseEntity<Object> getRequest(String path, Long userId, Map<String, Object> parameters) {
            return get(path, userId, parameters);
        }

        ResponseEntity<Object> postRequest(String path, String body) {
            return post(path, body);
        }

        ResponseEntity<Object> postRequest(String path, long userId, String body) {
            return post(path, userId, body);
        }

        ResponseEntity<Object> putRequest(String path, long userId, String body) {
            return put(path, userId, body);
        }

        ResponseEntity<Object> patchRequest(String path, String body) {
            return patch(path, body);
        }

        ResponseEntity<Object> patchRequest(String path, long userId) {
            return patch(path, userId);
        }

        ResponseEntity<Object> deleteRequest(String path) {
            return delete(path);
        }

        ResponseEntity<Object> deleteRequest(String path, long userId) {
            return delete(path, userId);
        }
    }
}
