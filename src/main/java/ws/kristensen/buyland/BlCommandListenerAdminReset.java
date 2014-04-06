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
 *      /adminbuyland reset [Region Name]
 * <hr/>
 * This will simply reset the Land back to when a player bought the land.<br/>
 * <br/>
 * NOTE: This will not sell back the land only reset it to default.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerAdminReset implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerAdminReset(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //make sure the correct number of args is used
        if(args.length != 1) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /abl reset [RegionName]");
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
                        plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.error1")));
                    } else {
                        //Get protected region min location
                        Location protectedRegionMinimum = new Location(world, 
                                                                       protectedRegion.getMinimumPoint().getBlockX(), 
                                                                       protectedRegion.getMinimumPoint().getBlockY(), 
                                                                       protectedRegion.getMinimumPoint().getBlockZ()
                                                                      );

                        //Reset the land to original based on config
                        if (plugin.getConfig().getBoolean("buyland.onBuyFromBank.placeSchematic") == true) {
                            plugin.worldEditPlaceSchematic(protectedRegionMinimum, argRegionName);
                            //Notify the user
                            plugin.sendMessageInfo(sender, "Region Reset to Default! Use forsale command to erase all owners and members!");
                        } else {
                            plugin.sendMessageInfo(sender, "Set buyland.onBuyFromBank.placeSchematic to true in the config file to use this feature!");
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
