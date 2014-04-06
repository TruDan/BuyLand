package ws.kristensen.buyland;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
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
 * Handles the subcommand:<br/>
 *      /priceland [Region Name]
 * <hr/>
 * Gives the price the Region to rent or buy.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerPriceland implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerPriceland(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 1) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /priceland [RegionName]");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();

            //See if the person requesting the information is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;
                String playerName = player.getName();
                World world = player.getWorld();

                //See if the player has permission to this command
                if (player.hasPermission("buyland.price") || player.hasPermission("buyland.all")) {
                    RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world);
                    //Get the protected region
                    ProtectedRegion protectedRegion = regionManager.getRegionExact(argRegionName);

                    //make sure the region exists
                    if (protectedRegion == null) {
                        //Region does not exist.
                        plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.error1")));
                    } else {
                        //Get the buyable flag for the region
                        Boolean isRegionBuyable = protectedRegion.getFlag(DefaultFlag.BUYABLE);
                            if (isRegionBuyable == null) {
                                isRegionBuyable = false; 
                            }

                        if (plugin.rentGetConfig().contains("rent." + argRegionName + ".rentable")) {
                            //send this to the rentland cost function instead of doing it here since they may not have rights for rent.
                            Bukkit.dispatchCommand(sender, "rentland cost " + argRegionName);
                        } else {
                            //See if the region if buyable
                            if (!isRegionBuyable) {
                                plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.price.dontown")));
                            } else {
                                //Get the price of the region
                                Double regionPrice = plugin.ownGetRegionPurchasePrice(protectedRegion);

                                //Let the player know how much the region costs.
                                plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.price.cost")) + regionPrice);

                                //do a withdraw of 0.00 from player to get the players balance 
                                EconomyResponse economyResponse = BuyLand.econ.withdrawPlayer(playerName, 0.00);
                                
                                //Notify the player of their current balance to purchase the region
                                plugin.sendMessageInfo(sender, String.format(ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.price.price")), BuyLand.econ.format(economyResponse.balance)));

                                //Get the number of regions the player already owns
                                int currentNumberPlayerOwnedRegions = plugin.customGetConfig().getInt(playerName);

                                //Get the maximum number of regions the player can own
                                int maximumNumberRegionsPlayerCanOwn = plugin.ownGetPlayerMaxNumberOfRegions(player);

                                //Notify the player of their current regions and max regions.
                                plugin.sendMessageInfo(sender,
                                                       ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.price.max1"))
                                                       + currentNumberPlayerOwnedRegions +
                                                       ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.price.max2"))
                                                       + maximumNumberRegionsPlayerCanOwn
                                                      );
                            }
                        }
                    }
                } else {
                    plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.permission")));
                }
            } else {
                plugin.sendMessageInfo(sender, "Currently not available at console.");
            }            
        }

        //command was utilized.
        return true;
    }
}
