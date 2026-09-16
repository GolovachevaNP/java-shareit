package ru.practicum.shareit;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.ErrorResponse;
import ru.practicum.shareit.request.dto.ItemRequestAnswerDto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GatewaySupportTest {

    // Проверка создания главного класса приложения
    @Test
    void shareItGatewayShouldBeCreated() {
        assertDoesNotThrow(ShareItGateway::new);
    }

    // Проверка ответа с ошибкой
    @Test
    void errorResponseShouldStoreMessage() {
        ErrorResponse response = new ErrorResponse("Ошибка");

        assertEquals("Ошибка", response.getError());
    }

    // Проверка обработчика ошибок валидации
    @Test
    void errorHandlerShouldHandleValidationException() {
        ErrorHandler handler = new ErrorHandler();

        ErrorResponse response = handler.handleBadRequest(new ValidationException("Ошибка"));

        assertEquals("Ошибка", response.getError());
    }

    // Проверка DTO ответа на запрос вещи
    @Test
    void itemRequestAnswerDtoShouldStoreFields() {
        ItemRequestAnswerDto answer = new ItemRequestAnswerDto(1L, "Вещь", 2L);

        assertEquals(1L, answer.getId());
        assertEquals("Вещь", answer.getName());
        assertEquals(2L, answer.getOwnerId());
    }
}