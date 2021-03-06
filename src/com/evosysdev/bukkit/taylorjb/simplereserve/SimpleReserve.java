package com.evosysdev.bukkit.taylorjb.simplereserve;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Simple reserve slot plugin the Bukkit API
 * 
 * @author TJ
 * 
 */
public class SimpleReserve extends JavaPlugin
{
    /**
     * set up the block listener and Permissions on enable
     */
    public void onEnable()
    {
        loadConfig();
        
        getLogger().info(getDescription().getName() + " version " + getDescription().getVersion() + " enabled!");
    }
    
    /**
     * Load the config options from config
     */
    private void loadConfig()
    {
        // ensure config file/options valid
        validateConfig();
        
        // load
        FileConfiguration config = getConfig(); // our config
        ReserveType reserveMethod; // type of reservations
        
        try
        {
            reserveMethod = ReserveType.valueOf(config.getString("reserve.type").toUpperCase());
        }
        // config not set right(enum can't be valueOf'd)
        catch (IllegalArgumentException iae)
        {
            // config not set right, default to both
            config.set("reserve.type", "both");
            saveConfig();
            
            reserveMethod = ReserveType.valueOf(config.getString("reserve.type").toUpperCase());
        }
        
        new SimpleReserveListener(reserveMethod,
                config.getInt("reserve.full.cap", 5),
                config.getBoolean("reserve.full.reverttokick", false),
                config.getString("kick-message", "Kicked to make room for reserved user!"),
                config.getString("full-message", "The server is full!"),
                this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("simplereserve"))
        {
            if (sender.hasPermission("simplereserve"))
            {
                // reload command
                if (args.length > 0 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")))
                {
                    if (sender.hasPermission("simplereserve.reload"))
                    {
                        reloadConfig(); // reload file
                        loadConfig(); // read config
                        
                        getLogger().fine("Config reloaded.");
                        sender.sendMessage("SimpleReserve config reloaded");
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
                    }
                }
                // help command
                else
                {
                    sender.sendMessage(ChatColor.AQUA + "/" + getCommand("simplereserve").getName() + ChatColor.WHITE + " | " + ChatColor.BLUE
                            + getCommand("simplereserve").getDescription());
                    sender.sendMessage("Usage: " + ChatColor.GRAY + getCommand("simplereserve").getUsage());
                }
            }
            else
            {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Validate nodes, if they don't exist or are wrong, set them
     * and resave config
     * 
     * Unfortunately we cannot use defaults because contains will
     * return true if node set in default OR config, and we want to
     * update the config file in case it has changed from version to
     * version.
     * (thatssodumb.jpg, rage.mkv, etc etc)
     */
    private void validateConfig()
    {
        boolean updated = false;
        
        // settings
        if (!getConfig().contains("reserve.type"))
        {
            getConfig().set("reserve.type", "both");
            updated = true;
        }
        
        if (!getConfig().contains("reserve.full.cap"))
        {
            getConfig().set("reserve.full.cap", 5);
            updated = true;
        }
        
        if (!getConfig().contains("reserve.full.reverttokick"))
        {
            getConfig().set("reserve.full.reverttokick", false);
            updated = true;
        }
        
        if (!getConfig().contains("kick-message"))
        {
            getConfig().set("kick-message", "Kicked to make room for reserved user!");
            updated = true;
        }
        
        if (!getConfig().contains("full-message"))
        {
            getConfig().set("full-message", "The server is full!");
            updated = true;
        }
        
        // if nodes have been updated, update header then save
        if (updated)
        {
            // set header for information
            getConfig().options().header(
                    "Config nodes:\n" +
                            "\n" +
                            "reserve.type(enum/string): Type of reserve slots, options:\n" +
                            "    full,kick,both,none\n" +
                            "reserve.full.cap(int): Max players allowed over capacity if using 'full' method, 0 for no max\n" +
                            "reserve.full.reverttokick(boolean): Should we fall back to kick method if we reach max over capacity using full?\n" +
                            "kick-message(string): Message player will recieve when kicked to let reserve in\n" +
                            "full-message(string): Message player will recieve when unable to join full server\n" +
                            "\n" +
                            "Reserve Types Overview:\n" +
                            "-----------------------\n" +
                            "\n" +
                            "Full: Allow reserves to log on past the server limit\n" +
                            "Kick: Attempt to kick a player without kick immunity to make room\n" +
                            "Both: Both methods of reservation based on Permission\n" +
                            "    NOTE: If a player has permission for kick and full, full applies\n" +
                            "None: No reservation. Effectively disables mod without needing to remove\n" +
                            "");
            
            // save
            saveConfig();
            getLogger().info(getDescription().getName() + " config file updated, please check settings!");
        }
    }
    
    /**
     * plugin disabled
     */
    public void onDisable()
    {
        System.out.println("SimpleReserve disabled!");
    }
}
