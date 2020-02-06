package pop3;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServerEvent {

    private EventType type;
    private List<String> arguments;
    private Instant time;

    public static ServerEvent of(EventType type, Instant time) {
        return ServerEvent.builder()
                .type(type)
                .arguments(new ArrayList<>())
                .time(time)
                .build();
    }

    public static ServerEvent of(EventType type, String arg, Instant time) {
        return ServerEvent.builder()
                .type(type)
                .arguments(Collections.singletonList(arg))
                .time(time)
                .build();
    }

    public void addArgument(String argument) {
        arguments.add(argument);
    }
}
