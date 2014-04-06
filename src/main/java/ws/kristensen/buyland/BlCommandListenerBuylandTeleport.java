package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the subcommand:<br/>
 *      /buyland TP [Region Name]
 * <hr/>
 * Teleports player to a region sign.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerBuylandTeleport implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerBuylandTeleport(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 1) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /buyland tp [RegionName]");
        } else {
            //Extract the passed arguments
            String argRegionName = args[0].toLowerCase();

            //See if the person requesting the information is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;

                //See if the player has permission to this command
                if (player.hasPermission("buyland.tp") || player.hasPermission("buyland.all")) {
                    //see if there is a sign for the requested region
                    if (plugin.signIsDefined(argRegionName)) {
                        //Get the location of the sign
                        Location tptosign = plugin.signGetLocation(argRegionName);

                        //adjust target so player arrives on top of sign and facing same direction as before.
                        tptosign.add(new Location(tptosign.getWorld(),.5,.5,.5));
                        tptosign.setPitch(player.getLocation().getPitch());
                        tptosign.setYaw(player.getLocation().getYaw());

                        //Teleport the player to the sign
                        player.teleport(tptosign);
                    } else {
                        //Notify player that teleport failed
                        plugin.sendMessageInfo(sender, "Teleportation has not been enabled for this region or it does not exist.");
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
