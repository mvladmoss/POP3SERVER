package pop3;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.SerializationUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Mailbox {

    private static final String RESOURCE_PATH = "D:\\6sem\\POP3SERVER\\src\\main\\resources";
    private static final String INVALID_MESSAGE_INDEX_EXCEPTION = "Invalid message index %d was received. There is no messages with such index";

    private List<Message> messages;
    private Set<Integer> markedMessages;
    private boolean isLocked;

    public Mailbox() {
        messages = new ArrayList<>();
        markedMessages = new LinkedHashSet<>();
    }

    public void loadFromFile(String fileName) throws IOException {
        messages = MailParser.parseMail(fileName);
    }

    public void saveToFile(String fileName) {
        MailParser.saveToFile(messages, fileName);
    }

    public void markMessageToDelete(int msgIndex) {
        if (isValidIndex(msgIndex)) {
            markedMessages.add(msgIndex);
        }
    }

    public void unmarkMessageToDelete(int msgIndex) {
        if (isValidIndex(msgIndex)) {
            markedMessages.remove(msgIndex);
        }
    }

    public boolean deleteMarkedMessages() {
        for (Integer msgIndex : markedMessages) {
            messages.remove(msgIndex - 1);
        }
        markedMessages.clear();
        return true;
    }

    public Set<Integer> getMarkedMessages() {
        return markedMessages;
    }

    public Message getMessage(int msgIndex) {
        if (isValidIndex(msgIndex)) {
            return messages.get(msgIndex - 1);
        } else {
            throw new IllegalArgumentException(String.format(INVALID_MESSAGE_INDEX_EXCEPTION, msgIndex));
        }
    }

    public int getMessageSize(int msgIndex) {
        if (isValidIndex(msgIndex)) {
            return SerializationUtils.serialize(messages.get(msgIndex - 1)).length;
        } else {
            return 0;
        }
    }

    public int getMessageCount() {
        return messages.size();
    }

    public int getMailSize() {
        int size = 0;
        for (int msgIndex = 1; msgIndex <= messages.size(); ++msgIndex) {
            size += getMessageSize(msgIndex);
        }

        return size;
    }

    public boolean isValidIndex(int msgIndex) {
        return msgIndex > 0 && msgIndex <= messages.size();
    }

    public boolean isMessageMarked(int msgIndex) {
        return markedMessages.contains(msgIndex);
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void lock() {
        isLocked = true;
    }

    public void unlock() {
        isLocked = false;
    }

    public static class MailParser {

        public static List<Message> parseMail(String fileName) throws IOException {
            String lines =
                    new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + fileName)));
            Gson gson = new Gson();
            Type type = new TypeToken<List<Message>>() {
            }.getType();
            return gson.fromJson(lines, type);
        }

        public static void saveToFile(List<Message> messages, String fileName) {
            Gson gson = new Gson();
            String json = gson.toJson(messages);
            try {
                Files.write(Paths.get(RESOURCE_PATH + fileName), json.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
