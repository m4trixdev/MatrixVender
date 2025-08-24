package br.com.matrixdev.matrixvender.menus;

import br.com.matrixdev.matrixvender.MatrixVender;
import br.com.matrixdev.matrixvender.utils.MessageUtil;
import br.com.matrixdev.matrixvender.utils.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public class SellMenu implements Listener {

    private final MatrixVender plugin;
    private final Map<String, Double> sellableItems;

    public SellMenu(MatrixVender plugin) {
        this.plugin = plugin;
        this.sellableItems = new HashMap<String, Double>();
        loadSellableItems();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        int maxSlots = getMaxSellSlots(player);
        String title = MessageUtil.color(plugin.getConfiguration().getConfig().getString("mensagens.menu-titulo", "&8Vender"));
        Inventory inv = Bukkit.createInventory(null, 54, title);
        loadSellableItems();
        setupInventory(inv, maxSlots, player);
        player.openInventory(inv);
        SoundUtil.playMenuOpen(player);
    }

    private void setupInventory(Inventory inv, int maxSlots, Player player) {
        ItemStack blocked = createBlockedSlotItem();
        for (int i = 0; i < 54; i++) inv.setItem(i, blocked);
        inv.setItem(4, createInfoItem(player, maxSlots));
        inv.setItem(49, createHelpItem());
        inv.setItem(53, createCloseItem());
        for (int i = 9; i < 9 + maxSlots && i < 45; i++) inv.setItem(i, null);
    }

    private ItemStack createInfoItem(Player player, int maxSlots) {
        ItemStack info = new ItemStack(Material.EMERALD);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(MessageUtil.color(plugin.getConfiguration().getConfig().getString("mensagens.info-titulo", "&aMenu de Venda")));
        List<String> lore = new ArrayList<String>();
        double multiplier = getItemMultiplier(player);
        for (String line : plugin.getConfiguration().getConfig().getStringList("mensagens.info-lore")) {
            String mstr = (multiplier == (int) multiplier) ? String.valueOf((int) multiplier) : String.valueOf(multiplier);
            line = line.replace("{slots}", String.valueOf(maxSlots)).replace("{multiplier}", mstr);
            lore.add(MessageUtil.color(line));
        }
        meta.setLore(lore);
        info.setItemMeta(meta);
        return info;
    }

    private ItemStack createHelpItem() {
        ItemStack help = new ItemStack(Material.BOOK);
        ItemMeta meta = help.getItemMeta();
        meta.setDisplayName(MessageUtil.color("&eAjuda"));
        List<String> lore = new ArrayList<String>();
        lore.add(MessageUtil.color("&7Coloque itens nos slots"));
        lore.add(MessageUtil.color("&7e feche o menu para vender"));
        meta.setLore(lore);
        help.setItemMeta(meta);
        return help;
    }

    private ItemStack createCloseItem() {
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta meta = close.getItemMeta();
        meta.setDisplayName(MessageUtil.color("&cFechar Menu"));
        close.setItemMeta(meta);
        return close;
    }

    private ItemStack createBlockedSlotItem() {
        Material blockedMaterial;
        short data = 0;
        try {
            blockedMaterial = Material.valueOf("BLACK_STAINED_GLASS_PANE");
        } catch (Exception e) {
            try {
                blockedMaterial = Material.valueOf("STAINED_GLASS_PANE");
                data = 15;
            } catch (Exception ex) {
                blockedMaterial = Material.GLASS;
            }
        }
        ItemStack blocked = new ItemStack(blockedMaterial, 1, data);
        ItemMeta meta = blocked.getItemMeta();
        meta.setDisplayName(MessageUtil.color(plugin.getConfiguration().getConfig().getString("mensagens.slot-bloqueado", "&cSlot Bloqueado")));
        List<String> lore = new ArrayList<String>();
        lore.add(MessageUtil.color(plugin.getConfiguration().getConfig().getString("mensagens.slot-bloqueado-lore", "&7Adquira mais slots")));
        meta.setLore(lore);
        blocked.setItemMeta(meta);
        return blocked;
    }

    private void loadSellableItems() {
        ConfigurationSection itemsSection = plugin.getConfiguration().getConfig().getConfigurationSection("venda.itens");
        if (itemsSection == null) return;
        sellableItems.clear();
        for (String itemName : itemsSection.getKeys(false)) {
            sellableItems.put(itemName.toUpperCase(), itemsSection.getDouble(itemName, 10.0));
        }
    }

    private int getMaxSellSlots(Player player) {
        if (player.hasPermission("matrixvender.slot.40")) return 36;
        if (player.hasPermission("matrixvender.slot.30")) return 27;
        if (player.hasPermission("matrixvender.slot.20")) return 18;
        return plugin.getConfiguration().getConfig().getInt("venda.slots-padrao", 9);
    }

    private double getItemMultiplier(Player player) {
        if (player.hasPermission("matrixvender.multiplier.3.0")) return 3.0;
        if (player.hasPermission("matrixvender.multiplier.2.0")) return 2.0;
        if (player.hasPermission("matrixvender.multiplier.1.5")) return 1.5;
        if (player.hasPermission("matrixvender.multiplier.1.2")) return 1.2;
        return 1.0;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String inventoryTitle = MessageUtil.stripColors(event.getView().getTitle());
        String menuTitle = MessageUtil.stripColors(plugin.getConfiguration().getConfig().getString("mensagens.menu-titulo", "&8Vender"));
        if (!inventoryTitle.equalsIgnoreCase(menuTitle)) return;
        int slot = event.getRawSlot();
        int maxSlots = getMaxSellSlots(player);
        if (slot == 4 || slot == 49 || slot == 53) {
            event.setCancelled(true);
            if (slot == 53) player.closeInventory();
            return;
        }
        if (slot >= 9 && slot < 9 + maxSlots && slot < 45) {
            ItemStack cursorItem = event.getCursor();
            if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                if (!isSellableItem(cursorItem)) {
                    event.setCancelled(true);
                    MessageUtil.sendMessage(player, plugin.getConfiguration().getConfig().getString("mensagens.item-nao-vendavel", "&cEste item nao pode ser vendido!"));
                    return;
                }
            }
            return;
        }
        if (slot >= 54) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String inventoryTitle = MessageUtil.stripColors(event.getView().getTitle());
        String menuTitle = MessageUtil.stripColors(plugin.getConfiguration().getConfig().getString("mensagens.menu-titulo", "&8Vender"));
        if (!inventoryTitle.equalsIgnoreCase(menuTitle)) return;
        List<ItemStack> itemsToSell = new ArrayList<ItemStack>();
        List<ItemStack> itemsToReturn = new ArrayList<ItemStack>();
        int maxSlots = getMaxSellSlots(player);
        for (int i = 9; i < 9 + maxSlots && i < 45; i++) {
            ItemStack item = event.getInventory().getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                if (isSellableItem(item)) {
                    itemsToSell.add(item);
                } else {
                    itemsToReturn.add(item);
                }
            }
        }
        if (!itemsToSell.isEmpty()) sellItems(player, itemsToSell);
        if (!itemsToReturn.isEmpty()) returnItems(player, itemsToReturn);
    }

    private void sellItems(Player player, List<ItemStack> items) {
        double totalValue = 0.0;
        int totalSold = 0;
        double multiplier = getItemMultiplier(player);
        for (ItemStack item : items) {
            String itemKey = item.getType().name().toUpperCase();
            Double basePrice = sellableItems.get(itemKey);
            if (basePrice != null) {
                totalValue += basePrice * item.getAmount() * multiplier;
                totalSold += item.getAmount();
            }
        }
        if (totalSold > 0) {
            plugin.getEconomyHook().depositPlayer(player, totalValue);
            sendSaleNotification(player, totalSold, totalValue);
            SoundUtil.playSuccess(player);
        }
    }

    private void returnItems(Player player, List<ItemStack> items) {
        for (ItemStack item : items) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
        if (!items.isEmpty()) {
            String msg = plugin.getConfiguration().getConfig().getString("mensagens.itens-devolvidos", "&e{quantidade} itens nao vendaveis foram devolvidos");
            MessageUtil.sendMessage(player, msg.replace("{quantidade}", String.valueOf(items.size())));
        }
    }

    private boolean isSellableItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        String itemKey = item.getType().name().toUpperCase();
        return sellableItems.containsKey(itemKey);
    }

    private void sendSaleNotification(Player player, int quantity, double value) {
        String type = plugin.getConfiguration().getConfig().getString("notificacoes.tipo-venda", "chat");
        String formattedValue = plugin.getEconomyHook().format(value);
        if ("actionbar".equalsIgnoreCase(type)) {
            if (plugin.getConfiguration().getConfig().getBoolean("notificacoes.actionbar.ativado", false)) {
                String msg = plugin.getConfiguration().getConfig().getString("notificacoes.actionbar.formato", "&aVenda: {quantidade} itens por ${valor}");
                MessageUtil.sendActionBar(player, msg.replace("{quantidade}", String.valueOf(quantity)).replace("{valor}", formattedValue));
            }
        } else if ("title".equalsIgnoreCase(type)) {
            if (plugin.getConfiguration().getConfig().getBoolean("notificacoes.title.ativado", false)) {
                String title = plugin.getConfiguration().getConfig().getString("notificacoes.title.titulo", "&aVenda Realizada!");
                String subtitle = plugin.getConfiguration().getConfig().getString("notificacoes.title.subtitulo", "&e{quantidade} itens por ${valor}");
                player.sendTitle(MessageUtil.color(title), MessageUtil.color(subtitle.replace("{quantidade}", String.valueOf(quantity)).replace("{valor}", formattedValue)));
            }
        } else {
            if (plugin.getConfiguration().getConfig().getBoolean("notificacoes.chat.ativado", true)) {
                String msg = plugin.getConfiguration().getConfig().getString("notificacoes.chat.formato", "&aVoce vendeu {quantidade} itens por ${valor}");
                MessageUtil.sendMessage(player, msg.replace("{quantidade}", String.valueOf(quantity)).replace("{valor}", formattedValue));
            }
        }
    }
}