package xyz.acrylicstyle.cpBlockMarker.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.cpBlockMarker.CPBlockMarker;
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerSubCommandExecutor;
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand;

@SubCommand(name = "integration", usage = "/cpbm integration", description = "CoreProtect連携をオン/オフします。")
public class CommandIntegration extends PlayerSubCommandExecutor {
    @Override
    public void onCommand(Player player, String[] args) {
        if (CPBlockMarker.instance.integration.contains(player.getUniqueId())) {
            CPBlockMarker.instance.integration.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "CoreProtect連携をオフにしました。");
        } else {
            CPBlockMarker.instance.integration.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "CoreProtect連携をオンにしました。");
        }
    }
}
