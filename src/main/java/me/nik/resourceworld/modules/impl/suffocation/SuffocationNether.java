package me.nik.resourceworld.modules.impl.suffocation;

import me.nik.resourceworld.ResourceWorld;
import me.nik.resourceworld.files.Config;
import me.nik.resourceworld.modules.ListenerModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class SuffocationNether extends ListenerModule {

    public SuffocationNether(ResourceWorld plugin) {
        super(Config.Setting.NETHER_DISABLE_SUFFOCATION.getBoolean(), plugin);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.SUFFOCATION) return;
        String world = p.getWorld().getName();
        if (!world.equals(Config.Setting.NETHER_NAME.getString())) return;
        e.setCancelled(true);
    }
}