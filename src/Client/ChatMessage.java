package Client;

class ChatMessage {
  private final String username;
  private final String message;
  private final int type;

  public ChatMessage(String username, String message, int type) {
    this.username = username;
    this.message = message;
    this.type = type;
  }

  public String getUsername() {
    return username;
  }

  public String getMessage() {
    return message;
  }

  public int getType() {
    return type;
  }

  @Override
  public String toString() {
    return String.format("[%s]: %s", username, message);
  }
}
