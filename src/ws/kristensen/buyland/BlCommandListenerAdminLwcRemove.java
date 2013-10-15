package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Handles the Admin command:<br/>
 *      /adminbuyland lwcremove [Region Name]
 * <hr/>
 * Removes LWC Protections for the requested region.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerAdminLwcRemove implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerAdminLwcRemove(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 1) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /abl lwcRemove [RegionName]");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();

            //See if the person requesting the information is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;
                World world = player.getWorld();

                //See if the player has permission to do the command
                if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {
                    RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world);
                    //Get the protected region
                    ProtectedRegion protectedRegion = regionManager.getRegionExact(argRegionName);

                    //make sure the region exists
                    if (protectedRegion == null) {
                        //Region does not exist.
                        plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.error1")));
                    } else {
                        //Get protected region min and max locations of the region
                        Location protectedRegionMinimum = new Location(world, 
                                                                       protectedRegion.getMinimumPoint().getBlockX(), 
                                                                       protectedRegion.getMinimumPoint().getBlockY(), 
                                                                       protectedRegion.getMinimumPoint().getBlockZ()
                                                                      );
                        Location protectedRegionMaximum = new Location(world, 
                                                                       protectedRegion.getMaximumPoint().getBlockX(), 
                                                                       protectedRegion.getMaximumPoint().getBlockY(), 
                                                                       protectedRegion.getMaximumPoint().getBlockZ()
                                                                      );

                        
                        //LWC - Remove protection from area based on config
                        if (plugin.getConfig().getBoolean("buyland.removelwcprotection") == true) {
                            plugin.LWCProtectionRemove(protectedRegionMinimum, protectedRegionMaximum);

                            //Notify user the protection was removed
                            plugin.sendMessageInfo(sender, "Removed LWCProtections from region: " + argRegionName);
                        } else {
                            plugin.sendMessageInfo(sender, "LWCProtections were not removed, you must enable it in the config.");
                            plugin.sendMessageInfo(sender, "Do not enable it if you do not have LWC installed!");
                        }
                    }
                }
            } else {
                plugin.sendMessageInfo(sender, "Currently not available at console.");
            }            
        }

        //command was utilized.
        return true;
    }
}
