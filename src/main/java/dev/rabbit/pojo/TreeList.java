package dev.rabbit.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.List;

@JsonPropertyOrder({"pool", "trees"})
@AllArgsConstructor
public class TreeList implements Serializable {
    private final String pool;
    private final boolean ordered;
    @JsonProperty
    private final List<Tree> trees;

    public static TreeList of(String pool, boolean ordered, List<Tree> trees) {
        return new TreeList(pool, ordered, trees);
    }

    @JsonProperty("_id")
    public String getPool() {
        return pool + ordered;
    }
}