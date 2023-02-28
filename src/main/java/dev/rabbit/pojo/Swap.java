package dev.rabbit.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
@SuppressWarnings("unchecked")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Swap implements Serializable {
    @JsonProperty
    private List<Exchange> exchanges;
    private final boolean ordered;

    public static <K, V> Swap of(Map<K, V> swapMap) {
        List<Exchange> exchanges = ((List<Map<String, Object>>) swapMap.get("exchanges")).stream().map(Exchange::of).collect(Collectors.toList());
        return new Swap(exchanges, (boolean) swapMap.get("ordered"));
    }

    public boolean containsPool(String pool) {
        return this.getExchanges().stream().anyMatch(exchange -> exchange.getPool().equalsIgnoreCase(pool));
    }

    public Swap trim(String pool) {
        this.exchanges = this.getExchanges().stream().filter(exchange -> exchange.getPool().equalsIgnoreCase(pool)).collect(Collectors.toList());
        return this;
    }
}
