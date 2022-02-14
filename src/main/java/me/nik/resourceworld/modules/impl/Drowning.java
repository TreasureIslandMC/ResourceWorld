package me.nik.resourceworld.modules.impl;

import me.nik.resourceworld.ResourceWorld;
import me.nik.resourceworld.files.Config;
import me.nik.resourceworld.modules.ListenerModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class Drowning extends ListenerModule {
    public Drowning(ResourceWorld plugin) {
        super(Config.Setting.WORLD_DISABLE_DROWNING.getBoolean(), plugin);
    }

    @EventHandler
    public void onDamage(@NotNull EntityDamageEvent e) {
        if (!(e.getEntity() instanceof final Player player))
            return;
        if (e.getCause() != EntityDamageEvent.DamageCause.DROWNING)
            return;
        final String world = player.getWorld().getName();
        if (!world.equals(Config.Setting.WORLD_NAME.getString()))
            return;
        e.setCancelled(true);
    }
}