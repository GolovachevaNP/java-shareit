package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        UserController.class,
        ItemController.class,
        ItemRequestController.class,
        BookingController.class
})
class GatewayControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @MockBean
    private ItemClient itemClient;

    @MockBean
    private ItemRequestClient requestClient;

    @MockBean
    private BookingClient bookingClient;

    // Проверка создания пользователя
    @Test
    void createUserShouldReturnOk() throws Exception {
        UserDto user = makeUser("Пользователь", "email@mail.ru");
        ResponseEntity<Object> response = ResponseEntity.ok(user);
        String json = objectMapper.writeValueAsString(user);

        when(userClient.create(any(UserDto.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    // Проверка создания вещи
    @Test
    void createItemShouldReturnOk() throws Exception {
        ItemDto item = makeItem("Вещь", "Описание вещи", true);
        ResponseEntity<Object> response = ResponseEntity.ok(item);
        String json = objectMapper.writeValueAsString(item);

        when(itemClient.create(anyLong(), any(ItemDto.class))).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    // Проверка создания запроса вещи
    @Test
    void createRequestShouldReturnOk() throws Exception {
        ItemRequestDto request = new ItemRequestDto();
        request.setDescription("Нужна вещь");
        ResponseEntity<Object> response = ResponseEntity.ok(request);
        String json = objectMapper.writeValueAsString(request);

        when(requestClient.create(anyLong(), any(ItemRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    // Проверка получения запросов других пользователей
    @Test
    void findAllRequestsShouldReturnOk() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.ok("Запросы");

        when(requestClient.findAllByOtherUsers(anyLong())).thenReturn(response);

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка создания бронирования
    @Test
    void createBookingShouldReturnOk() throws Exception {
        BookItemRequestDto booking = new BookItemRequestDto();
        booking.setItemId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        ResponseEntity<Object> response = ResponseEntity.ok(booking);
        String json = objectMapper.writeValueAsString(booking);

        when(bookingClient.bookItem(anyLong(), any(BookItemRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    // Проверка обновления пользователя
    @Test
    void updateUserShouldReturnOk() throws Exception {
        UserDto user = makeUser("Пользователь", "email@mail.ru");
        when(userClient.update(anyLong(), any(UserDto.class))).thenReturn(ResponseEntity.ok(user));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    // Проверка получения списка всех пользователей
    @Test
    void findUsersShouldReturnOk() throws Exception {
        when(userClient.findAll()).thenReturn(ResponseEntity.ok("Пользователи"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    // Проверка получения пользователя по id
    @Test
    void findUserByIdShouldReturnOk() throws Exception {
        when(userClient.findById(1L)).thenReturn(ResponseEntity.ok("Пользователь"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    // Проверка удаления пользователя
    @Test
    void deleteUserShouldReturnOk() throws Exception {
        when(userClient.delete(1L)).thenReturn(ResponseEntity.ok(null));

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    // Проверка обновления вещи
    @Test
    void updateItemShouldReturnOk() throws Exception {
        ItemDto item = makeItem("Вещь", "Описание вещи", true);

        when(itemClient.update(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(item));

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk());
    }

    // Проверка получения вещи по id
    @Test
    void findItemByIdShouldReturnOk() throws Exception {
        when(itemClient.findById(1L, 1L)).thenReturn(ResponseEntity.ok("Вещь"));

        mockMvc.perform(get("/items/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка получения вещей пользователя
    @Test
    void findItemsByOwnerShouldReturnOk() throws Exception {
        when(itemClient.findByOwnerId(1L)).thenReturn(ResponseEntity.ok("Вещи"));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка поиска вещей
    @Test
    void searchItemsShouldReturnOk() throws Exception {
        when(itemClient.search("Вещь")).thenReturn(ResponseEntity.ok("Вещи"));

        mockMvc.perform(get("/items/search")
                        .param("text", "Вещь"))
                .andExpect(status().isOk());
    }

    // Проверка добавления комментария к вещи
    @Test
    void addCommentShouldReturnOk() throws Exception {
        CommentCreateDto comment = new CommentCreateDto();
        comment.setText("Комментарий");

        when(itemClient.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenReturn(ResponseEntity.ok(comment));

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk());
    }

    // Проверка получения своих запросов вещей
    @Test
    void findOwnRequestsShouldReturnOk() throws Exception {
        when(requestClient.findOwn(1L)).thenReturn(ResponseEntity.ok("Запросы"));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка получения запроса вещи по id
    @Test
    void findRequestByIdShouldReturnOk() throws Exception {
        when(requestClient.findById(1L, 1L)).thenReturn(ResponseEntity.ok("Запрос"));

        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка получения бронирований пользователя
    @Test
    void getBookingsShouldReturnOk() throws Exception {
        when(bookingClient.getBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok("Бронирования"));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка получения бронирования по id
    @Test
    void getBookingShouldReturnOk() throws Exception {
        when(bookingClient.getBooking(1L, 1L)).thenReturn(ResponseEntity.ok("Бронирование"));

        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка подтверждения бронирования
    @Test
    void approveBookingShouldReturnOk() throws Exception {
        when(bookingClient.approve(1L, 1L, true)).thenReturn(ResponseEntity.ok("Бронирование"));

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    private UserDto makeUser(String name, String email) {
        UserDto user = new UserDto();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private ItemDto makeItem(String name, String description, Boolean available) {
        ItemDto item = new ItemDto();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        return item;
    }
}