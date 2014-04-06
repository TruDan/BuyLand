package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.selections.Selection;

/**
 * Handles the Admin command:<br/>
 *      /rentland save [Region Name]
 * <hr/>
 * Select your cuboid using WorldEdit then run this command to setup a RentLand region.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerRentlandSave implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerRentlandSave(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 1) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /rentland save [RegionName]");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();

            if (sender instanceof Player) {
                Player player = (Player)sender;

                if (player.hasPermission("buyland.rent") || player.hasPermission("buyland.all")) {    
                    //Get the selected worldedit region
                    Selection worldEditSelection = BuyLand.worldEditGetSelectionOfPlayer(player);
                    //make sure we have a player selection
                    if (worldEditSelection == null) {
                        plugin.sendMessageWarning(sender, "Select a worldedit region first.");
                    } else {
                        //Add region to manager
                        plugin.protectedRegionAdd(worldEditSelection.getMinimumPoint(), worldEditSelection.getMaximumPoint(), argRegionName, "rent", player);

                        //Notify player
                        plugin.sendMessageInfo(sender, "Region Added!");
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
