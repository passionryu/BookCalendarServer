package bookcalendar.server.global.ExternalConnection.DTO;

public class TextInput {
    private String text;

    public TextInput(String text) {
        this.text = text;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
