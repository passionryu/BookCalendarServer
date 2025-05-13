package bookcalendar.server.Domain.Community.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse{

    private Integer postId;
    private Integer memberId;
    private String author;
    private String title;
    private String contents;
    private Integer rank;
    private Integer reviewCount;
    private Boolean clicked;

}
