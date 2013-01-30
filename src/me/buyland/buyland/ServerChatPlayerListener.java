package me.buyland.buyland;

import java.util.HashMap;

import me.buyland.buyland.main;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ServerChatPlayerListener extends JavaPlugin implements Listener  {
	
	public static main plugin;
	public static Economy econ = null;
	public static Chat chat = null;
	
	public ServerChatPlayerListener(main instance) {
		plugin = instance;
	}
	
	public static HashMap<String, Long> hashbuy = new HashMap<String, Long>();
	
	@EventHandler(priority = EventPriority.HIGH)
	   public void onPlayerjoin(PlayerJoinEvent event){
		//This adds any user who joins the server to the DB list.
		Player player = event.getPlayer();
		String pn = player.getName();
		
		plugin.getCustomConfig().addDefault(pn, 0);
		plugin.getCustomConfig().options().copyDefaults(true);
		plugin.saveCustomConfig();
	}
	
	
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignChange(SignChangeEvent event) {
		
        Player p = event.getPlayer();
        
        
        Location loc = event.getBlock().getLocation();
        loc.setY(loc.getY() - 1);
        loc.setX(loc.getX() - 1);
        
        Sign s = (Sign) event.getBlock().getState();
        
       if(event.getLine(0).contains("[BuyLand]") || event.getLine(0).equalsIgnoreCase("[BuyLand]")){
       if(p.hasPermission("buyland.signcreate") || p.hasPermission("buyland.*")){
    	   
           event.setLine(0, "[BuyLand]"); 
  //-----               
           if (event.getLine(1).equalsIgnoreCase("For Sale")){
        	event.setLine(1, "For Sale");
        	
			String plotname = event.getLine(2);
	        World world1 = p.getWorld();
	        RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world1);
	        if(regionManager.getRegionExact(plotname) == null){
event.setLine(2, "Invalid Region");
	        }else{
				World world2 = p.getWorld();
			    RegionManager regionManager1 = plugin.getWorldGuard().getRegionManager(world2);
				ProtectedRegion set2 = regionManager1.getRegionExact(plotname);
				
		        Double pflag = set2.getFlag(DefaultFlag.PRICE);
		
			if (pflag == null){
				if (plugin.getConfig().getBoolean("buyland.usepriceperblock") == true){
					
			        	int size =set2.volume();
			        	int ppb = plugin.getConfig().getInt("buyland.defaultpriceperblock");
			        	pflag = (double)size * ppb;
			        
				}else{
			pflag = plugin.getConfig().getDouble("buyland.defaultprice");
				}
			}
				
				String price = pflag.toString();
				event.setLine(3, price);
				
	        	Location signloc = s.getLocation();
	            String location = (signloc.getWorld().getName() + ":" + signloc.getX() + ":" + signloc.getY() + ":" + signloc.getZ());
	            String signname = event.getLine(2);
	            plugin.getsignConfig().set("sign." + signname, location);
	            plugin.savesignConfig();
	            plugin.reloadsignConfig();
	        }
        	
           }else if (event.getLine(1).equalsIgnoreCase("For Rent")){
        	event.setLine(1, "For Rent");
        	
			String plotname = event.getLine(2);
	        World world1 = p.getWorld();
	        RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world1);
	        if(regionManager.getRegionExact(plotname) == null){
event.setLine(2, "Invalid Region");
	        }else{
        	
	        }
	        }else if (event.getLine(1).equalsIgnoreCase("TEST")){

	            
	        }else{ 	
        event.setLine(1, "Error");
           }
          
                     
           s.update();
    	   
       p.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "BuyLand Sign Created!");
       }else{
       p.sendMessage(ChatColor.RED + "BuyLand: You Do Not Have Permission To Create A BuyLand Sign!");
       event.setLine(0, "Error");
       event.setLine(1, "---");
       event.setLine(2, "---");
       event.setLine(3, "---");
       
       s.update();
       }
}
}
	

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignUse(PlayerInteractEvent event) {
	
	    RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
	    econ = rsp.getProvider();
	    
		
	final Player p = event.getPlayer();
	
	if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
	if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN) {
	
	Sign s = (Sign) event.getClickedBlock().getState();
	 
	if (s.getLine(0).equalsIgnoreCase("[BuyLand]") || (s.getLine(0).contains("[BuyLand]"))) {
	//------------------	
		
        Location loc = event.getClickedBlock().getLocation();
        loc.setY(loc.getY() - 1);
        loc.setX(loc.getX() - 1);

	if (!p.hasPermission("buyland.signbreak") || !p.hasPermission("buyland.*")) {
	p.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.RED + "You can not break this sign.");
	event.setCancelled(true);
	return;
	}
	}
	}
	}
	 
	if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
	if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN) {
	
	Sign s = (Sign) event.getClickedBlock().getState();
	 
	if (s.getLine(0).equalsIgnoreCase("[BuyLand]") || (s.getLine(0).contains("[BuyLand]"))) {
		
		
if (p.hasPermission("buyland.signuse") || !p.hasPermission("buyland.*")) {
	
	String plotname = s.getLine(2).toString();
	
//RENT SIGN	
	
	if (s.getLine(1).equalsIgnoreCase("For Rent") || (s.getLine(1).contains("FOR RENT"))) {
		if (p.hasPermission("buyland.rent") || p.hasPermission("buyland.*")){
//-------------------		

					   String line3 = s.getLine(3);
						
						Bukkit.dispatchCommand(Bukkit.getPlayer(p.getName()), "rentland " + plotname + " " + line3);

			
//-----------------			
	}else{
		p.sendMessage(ChatColor.DARK_RED + "BuyLand: Sorry you do not have permission to rent land.");
	}
	}
	
//Sale Back SIGN	
	
		if (s.getLine(1).equalsIgnoreCase("Sale Back")) {
			if (p.hasPermission("buyland.sell") || p.hasPermission("buyland.*")){
			
		
		World world2 = p.getWorld();
	    RegionManager regionManager1 = plugin.getWorldGuard().getRegionManager(world2);
		ProtectedRegion set2 = regionManager1.getRegionExact(plotname);
		
        Double pflag = set2.getFlag(DefaultFlag.PRICE);
        
      	
  	  DefaultDomain owner = set2.getOwners();
  	  	String owner2 = owner.toPlayersString();
  	  	String pn = p.getName().toLowerCase();

  	  	
  	  	if (owner2.contains(pn)){
  	  		
  	  	//------------
  	  	if (pflag == null){
  	  		if (plugin.getConfig().getBoolean("buyland.usepriceperblock") == true){
  	  			//BETA AREA
  	  	        	int size =set2.volume();
  	  	        	//player.sendMessage("Area of blocks: " + size);
  	  	        	int ppb = plugin.getConfig().getInt("buyland.defaultpriceperblock");
  	  	        	pflag = (double)size * ppb;
  	  	        //BETA AREA
  	  		}else{
  	  	pflag = plugin.getConfig().getDouble("buyland.defaultprice");
  	  		}
  	  	}
  	  		
  	  		String price = pflag.toString();
  	  		
  	  	hashbuy.put(plotname, System.currentTimeMillis());

s.setLine(0, "[BuyLand]");
s.setLine(1, "For Sale");
s.setLine(2, plotname);
s.setLine(3, price);
s.update();
Bukkit.dispatchCommand(Bukkit.getPlayer(p.getName()), "sellland " + plotname);

  	     
  	     
  	  	//-----------------------------------------	
  	  	}else{
  	  		p.sendMessage(ChatColor.DARK_RED + "BuyLand: Sorry you are not the owner of this region!");
  	  	}

		
		}else{
			p.sendMessage(ChatColor.DARK_RED + "BuyLand: Sorry you do not have permission to sale land.");
		}
		}
	
//Buy Sign	
	if (s.getLine(1).equalsIgnoreCase("For Sale")) {
		if (p.hasPermission("buyland.buy") || p.hasPermission("buyland.*")){
	//--------------------------------
		
		     double bal = econ.getBalance(p.getName());
		     
			String sb = s.getLine(3);
			
			double signbal = Double.parseDouble(sb);
			

			
			   if(hashbuy.containsKey(plotname))
			    {
					
				 
				   long time = hashbuy.get(plotname);
				   int timebuy = 3 * 1000;
				   if(System.currentTimeMillis() - time < timebuy){
					//   p.sendMessage("Error 100");
			        	 return;
				   }else{
					   
						World world2 = p.getWorld();
					    RegionManager regionManager1 = plugin.getWorldGuard().getRegionManager(world2);
						ProtectedRegion set2 = regionManager1.getRegionExact(plotname);

						
				  	  Boolean bflag = set2.getFlag(DefaultFlag.BUYABLE);
				  	  	
				  	  	if (bflag == true){
					   
					   if (signbal > bal){
							p.sendMessage(ChatColor.RED + "BuyLand: Insignificant funds to buy.");
						}else{
							
							String nm = p.getName();
							int numofland = plugin.getCustomConfig().getInt(nm);
							int maxofland = plugin.getConfig().getInt("buyland.maxamountofland");
							

						if (numofland +1 > maxofland){
							String convertedmax = ChatColor.translateAlternateColorCodes('&', plugin.getlanguageConfig().getString("buyland.buy.max"));
							
							p.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedmax);
						}else{

							
				    	 if (plugin.getConfig().getBoolean("buyland.breaksignonbuy") == true){
						    	s.getBlock().setType(Material.AIR);
						    	 }
						    	 
						        s.setLine(0, "[BuyLand]");
						        s.setLine(1, "Sale Back");
						        s.setLine(2, plotname);
						        s.setLine(3, p.getName());
						        s.update();
						        Bukkit.dispatchCommand(Bukkit.getPlayer(p.getName()), "buyland " + plotname);
			   hashbuy.remove(plotname);
			   
						}
			   

						}
					   					  	  	}else{
					  	  		
							     String convertednotforsale = ChatColor.translateAlternateColorCodes('&', plugin.getlanguageConfig().getString("buyland.price.dontown"));
									
								 p.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertednotforsale);
							
					  	  		
					  	  	}
				   }
			    
			    }else{
			    	
			    			World world2 = p.getWorld();
					    RegionManager regionManager1 = plugin.getWorldGuard().getRegionManager(world2);
						ProtectedRegion set2 = regionManager1.getRegionExact(plotname);

						
				  	  Boolean bflag = set2.getFlag(DefaultFlag.BUYABLE);
				  	  	
				  	  	if (bflag == true){
					if (signbal > bal){

						p.sendMessage(ChatColor.RED + "BuyLand: Insignificant funds to buy.");
					}else{
						
				
						
			    	 if (plugin.getConfig().getBoolean("buyland.breaksignonbuy") == true){
					    	s.getBlock().setType(Material.AIR);
					    	 }
						String nm = p.getName();
						int numofland = plugin.getCustomConfig().getInt(nm);
						int maxofland = plugin.getConfig().getInt("buyland.maxamountofland");
						

					if (numofland +1 > maxofland){
						String convertedmax = ChatColor.translateAlternateColorCodes('&', plugin.getlanguageConfig().getString("buyland.buy.max"));
						
						p.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedmax);
					}else{
					        s.setLine(0, "[BuyLand]");
					        s.setLine(1, "Sale Back");
					        s.setLine(2, plotname);
					        s.setLine(3, p.getName());
					        s.update();
					        Bukkit.dispatchCommand(Bukkit.getPlayer(p.getName()), "buyland " + plotname);
					}
					        hashbuy.remove(plotname);	
		 
				  	  	}
				  	  	}else{
				  	  		
						     String convertednotforsale = ChatColor.translateAlternateColorCodes('&', plugin.getlanguageConfig().getString("buyland.price.dontown"));
								
							 p.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertednotforsale);
						
				  	  		
				  	  	}
					
					
	
					
			    }
			
//----------------------------------
		}else{
			p.sendMessage(ChatColor.DARK_RED + "BuyLand: Sorry you do not have permission to buy.");
		}
		
	
	}
	
	}else{
p.sendMessage("You are not allowed to use plugin.");
event.setCancelled(true);
return;
}
	
	}
	
	}
	}
	
	}
	

	  
	}
