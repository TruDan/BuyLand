package me.buyland.buyland;

import me.buyland.buyland.main;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;



public class ServerChatPlayerListener implements Listener  {

	public static main plugin;
	
	public ServerChatPlayerListener(main instance) {
		plugin = instance;
	}
	@EventHandler(priority = EventPriority.HIGH)
	   public void onPlayerjoin(PlayerJoinEvent event){
		//This adds any user who joins the server to the DB list.
		Player player = event.getPlayer();
		String pn = player.getName();
		
		plugin.getCustomConfig().addDefault(pn, 0);
		plugin.getCustomConfig().options().copyDefaults(true);
		plugin.saveCustomConfig();
	}
	

	  
	}
