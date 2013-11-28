package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Handles the command:<br/>
 *      /rentland addmember [Region Name] [Player Name]
 * <hr/>
 * Add a member to a region.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerRentlandMemberAdd implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerRentlandMemberAdd(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 2) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /rentland addmember [RegionName] [PlayerName]");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();
            String argPlayerName = args[1];

            //See if the person requesting the information is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;
                String playerName = player.getName();
                World world = player.getWorld();

                //See if the player has permission to this command
                if (player.hasPermission("buyland.rent.addmember") || player.hasPermission("buyland.rent") || player.hasPermission("buyland.all")) {    
                    RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world);
                    //Get the protected region
                    ProtectedRegion protectedRegion = regionManager.getRegionExact(argRegionName);

                    //Make sure the region exists
                    if (protectedRegion == null) {
                        //Region does not exist.
                        plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.error1")));
                    } else {
                        //See if the region is rentable
                        if (!plugin.rentGetConfig().contains("rent." + argRegionName +".rentable")) {
                            // can't add a member while region is not rentable
                            plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.rent.error2")));
                        } else {
                            //Get the current region owners
                            DefaultDomain owners = protectedRegion.getOwners();
                            
                            //See if the player is an owner of the region and has rights to add a member
                            if (!owners.toPlayersString().contains(playerName.toLowerCase())) {
                                plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sell.dontown")));
                            } else {
                                //Get the members of the region and add the new player name
                                protectedRegion.getMembers().addPlayer(argPlayerName);
                                
                                //Try to save the changes
                                try {
                                    regionManager.save();
                                } catch (ProtectionDatabaseException e) {
                                    e.printStackTrace();
                                }
                                
                                //Notify the user
                                plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.member.addmember")));
                            }
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
