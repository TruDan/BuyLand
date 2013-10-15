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
 * Handles the subcommand:<br/>
 *      /buyland addmember [Region Name] [Player Name]
 * <hr/>
 * Add a member to a region.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerBuylandMemberAdd implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerBuylandMemberAdd(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 2) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /buyland addmember [RegionName] [PlayerName]");
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
                if (player.hasPermission("buyland.buy.addmember") || player.hasPermission("buyland.all")) {
                    if (plugin.getRentConfig().contains("rent." + argRegionName + ".rentable")) {
                        // can't add a member while region is rentable
                        //TODO: Add more appropriate comment for this 
                        plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.rent.error1")));
                    } else {
                        RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world);
                        //Get the protected region
                        ProtectedRegion protectedRegion = regionManager.getRegionExact(argRegionName);

                        //Make sure the region exists
                        if (protectedRegion == null) {
                            //Region does not exist.
                            plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.error1")));
                        } else {
                            //Get the current region owners
                            DefaultDomain owners = protectedRegion.getOwners();
                
                            //See if the player is an owner of the region and has rights to add a member
                            if (!owners.toPlayersString().contains(playerName.toLowerCase())) {
                                plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.sell.dontown")));
                            } else {
                                //Get the members of the region and add the new player name
                                if (protectedRegion.getMembers().contains(argPlayerName)) {
                                    //TODO: give better message
                                    //Notify the user
                                    plugin.sendMessageInfo(sender, "Player is already a member of region.");
                                } else {
                                    protectedRegion.getMembers().addPlayer(argPlayerName);
                                    
                                    //Try to save the changes
                                    try {
                                        regionManager.save();
                                    } catch (ProtectionDatabaseException e) {
                                        e.printStackTrace();
                                    }
    
                                    //Notify the user
                                    plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.member.addmember")));
                                }
                            }
                        }
                    }
                } else {
                    plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.permission")));
                }
            } else {
                plugin.sendMessageInfo(sender, "Currently not available at console.");
            }            
        }

        //command was utilized.
        return true;
    }
}
