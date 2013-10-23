package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.selections.Selection;

/**
 * Handles the Admin command:<br/>
 *      /adminbuyland save [Region Name]
 * <hr/>
 * Select your cuboid using WorldEdit then run this command to save a new region with buyable set to true automatically.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerAdminSave implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerAdminSave(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 1) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /abl save [RegionName]");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();

            //See if the person requesting the information is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;

                //See if the player has permission to do the command
                if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {    
                    //Get the selected worldedit region
                    Selection worldEditSelection = BuyLand.getWorldEditSelectionOfPlayer(player);
                    //make sure we have a player selection
                    if (worldEditSelection == null) {
                        plugin.sendMessageWarning(sender, "Select a worldedit region first.");
                    } else {
                        //Add region to manager
                        plugin.AddProtectedRegion(worldEditSelection.getMinimumPoint(), worldEditSelection.getMaximumPoint(), argRegionName, "buy", player);

                        //Notify player
                        plugin.sendMessageInfo(sender, "Region Added!");
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
