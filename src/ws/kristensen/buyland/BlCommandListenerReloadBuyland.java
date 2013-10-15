package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the Admin command:<br/>
 *      /reloadbuyland
 * <hr/>
 * Will Reload all Config Files... Config.yml, DB.yml, and Language.yml <br/>
 * <br/> 
 * 
 */
public class BlCommandListenerReloadBuyland implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerReloadBuyland(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 0) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /reloadbuyland");
        } else {
            //is the requester allowed to reload
            boolean allowReload = false;

            //set default return message
            String returnMessage = ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.permission"));

            //see if requester is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;
    
                //See if the player has permission to this command
                if (player.hasPermission("buyland.reload") || player.hasPermission("buyland.all")) {
                    allowReload = true;
                }
            } else {
                //console
                allowReload = true;
            }

            //do reload if allowed
            if (allowReload) {
                plugin.reloadConfig();  
                plugin.reloadCustomConfig();
                plugin.reloadLanguageConfig();
                plugin.reloadRentConfig();
                plugin.reloadrentdbConfig();
                plugin.reloadSignConfig();
                
                returnMessage = ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.reload"));
            }

            //send the message
            plugin.sendMessageInfo(sender, returnMessage);
        }

        //command was utilized.
        return true;
    }
}
