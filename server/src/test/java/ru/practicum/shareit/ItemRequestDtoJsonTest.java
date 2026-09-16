package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    // Проверка записи запроса вещи в JSON с ответами
    @Test
    void itemRequestDtoShouldSerializeToJson() throws Exception {
        ItemRequestAnswerDto answer = new ItemRequestAnswerDto(2L, "Вещь", 1L);

        ItemRequestDto request = new ItemRequestDto();
        request.setId(1L);
        request.setDescription("Нужна вещь");
        request.setCreated(LocalDateTime.of(2026, 7, 2, 12, 0));
        request.setItems(List.of(answer));

        assertThat(json.write(request)).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json.write(request)).extractingJsonPathStringValue("$.description").isEqualTo("Нужна вещь");
        assertThat(json.write(request)).extractingJsonPathStringValue("$.created").isEqualTo("2026-07-02T12:00:00");
        assertThat(json.write(request)).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Вещь");
    }

    // Проверка чтения запроса вещи из JSON с ответами
    @Test
    void itemRequestDtoShouldDeserializeFromJson() throws Exception {
        String content = "{\"id\":1,\"description\":\"Нужна вещь\","
                + "\"created\":\"2026-07-02T12:00:00\",\"items\":[]}";

        ItemRequestDto request = json.parseObject(content);

        assertThat(request.getId()).isEqualTo(1L);
        assertThat(request.getDescription()).isEqualTo("Нужна вещь");
        assertThat(request.getCreated()).isEqualTo(LocalDateTime.of(2026, 7, 2, 12, 0));
        assertThat(request.getItems()).isEmpty();
    }
}