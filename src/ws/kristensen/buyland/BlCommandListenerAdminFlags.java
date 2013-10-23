package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.VectorFlag;

/**
* Command to show info about all of WorldGuard's flags, or all flags in a preset.
*
* @author Mitch
*
*/
public class BlCommandListenerAdminFlags implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerAdminFlags(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 0) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /abl Flags");
        } else {
            //See if the person requesting the information is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;

                //See if the player has permission to do the command
                if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {
                    plugin.sendMessageInfo(sender, "WorldGuard flag list:");
                    int totalItems = DefaultFlag.flagsList.length;

                    for (int i = 0; i < totalItems; i++) {
                        Flag<?> flag = DefaultFlag.flagsList[i];
                        plugin.sendMessageInfo(sender, "" + (i + 1) + ". " + flag.getName() + ": " + getFlagDescription(flag));
                    }
                }
            }
        }
        //command was utilized.
        return true;
    }
    public String getFlagDescription(Flag<?> flag) {
        if (flag instanceof StateFlag) { return "allow/deny"; }
        else if (flag instanceof SetFlag && flag.getName() == "deny-spawn") { return "comma seperated creature names"; }
        else if (flag instanceof StringFlag) { return "String of words"; }
        else if (flag instanceof BooleanFlag) { return "true/false"; }
        else if (flag instanceof IntegerFlag) { return "number: 1"; }
        else if (flag instanceof DoubleFlag) { return "number:1.0"; }
        else if (flag instanceof SetFlag) { return "comma seperated list"; }
        else if (flag instanceof VectorFlag) { return "comma seperated coordinates"; }
        else if (flag instanceof RegionGroupFlag) { return "members/owners/nonmembers/nonowners"; }
        else if (flag.getName().equals("spawn")) { return "worldName, x,y,z"; }
        else if (flag.getName().equals("teleport")) { return "worldName, x,y,z"; }
        else if (flag.getName().equals("game-mode")) { return "adventure/creative/survival"; }
        return "unknown";
        }
}
