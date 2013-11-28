
package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the subcommand:<br/>
 *      /buyland [Region Name]
 * <hr/>
 * Buy a region.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerBuylandBuy implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerBuylandBuy(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 1) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /buyland [RegionName]");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();

            //See if the person requesting the information is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;
                World world = player.getWorld();

                //See if the player has permission to do the command
                if (player.hasPermission("buyland.buy") || player.hasPermission("buyland.all")) {
                    if (plugin.ownBuyRegion(player, world, argRegionName)) {
                        //purchase was successful
                    } else {
                        //purchase was unsuccessful
                    }
                } else {
                    plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.buy.permission")));
                }
            } else {
                plugin.sendMessageInfo(sender, "Currently not available at console.");
            }            
        }

        //command was utilized.
        return true;
    }
}
