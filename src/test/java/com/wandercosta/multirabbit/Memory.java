package com.wandercosta.multirabbit;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class Memory {

    private final Map<String, Object> memory = new HashMap<>();

    public void put(final String key, final Object object) {
        this.memory.put(key, object);
    }

    public Object get(final String key) {
        return this.memory.get(key);
    }

    public void clear() {
        this.memory.clear();
    }
}