package dev.rabbit.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rabbit.pojo.TreeList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Mapper implements Serializable {
    public static final Mapper INSTANCE = new Mapper();
    private static final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS);

    public String toJsonString(TreeList treeList) {
        try {
            return objectMapper.writeValueAsString(treeList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T deepCopy(T t) throws JsonProcessingException {
        return (T) objectMapper.readValue(objectMapper.writeValueAsString(t), t.getClass());
    }
}
