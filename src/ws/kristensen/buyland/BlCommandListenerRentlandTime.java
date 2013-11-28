package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.managers.RegionManager;

/**
 * Handles the command:<br/>
 *      /rentland [Region Name] time
 * <hr/>
 * Displays time left for a rented region.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerRentlandTime implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerRentlandTime(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 1) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /rentland time [RegionName]");
            plugin.sendMessageInfo(sender, "Usage: /rentland [RegionName] time");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();

            if (sender instanceof Player) {
                Player player = (Player)sender;
                World world = player.getWorld();

                if (player.hasPermission("buyland.rent") || player.hasPermission("buyland.all")) {    
                    RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world);
                    if (regionManager.getRegionExact(argRegionName) == null) {
                        //Region does not exist
                        plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.error1")));
                        //player.sendMessage("args0 " + argRegionName);
                    } else {
                        if (plugin.rentGetConfig().contains("rent." + argRegionName + ".time")) {
                            long end = plugin.rentGetConfig().getLong("rent." + argRegionName + ".time");
                            long start = System.currentTimeMillis();

                            if (start > end) {
                                plugin.sendMessageInfo(sender, "Time left for " + argRegionName + ": 0 - This land is rentable!");
                            } else {
                                plugin.sendMessageInfo(sender, "Time left for " + argRegionName + ": " + BuyLand.elapsedTimeToString(start, end));
                            }
                        } else {
                            plugin.sendMessageInfo(sender, plugin.languageGetConfig().getString("buyland.rent.error2"));
                        }
                    }
                } else {
                    plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.rent.noperm")));
                }
            } else {
                plugin.sendMessageInfo(sender, "Currently not available at console.");
            }            
        }

        //command was utilized.
        return true;
    }
}
