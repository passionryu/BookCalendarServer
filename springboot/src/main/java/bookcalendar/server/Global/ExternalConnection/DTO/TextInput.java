package bookcalendar.server.global.ExternalConnection.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // @Getter, @Setter, @NoArgsConstructor 포함
@NoArgsConstructor
@AllArgsConstructor
public class TextInput {
    private String text;
}