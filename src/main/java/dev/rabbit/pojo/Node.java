package dev.rabbit.pojo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Node implements Serializable {
    private final String name;
    private final String symbol;
    private final String address;
    private final int decimals;

    public static <K, V> Node of(Map<K, V> nodeMap) {
        return new Node(
                (String) nodeMap.get("name"),
                (String) nodeMap.get("symbol"),
                (String) nodeMap.get("address"),
                nodeMap.get("decimals") != null ? Integer.parseInt((String) nodeMap.get("decimals")) : 0);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("symbol", symbol);
        map.put("address", address);
        map.put("decimals", decimals);
        return map;
    }
}
