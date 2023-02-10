package Chat;

public class ChatMessage {

  private final String username;
  private final String message;
  private final long timestamp;

  private final int type;

  public ChatMessage(String username, String message, long timestamp, int type) {
    this.username = username;
    this.message = message;
    this.timestamp = timestamp;
    this.type = type;
  }

  public String getUsername() {
    return username;
  }

  public String getMessage() {
    return message;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public int getType() {
    return type;
  }

  @Override
  public String toString() {
    return String.format("[%s]: %s", username, message);
  }
}
