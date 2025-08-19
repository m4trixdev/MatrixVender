package br.com.matrixdev.matrixvender;

import br.com.matrixdev.matrixvender.commands.VenderCommand;
import br.com.matrixdev.matrixvender.hooks.EconomyHook;
import br.com.matrixdev.matrixvender.menus.SellMenu;
import br.com.matrixdev.matrixvender.utils.ConfigUtil;
import br.com.matrixdev.matrixvender.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MatrixVender extends JavaPlugin {

    private static MatrixVender instance;
    private ConfigUtil config;
    private EconomyHook economyHook;
    private SellMenu sellMenu;

    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("MatrixVender v" + getDescription().getVersion());
        getLogger().info("Autor: matrixdev");
        getLogger().info("Iniciando plugin...");
        
        saveDefaultConfig();
        
        config = new ConfigUtil(this, "config.yml");
        MessageUtil.initialize(this);
        
        getLogger().info("Configuracoes carregadas!");
        
        economyHook = new EconomyHook(this);
        sellMenu = new SellMenu(this);
        
        getLogger().info("Managers inicializados!");
        
        registerCommands();
        
        Bukkit.getScheduler().runTaskLater(this, () -> {
            economyHook.setupEconomy();
            if (!economyHook.isHooked()) {
                getLogger().warning("AVISO: Nenhum plugin de economia encontrado!");
                getLogger().warning("O plugin funcionara em modo TESTE.");
            } else {
                getLogger().info("Economia conectada com sucesso!");
                getLogger().info("Sistema: " + economyHook.getEconomyName());
            }
        }, 100L);
        
        getLogger().info("MatrixVender ativado com sucesso!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MatrixVender desativado!");
    }

    private void registerCommands() {
        try {
            VenderCommand venderCommand = new VenderCommand(this);
            getCommand("vender").setExecutor(venderCommand);
            getLogger().info("Comandos registrados!");
        } catch (Exception e) {
            getLogger().severe("Erro ao registrar comandos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static MatrixVender getInstance() {
        return instance;
    }

    public ConfigUtil getConfiguration() {
        return config;
    }

    public EconomyHook getEconomyHook() {
        return economyHook;
    }

    public SellMenu getSellMenu() {
        return sellMenu;
    }
}
