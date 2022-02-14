package me.nik.resourceworld.utils;

import me.nik.resourceworld.ResourceWorld;
import me.nik.resourceworld.managers.custom.ResourceWorldException;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public final class TaskUtils {

    private TaskUtils() {
        throw new ResourceWorldException("This is a static class dummy!");
    }

    public static @NotNull BukkitTask taskLater(Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskLater(ResourceWorld.getInstance(), runnable, delay);
    }

    public static @NotNull BukkitTask taskLaterAsync(Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(ResourceWorld.getInstance(), runnable, delay);
    }
}