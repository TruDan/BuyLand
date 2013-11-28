package ws.kristensen.buyland;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.VectorFlag;

/**
* Command to show info about all of WorldGuard's flags, or all flags in a preset.
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
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
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
                        if (flag instanceof StateFlag ||
                            (flag instanceof BooleanFlag && !flag.getName().equals("buyable")) ||
                            (flag instanceof SetFlag && flag.getName().equals("deny-spawn")) ||
                            flag.getName().equals("game-mode")
                           ) {
                            plugin.sendMessageInfo(sender, "" + (i + 1) + ". " + flag.getName() + ": " + getFlagDescription(flag));
                        }
                    }
                }
            }
        }
        //command was utilized.
        return true;
    }
    public String getFlagDescription(Flag<?> flag) {
        if (flag instanceof StateFlag) { return getStateFlagValues(); }
        else if (flag instanceof SetFlag && flag.getName() == "deny-spawn") { return getCreatureValues(); }
        else if (flag instanceof StringFlag) { return "String of words"; }
        else if (flag instanceof BooleanFlag) { return "TRUE/FALSE"; }
        else if (flag instanceof IntegerFlag) { return "number: 1"; }
        else if (flag instanceof DoubleFlag) { return "number:1.0"; }
        else if (flag instanceof SetFlag) { return "comma seperated list"; }
        else if (flag instanceof VectorFlag) { return "comma seperated coordinates"; }
        else if (flag.getName().equals("spawn")) { return "worldName, x,y,z"; }
        else if (flag.getName().equals("teleport")) { return "worldName, x,y,z"; }
        else if (flag.getName().equals("game-mode")) { return getGameModeValues(); }
        return "unknown";
    }
    public String getStateFlagValues(){
        String returnValue = "";
        for (StateFlag.State flag: StateFlag.State.values()) {
            returnValue += flag.name() + "/";
        }
        return returnValue.substring(0, returnValue.length()-1);
    }
    public String getGameModeValues(){
        String returnValue = "";
        for (org.bukkit.GameMode flag: org.bukkit.GameMode.values()) {
            returnValue += flag.name() + "/";
        }
        return returnValue.substring(0, returnValue.length()-1);
    }
    public String getCreatureValues(){
        String returnValue = "";
        for (EntityType flag: EntityType.values()) {
            returnValue += flag.name() + "/";
        }
        return returnValue.substring(0, returnValue.length()-1);
    }
}
