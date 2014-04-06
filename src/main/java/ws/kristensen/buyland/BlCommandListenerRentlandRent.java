package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the command:<br/>
 *      /rentland [Region Name] [Time Quantity] [Sec/Min/Hr/Day/Wk]
 * <hr/>
 * Rents a region.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerRentlandRent implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerRentlandRent(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 3) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /rentland [RegionName] [TimeQuantity] [Sec/Min/Hr/Day/Wk]");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();
            String argTimeQuantity = args[1];
            String argTimeType = args[2];

            //See if the person requesting the action is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;
                World world = player.getWorld();

                //See if the player has permission to do the command
                if (player.hasPermission("buyland.rent") || player.hasPermission("buyland.all")) {
                    if (plugin.rentRegion(player, world, argRegionName, Long.valueOf(argTimeQuantity), argTimeType)) {
                        //region rented or extended
                    } else {
                        //region not rented or extended
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
