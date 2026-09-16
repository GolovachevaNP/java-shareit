package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GatewayClientTest {
    private static final String SERVER_URL = "http://localhost:9090";
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private MockServerRestTemplateCustomizer customizer;
    private RestTemplateBuilder builder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        customizer = new MockServerRestTemplateCustomizer();
        builder = new RestTemplateBuilder(customizer);
    }

    // Проверка отправки запроса на создание пользователя
    @Test
    void createUserShouldSendRequestToServer() {
        UserClient userClient = new UserClient(SERVER_URL, builder);
        expect(HttpMethod.POST, "/users");

        assertEquals(HttpStatus.OK, userClient.create(makeUser()).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на обновление пользователя
    @Test
    void updateUserShouldSendRequestToServer() {
        UserClient userClient = new UserClient(SERVER_URL, builder);
        expect(HttpMethod.PATCH, "/users/1");

        assertEquals(HttpStatus.OK, userClient.update(1L, makeUser()).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на получение пользователей
    @Test
    void findUsersShouldSendRequestToServer() {
        UserClient userClient = new UserClient(SERVER_URL, builder);
        expect(HttpMethod.GET, "/users");

        assertEquals(HttpStatus.OK, userClient.findAll().getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на получение пользователя по id
    @Test
    void findUserByIdShouldSendRequestToServer() {
        UserClient userClient = new UserClient(SERVER_URL, builder);
        expect(HttpMethod.GET, "/users/1");

        assertEquals(HttpStatus.OK, userClient.findById(1L).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на удаление пользователя
    @Test
    void deleteUserShouldSendRequestToServer() {
        UserClient userClient = new UserClient(SERVER_URL, builder);
        expect(HttpMethod.DELETE, "/users/1");

        assertEquals(HttpStatus.OK, userClient.delete(1L).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на создание вещи
    @Test
    void createItemShouldSendRequestToServer() {
        ItemClient itemClient = new ItemClient(SERVER_URL, builder);
        expectWithUser(HttpMethod.POST, "/items", 1L);

        assertEquals(HttpStatus.OK, itemClient.create(1L, makeItem()).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на обновление вещи
    @Test
    void updateItemShouldSendRequestToServer() {
        ItemClient itemClient = new ItemClient(SERVER_URL, builder);
        expectWithUser(HttpMethod.PATCH, "/items/2", 1L);

        assertEquals(HttpStatus.OK, itemClient.update(1L, 2L, makeItem()).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на получение вещи
    @Test
    void findItemByIdShouldSendRequestToServer() {
        ItemClient itemClient = new ItemClient(SERVER_URL, builder);
        expectWithUser(HttpMethod.GET, "/items/2", 1L);

        assertEquals(HttpStatus.OK, itemClient.findById(1L, 2L).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на получение вещей пользователя
    @Test
    void findItemsByOwnerShouldSendRequestToServer() {
        ItemClient itemClient = new ItemClient(SERVER_URL, builder);
        expectWithUser(HttpMethod.GET, "/items", 1L);

        assertEquals(HttpStatus.OK, itemClient.findByOwnerId(1L).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на поиск вещей
    @Test
    void searchItemsShouldSendRequestToServer() {
        ItemClient itemClient = new ItemClient(SERVER_URL, builder);
        prepareServer();
        server.expect(requestToUriTemplate(SERVER_URL + "/items/search?text={text}", "Вещь"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertEquals(HttpStatus.OK, itemClient.search("Вещь").getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на добавление комментария
    @Test
    void addCommentShouldSendRequestToServer() {
        ItemClient itemClient = new ItemClient(SERVER_URL, builder);
        CommentCreateDto comment = new CommentCreateDto();
        comment.setText("Комментарий");
        expectWithUser(HttpMethod.POST, "/items/2/comment", 1L);

        assertEquals(HttpStatus.OK, itemClient.addComment(1L, 2L, comment).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на создание бронирования
    @Test
    void createBookingShouldSendRequestToServer() {
        BookingClient bookingClient = new BookingClient(SERVER_URL, builder);
        expectWithUser(HttpMethod.POST, "/bookings", 1L);

        assertEquals(HttpStatus.OK, bookingClient.bookItem(1L, makeBooking()).getStatusCode());

        server.verify();
    }

    // Проверка получения бронирований пользователя
    @Test
    void findBookingsShouldSendRequestToServer() {
        BookingClient bookingClient = new BookingClient(SERVER_URL, builder);
        expectWithUser(HttpMethod.GET, "/bookings?state=ALL&from=0&size=10", 1L);

        assertEquals(HttpStatus.OK,
                bookingClient.getBookings(1L, BookingState.ALL, 0, 10).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на получение бронирования по id
    @Test
    void findBookingByIdShouldSendRequestToServer() {
        BookingClient bookingClient = new BookingClient(SERVER_URL, builder);
        expectWithUser(HttpMethod.GET, "/bookings/3", 1L);

        assertEquals(HttpStatus.OK, bookingClient.getBooking(1L, 3L).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на подтверждение бронирования
    @Test
    void approveBookingShouldSendRequestToServer() {
        BookingClient bookingClient = new BookingClient(SERVER_URL, builder);
        expectWithUser(HttpMethod.PATCH, "/bookings/3?approved=true", 1L);

        assertEquals(HttpStatus.OK, bookingClient.approve(1L, 3L, true).getStatusCode());

        server.verify();
    }

    // Проверка получения бронирований владельца
    @Test
    void findOwnerBookingsShouldSendRequestToServer() {
        BookingClient bookingClient = new BookingClient(SERVER_URL, builder);
        expectWithUser(HttpMethod.GET, "/bookings/owner?state=WAITING&from=0&size=5", 1L);

        assertEquals(HttpStatus.OK,
                bookingClient.getOwnerBookings(1L, BookingState.WAITING, 0, 5).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на создание запроса вещи
    @Test
    void createItemRequestShouldSendRequestToServer() {
        ItemRequestClient requestClient = new ItemRequestClient(SERVER_URL, builder);
        expectWithUser(HttpMethod.POST, "/requests", 1L);

        assertEquals(HttpStatus.OK, requestClient.create(1L, makeRequest()).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на получение своих запросов вещей
    @Test
    void findOwnRequestsShouldSendRequestToServer() {
        ItemRequestClient requestClient = new ItemRequestClient(SERVER_URL, builder);
        expectWithUser(HttpMethod.GET, "/requests", 1L);

        assertEquals(HttpStatus.OK, requestClient.findOwn(1L).getStatusCode());

        server.verify();
    }

    // Проверка получения запросов других пользователей
    @Test
    void findAllRequestsShouldSendRequestToServer() {
        ItemRequestClient requestClient = new ItemRequestClient(SERVER_URL, builder);
        expectWithUser(HttpMethod.GET, "/requests/all", 1L);

        assertEquals(HttpStatus.OK, requestClient.findAllByOtherUsers(1L).getStatusCode());

        server.verify();
    }

    // Проверка отправки запроса на получение запроса вещи по id
    @Test
    void findRequestByIdShouldSendRequestToServer() {
        ItemRequestClient requestClient = new ItemRequestClient(SERVER_URL, builder);
        expectWithUser(HttpMethod.GET, "/requests/2", 1L);

        assertEquals(HttpStatus.OK, requestClient.findById(1L, 2L).getStatusCode());

        server.verify();
    }

    // Проверка возврата ошибки, полученной от сервера
    @Test
    void clientShouldReturnServerErrorStatus() {
        UserClient userClient = new UserClient(SERVER_URL, builder);
        prepareServer();
        server.expect(requestTo(SERVER_URL + "/users/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"Ошибка\"}"));

        assertEquals(HttpStatus.NOT_FOUND, userClient.findById(999L).getStatusCode());

        server.verify();
    }

    private void expect(HttpMethod method, String path) {
        prepareServer();
        server.expect(requestTo(SERVER_URL + path))
                .andExpect(method(method))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
    }

    private void expectWithUser(HttpMethod method, String path, Long userId) {
        prepareServer();
        server.expect(requestTo(SERVER_URL + path))
                .andExpect(method(method))
                .andExpect(header(USER_ID_HEADER, String.valueOf(userId)))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
    }

    private void prepareServer() {
        if (server == null) {
            server = customizer.getServer();
        }
    }

    private UserDto makeUser() {
        UserDto user = new UserDto();
        user.setName("Пользователь");
        user.setEmail("email@mail.ru");
        return user;
    }

    private ItemDto makeItem() {
        ItemDto item = new ItemDto();
        item.setName("Вещь");
        item.setDescription("Описание вещи");
        item.setAvailable(true);
        return item;
    }

    private BookItemRequestDto makeBooking() {
        BookItemRequestDto booking = new BookItemRequestDto();
        booking.setItemId(2L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        return booking;
    }

    private ItemRequestDto makeRequest() {
        ItemRequestDto request = new ItemRequestDto();
        request.setDescription("Заказ");
        return request;
    }
}