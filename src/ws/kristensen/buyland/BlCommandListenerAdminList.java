package ws.kristensen.buyland;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Handles the Admin command:<br/>
 *      /adminbuyland list [Player Name]
 * <hr/>
 * Lists all regions a player owns in any world.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerAdminList implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerAdminList(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 1) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /abl list [PlayerName]");
        } else {
            //Extract the passed arguments
            String argPlayerName = args[0];

            //set flag to not show list by default
            boolean showList = false;
            
            //See if the person requesting the information is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;

                //See if the player has permission to this command
                if (player.hasPermission("buyland.admin.list") || player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {
                    //The player has permission, display the list
                    showList = true;
                }
            } else {
                //The console always has permission to display the list
                showList = true;
            }
            
            if (showList) {
                //State who the list is for
                plugin.sendMessageInfo(sender, argPlayerName + " owns regions: ");
                //Loop through all the worlds
                for (World world : Bukkit.getWorlds()) {
                    //get the list of regions for the selected world
                    Map<String, ProtectedRegion> regionMap = WGBukkit.getRegionManager(world).getRegions();
                    //Loop through all the regions 
                    for(ProtectedRegion region : regionMap.values()) {
                        //see if the person in argPlayerName is an owner of the region
                        if(region.isOwner(argPlayerName)) {   
                            //see if the region is buyable
                            if(region.getFlag(DefaultFlag.BUYABLE) == null) {
                                //if it is null, it is not purchased by anyone.
                            } else {
                                //See if the region was purchased.
                                if(region.getFlag(DefaultFlag.BUYABLE) == false) {
                                    //list this as owned by the player in question
                                    plugin.sendMessageInfo(sender, " " + world.getName() + ": " + region.getId(), false);
                                }
                            }               
                        }
                    }                           
                }                                    
            }            
        }

        //command was utilized.
        return true;
    }
}
