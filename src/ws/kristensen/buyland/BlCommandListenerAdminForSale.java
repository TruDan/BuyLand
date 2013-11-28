package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the Admin command:<br/>
 *      /adminbuyland forsale [Region Name]
 * <hr/>
 * This will set the land back to default. Just as if the player sold the land back.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerAdminForSale implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerAdminForSale(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 1) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /abl forsale [RegionName]");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();
            
            //See if the person requesting the information is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;
                World world = player.getWorld();

                //See if the player has permission to do the command
                if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {
                    if (plugin.ownSellRegion(player, true, world, argRegionName)) {
                        //optionally do something when it is sold.
                    } else {
                        //optionally do something if it is unsuccessful.
                    }
                }
            } else {
                //plugin.sendMessageInfo(sender, "Currently not available at console.");
            }            
        }

        //command was utilized.
        return true;
    }
}
