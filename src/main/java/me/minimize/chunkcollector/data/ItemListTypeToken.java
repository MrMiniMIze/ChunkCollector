package me.minimize.chunkcollector.data;

import com.google.gson.reflect.TypeToken;
import me.minimize.chunkcollector.collectors.ChunkCollectorEntity;

import java.lang.reflect.Type;
import java.util.List;

/**
 * I made this class to provide a TypeToken for "List<ChunkCollectorEntity>"
 * so Gson can properly deserialize it.
 */
public class ItemListTypeToken {
    private static final Type TYPE = new TypeToken<List<ChunkCollectorEntity>>() {}.getType();

    public static Type getToken() {
        return TYPE;
    }
}
