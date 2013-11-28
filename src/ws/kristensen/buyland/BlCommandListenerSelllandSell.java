package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the subcommand:<br/>
 *      /sellland [Region Name]
 * <hr/>
 * Sell a region.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerSelllandSell implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerSelllandSell(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 1) {
            plugin.sendMessageInfo(sender, "Usage: /sellland [Region Name]");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();

            if (sender instanceof Player) {
                Player player = (Player)sender;
                World world = player.getWorld();

                if (player.hasPermission("buyland.sell") || player.hasPermission("buyland.all")) {
                    //sell the region
                    if (plugin.ownSellRegion(player, false,  world, argRegionName)) {
                        //optionally do something when it is sold.
                    } else {
                        //optionally do something if it is unsuccessful.
                    }
                } else {
                    plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sell.permission")));
                }
            } else {
                plugin.sendMessageInfo(sender, "Currently not available at console.");
            }            
        }

        //command was utilized.
        return true;
    }
}
