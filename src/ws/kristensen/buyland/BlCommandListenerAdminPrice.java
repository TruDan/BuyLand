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
 *      /adminbuyland price [Sale Region Name] [Cost]
 *      /adminbuyland price [Rent Region Name] [Cost] [Sec/Min/Hr/Day/Wk]
 * <hr/>
 * Use this to set a price for each individual region.<br/>
 * This works for both rent and Sale regions<br/>
 * <br/>
 * The {Sec/Min/Hr/Day} parameter is only required for setting rent on rent regions.<br/>
 * On Sale regions, it is ignored<br/>
 * <br/>
 * NOTE: If you don't use this command the default price from config will continue to be used.) <br/>
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
        if(args.length < 2 || args.length > 3) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /abl price [SaleRegionName] [Cost]");
            plugin.sendMessageInfo(sender, "Usage: /abl price [RentRegionName] [Cost] [Sec/Min/Hr/Day/Wk]");
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
                        if (plugin.isRentRegion(protectedRegion)) {
                            //TODO: Allow easy adjusting of pricing for rented regions.  
                            //      Rent pricing would take 1 additional parameter [Sec/Min/Hr/Day/Wk] To indicate what scale the cost arg represents
                            //      The price would be stored as price per minute, just as it is stored in the config files.

                            //Make sure there is a 3rd parameter
                            if (args.length != 3) {
                                plugin.sendMessageInfo(sender, "Incorrect number of parameters for a rent region.");
                                plugin.sendMessageInfo(sender, "Usage: /abl price [RentRegionName] [Cost] [Sec/Min/Hr/Day/Wk]");                                
                            } else {
                                //Get the passed in duration of the cost
                                String argTimeType = args[2];
                                
                                //get the unadjusted cost per minute passed in
                                Double rentCostPerMinute = Double.valueOf(argCost);
                                
                                if (argTimeType.equalsIgnoreCase("s") || argTimeType.equalsIgnoreCase("sec") || argTimeType.equalsIgnoreCase("second")) {
                                    rentCostPerMinute /= 1/2;
                                }
                                if (argTimeType.equalsIgnoreCase("m") || argTimeType.equalsIgnoreCase("min") || argTimeType.equalsIgnoreCase("minute")) { 
                                    //Do nothing since it is already in the correct scale
                                }
                                if (argTimeType.equalsIgnoreCase("h") || argTimeType.equalsIgnoreCase("hr")  || argTimeType.equalsIgnoreCase("hour"))   { 
                                    rentCostPerMinute /= 60;
                                }
                                if (argTimeType.equalsIgnoreCase("d") || argTimeType.equalsIgnoreCase("day"))    { 
                                    rentCostPerMinute /= 60 * 24;
                                }
                                if (argTimeType.equalsIgnoreCase("w") || argTimeType.equalsIgnoreCase("wk") || argTimeType.equalsIgnoreCase("week")) {
                                    rentCostPerMinute /= 7 * 24 * 60;
                                }
                                //Set the cost of the rent for the period of 1 minute
                                plugin.getRentConfig().set("rent." + argRegionName +".costpermin", rentCostPerMinute);
                                plugin.saveRentConfig();
                                
                                //Notify the player
                                plugin.sendMessageInfo(sender, "Rent Price Updated!");
                            }
                        } else { //This is a Sellable region
                            if (args.length != 2) {
                                plugin.sendMessageInfo(sender, "Incorrect number of parameters for a For Sale region.");
                                plugin.sendMessageInfo(sender, "Usage: /abl price [SaleRegionName] [Cost]");                                
                            } else {
                                //get the value to set for the region
                                Double regionPrice = Double.valueOf(argCost);
                            
                                //Set the price of the region
                                protectedRegion.setFlag(DefaultFlag.PRICE, regionPrice);
                            
                                //Update a sign if it exists
                                //TODO: update the last line of the sign to be the new price

                                //Notify the player
                                plugin.sendMessageInfo(sender, "For Sale Price Updated!");
                            }
                        }
                        
                        //save the changes
                        try {
                            regionManager.save();
                        } catch (Exception exp) {
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
