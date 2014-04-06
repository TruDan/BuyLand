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
 * Handles the subcommand:<br/>
 *      /buyland list
 * <hr/>
 * Lists all regions the player owns in any world.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerBuylandList implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerBuylandList(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 0) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /buyland list");
        } else {
            //See if the person requesting the information is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;
                String playerName = player.getName();

                //See if the player has permission to this command
                if (player.hasPermission("buyland.list") || player.hasPermission("buyland.all")) {
                    //State who the list is for
                    plugin.sendMessageInfo(sender, "You own regions: ");

                    //Loop through all the worlds
                    for (World world : Bukkit.getWorlds()) {
                        //get the list of regions for the selected world
                        Map<String, ProtectedRegion> regionMap = WGBukkit.getRegionManager(world).getRegions();
                        //Loop through all the regions 
                        for(ProtectedRegion region : regionMap.values()) {
                            //see if the person is an owner of the region
                            if(region.isOwner(playerName)) {   
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
            } else {
                plugin.sendMessageInfo(sender, "Currently not available at console.");
            }            
        }

        //command was utilized.
        return true;
    }
}
