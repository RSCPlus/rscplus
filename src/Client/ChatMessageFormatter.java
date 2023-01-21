package Client;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMessageFormatter {
  public static String removeTagsFromMessage(String message) {
    ArrayList<String> tags = ChatColors.getColorTags();
    Pattern pattern = Pattern.compile(String.join("|", tags), Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(message);
    message = matcher.replaceAll("");

    return message;
  }
}
