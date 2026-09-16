package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    // Проверка записи бронирования
    @Test
    void bookingCreateDtoShouldSerializeToJson() throws Exception {
        BookingCreateDto booking = new BookingCreateDto();
        booking.setItemId(1L);
        booking.setStart(LocalDateTime.of(2026, 7, 3, 10, 0));
        booking.setEnd(LocalDateTime.of(2026, 7, 4, 10, 0));

        assertThat(json.write(booking)).hasJsonPathNumberValue("$.itemId");
        assertThat(json.write(booking)).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(json.write(booking)).extractingJsonPathStringValue("$.start").isEqualTo("2026-07-03T10:00:00");
        assertThat(json.write(booking)).extractingJsonPathStringValue("$.end").isEqualTo("2026-07-04T10:00:00");
    }

    // Проверка чтения бронирования
    @Test
    void bookingCreateDtoShouldDeserializeFromJson() throws Exception {
        String content = "{\"itemId\":1,\"start\":\"2026-07-03T10:00:00\",\"end\":\"2026-07-04T10:00:00\"}";

        BookingCreateDto booking = json.parseObject(content);

        assertThat(booking.getItemId()).isEqualTo(1L);
        assertThat(booking.getStart()).isEqualTo(LocalDateTime.of(2026, 7, 3, 10, 0));
        assertThat(booking.getEnd()).isEqualTo(LocalDateTime.of(2026, 7, 4, 10, 0));
    }
}