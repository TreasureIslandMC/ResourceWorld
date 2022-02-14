package me.nik.resourceworld.modules.impl.disabledcommands;

import me.nik.resourceworld.Permissions;
import me.nik.resourceworld.ResourceWorld;
import me.nik.resourceworld.files.Config;
import me.nik.resourceworld.managers.MsgType;
import me.nik.resourceworld.modules.ListenerModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

public class CommandsNether extends ListenerModule {

    public CommandsNether(ResourceWorld plugin) {
        super(Config.Setting.NETHER_DISABLED_COMMANDS_ENABLED.getBoolean(), plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void disableWorldCommands(@NotNull PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission(Permissions.ADMIN.getPermission())) return;
        if (p.getWorld().getName().equals(Config.Setting.NETHER_NAME.getString())) {
            if (e.getMessage().equals("/")) return;
            for (String cmd : Config.Setting.NETHER_DISABLED_COMMANDS_LIST.getStringList()) {
                if (e.getMessage().contains(cmd)) {
                    e.setCancelled(true);
                    p.sendMessage(MsgType.DISABLED_COMMAND.getMessage());
                }
            }
        }
    }
}