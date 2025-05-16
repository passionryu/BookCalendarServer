package bookcalendar.server.global.ExternalConnection.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TextInput {
    @JsonProperty("text")
    private String text;

    public TextInput() {}
    public TextInput(String text) { this.text = text; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}