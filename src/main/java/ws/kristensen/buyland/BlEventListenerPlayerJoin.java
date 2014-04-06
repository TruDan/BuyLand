package ws.kristensen.buyland;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import ws.kristensen.buyland.BuyLand;


import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class BlEventListenerPlayerJoin extends JavaPlugin implements Listener  {
	public static BuyLand plugin;
	
	public BlEventListenerPlayerJoin(BuyLand instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLeave(PlayerQuitEvent event){
	    Player player = event.getPlayer();
	    String playerName = player.getName();
	    //see if we need to update any information about regions they own.
	    //Loop through all the worlds
	    for (World world : Bukkit.getWorlds()) {
	        Map<String, ProtectedRegion> regionMap = WGBukkit.getRegionManager(world).getRegions();
	        //Loop through all the regions of the world
	        for (ProtectedRegion region : regionMap.values()) {
	            if (region.isOwner(playerName)) {
	                if (plugin.rentGetConfig().contains("rent." + region.getId() + ".time")) {
	                    //This is a rented region, nothing to do
	                } else {
                        //region is a buy region, so update the exempt status of the player for the region
                        plugin.signSetExempt(player, region.getId());
	                }
	            }
	        }
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerjoin(PlayerJoinEvent event) {
		 
		//This adds any user who joins the server to the DB list.
		Player player = event.getPlayer();
		String playerName = player.getName();
		String playerNameLowerCase = playerName.toLowerCase();
		
		//Add the player to the CustomConfig
        plugin.customGetConfig().addDefault(playerNameLowerCase + ".own", 0);
        plugin.customGetConfig().addDefault(playerNameLowerCase + ".earned", 0.00);
        plugin.customGetConfig().addDefault(playerNameLowerCase + ".spent", 0.00);
		plugin.customGetConfig().options().copyDefaults(true);
		plugin.customSaveConfig();
		

		//Add the player to the rentDbConfig
        plugin.rentDbGetConfig().addDefault(playerNameLowerCase + ".renting", 0);
        plugin.rentDbGetConfig().addDefault(playerNameLowerCase + ".earned", 0.00);
        plugin.rentDbGetConfig().addDefault(playerNameLowerCase + ".spent", 0.00);
		plugin.rentDbGetConfig().options().copyDefaults(true);
		plugin.rentDbSaveConfig();
		
		    
	    //Loop through all the worlds
	    for (World world : Bukkit.getWorlds()) {
	        //Get a list of regions for the world
	        Map<String, ProtectedRegion> regionMap = WGBukkit.getRegionManager(world).getRegions();
	        //Loop through all the regions of the world
	        for(ProtectedRegion region : regionMap.values()) {
	            //see if the player is owner of region
	            if (region.isOwner(playerName)) {
	                if (plugin.isRentRegion(region)) {
	                  //see if we need to notify the player of time left on regions they own
	                    if (plugin.getConfig().getBoolean("rentland.onPlayerJoin.notifyOfTimeLeft") == true) {
                            long end = plugin.rentGetConfig().getLong("rent." + region.getId() + ".time");
                            long start = System.currentTimeMillis();
                            plugin.sendMessageInfo(player, "Time left for " + region.getId() + ": " + BuyLand.elapsedTimeToString(start, end));
	                    }
	                } else {
                        //region is a buy region, so update the exempt status of the player for the region
                        plugin.signSetExempt(player, region.getId());	                    
                    }
		        }	
		    }
		}
	}
}