package me.nik.resourceworld.commands.subcommands;

import me.nik.resourceworld.ResourceWorld;
import me.nik.resourceworld.commands.SubCommand;
import me.nik.resourceworld.files.Lang;
import me.nik.resourceworld.utils.ColourUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Menu extends SubCommand {
    @Override
    public String getName() {
        return "Menu";
    }

    @Override
    public String getDescription() {
        return "Open the Resource World Menu!";
    }

    @Override
    public String getSyntax() {
        return "/Resource Menu";
    }

    @Override
    public void perform(Player player, String[] args) {
        Plugin plugin = ResourceWorld.getPlugin(ResourceWorld.class);
        if (!player.hasPermission("rw.admin")) {
            player.sendMessage(ColourUtils.format(Lang.get().getString("prefix")) + ColourUtils.format(Lang.get().getString("no_perm")));
        } else {
            if (args.length > 0) {
                Inventory gui = Bukkit.createInventory(player, 9, ColourUtils.format(Lang.get().getString("gui_name")));

                //Items Here

                player.openInventory(gui);
                final Player pAnonymous = player;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        InventoryView gui = pAnonymous.getOpenInventory();
                        if (!gui.getTitle().equalsIgnoreCase(ColourUtils.format(Lang.get().getString("gui_name")))) {
                            cancel();
                            return;
                        }
                        //GUI Items
                        ItemStack server = new ItemStack(Material.BEACON);
                        //GUI Item Metadata + Lore
                        ItemMeta server_meta = server.getItemMeta();
                        server_meta.setDisplayName(ChatColor.AQUA + "Server Status");
                        ArrayList<String> server_lore = new ArrayList<>();
                        server_lore.add(ChatColor.GREEN + "> System Information:");
                        server_lore.add(ChatColor.GRAY + "Server Version: " + Bukkit.getServer().getVersion());
                        server_lore.add(ChatColor.GRAY + "CPU Proccessors: " + Runtime.getRuntime().availableProcessors());
                        server_lore.add(ChatColor.GRAY + "Memory: " + Runtime.getRuntime().maxMemory() / 1024L / 1024L + "/" + Runtime.getRuntime().freeMemory() / 1024L / 1024L);
                        server_meta.setLore(server_lore);
                        server.setItemMeta(server_meta);
                        gui.setItem(2, server);
                        pAnonymous.updateInventory();
                    }
                }.runTaskTimer(ResourceWorld.getPlugin(ResourceWorld.class), 1, 10);
            }
        }
    }
}