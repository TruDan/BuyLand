package ws.kristensen.buyland;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Handles the Admin command:<br/>
 *      /rentland [Region Name] reset
 * <hr/>
 * Resets a rentland region so that it is rentable.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerRentlandReset implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerRentlandReset(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean doResetRegion = false;

        World world = null;
        String argRegionName = null;
        
        if (sender instanceof Player) {
            Player player = (Player)sender;
            //String playerName = player.getName();
            world = player.getWorld();

            if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {    
                if(args.length != 1) {
                    plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
                    plugin.sendMessageInfo(sender, "Usage: /rentland reset [RegionName]");
                } else {
                    //Extract the passed arguments
                    argRegionName = args[0].toLowerCase();

                    doResetRegion = true;
                }
            } else {
                plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.rent.noperm")));
            }            
        } else {
            //executed from console
            if(args.length != 2) {
                plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
                plugin.sendMessageInfo(sender, "Usage: /rentland reset [RegionName] [WorldOfRegion]");
            } else {
                //Extract the passed arguments
                argRegionName = args[0].toLowerCase();
                String argWorldName = args[1].toLowerCase();
                world = Bukkit.getWorld(argWorldName);
                if (world != null) {
                    doResetRegion = true;
                } else {
                    plugin.sendMessageWarning(sender, "Invalid specified world: " + argWorldName);
                }
            }
            plugin.sendMessageInfo(sender, "Currently not available at console.");
        }
        
        if (doResetRegion) {
            //Get region manager for the world
            RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world);
            //Get the protected region
            ProtectedRegion protectedRegion = regionManager.getRegionExact(argRegionName);
            
            //make sure the region exists
            if (protectedRegion == null) {
                //Region does not exist
                plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.error1")));
            } else {
                //Set the expire time to zero so it is expired
                plugin.rentGetConfig().set("rent." + argRegionName + ".time", 0);

                //Save the config
                plugin.rentSaveConfig();
                
                //reset the expired region
                if (plugin.rentResetExpiredRegion(sender, world, argRegionName)) {
                    //Notify user it was reset
                    plugin.sendMessageInfo(sender, argRegionName + " has been reset!");
                } else {
                    //not sure why it would not be reset
                    plugin.sendMessageInfo(sender, argRegionName + " has NOT been reset!");
                }
            }
        }

        //command was utilized.
        return true;
    }
}





























