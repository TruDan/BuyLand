package ws.kristensen.buyland;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import ws.kristensen.buyland.BuyLand;

public class BlEventListenerPlayerInteract extends JavaPlugin implements Listener  {
	public static BuyLand plugin;
	
	//Used to prevent a sell and buy within 3 seconds of each other.
	//Can happen because of clicking twice on sign
	public static HashMap<String, Long> hashbuy = new HashMap<String, Long>();

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
	public BlEventListenerPlayerInteract(BuyLand instance) {
		plugin = instance;
	}
	
	/**
	 * Make sure that the clickedBlock is never null.
	 * If clickedBlock is null, get what the player is looking up to lookingRange blocks away, and get that block.
	 * 
	 * @param player Player that clicked
	 * @param clickedBlock Block to try and safely get
	 * @param lookingRange int maximum range to try and get a block
	 * @return Block
	 */
	@SuppressWarnings("deprecation")
    private Block getSafeClickedBlock(Player player, Block clickedBlock, int lookingRange) {
	    //see if there is any block that was clicked on
	    if (clickedBlock == null) {
	        //get what the player was looking at up to x blocks away
	        //NOTE: this will return an AIR block if what they were looking at is further away than looking range
	        return player.getTargetBlock(null,  lookingRange);
	    } else {
	        return clickedBlock;
	    }
	}
	private boolean sendMessageLimited(Location location, Action action, Boolean isSneaking, signState state, Integer displayLine, int disallowWithinSeconds, Player player, String message) {
	    String identifier = plugin.locationToString(location) + ":" + action.toString() + ":" + isSneaking.toString() + ":" + state.toString() + ":" + displayLine.toString();
        if (hashbuy.containsKey(identifier)) {
            //get the next time the message is allowed to be sent 
            long nextAllowed = hashbuy.get(identifier);
            
            //See if it was purchased within last 3 seconds
            if (System.currentTimeMillis() < nextAllowed) {
                //abort sending of message
                return false;
            }
            //Send the message
            plugin.sendMessageInfo(player, message);
            
            //Set when the next message can be sent
            hashbuy.put(identifier, System.currentTimeMillis() + (disallowWithinSeconds * 1000));
            return true;
        }
	    return false;
	}
	
	@EventHandler(priority = EventPriority.LOWEST) //be the first to see what was clicked on so we can do our thing without stuff being changed
	public void onSignUse(PlayerInteractEvent event) {
	    //get what was clicked on (directly or indirectly) up to 5 blocks away 
	    Block clickedBlock = getSafeClickedBlock(event.getPlayer(), event.getClickedBlock(), 5);
	    
	    if (plugin.signIsBuyland(clickedBlock)) {
            //Save the sign object for use
            Sign sign = (Sign) clickedBlock.getState();

            //Get the player making the change
            Player player = event.getPlayer();

            //Get the region Name
            String regionName = plugin.signGetRegion(sign.getLocation());
            
            RegionManager regionManager = plugin.getWorldGuard().getRegionManager(sign.getLocation().getWorld());
            ProtectedRegion protectedRegion = regionManager.getRegionExact(regionName);
            
            //Make sure the protectedRegion is valid
            if (protectedRegion != null) {
                boolean isOwner = false;
                //Get the region owner
                if (protectedRegion.getOwners() != null) {
                    isOwner = protectedRegion.getOwners().contains(player.getName());
                }
                
                //get the sign location
                Location signLocation = sign.getLocation();
                
                //Get the sign state
                signState state = plugin.signGetState(signLocation);
                if (state == null) {
                    if (plugin.isOwnRegion(protectedRegion)) {
                        if (protectedRegion.getFlag(DefaultFlag.BUYABLE) == false)
                            plugin.signSetState(signLocation, signState.OWNED);
                        else 
                            plugin.signSetState(signLocation, signState.FOR_SALE);
                    } else  {
                        if (System.currentTimeMillis() <  plugin.rentGetConfig().getLong("rent." + regionName + ".time"))
                            plugin.signSetState(signLocation, signState.RENTED);
                        else
                            plugin.signSetState(signLocation, signState.FOR_RENT);
                    }
                    state = plugin.signGetState(signLocation);
                }
    
                switch (state) {
                    case FOR_SALE:
                        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                            if (!player.isSneaking()) { //player not sneaking left-click
                                if (player.hasPermission("buyland.signbreak") || player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {
                                    //player has permission, do nothing to stop them
                                } else {
                                    //Stop the sign break
                                    plugin.sendMessageWarning(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.break.adminonly")));
                                    event.setCancelled(true);
                                }
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forsale.sneakright")));
                            } else { //player is sneaking left-click
                                if (player.hasPermission("buyland.signbreak") || player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {
                                    //player has permission, do nothing to stop them
                                } else {
                                    //stop the breaking of the sign
                                    plugin.sendMessageWarning(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.break.adminonly")));
                                }
                                event.setCancelled(true);
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forsale.sneakright")));
                            }
                        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            if (!player.isSneaking()) { //player not sneaking right-click
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forsale.sneakright")));
                            } else { //player is sneaking right-click
                                //Issue the command to buy the land as if it were typed
                                Bukkit.dispatchCommand(player, "buyland " + regionName);
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', ChatColor.YELLOW + plugin.languageGetConfig().getString("buyland.sign.instructions.header")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.own.left")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.own.right")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.own.sneakleft")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.own.sneakright")));
                            }
                        }
                        break;
                    case OWNED:
                        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                            if (!player.isSneaking()) { //player not sneaking left-click
                                //Trying to break sign
                                if (isOwner || player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {
                                    //Do nothing to stop the breaking of the sign
                                    //give message of other options
                                    sendMessageLimited(signLocation, event.getAction(), player.isSneaking(), state, 10, 1, player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.own.sneakleft")));
                                    sendMessageLimited(signLocation, event.getAction(), player.isSneaking(), state, 10, 1, player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.own.sneakright")));
                                } else {
                                    //notify they can not break it
                                    plugin.sendMessageWarning(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.break.notowner")));
                                    event.setCancelled(true);
                                }
                            } else { //player is sneaking left-click
                                //Trying to view the Region flags
                                if (isOwner) {
                                    plugin.signDoStateAction(player, protectedRegion, signState.OWN_FLAGS, "set");
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', ChatColor.YELLOW + plugin.languageGetConfig().getString("buyland.sign.instructions.header")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.flags.left")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.flags.right")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.flags.sneakleft")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.flags.sneakright")));
                                }
                                //stop the breaking of the sign
                                event.setCancelled(true);
                            }
                        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            if (!player.isSneaking()) { //player not sneaking right-click
                                if (isOwner) {
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.own.sneakleft")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.own.sneakright")));
                                }
                            } else { //player is sneaking right-click
                                if (isOwner) {
                                    //Issue the command to sell the land as if it were typed
                                    Bukkit.dispatchCommand(player, "sellland " + regionName);
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', ChatColor.YELLOW + plugin.languageGetConfig().getString("buyland.sign.instructions.header")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forsale.left")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forsale.right")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forsale.sneakleft")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forsale.sneakright")));
                                }
                            }
                        }
                        break;
                    case FOR_RENT:
                        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                            if (!player.isSneaking()) { //player not sneaking left-click
                                if (player.hasPermission("buyland.signbreak") || player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {
                                    //player has permission, do nothing to stop them
                                } else {
                                    //Stop the sign break
                                    plugin.sendMessageWarning(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.break.adminonly")));
                                    event.setCancelled(true);
                                }
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forrent.right")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forrent.sneakright")));
                            } else { //player is sneaking left-click
                                if (player.hasPermission("buyland.signbreak") || player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {
                                    //player has permission, do nothing to stop them
                                } else {
                                    //Stop the sign break
                                    plugin.sendMessageWarning(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.break.adminonly")));
                                }
                                event.setCancelled(true);
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forrent.right")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forrent.sneakright")));
                            }
                        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            if (!player.isSneaking()) { //player not sneaking right-click
                                plugin.signDoStateAction(player, protectedRegion, state, "rotateRentTimeframe");
                                sendMessageLimited(signLocation, event.getAction(), player.isSneaking(), state, 10, 1, player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forrent.right")));
                                sendMessageLimited(signLocation, event.getAction(), player.isSneaking(), state, 10, 1, player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forrent.sneakright")));
                            } else { //player is sneaking right-click
                                //call to rentland is handled in signDoStateAction
                                int signDurationLine    = 3;    /* coordinate this with BlEventListenserPlayerInteract.java */
                                if (!plugin.getConfig().getBoolean("general.sign.showBuyLand"))
                                    signDurationLine = 2;
    
                                Bukkit.dispatchCommand(player, "rentland " + regionName + " " + sign.getLine(signDurationLine));
                                plugin.signDoStateAction(player, protectedRegion, signState.RENTED, "set");
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', ChatColor.YELLOW + plugin.languageGetConfig().getString("buyland.sign.instructions.header")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.left")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.right")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.sneakleft")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.sneakright")));
                            }
                        }
                        break;
                    case RENTED:
                    case RENT_EXPIRING:
                        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                            if (!player.isSneaking()) { //player not sneaking left-click
                                if (isOwner || player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")) {
                                    //player has permission, do nothing to stop them
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.right")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.sneakleft")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.sneakright")));
                                } else {
                                    //Stop the sign break
                                    plugin.sendMessageWarning(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.break.adminonly")));
                                    event.setCancelled(true);
                                }
                                //Show time remaining
                                long end = plugin.rentGetConfig().getLong("rent." + protectedRegion.getId() + ".time");
                                long start = System.currentTimeMillis();
                                plugin.sendMessageInfo(player, "Time left for " + protectedRegion.getId() + ": " + BuyLand.elapsedTimeToString(start, end));
                            } else { //player is sneaking left-click
                                //Trying to view the Region flags
                                if (isOwner) {
                                    plugin.signDoStateAction(player, protectedRegion, signState.RENT_FLAGS, "set");
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', ChatColor.YELLOW + plugin.languageGetConfig().getString("buyland.sign.instructions.header")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.flags.left")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.flags.right")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.flags.sneakleft")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.flags.sneakright")));
                                }
                                //stop the breaking of the sign
                                event.setCancelled(true);
                            }
                        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            if (!player.isSneaking()) { //player not sneaking right-click
                                plugin.signDoStateAction(player, protectedRegion, state, "rotateRentTimeframe");
                                sendMessageLimited(signLocation, event.getAction(), player.isSneaking(), state, 10, 1, player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forrent.right")));
                                sendMessageLimited(signLocation, event.getAction(), player.isSneaking(), state, 10, 1, player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.forrent.sneakright")));
                            } else { //player is sneaking right-click
                                if (isOwner) { 
                                    //call to rentland is handled in signDoStateAction
                                    int signDurationLine    = 3;    /* coordinate this with BlEventListenserPlayerInteract.java */
                                    if (!plugin.getConfig().getBoolean("general.sign.showBuyLand"))
                                        signDurationLine = 2;
        
                                    Bukkit.dispatchCommand(player, "rentland " + regionName + " " + sign.getLine(signDurationLine));
        
                                    plugin.signDoStateAction(player, protectedRegion, signState.RENTED, "set");
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', ChatColor.YELLOW + plugin.languageGetConfig().getString("buyland.sign.instructions.header")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.left")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.right")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.sneakleft")));
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.sneakright")));
                                }
                            }
                        }
                        break;
                    case OWN_FLAGS:
                        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                            if (!player.isSneaking()) { //player not sneaking left-click
                                //display the next flag
                                plugin.signDoStateAction(player, protectedRegion, state, "RotateFlagValue");
                            } else { //player is sneaking left-click
                                plugin.signDoStateAction(player, protectedRegion, signState.OWNED, "set");
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', ChatColor.YELLOW + plugin.languageGetConfig().getString("buyland.sign.instructions.header")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.own.left")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.own.right")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.own.sneakleft")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.own.sneakright")));
                            }
                            //stop the breaking of the sign
                            event.setCancelled(true);
                        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            if (!player.isSneaking()) { //player not sneaking right-click
                                //display the next flag
                                plugin.signDoStateAction(player, protectedRegion, state, "RotateFlag");
                            } else { //player is sneaking right-click
                                plugin.signDoStateAction(player, protectedRegion, state, "ToggleFlag");
                                //Save the region changes
                                try {
                                    regionManager.save();
                                } catch (Exception exp) {
                                }
                            }
                        }
                        break;
                    case RENT_FLAGS:
                        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                            if (!player.isSneaking()) { //player not sneaking left-click
                                //display the next flag
                                plugin.signDoStateAction(player, protectedRegion, state, "RotateFlagValue");
                            } else { //player is sneaking left-click
                                plugin.signDoStateAction(player, protectedRegion, signState.RENTED, "set");
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', ChatColor.YELLOW + plugin.languageGetConfig().getString("buyland.sign.instructions.header")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.left")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.right")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.sneakleft")));
                                plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.sign.instructions.rent.sneakright")));
                            }
                            //stop the breaking of the sign
                            event.setCancelled(true);
                        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            if (!player.isSneaking()) { //player not sneaking right-click
                                //display the next flag
                                plugin.signDoStateAction(player, protectedRegion, state, "RotateFlag");
                            } else { //player is sneaking right-click
                                plugin.signDoStateAction(player, protectedRegion, state, "ToggleFlag");
                                //Save the region changes
                                try {
                                    regionManager.save();
                                } catch (Exception exp) {
                                }
                            }
                        }
                        break;
                }
            }
	    }
	}
}
