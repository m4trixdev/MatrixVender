package br.com.matrixdev.matrixvender.commands;

import br.com.matrixdev.matrixvender.MatrixVender;
import br.com.matrixdev.matrixvender.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VenderCommand implements CommandExecutor {

    private final MatrixVender plugin;

    public VenderCommand(MatrixVender plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando e apenas para jogadores.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("matrixvender.use")) {
            String noPermMessage = "&cVoce nao tem permissao para usar este comando.";
            if (!noPermMessage.trim().isEmpty()) {
                player.sendMessage(MessageUtil.color(noPermMessage));
            }
            return true;
        }

        try {
            plugin.getSellMenu().open(player);
        } catch (Exception e) {
            String errorMessage = "&cNao foi possivel abrir o menu de venda agora. Tente novamente mais tarde.";
            if (!errorMessage.trim().isEmpty()) {
                player.sendMessage(MessageUtil.color(errorMessage));
            }
            plugin.getLogger().severe("Erro ao abrir SellMenu para " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}
