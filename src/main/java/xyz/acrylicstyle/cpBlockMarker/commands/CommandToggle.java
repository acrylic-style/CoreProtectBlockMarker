package xyz.acrylicstyle.cpBlockMarker.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.cpBlockMarker.CPBlockMarker;
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerSubCommandExecutor;
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand;

@SubCommand(name = "toggle", usage = "/cpbm toggle", description = "BlockMarkerの機能をオン/オフします。")
public class CommandToggle extends PlayerSubCommandExecutor {
    @Override
    public void onCommand(Player player, String[] args) {
        if (CPBlockMarker.instance.inspecting.contains(player.getUniqueId())) {
            CPBlockMarker.instance.inspecting.remove(player.getUniqueId());
            CPBlockMarker.instance.clearEntities(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "BlockMarkerをオフにしました。");
        } else {
            CPBlockMarker.instance.inspecting.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "BlockMarkerをオンにしました。");
        }
    }
}
