package ws.kristensen.buyland;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import ws.kristensen.buyland.BuyLand;


import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class BlEventListenerPlayerJoin extends JavaPlugin implements Listener  {
	public static BuyLand plugin;
	
	public BlEventListenerPlayerJoin(BuyLand instance) {
		plugin = instance;
	}
    
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerjoin(PlayerJoinEvent event) {
		 
		//This adds any user who joins the server to the DB list.
		Player player = event.getPlayer();
		String playerName = player.getName();
		String playerNameLowerCase = playerName.toLowerCase();
		
		//Add the player to the CustomConfig
		plugin.getCustomConfig().addDefault(playerNameLowerCase, 0);
		plugin.getCustomConfig().options().copyDefaults(true);
		plugin.saveCustomConfig();
		

		//Add the player to the rentDbConfig
		plugin.getrentdbConfig().addDefault(playerNameLowerCase, 0);
		plugin.getrentdbConfig().options().copyDefaults(true);
		plugin.saverentdbConfig();
		
		//see if we need to notify the player of time left on regions they own
		if (plugin.getConfig().getBoolean("buyland.notifyplayerofrenttime") == true) {
		    //Loop through all the worlds
		    for (World world : Bukkit.getWorlds()) {
		        //Unknown why we check, because it was given to us.
		        if (world != null) {
    		        //Get a list of regions for the world
    		        Map<String, ProtectedRegion> regionMap = WGBukkit.getRegionManager(world).getRegions();
    		        //Loop through all the regions of the world
    		        for(ProtectedRegion region : regionMap.values()) {
    		            //unknown why we check, because it was given to us.
    		            if (region != null) {
        		            //see if the player is owner of region
        		            if (region.isOwner(playerName)) {
        		                if (region.getFlag(DefaultFlag.BUYABLE) == null) {
                                    //if the player owns the region, no notification is necessary
        		                } else {
        		                    //region is not buyable, but owned, so it is rented.
        		                    if(region.getFlag(DefaultFlag.BUYABLE) == false){
        		                        //see if there is time on the region
        		                        if (plugin.getRentConfig().contains("rent." + region.getId() + ".time")) {
        		                            long end = plugin.getRentConfig().getLong("rent." + region.getId() + ".time");
        		                            long start = System.currentTimeMillis();
        		                            plugin.sendMessageInfo(player, "Time left for " + region.getId() + ": " + BuyLand.elapsedTime(start, end));
        		                        }
        		                    }
        		                }				
        		            }
                        }
                    }
		        }	
		    }
		}
	}
}