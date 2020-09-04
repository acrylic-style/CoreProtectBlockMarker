package xyz.acrylicstyle.cpBlockMarker.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.acrylicstyle.tomeito_api.utils.TabCompleterHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CPBMTabCompleter extends TabCompleterHelper implements TabCompleter {
    private static final List<String> commands = Arrays.asList("toggle", "integration");

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
        if (args.length == 0) return commands;
        if (args.length == 1) return filterArgsList(commands, args[0]);
        return Collections.emptyList();
    }
}
