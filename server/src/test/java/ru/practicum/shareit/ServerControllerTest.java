package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        UserController.class,
        ItemController.class,
        BookingController.class,
        ItemRequestController.class
})
class ServerControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private ItemRequestService requestService;

    // Проверка создания пользователя
    @Test
    void createUserShouldReturnOk() throws Exception {
        UserDto user = makeUser();

        when(userService.create(any(UserDto.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    // Проверка обновления пользователя
    @Test
    void updateUserShouldReturnOk() throws Exception {
        UserDto user = makeUser();

        when(userService.update(anyLong(), any(UserDto.class))).thenReturn(user);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    // Проверка получения всех пользователей
    @Test
    void findUsersShouldReturnOk() throws Exception {
        when(userService.findAll()).thenReturn(List.of(makeUser()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    // Проверка получения пользователя по id
    @Test
    void findUserByIdShouldReturnOk() throws Exception {
        when(userService.findById(1L)).thenReturn(makeUser());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    // Проверка удаления пользователя
    @Test
    void deleteUserShouldReturnOk() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    // Проверка создания вещи
    @Test
    void createItemShouldReturnOk() throws Exception {
        ItemDto item = makeItem();

        when(itemService.create(any(ItemDto.class), anyLong())).thenReturn(item);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk());
    }

    // Проверка обновления вещи
    @Test
    void updateItemShouldReturnOk() throws Exception {
        ItemDto item = makeItem();

        when(itemService.update(anyLong(), any(ItemDto.class), anyLong())).thenReturn(item);

        mockMvc.perform(patch("/items/2")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk());
    }

    // Проверка получения вещи по id
    @Test
    void findItemByIdShouldReturnOk() throws Exception {
        when(itemService.findById(2L, 1L)).thenReturn(makeItemResponse());

        mockMvc.perform(get("/items/2")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка получения вещей владельца
    @Test
    void findItemsByOwnerShouldReturnOk() throws Exception {
        when(itemService.findByOwnerId(1L)).thenReturn(List.<ItemDto>of(makeItemResponse()));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка поиска вещей
    @Test
    void searchItemsShouldReturnOk() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of(makeItem()));

        mockMvc.perform(get("/items/search")
                        .param("text", "Вещь"))
                .andExpect(status().isOk());
    }

    // Проверка добавления комментария
    @Test
    void addCommentShouldReturnOk() throws Exception {
        CommentCreateDto comment = new CommentCreateDto();
        comment.setText("Комментарий");

        when(itemService.addComment(anyLong(), anyLong(), any(CommentCreateDto.class))).thenReturn(makeComment());

        mockMvc.perform(post("/items/2/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk());
    }

    // Проверка создания бронирования
    @Test
    void createBookingShouldReturnOk() throws Exception {
        BookingCreateDto booking = makeBookingCreate();

        when(bookingService.create(any(BookingCreateDto.class), anyLong())).thenReturn(makeBooking());

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isOk());
    }

    // Проверка подтверждения бронирования
    @Test
    void approveBookingShouldReturnOk() throws Exception {
        when(bookingService.approve(3L, 1L, true)).thenReturn(makeBooking());

        mockMvc.perform(patch("/bookings/3")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    // Проверка получения бронирования по id
    @Test
    void findBookingByIdShouldReturnOk() throws Exception {
        when(bookingService.findById(3L, 1L)).thenReturn(makeBooking());

        mockMvc.perform(get("/bookings/3")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка получения бронирований пользователя
    @Test
    void findBookingsByBookerShouldReturnOk() throws Exception {
        when(bookingService.findByBooker(anyLong(), any())).thenReturn(List.of(makeBooking()));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка получения бронирований владельца
    @Test
    void findBookingsByOwnerShouldReturnOk() throws Exception {
        when(bookingService.findByOwner(anyLong(), any())).thenReturn(List.of(makeBooking()));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка создания запроса вещи
    @Test
    void createItemRequestShouldReturnOk() throws Exception {
        ItemRequestDto request = makeRequest();

        when(requestService.create(any(ItemRequestDto.class), anyLong())).thenReturn(request);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // Проверка получения своих запросов вещей
    @Test
    void findOwnRequestsShouldReturnOk() throws Exception {
        when(requestService.findOwn(1L)).thenReturn(List.of(makeRequest()));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка получения запросов других пользователей
    @Test
    void findAllRequestsShouldReturnOk() throws Exception {
        when(requestService.findAllByOtherUsers(1L)).thenReturn(List.of(makeRequest()));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка получения запроса вещи по id
    @Test
    void findRequestByIdShouldReturnOk() throws Exception {
        when(requestService.findById(2L, 1L)).thenReturn(makeRequest());

        mockMvc.perform(get("/requests/2")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    // Проверка ошибки при создании пользователя без имени
    @Test
    void createUserShouldReturnBadRequestWhenNameIsBlank() throws Exception {
        UserDto user = makeUser();
        user.setName("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    // Проверка ошибки при создании вещи без описания
    @Test
    void createItemShouldReturnBadRequestWhenDescriptionIsBlank() throws Exception {
        ItemDto item = makeItem();
        item.setDescription("");

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    // Проверка ошибки при создании комментария без текста
    @Test
    void addCommentShouldReturnBadRequestWhenTextIsBlank() throws Exception {
        CommentCreateDto comment = new CommentCreateDto();
        comment.setText("");

        mockMvc.perform(post("/items/2/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest());
    }

    // Проверка ошибки при создании запроса вещи без описания
    @Test
    void createRequestShouldReturnBadRequestWhenDescriptionIsBlank() throws Exception {
        ItemRequestDto request = makeRequest();
        request.setDescription("");

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private UserDto makeUser() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("Пользователь");
        user.setEmail("email@mail.ru");
        return user;
    }

    private ItemDto makeItem() {
        ItemDto item = new ItemDto();
        item.setId(2L);
        item.setName("Вещь");
        item.setDescription("Описание вещи");
        item.setAvailable(true);
        item.setOwnerId(1L);
        return item;
    }

    private ItemResponseDto makeItemResponse() {
        ItemResponseDto item = new ItemResponseDto();
        item.setId(2L);
        item.setName("Вещь");
        item.setDescription("Описание вещи");
        item.setAvailable(true);
        item.setOwnerId(1L);
        item.setComments(List.of(makeComment()));
        return item;
    }

    private CommentDto makeComment() {
        CommentDto comment = new CommentDto();
        comment.setId(4L);
        comment.setText("Комментарий");
        comment.setAuthorName("Пользователь");
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    private BookingCreateDto makeBookingCreate() {
        BookingCreateDto booking = new BookingCreateDto();
        booking.setItemId(2L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        return booking;
    }

    private BookingDto makeBooking() {
        BookingDto booking = new BookingDto();
        booking.setId(3L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStatus(BookingStatus.APPROVED);
        return booking;
    }

    private ItemRequestDto makeRequest() {
        ItemRequestDto request = new ItemRequestDto();
        request.setId(5L);
        request.setDescription("Заказ");
        request.setCreated(LocalDateTime.now());
        request.setItems(List.of());
        return request;
    }
}