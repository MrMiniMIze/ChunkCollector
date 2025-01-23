package me.minimize.chunkcollector.utils;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Here, I serialize and deserialize ItemStacks to/from Base64 strings,
 * so they can be stored easily in JSON.
 */
public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack itemStack, Type type, JsonSerializationContext context) {
        if (itemStack == null) {
            return JsonNull.INSTANCE;
        }
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             BukkitObjectOutputStream bukkitOut = new BukkitObjectOutputStream(byteOut)) {

            // I write the entire ItemStack object to a byte array.
            bukkitOut.writeObject(itemStack);
            bukkitOut.flush();

            // Then I encode it as Base64.
            return new JsonPrimitive(org.apache.commons.codec.binary.Base64.encodeBase64String(byteOut.toByteArray()));

        } catch (IOException e) {
            e.printStackTrace();
            return JsonNull.INSTANCE;
        }
    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        if (jsonElement.isJsonNull()) {
            return null;
        }
        // I decode the Base64 back into bytes, then read it as an ItemStack object.
        byte[] data = org.apache.commons.codec.binary.Base64.decodeBase64(jsonElement.getAsString());
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
             BukkitObjectInputStream bukkitIn = new BukkitObjectInputStream(byteIn)) {

            return (ItemStack) bukkitIn.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
