package me.nik.resourceworld.modules.impl.entityspawning;

import me.nik.resourceworld.ResourceWorld;
import me.nik.resourceworld.files.Config;
import me.nik.resourceworld.modules.ListenerModule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.jetbrains.annotations.NotNull;

public class EntitySpawningNether extends ListenerModule {

    public EntitySpawningNether(ResourceWorld plugin) {
        super(Config.Setting.NETHER_DISABLE_ENTITY_SPAWNING.getBoolean(), plugin);
    }

    @EventHandler
    public void onEntitySpawn(@NotNull CreatureSpawnEvent e) {
        LivingEntity entity = e.getEntity();
        if (entity instanceof Player) return;
        if (!entity.getWorld().getName().equals(Config.Setting.NETHER_NAME.getString())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onChunkLoad(@NotNull ChunkLoadEvent e) {
        if (!e.getWorld().getName().equals(Config.Setting.NETHER_NAME.getString())) return;
        for (Entity entity : e.getChunk().getEntities()) {
            if (!(entity instanceof LivingEntity ent)) continue;
            if (ent instanceof Player) continue;
            ent.remove();
        }
    }
}