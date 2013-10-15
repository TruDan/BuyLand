package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class listens to and handles the commands:<br/>
 * <br/> 
 *      - rentland<br/>
 * <br/>
 * Redirects the subcommands:<br/>      
 *      /buyland list - Lists all regions the player owns.<br/>
 *      /buyland tp [Region Name] - Teleports a player to the region.<br/>
 *      /buyland addmember [Region Name] [Player Name] - Add a member to a region.<br/>
 *      /buyland removemember [Region Name] [Player Name] - Remove a member from a region.<br/>
 *      /buyland [Region Name] - Buys the Region
 * <hr/>
 * This will redirect to the class that handles the specific sub-command.<br/>
 * <br/>
 * 
 */
public class BlCommandListenerBuyland implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerBuyland(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //See if the person requesting the information is a player
        if (sender instanceof Player) {
            Player player = (Player)sender;

            //See if the player has permission to do the command
            if (player.hasPermission("buyland.buy") || player.hasPermission("buyland.all")) {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("list"))         { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerBuylandList(plugin).onCommand(sender, command, label, args); }
                    if (args[0].equalsIgnoreCase("tp"))           { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerBuylandTeleport(plugin).onCommand(sender, command, label, args); }
                    if (args[0].equalsIgnoreCase("addmember"))    { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerBuylandMemberAdd(plugin).onCommand(sender, command, label, args); }
                    if (args[0].equalsIgnoreCase("removemember")) { args = plugin.removeItemFromArgs(args, 0); return new BlCommandListenerBuylandMemberRemove(plugin).onCommand(sender, command, label, args); }
                    else                                          {                                            return new BlCommandListenerBuylandBuy(plugin).onCommand(sender, command, label, args); }
                } else {
                    plugin.sendMessageInfo(sender, ChatColor.YELLOW + "Buyland Commands", false);
    
                    if (player.hasPermission("buyland.list") || player.hasPermission("buyland.all")) {
                        plugin.sendMessageInfo(sender, "/buyland list - Lists all owned regions.", false);
                    }
    
                    if (player.hasPermission("buyland.price") || player.hasPermission("buyland.all")) {
                        plugin.sendMessageInfo(sender, "/priceland [RegionName] - Prices a region that is buyable.", false);
                    }
                    if (player.hasPermission("buyland.buy") || player.hasPermission("buyland.all")) {
                        plugin.sendMessageInfo(sender, "/buyland [RegionName] - Buy a region.", false);
                    }
                    if (player.hasPermission("buyland.sell") || player.hasPermission("buyland.all")) {
                        plugin.sendMessageInfo(sender, "/sellland [RegionName] - Sell a region.", false);
                    }
    
                    if (player.hasPermission("buyland.buy.addmember") || player.hasPermission("buyland.all")) {
                        plugin.sendMessageInfo(sender, "/buyland addMember [RegionName] [PlayerName] - Add Member to region.", false);
                    }
                    if (player.hasPermission("buyland.buy.removemember") || player.hasPermission("buyland.all")) {
                        plugin.sendMessageInfo(sender, "/buyland removeMember [RegionName] [PlayerName] - Remove Member from region.", false);
                    }
   
                    if (player.hasPermission("buyland.tp") || player.hasPermission("buyland.all")) {
                        plugin.sendMessageInfo(sender, "/buyland tp [RegionName] - Teleport you to region.", false);
                    }
                }
            } else {
                plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.permission")));
            }
            return true;
        } else {
            //at console
            //Show commands for Buyland
            plugin.sendMessageInfo(sender, " ", false);
            plugin.sendMessageInfo(sender, ChatColor.YELLOW + "Buyland Commands", false);
            plugin.sendMessageInfo(sender, "/buyland list - Lists all owned regions.", false);
            plugin.sendMessageInfo(sender, "/buyland [RegionName] - Buys the region.", false);
            plugin.sendMessageInfo(sender, "/buyland addMember [RegionName] [PlayerName] - Add a member to a region.", false);
            plugin.sendMessageInfo(sender, "/buyland removeMember [RegionName] [PlayerName] - Remove a member to a region.", false);
            plugin.sendMessageInfo(sender, "/buyland tp [RegionName] - Teleports a player to the region.", false);
        }

        //command was utilized.
        return true;
    }
}
