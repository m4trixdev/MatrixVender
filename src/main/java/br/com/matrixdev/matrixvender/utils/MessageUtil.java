package br.com.matrixdev.matrixvender.utils;

import br.com.matrixdev.matrixvender.MatrixVender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class MessageUtil {

    private static MatrixVender plugin;
    private static boolean spigotAPI = false;
    private static Method sendTitleMethod;
    private static boolean titleMethodFound = false;

    public static void initialize(MatrixVender plugin) {
        MessageUtil.plugin = plugin;
        try {
            Player.class.getMethod("spigot");
            spigotAPI = true;
        } catch (NoSuchMethodException ignored) {
            spigotAPI = false;
        }

        try {
            sendTitleMethod = Player.class.getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
            titleMethodFound = true;
        } catch (NoSuchMethodException ignored) {
            titleMethodFound = false;
        }
    }

    public static String color(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String stripColors(String message) {
        if (message == null) return "";
        return ChatColor.stripColor(color(message));
    }

    public static void sendMessage(Player player, String message) {
        if (message == null || message.trim().isEmpty()) return;
        String prefix = plugin.getConfiguration().getConfig().getString("configuracao.prefixo", "");
        player.sendMessage(color(prefix + message));
    }

    public static void sendActionBar(Player player, String message) {
        if (message == null || message.trim().isEmpty()) return;
        
        try {
            Class<?> chatMessageTypeClass = Class.forName("net.md_5.bungee.api.ChatMessageType");
            Class<?> textComponentClass = Class.forName("net.md_5.bungee.api.chat.TextComponent");
            
            Object actionBarType = chatMessageTypeClass.getField("ACTION_BAR").get(null);
            Object textComponent = textComponentClass.getConstructor(String.class).newInstance(color(message));
            
            Method sendMessage = player.spigot().getClass().getMethod("sendMessage", chatMessageTypeClass, Class.forName("net.md_5.bungee.api.chat.BaseComponent"));
            sendMessage.invoke(player.spigot(), actionBarType, textComponent);
            return;
        } catch (Exception ignored) {}
        
        if (spigotAPI) {
            try {
                Class<?> textComponentClass = Class.forName("net.md_5.bungee.api.chat.TextComponent");
                Object textComponent = textComponentClass.getConstructor(String.class).newInstance(color(message));
                Method sendMessage = player.spigot().getClass().getMethod("sendMessage", Class.forName("net.md_5.bungee.api.chat.BaseComponent"));
                sendMessage.invoke(player.spigot(), textComponent);
                return;
            } catch (Exception ignored) {}
        }

        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getNMSVersion() + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object handle = craftPlayer.getClass().getMethod("getHandle").invoke(craftPlayer);
            Object connection = handle.getClass().getField("playerConnection").get(handle);

            Class<?> packetClass = Class.forName("net.minecraft.server." + getNMSVersion() + ".PacketPlayOutChat");
            Class<?> chatComponentClass = Class.forName("net.minecraft.server." + getNMSVersion() + ".IChatBaseComponent");
            Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + getNMSVersion() + ".IChatBaseComponent$ChatSerializer");

            Object chatComponent = chatSerializerClass.getMethod("a", String.class).invoke(null, "{\"text\":\"" + color(message).replace("\"", "\\\"") + "\"}");
            Object packet = packetClass.getConstructor(chatComponentClass, byte.class).newInstance(chatComponent, (byte) 2);

            connection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + getNMSVersion() + ".Packet")).invoke(connection, packet);
        } catch (Exception e) {
            player.sendMessage(color(message));
        }
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (titleMethodFound) {
            try {
                sendTitleMethod.invoke(player, color(title), color(subtitle), fadeIn, stay, fadeOut);
                return;
            } catch (Exception ignored) {}
        }

        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getNMSVersion() + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object handle = craftPlayer.getClass().getMethod("getHandle").invoke(craftPlayer);
            Object connection = handle.getClass().getField("playerConnection").get(handle);

            Class<?> packetClass = Class.forName("net.minecraft.server." + getNMSVersion() + ".PacketPlayOutTitle");
            Class<?> chatComponentClass = Class.forName("net.minecraft.server." + getNMSVersion() + ".IChatBaseComponent");
            Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + getNMSVersion() + ".IChatBaseComponent$ChatSerializer");
            Class<?> titleActionClass = Class.forName("net.minecraft.server." + getNMSVersion() + ".PacketPlayOutTitle$EnumTitleAction");

            Object titleComponent = chatSerializerClass.getMethod("a", String.class).invoke(null, "{\"text\":\"" + color(title).replace("\"", "\\\"") + "\"}");
            Object subtitleComponent = chatSerializerClass.getMethod("a", String.class).invoke(null, "{\"text\":\"" + color(subtitle).replace("\"", "\\\"") + "\"}");

            Object titlePacket = packetClass.getConstructor(titleActionClass, chatComponentClass).newInstance(titleActionClass.getEnumConstants()[0], titleComponent);
            Object subtitlePacket = packetClass.getConstructor(titleActionClass, chatComponentClass).newInstance(titleActionClass.getEnumConstants()[1], subtitleComponent);
            Object timesPacket = packetClass.getConstructor(int.class, int.class, int.class).newInstance(fadeIn, stay, fadeOut);

            Method sendPacket = connection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + getNMSVersion() + ".Packet"));
            sendPacket.invoke(connection, timesPacket);
            sendPacket.invoke(connection, titlePacket);
            sendPacket.invoke(connection, subtitlePacket);
        } catch (Exception e) {
            player.sendMessage(color(title + " - " + subtitle));
        }
    }

    private static String getNMSVersion() {
        String version = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
        return version.substring(version.lastIndexOf('.') + 1);
    }
}