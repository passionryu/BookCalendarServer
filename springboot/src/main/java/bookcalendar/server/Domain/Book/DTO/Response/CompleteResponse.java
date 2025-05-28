package bookcalendar.server.Domain.Book.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompleteResponse {
    private String bookName;
    private String author;
    private String reason;
    private String url;
}
