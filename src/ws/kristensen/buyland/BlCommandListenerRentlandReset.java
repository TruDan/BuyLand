package ws.kristensen.buyland;

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
        if(args.length != 1) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /rentland reset [RegionName]");
            plugin.sendMessageInfo(sender, "Usage: /rentland [RegionName] reset");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();

            if (sender instanceof Player) {
                Player player = (Player)sender;
                //String playerName = player.getName();
                World world = player.getWorld();

                if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {    
                    RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world);
                    //Get the protected region
                    ProtectedRegion protectedRegion = regionManager.getRegionExact(argRegionName);
                    
                    //make sure the region exists
                    if (protectedRegion == null) {
                        //Region does not exist
                        plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.error1")));
                    } else {
                        //Set the expire time to zero so it is expired
                        plugin.getRentConfig().set("rent." + argRegionName + ".time", 0);

                        //Save the config
                        plugin.saveRentConfig();
                        
                        //reset the expired region
                        if (plugin.resetExpiredRentedRegion(sender, world, argRegionName)) {
                            //Notify user it was reset
                            plugin.sendMessageInfo(sender, argRegionName + " has been reset!");
                        } else {
                            //not sure why it would not be reset
                            plugin.sendMessageInfo(sender, argRegionName + " has NOT been reset!");
                        }
                    }
                } else {
                    plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.rent.noperm")));
                }
            } else {
                plugin.sendMessageInfo(sender, "Currently not available at console.");
            }            
        }

        //command was utilized.
        return true;
    }
}
