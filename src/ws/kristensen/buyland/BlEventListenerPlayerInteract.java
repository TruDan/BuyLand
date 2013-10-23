package ws.kristensen.buyland;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
	
	@EventHandler(priority = EventPriority.LOWEST) //be the first to see what was clicked on so we can do our thing without stuff being changed
	public void onSignUse(PlayerInteractEvent event) {
	    //get what was clicked on (directly or indirectly) up to 5 blocks away 
	    Block clickedBlock = getSafeClickedBlock(event.getPlayer(), event.getClickedBlock(), 5);
	    
        if (clickedBlock.getType() == Material.SIGN || clickedBlock.getType() == Material.SIGN_POST || clickedBlock.getType() == Material.WALL_SIGN) {
            //Abort if the item clicked on is not an instance of a sign
            if(clickedBlock.getState() instanceof Sign) {
                //Save the sign object for use
                Sign sign = (Sign) clickedBlock.getState();
    
                //See if this is a buyland sign
                if (sign.getLine(0).contains("[BuyLand]") || sign.getLine(0).equalsIgnoreCase("[BuyLand]")) {
                    //This is a buyland sign. Control it
    
                    //Get the player making the change
                    Player player = event.getPlayer();

                    //Get the region Name
                    String regionName = sign.getLine(2).toLowerCase();
                    
            	    //See if the player trying to break the sign	
            	    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            	        //See if the player has permission to break the sign
    	                if (player.hasPermission("buyland.signbreak") || player.hasPermission("buyland.all") || sign.getLine(1) == "Inactive Sign") {
    	                    //player has permission, do nothing to stop them
    	                    //TODO: possibly remove the sign from the config and do related actions
    	                } else {
    	                    //Stop the sign break
    	                    plugin.sendMessageWarning(player, "You can not break this sign.");
    	                    event.setCancelled(true);
    	                    return;
    	                }
            	    }
    	
            	    //See if the player is trying to use the sign
            	    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            	        //See if the player has rights to use the sign
    	                if (player.hasPermission("buyland.signuse") || player.hasPermission("buyland.all")) {
                            //RENT SIGN 
                            if (sign.getLine(1).equalsIgnoreCase("For Rent") || sign.getLine(1).equalsIgnoreCase("Extend Rent")) {
                                //Get the command on the sign line 3 which contains the [TimeQuantity] [Sec/Min/Hr/Day/Wk] combination to pass to the command
                                String line3 = sign.getLine(3);
                                
                                //execute a command as if the player typed it
                                Bukkit.dispatchCommand(player, "rentland " + regionName + " " + line3);     
                            }
    	                    //Sell Back SIGN	
                            else if (sign.getLine(1).equalsIgnoreCase("Sale Back") || sign.getLine(1).equalsIgnoreCase("Sell Back")) {
	                            if (!player.isSneaking()) {
                                    //notify that player that they must be sneaking
                                    plugin.sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', plugin.getLanguageConfig().getString("buyland.sell.notsneak")));
                                } else {
                                    //TODO: Possibly eleimninate this delay check 
                                    //      as it was probably only because the else was not 
                                    //      added to each if and would trigger the next if statement
                                    
                                    //See if it has been too soon to sell the region
                                    if (hashbuy.containsKey(regionName)) {
                                        //Get the timestamp of when the region was purchased
                                        long soldTime = hashbuy.get(regionName);
                                        //Define three seconds for ease of comparison
                                        long threeSeconds = 3 * 1000L;
                                        //See if it was purchased within last 3 seconds
                                        if (System.currentTimeMillis() - soldTime < threeSeconds) {
                                            //abort the right click on sign.  Too soon
                                            return;
                                        }
                                    }
                                    
                                    //Issue the command to sell the land as if it were typed
                                    Bukkit.dispatchCommand(player, "sellland " + regionName);
                                        
                                    //reset the time on the region
                                    hashbuy.put(regionName, System.currentTimeMillis());
	                            }
    	                    }
    	                    //Buy Sign	
                            else if (sign.getLine(1).equalsIgnoreCase("For Sale")) {
                                //TODO: Possibly eliminate this delay check 
                                //      as it was probably only because the else was not 
                                //      added to each if and would trigger the next if statement
                                
	                            //See if it has been too soon to buy the region
                                if (hashbuy.containsKey(regionName)) {
                                    //Get the timestamp of when the region was sold
                                    long soldTime = hashbuy.get(regionName);
                                    //Define three seconds for ease of comparison
                                    long threeSeconds = 3 * 1000L;
                                    //See if it was sold within last 3 seconds
                                    if (System.currentTimeMillis() - soldTime < threeSeconds) {
                                        //abort the right click on sign.  Too soon
                                        return;
                                    }
                                }

                                //Issue the command to buy the land as if it were typed
                                Bukkit.dispatchCommand(player, "buyland " + regionName);

                                //reset the time on the region
                                hashbuy.put(regionName, System.currentTimeMillis());
    	                    }
    	                } else {
    	                    plugin.sendMessageInfo(player, "You do not have the correct permissions to use this.");
    	                    event.setCancelled(true);
    	                    return;
    	                }
    	            }
    	        }
    	    }
        }
	}
}
