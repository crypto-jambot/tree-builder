package dev.rabbit.pojo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Exchange implements Serializable {
    private final String name;
    private final int fee;
    private final String pool;

    public static <K, V> Exchange of(Map<K, V> exchangeMap) {
        return new Exchange(
                (String) exchangeMap.get("name"),
                Integer.parseInt((String) exchangeMap.get("fee")),
                (String) exchangeMap.get("pool"));
    }
}
