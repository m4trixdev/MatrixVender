package br.com.matrixdev.matrixvender.hooks;

import br.com.matrixdev.matrixvender.MatrixVender;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Logger;

public class EconomyHook {

    private final Logger logger;
    private EconomyType activeEconomy = EconomyType.NONE;
    private Object economyAPI = null;
    private Economy vaultEconomy = null;

    @FunctionalInterface
    private interface SetupTask {
        void run() throws Exception;
    }

    public EconomyHook(MatrixVender plugin) {
        this.logger = plugin.getLogger();
    }

    public void setupEconomy() {
        if (tryHook("Vault", this::setupVault)) return;
        if (tryHook("PlayerPoints", this::setupPlayerPoints)) return;
        if (tryHook("yPoints", this::setupYPoints)) return;
        if (tryHook("JH_Shop", this::setupJHShop)) return;
        if (tryHook("AtlasEconomiaSecundaria", this::setupAtlasEconomia)) return;
        if (tryHook("yCash", this::setupYCash)) return;
        if (tryHook("LegendaryEconomy", this::setupLegendaryEconomy)) return;
        if (tryHook("NextEconomy", this::setupNextEconomy)) return;
    }

    private boolean tryHook(String pluginName, SetupTask setupFunction) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin != null && plugin.isEnabled()) {
            try {
                setupFunction.run();
                return true;
            } catch (Exception e) {
                logger.severe("Falha ao ativar a integracao com " + pluginName + ".");
                e.printStackTrace();
            }
        }
        return false;
    }

    private void setupVault() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            vaultEconomy = rsp.getProvider();
            activeEconomy = EconomyType.VAULT;
        }
    }

    private void setupPlayerPoints() throws Exception {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
        economyAPI = plugin.getClass().getMethod("getAPI").invoke(plugin);
        activeEconomy = EconomyType.PLAYER_POINTS;
    }

    private void setupYPoints() throws Exception {
        economyAPI = Class.forName("yStore.api.yPointsAPI").newInstance();
        activeEconomy = EconomyType.Y_POINTS;
    }

    private void setupJHShop() throws Exception {
        economyAPI = Class.forName("com.jhshop.api.JHShopAPI").getMethod("getInstance").invoke(null);
        activeEconomy = EconomyType.JH_SHOP;
    }

    private void setupAtlasEconomia() throws Exception {
        Class<?> apiClass = Class.forName("com.atlas.economia.api.AtlasEconomyAPI");
        economyAPI = apiClass.getMethod("getInstance").invoke(null);
        activeEconomy = EconomyType.ATLAS_ECONOMIA;
    }

    private void setupYCash() throws Exception {
        Class<?> apiClass = Class.forName("ycash.api.yCashAPI");
        economyAPI = apiClass.getMethod("getInstance").invoke(null);
        activeEconomy = EconomyType.Y_CASH;
    }

    private void setupLegendaryEconomy() throws Exception {
        Class<?> apiClass = Class.forName("com.legendary.economy.api.LegendaryEconomyAPI");
        economyAPI = apiClass.getMethod("getInstance").invoke(null);
        activeEconomy = EconomyType.LEGENDARY_ECONOMY;
    }

    private void setupNextEconomy() throws Exception {
        Class<?> apiClass = Class.forName("com.nextplugins.economy.api.NextEconomyAPI");
        economyAPI = apiClass.getMethod("getInstance").invoke(null);
        activeEconomy = EconomyType.NEXT_ECONOMY;
    }

    public double getBalance(Player player) {
        try {
            switch (activeEconomy) {
                case VAULT:
                    if (vaultEconomy != null) {
                        try {
                            return vaultEconomy.getBalance(player);
                        } catch (Exception e) {
                            return vaultEconomy.getBalance(player.getName());
                        }
                    }
                    return 0.0;
                case PLAYER_POINTS:
                    Method look = economyAPI.getClass().getMethod("look", UUID.class);
                    return (int) look.invoke(economyAPI, player.getUniqueId());
                case Y_POINTS:
                    Method getPoints = economyAPI.getClass().getMethod("getPoints", String.class);
                    return (int) getPoints.invoke(economyAPI, player.getName());
                case JH_SHOP:
                    Method getBalanceJH = economyAPI.getClass().getMethod("getBalance", UUID.class);
                    return (double) getBalanceJH.invoke(economyAPI, player.getUniqueId());
                case ATLAS_ECONOMIA:
                    Method getBalanceAtlas = economyAPI.getClass().getMethod("getBalance", Player.class);
                    return (double) getBalanceAtlas.invoke(economyAPI, player);
                case Y_CASH:
                    Method getCash = economyAPI.getClass().getMethod("getCash", String.class);
                    return (double) getCash.invoke(economyAPI, player.getName());
                case LEGENDARY_ECONOMY:
                    Method getBalanceLegendary = economyAPI.getClass().getMethod("getBalance", UUID.class);
                    return (double) getBalanceLegendary.invoke(economyAPI, player.getUniqueId());
                case NEXT_ECONOMY:
                    Class<?> accountClass = Class.forName("com.nextplugins.economy.model.account.SimpleAccount");
                    Method getByPlayer = economyAPI.getClass().getMethod("getByPlayer", Player.class);
                    Object account = getByPlayer.invoke(economyAPI, player);
                    if (account != null) {
                        Method getBalanceMethod = account.getClass().getMethod("getBalance");
                        return (double) getBalanceMethod.invoke(account);
                    }
                    return 0.0;
                default:
                    return 0.0;
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    public void depositPlayer(Player player, double amount) {
        try {
            switch (activeEconomy) {
                case VAULT:
                    if (vaultEconomy != null) {
                        try {
                            vaultEconomy.depositPlayer(player, amount);
                        } catch (Exception e) {
                            vaultEconomy.depositPlayer(player.getName(), amount);
                        }
                    }
                    break;
                case PLAYER_POINTS:
                    Method give = economyAPI.getClass().getMethod("give", UUID.class, int.class);
                    give.invoke(economyAPI, player.getUniqueId(), (int) amount);
                    break;
                case Y_POINTS:
                    double currentPoints = getBalance(player);
                    Method setPoints = economyAPI.getClass().getMethod("setPoints", String.class, int.class);
                    setPoints.invoke(economyAPI, player.getName(), (int) (currentPoints + amount));
                    break;
                case JH_SHOP:
                    Method addBalanceJH = economyAPI.getClass().getMethod("addBalance", UUID.class, double.class);
                    addBalanceJH.invoke(economyAPI, player.getUniqueId(), amount);
                    break;
                case ATLAS_ECONOMIA:
                    Method addMoney = economyAPI.getClass().getMethod("addMoney", Player.class, double.class);
                    addMoney.invoke(economyAPI, player, amount);
                    break;
                case Y_CASH:
                    Method addCash = economyAPI.getClass().getMethod("addCash", String.class, double.class);
                    addCash.invoke(economyAPI, player.getName(), amount);
                    break;
                case LEGENDARY_ECONOMY:
                    Method depositLegendary = economyAPI.getClass().getMethod("deposit", UUID.class, double.class);
                    depositLegendary.invoke(economyAPI, player.getUniqueId(), amount);
                    break;
                case NEXT_ECONOMY:
                    Class<?> accountClass = Class.forName("com.nextplugins.economy.model.account.SimpleAccount");
                    Method getByPlayer = economyAPI.getClass().getMethod("getByPlayer", Player.class);
                    Object account = getByPlayer.invoke(economyAPI, player);
                    if (account != null) {
                        Method depositMethod = account.getClass().getMethod("deposit", double.class);
                        depositMethod.invoke(account, amount);
                        Method updateMethod = economyAPI.getClass().getMethod("update", accountClass);
                        updateMethod.invoke(economyAPI, account);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasBalance(Player player, double amount) {
        return getBalance(player) >= amount;
    }

    public String getEconomyName() {
        if (activeEconomy == EconomyType.VAULT && vaultEconomy != null) {
            return "Vault (" + vaultEconomy.getName() + ")";
        }
        return activeEconomy.name();
    }

    public boolean isHooked() {
        return activeEconomy != EconomyType.NONE;
    }

    public String format(double amount) {
        if (!isHooked()) return String.valueOf(amount);
        try {
            switch (activeEconomy) {
                case VAULT:
                    if (vaultEconomy != null) {
                        return vaultEconomy.format(amount);
                    }
                    break;
                default:
                    return String.format("%.2f", amount);
            }
        } catch (Exception e) {
            return String.valueOf(amount);
        }
        return String.valueOf(amount);
    }

    private enum EconomyType {
        NONE, VAULT, PLAYER_POINTS, Y_POINTS, JH_SHOP, ATLAS_ECONOMIA, Y_CASH, LEGENDARY_ECONOMY, NEXT_ECONOMY
    }
}
