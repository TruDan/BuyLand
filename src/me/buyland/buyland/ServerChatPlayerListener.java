package me.buyland.buyland;

import java.util.HashMap;
import java.util.Map;

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


import com.sk89q.worldguard.bukkit.WGBukkit;
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
	
	
	
	public boolean checkArea(Location player, double minx, double miny, double minz, double maxx, double maxy, double maxz)
    {
            if(player.getX() > minx && player.getX() < maxx)
            {
                if(player.getY() > miny && player.getY() < maxy)
                {
                    if(player.getZ() > minz && player.getZ() < maxz)
                    {
                        return true;
                    }
                }
            }
        return false;
    } 
	
	
	public static String elapsedTime(long start, long end){

		String auxRet= "";

		long aux = end - start;
		long days=0, hours=0, minutes = 0, seconds = 0;
		//days
		if (aux > 24*60*60*1000){
		days = aux/(24*60*60*1000);
		}
		aux = aux%(24*60*60*1000);
		//hours
		if (aux > 60*60*1000){
		hours = aux/(60*60*1000);
		}
		aux = aux%(60*60*1000);
		//minutes
		if (aux > 60*1000){
		minutes = aux/(60*1000);
		}
		aux = aux%(60*1000);
		//seconds
		if (aux > 1000){
		seconds = aux/(1000);
		}
		//milliseconds = aux%1000;

		if(days>0){
		auxRet = days +" days " ;
		}
		if(days != 0 || hours>0){
		auxRet += hours+ " hours " ;
		}
		if(days != 0 || hours!= 0 || minutes>0){
		auxRet += minutes +" minutes " ;
		}
		if(days != 0 || hours!= 0 || minutes!=0 || seconds>0){
		auxRet += seconds +" seconds " ;
		}
		//auxRet += milliseconds +" milliseconds ";

		return auxRet;
		}
	
	@EventHandler(priority = EventPriority.HIGH)
	   public void onPlayerjoin(PlayerJoinEvent event){
		 
		//This adds any user who joins the server to the DB list.
		Player player = event.getPlayer();
		//String Cpn = player.getName();
		String pn = player.getName().toLowerCase();
		
		//if(plugin.getCustomConfig().contains(Cpn)){
		//	int t = plugin.getCustomConfig().getInt(Cpn);
		//	plugin.getCustomConfig().addDefault(pn, t);
		//}else{
			plugin.getCustomConfig().addDefault(pn, 0);
		//}
		
		
		plugin.getCustomConfig().options().copyDefaults(true);
		plugin.saveCustomConfig();
		
		
		//if(plugin.getrentdbConfig().contains(Cpn)){
		//	int t = plugin.getrentdbConfig().getInt(Cpn);
		//	plugin.getrentdbConfig().addDefault(pn, t);
		//}else{
			plugin.getrentdbConfig().addDefault(pn, 0);
		//}

		plugin.getrentdbConfig().options().copyDefaults(true);
		plugin.saverentdbConfig();
		
		if(plugin.getConfig().getBoolean("buyland.notifyplayerofrenttime") == true){
			
			
			
    		for (World w1 : Bukkit.getWorlds())
    		{
if (w1 == null){
	return;
}
			
		Map<String, ProtectedRegion> regionMap = WGBukkit.getRegionManager(w1).getRegions();
		for(ProtectedRegion region : regionMap.values()) {	
			if (region == null){
				return;
			}
			
		if(region.isOwner(player.getName())) {	
	if(region.getFlag(DefaultFlag.BUYABLE) == null){
	//	player.sendMessage("Null " + region.getId());
	}else{
			if(region.getFlag(DefaultFlag.BUYABLE) == false){
				//player.sendMessage(" " + region.getId());
					
				if (plugin.getRentConfig().contains("rent." + region.getId() + ".time")){
				long end = plugin.getRentConfig().getLong("rent." + region.getId() + ".time");
				long start = System.currentTimeMillis();
		    	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Time left for " + region.getId() + ": " + elapsedTime(start, end));
				}
				
				
			}
		}				
		}
		}	
		
		
    		}
		
		//END OF TIME
	}
	}
	
	
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignChange(SignChangeEvent event) {
	//	if(!(event.getBlock().getState() instanceof Sign)) return;
		//HERE	

        Player p = event.getPlayer();
        
       
        Location loc = event.getBlock().getLocation();
        loc.setY(loc.getY() - 1);
        loc.setX(loc.getX() - 1);
        
        Sign s = (Sign) event.getBlock().getState();
        
        
       if(event.getLine(0).contains("[BuyLand]") || event.getLine(0).equalsIgnoreCase("[BuyLand]")){
       if(p.hasPermission("buyland.signcreate") || p.hasPermission("buyland.all")){
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
	        	
	        	//=====================
	        	//Deny placing a sign within its own region!
	        	World wor = p.getWorld();
			    RegionManager regionManager5 = plugin.getWorldGuard().getRegionManager(wor);
				ProtectedRegion se = regionManager5.getRegionExact(plotname);
	        	
				 
			    
			    Location ploc = event.getBlock().getLocation();
			    Location Pos1 = new Location(wor, se.getMaximumPoint().getBlockX(), se.getMaximumPoint().getBlockY(), se.getMaximumPoint().getBlockZ());	   
			    Location Pos2 = new Location(wor, se.getMinimumPoint().getBlockX(), se.getMinimumPoint().getBlockY(), se.getMinimumPoint().getBlockZ());
				   
			    double minx = Math.min(Pos1.getX(), Pos2.getX());
			    double maxx = Math.max(Pos1.getX(), Pos2.getX());
			    double miny = Math.min(Pos1.getY(), Pos2.getY());
			    double maxy = Math.max(Pos1.getY(), Pos2.getY());
			    double minz = Math.min(Pos1.getZ(), Pos2.getZ());
			    double maxz = Math.max(Pos1.getZ(), Pos2.getZ());
				if(checkArea(ploc,minx,miny,minz,maxx,maxy,maxz) == true){
			//DO SOMETHING
					event.setLine(1, "ERROR!");
					event.setLine(2, "ERROR!");
					 p.sendMessage(ChatColor.RED + "BuyLand: Placing a sign on its own region will cause errors!");
					 p.sendMessage(ChatColor.RED + "BuyLand: Please place it outside of the region!");
						
				}else{
			//=================	
			
	        	
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
	        }
        	
           }else if (event.getLine(1).equalsIgnoreCase("For Rent")){
        	event.setLine(1, "For Rent");
        	
			String plotname = event.getLine(2);
	        World world1 = p.getWorld();
	        RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world1);
	        if(regionManager.getRegionExact(plotname) == null){
event.setLine(2, "Invalid Region");
	        }else{
        	
	        	
	        	//=====================
	        	//Deny placing a sign within its own region!
	        	World wor = p.getWorld();
			    RegionManager regionManager5 = plugin.getWorldGuard().getRegionManager(wor);
				ProtectedRegion se = regionManager5.getRegionExact(plotname);
	        	
				 
			    
			    Location ploc = event.getBlock().getLocation();
			    Location Pos1 = new Location(wor, se.getMaximumPoint().getBlockX(), se.getMaximumPoint().getBlockY(), se.getMaximumPoint().getBlockZ());	   
			    Location Pos2 = new Location(wor, se.getMinimumPoint().getBlockX(), se.getMinimumPoint().getBlockY(), se.getMinimumPoint().getBlockZ());
				   
			    double minx = Math.min(Pos1.getX(), Pos2.getX());
			    double maxx = Math.max(Pos1.getX(), Pos2.getX());
			    double miny = Math.min(Pos1.getY(), Pos2.getY());
			    double maxy = Math.max(Pos1.getY(), Pos2.getY());
			    double minz = Math.min(Pos1.getZ(), Pos2.getZ());
			    double maxz = Math.max(Pos1.getZ(), Pos2.getZ());
				if(checkArea(ploc,minx,miny,minz,maxx,maxy,maxz) == true){
			//DO SOMETHING
					event.setLine(1, "ERROR!");
					event.setLine(2, "ERROR!");
					 p.sendMessage(ChatColor.RED + "BuyLand: Placing a sign on its own region will cause errors!");
					 p.sendMessage(ChatColor.RED + "BuyLand: Please place it outside of the region!");
						
				}else{
			//=================	
	        	
	        	
	        	Location signloc = s.getLocation();
	            String location = (signloc.getWorld().getName() + ":" + signloc.getX() + ":" + signloc.getY() + ":" + signloc.getZ());
	            String signname = event.getLine(2);
	            plugin.getsignConfig().set("sign." + signname, location);
	            plugin.savesignConfig();
	            plugin.reloadsignConfig();
				}
	        	
	        }
	        }else if (event.getLine(1).equalsIgnoreCase("unrent")){
//UNRENT COMMAND
	        	event.setLine(1, "UnRent");
	        	
				String plotname = event.getLine(2);
		        World world1 = p.getWorld();
		        RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world1);
		        if(regionManager.getRegionExact(plotname) == null){
	event.setLine(2, "Invalid Region");
		        }else{
	        	
		        	//=====================
		        	//Deny placing a sign within its own region!
		        	World wor = p.getWorld();
				    RegionManager regionManager5 = plugin.getWorldGuard().getRegionManager(wor);
					ProtectedRegion se = regionManager5.getRegionExact(plotname);
		        	
					 
				    
				    Location ploc = event.getBlock().getLocation();
				    Location Pos1 = new Location(wor, se.getMaximumPoint().getBlockX(), se.getMaximumPoint().getBlockY(), se.getMaximumPoint().getBlockZ());	   
				    Location Pos2 = new Location(wor, se.getMinimumPoint().getBlockX(), se.getMinimumPoint().getBlockY(), se.getMinimumPoint().getBlockZ());
					   
				    double minx = Math.min(Pos1.getX(), Pos2.getX());
				    double maxx = Math.max(Pos1.getX(), Pos2.getX());
				    double miny = Math.min(Pos1.getY(), Pos2.getY());
				    double maxy = Math.max(Pos1.getY(), Pos2.getY());
				    double minz = Math.min(Pos1.getZ(), Pos2.getZ());
				    double maxz = Math.max(Pos1.getZ(), Pos2.getZ());
					if(checkArea(ploc,minx,miny,minz,maxx,maxy,maxz) == true){
				//DO SOMETHING
						event.setLine(1, "ERROR!");
						event.setLine(2, "ERROR!");
						 p.sendMessage(ChatColor.RED + "BuyLand: Placing a sign on its own region will cause errors!");
						 p.sendMessage(ChatColor.RED + "BuyLand: Please place it outside of the region!");
							
					
				//=================	
					}else{	
					//UNRENT CODE GOES HERE!	
						
						
					}
		        	
		        	
		        }
	        	
//END UNRENT COMMAND	
	        }else{ 	
        event.setLine(1, "Error");
           }
          
                     
           s.update();
    	   
       p.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "BuyLand Sign Created!");
       
       //}else{
    	//   p.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Creating a sign on the region it is for will cause problems! Please move the sign outside of the region.");
             
      // }
       
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
//if(!(event.getClickedBlock().getState() instanceof Sign)) return;
//HERE	
	
	    RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
	    econ = rsp.getProvider();
	final Player p = event.getPlayer();
	
	
//Stops players from breaking signs.	
	if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
		
	if (event.getClickedBlock().getType() == Material.SIGN || event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN) {
		if(!(event.getClickedBlock().getState() instanceof Sign)) return;

		
		Sign s = (Sign) event.getClickedBlock().getState();
	if (s.getLine(0).equalsIgnoreCase("[BuyLand]") || (s.getLine(0).contains("[BuyLand]"))) {
        Location loc = event.getClickedBlock().getLocation();
        loc.setY(loc.getY() - 1);
        loc.setX(loc.getX() - 1);
	if (!p.hasPermission("buyland.signbreak") || !p.hasPermission("buyland.all")) {
	p.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.RED + "You can not break this sign.");
	event.setCancelled(true);
	return;
	}
	}
	}
	}
//Stops Players from breakins signs.	
	
	 
	if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
	if (event.getClickedBlock().getType() == Material.SIGN || event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN) {
	
	Sign s = (Sign) event.getClickedBlock().getState();
	 
	
	if (s.getLine(0).equalsIgnoreCase("[BuyLand]") || (s.getLine(0).contains("[BuyLand]"))) {
		
		
if (p.hasPermission("buyland.signuse") || p.hasPermission("buyland.all")) {
	String plotname = s.getLine(2).toString();
	
	
//RENT SIGN	
	if (s.getLine(1).equalsIgnoreCase("For Rent") || (s.getLine(1).contains("FOR RENT"))) {
		if (p.hasPermission("buyland.rent") || p.hasPermission("buyland.all")){
					   String line3 = s.getLine(3);
						Bukkit.dispatchCommand(Bukkit.getPlayer(p.getName()), "rentland " + plotname + " " + line3);		
	}else{
		p.sendMessage(ChatColor.DARK_RED + "BuyLand: Sorry you do not have permission to rent land.");
	}
	}
	
	
	
	
//Sale Back SIGN	

		if (s.getLine(1).equalsIgnoreCase("Sale Back")) {

			if (p.hasPermission("buyland.sell") || p.hasPermission("buyland.all")){
				if(p.isSneaking()){
				//	p.sendMessage("DEBUG: Player is Sneaking...");

		
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
				//	p.sendMessage("DEBUG: Player is NOT Sneaking...");
				     String convertednotsneak = ChatColor.translateAlternateColorCodes('&', plugin.getlanguageConfig().getString("buyland.sell.notsneak"));
						
					p.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertednotsneak);
							
				}
  	  	
		
		}else{
			p.sendMessage(ChatColor.DARK_RED + "BuyLand: Sorry you do not have permission to sale land.");
		}
		}
	
//Buy Sign	
	if (s.getLine(1).equalsIgnoreCase("For Sale")) {
		if (p.hasPermission("buyland.buy") || p.hasPermission("buyland.all")){

		     double bal = econ.getBalance(p.getName());
			String sb = s.getLine(3);
			double signbal = Double.parseDouble(sb);

			   if(hashbuy.containsKey(plotname))
			    {

				   long time = hashbuy.get(plotname);
				   int timebuy = 3 * 1000;
				   if(System.currentTimeMillis() - time < timebuy){
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

							//int maxofland = plugin.getConfig().getInt("buyland.maxamountofland");
							
							int loopVal;
							int end_value = 51;
							String loop = null;

							for(loopVal = 0; loopVal < end_value; loopVal++){
								 loop = Integer.toString(loopVal);
							 if (p.hasPermission("buyland.maxland."+loop)){
									
						
							int maxofland = loopVal;

							

						if (numofland +1 > maxofland){
							String convertedmax = ChatColor.translateAlternateColorCodes('&', plugin.getlanguageConfig().getString("buyland.buy.max"));
							
							p.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedmax);
						}else{

							
				    	 if (plugin.getConfig().getBoolean("buyland.breaksignonbuy") == true){
						    	s.getBlock().setType(Material.AIR);
						    	 }else{
						    	 
						        s.setLine(0, "[BuyLand]");
						        s.setLine(1, "Sale Back");
						        s.setLine(2, plotname);
						        s.setLine(3, p.getName());
						        s.update();
						    	 }
						        Bukkit.dispatchCommand(Bukkit.getPlayer(p.getName()), "buyland " + plotname);
			   hashbuy.remove(plotname);
			   
						}
						
						
						}
				  	  	}
				  	  	//END OF MAX
			   

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
						//int maxofland = plugin.getConfig().getInt("buyland.maxamountofland");
						
						int loopVal;
						int end_value = 51;
						String loop = null;

						for(loopVal = 0; loopVal < end_value; loopVal++){
							 loop = Integer.toString(loopVal);
						 if (p.hasPermission("buyland.maxland."+loop)){
						int maxofland = loopVal;
						

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
					
					}
				  	}
				  	  	//END FOR MAX
				  	  	
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
p.sendMessage("[BUYLAND] You do not have the correct permissions to use this.");
event.setCancelled(true);
return;
}
	
	}
	
	}
	}
	
	}
	

	  
	}
