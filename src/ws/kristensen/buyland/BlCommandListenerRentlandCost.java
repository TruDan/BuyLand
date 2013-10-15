package ws.kristensen.buyland;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.managers.RegionManager;

/**
 * Handles the command:<br/>
 *      /rentland [Region Name] cost
 * <hr/>
 * Shows the cost of a region.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerRentlandCost implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerRentlandCost(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 1) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /rentland cost [RegionName]");
            plugin.sendMessageInfo(sender, "Usage: /rentland [RegionName] cost");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();

            //See if the person requesting the information is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;
                World world = player.getWorld();

                //See if the player has permission to this command
                if (player.hasPermission("buyland.rent") || player.hasPermission("buyland.all")) {    
                    RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world);
                    if (regionManager.getRegionExact(argRegionName) == null) {
                        //Region does not exist
                        plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.error1")));
                    } else {
                        if (plugin.getRentConfig().contains("rent." + argRegionName + ".rentable")) {
                            //this is rentland
                            double s = plugin.getRentConfig().getDouble("rent." + argRegionName +".costpermin") / 2;
                            double m = plugin.getRentConfig().getDouble("rent." + argRegionName +".costpermin");
                            double h = plugin.getRentConfig().getDouble("rent." + argRegionName +".costpermin") * 60;
                            double d = plugin.getRentConfig().getDouble("rent." + argRegionName +".costpermin") * 1440;

                            plugin.sendMessageInfo(sender, "The rent of " + argRegionName + " is: ");
                            plugin.sendMessageInfo(sender, "1 Second = " + s);
                            plugin.sendMessageInfo(sender, "1 Minute = " + m);
                            plugin.sendMessageInfo(sender, "1 Hour = " + h);
                            plugin.sendMessageInfo(sender, "1 Day = " + d);
                        } else {
                            //this is buyland... call that command instead
                            Bukkit.dispatchCommand(sender, "priceland " + argRegionName);
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
