package de.rexlmanu.mlgrush.plugin.utility.fetcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.jitse.npclib.api.skin.Skin;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SkinFetcher {

  private static final String URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

  private static Map<UUID, Skin> skinMap = new ConcurrentHashMap<>();

  public static CompletableFuture<Skin> fetch(UUID uuid) {
    return CompletableFuture.supplyAsync(() -> {
      if (skinMap.containsKey(uuid)) return skinMap.get(uuid);
      try {
        StringBuilder builder = new StringBuilder();
        HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(String.format(URL, uuid.toString().replace("-", "")))).openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.connect();
        Scanner scanner = new Scanner(httpURLConnection.getInputStream());

        while (scanner.hasNextLine()) {
          builder.append(scanner.nextLine());
        }

        scanner.close();
        httpURLConnection.disconnect();
        JsonObject jsonObject = (JsonObject) (new JsonParser()).parse(builder.toString());
        JsonArray properties = jsonObject.getAsJsonArray("properties");
        JsonObject property = properties.get(0).getAsJsonObject();
        String value = property.get("value").getAsString();
        String signature = property.get("signature").getAsString();
        Skin skin = new Skin(value, signature);
        skinMap.put(uuid, skin);
        return skin;
      } catch (IOException var9) {
        Bukkit.getLogger().severe("Could not fetch skin! (Id: " + uuid.toString() + "). Message: " + var9.getMessage());
        return null;
      }
    });
  }

}
