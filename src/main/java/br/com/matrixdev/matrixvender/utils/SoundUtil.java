package br.com.matrixdev.matrixvender.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtil {

    public static void playMenuOpen(Player player) {
        playSound(player, "sons.menu-abrir", "BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING");
    }

    public static void playSuccess(Player player) {
        playSound(player, "sons.venda-sucesso", "ENTITY_PLAYER_LEVELUP", "LEVEL_UP");
    }

    public static void playError(Player player) {
        playSound(player, "sons.venda-erro", "BLOCK_NOTE_BLOCK_BASS", "NOTE_BASS");
    }

    public static void playItemPlace(Player player) {
        playSound(player, "sons.item-colocado", "ENTITY_CHICKEN_EGG", "CHICKEN_EGG_POP");
    }

    public static void playMenuClose(Player player) {
        playSound(player, "sons.menu-fechar", "BLOCK_CHEST_CLOSE", "CHEST_CLOSE");
    }

    private static void playSound(Player player, String configPath, String modernSound, String legacySound) {
        try {
            Sound sound = getSound(modernSound, legacySound);
            if (sound != null) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        } catch (Exception ignored) {}
    }

    private static Sound getSound(String modernSound, String legacySound) {
        try {
            return Sound.valueOf(modernSound);
        } catch (IllegalArgumentException e) {
            try {
                return Sound.valueOf(legacySound);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }
}