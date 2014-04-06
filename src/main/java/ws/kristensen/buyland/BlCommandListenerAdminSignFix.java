package ws.kristensen.buyland;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the Admin command:<br/>
 *      /adminbuyland signfix
 * <hr/>
 * when changing the config.yml setting general.sign.showBuyLand, the existing signs need to be updated or they will error when clicked.
 * This will change the sign to the correct state of all signs recorded.<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerAdminSignFix implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerAdminSignFix(BuyLand plugin) {
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
            if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {
                //Do the fix
                int signUpdatedCount = plugin.signUpdateAll((Player) sender);
                plugin.sendMessageInfo(sender, "Signs updated: " + String.valueOf(signUpdatedCount));
            }
        } else {
            //Do the fix
            int signUpdatedCount = plugin.signUpdateAll(null);
            plugin.sendMessageInfo(null, "Signs updated: " + String.valueOf(signUpdatedCount));
        }            
        
        //command was utilized.
        return true;
    }
}
