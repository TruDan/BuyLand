package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the Admin command:<br/>
 *      /adminbuyland<br/>
 * <br/>
 * Redirects the subcommands:<br/>      
 *      /adminbuyland forsale [Region Name]<br/>
 *      /adminbuyland save [Region Name]<br/>
 *      /adminbuyland price [Region Name] [Cost]<br/>
 *      /adminbuyland list [Player Name] [Region Name]<br/>
 *      /adminbuyland lwcremove [Region Name]<br/>
 *      /adminbuyland reset [Region Name]<br/>
 *      /adminbuyland flags<br/>
 * <hr/>
 * This will redirect to the class that handles the specific sub-command.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerAdmin implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param instance BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerAdmin(BuyLand instance) {
        if (instance == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean showCommands = false;
        
            if (args.length > 0) {
                //capture the first argument so we know where to redirect
                String arg = args[0];
                if (arg.equalsIgnoreCase("forsale"))   { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerAdminForSale(plugin).onCommand(sender, command, label, args); }
                if (arg.equalsIgnoreCase("list"))      { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerAdminList(plugin).onCommand(sender, command, label, args); }
                if (arg.equalsIgnoreCase("lwcremove")) { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerAdminLwcRemove(plugin).onCommand(sender, command, label, args); }
                if (arg.equalsIgnoreCase("price"))     { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerAdminPrice(plugin).onCommand(sender, command, label, args); }
                if (arg.equalsIgnoreCase("reset"))     { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerAdminReset(plugin).onCommand(sender, command, label, args); }
                if (arg.equalsIgnoreCase("save"))      { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerAdminSave(plugin).onCommand(sender, command, label, args); }
                if (arg.equalsIgnoreCase("flags"))     { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerAdminFlags(plugin).onCommand(sender, command, label, args); }
                //See BlCommandListenerRentland for /rentland save [region_name] command
                //See BlCommandListenerRentland for /rentland [region_name] reset command
            } else {
                if (sender instanceof Player) {
                    Player player = (Player)sender;

                    if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {
                        showCommands = true;
                    }
                } else {
                    //at console
                    showCommands = true;
                }
            }
        
        //Show commands for adminBuyland
        if (showCommands) {
            plugin.sendMessageInfo(sender, " ", false);
            plugin.sendMessageInfo(sender, ChatColor.YELLOW + "Admin Commands", false);
            plugin.sendMessageInfo(sender, "/abl forsale [RegionName] - Makes an existing region buyable.", false);  
            plugin.sendMessageInfo(sender, "/abl save [RegionName] - Select with WorldEdit first.", false);
            plugin.sendMessageInfo(sender, "/abl price [RegionName] [Cost] - Sets a price for buyable region.", false);  
            plugin.sendMessageInfo(sender, "/abl list [PlayerName] - Lists Owned region of player.", false);
            plugin.sendMessageInfo(sender, "/abl lwcremove [RegionName] - Removes LWC Protections for the region.", false);
            plugin.sendMessageInfo(sender, "/abl reset [RegionName] - Resets buyable region.", false);  
            plugin.sendMessageInfo(sender, "/rentland save [RegionName] - Select with WorldEdit first.", false);  
            plugin.sendMessageInfo(sender, "/rentland reset [RegionName] - Reset rentable region.", false);
        }

        //command was utilized.
        return true;
    }
}
