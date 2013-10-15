package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the command:<br/>
 *      /rentland<br/>
 * <br/>
 * Redirects the Admin subcommands:<br/>      
 *      /rentland save [Region Name]<br/>
 *      /rentland reset [Region Name]<br/>
 * <br/>
 * Redirects the subcommands:<br/>      
 *      /rentland addmember [Region Name] [Player Name]<br/>
 *      /rentland removemember [Region Name] [Player Name]<br/>
 *      /rentland cost [Region Name]<br/>
 *      /rentland time [Region Name]<br/>
 *      /rentland [Region Name] [TimeQuantity] [Sec/Min/Hour/Day]<br/>
 * <hr/>
 * This will redirect to the class that handles the specific sub-command.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerRentland implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerRentland(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean showCommands = false;
        
        if (sender instanceof Player) {
            Player player = (Player)sender;

            if (args.length > 0) {
                //Handle commands where the indicator is the 1st word
                if (args[0].equalsIgnoreCase("addmember"))    { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerRentlandMemberAdd(plugin).onCommand(sender, command, label, args); }
                if (args[0].equalsIgnoreCase("removemember")) { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerRentlandMemberRemove(plugin).onCommand(sender, command, label, args); }
                if (args[0].equalsIgnoreCase("save"))         { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerRentlandSave(plugin).onCommand(sender, command, label, args); }
                if (args[0].equalsIgnoreCase("cost"))         { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerRentlandCost(plugin).onCommand(sender, command, label, args); }                    
                if (args[0].equalsIgnoreCase("reset"))        { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerRentlandReset(plugin).onCommand(sender, command, label, args); }                    
                if (args[0].equalsIgnoreCase("time"))         { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerRentlandTime(plugin).onCommand(sender, command, label, args); }                    
                
                //Handle commands where the indicator is in the 2nd word
                //The following 3 are deprecated but left in for compatability
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("cost"))     { args = plugin.removeItemFromArgs(args, 1); return new BlCommandListenerRentlandCost(plugin).onCommand(sender, command, label, args); }                    
                    if (args[1].equalsIgnoreCase("reset"))    { args = plugin.removeItemFromArgs(args, 1); return new BlCommandListenerRentlandReset(plugin).onCommand(sender, command, label, args); }                    
                    if (args[1].equalsIgnoreCase("time"))     { args = plugin.removeItemFromArgs(args, 1); return new BlCommandListenerRentlandTime(plugin).onCommand(sender, command, label, args); }                    
                }

                //Handle commands where the indicator is after the 2nd word
                return new BlCommandListenerRentlandRent(plugin).onCommand(sender, command, label, args);
            } else {
                if (player.hasPermission("buyland.rent") || player.hasPermission("buyland.all")) {
                    showCommands = true;
                }
            }
        } else {
            //at console
            showCommands = true;
        }
        
        //Show commands for Rentland
        if (showCommands) {
            plugin.sendMessageInfo(sender, " ", false);
            plugin.sendMessageInfo(sender, ChatColor.YELLOW + "Rentland Commands", false);
            plugin.sendMessageInfo(sender, "/rentland cost [RegionName] - Check cost of rentable region.", false);
            plugin.sendMessageInfo(sender, "/rentland time [RegionName] - Check time left of a rented region.", false);
            plugin.sendMessageInfo(sender, "/rentland [RegionName] [TimeQuantity] [Sec/Min/Hour/Day] - Rent a region.", false);
            plugin.sendMessageInfo(sender, "/rentland addmember [RegionName] [PlayerName] - Add a player as member of region.", false);
            plugin.sendMessageInfo(sender, "/rentland removemember [RegionName] [PlayerName] - Remove a player as member of region.", false);
        }

        //command was utilized.
        return true;
    }
    
}
