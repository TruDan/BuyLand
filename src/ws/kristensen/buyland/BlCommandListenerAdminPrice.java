package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Handles the Admin command:<br/>
 *      /adminbuyland price [Region Name] [Cost]
 * <hr/>
 * Use this to set a price for each individual region.<br/>
 * <br/>
 * NOTE: If you don't use this command the default price from config will take over.) <br/>
 * <br/> 
 * 
 */
public class BlCommandListenerAdminPrice implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerAdminPrice(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 2) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /abl price [RegionName] [Cost]");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();
            String argCost = args[1];

            //See if the person requesting the information is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;
                World world = player.getWorld();

                //See if the player has permission to do the command
                if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {
                    //Make sure the correct number of args is passed in
                    //Get the region manager
                    RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world);
                    //Get the protected region
                    ProtectedRegion protectedRegion = regionManager.getRegionExact(argRegionName);
                    
                    //make sure the region exists
                    if (protectedRegion == null) {
                        //Region does not exist.
                        plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.error1")));
                    } else {
                        //get the value to set for the region
                        Double regionPrice = Double.valueOf(argCost);
                        
                        //Set the price of the region
                        protectedRegion.setFlag(DefaultFlag.PRICE, regionPrice);
                        
                        //Update a sign if it exists
                        //TODO: update the last line of the sign to be the price
                        
                        //save the changes
                        try {
                            regionManager.save();
                        } catch (Exception exp) {
                        }
                        
                        //Notify the player
                        plugin.sendMessageInfo(sender, "Price Added!");
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
