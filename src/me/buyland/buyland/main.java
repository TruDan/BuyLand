package me.buyland.buyland;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;


import org.bukkit.ChatColor;
import org.bukkit.World;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;

import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class main extends JavaPlugin {

	public static main plugin;
	public final Logger logger = Logger.getLogger("Minecraft");
	public ServerChatPlayerListener playerListener = new ServerChatPlayerListener(this);
	
@Override
public void onDisable() {

	PluginDescriptionFile pdffile = this.getDescription();
	this.logger.info(pdffile.getName() + " is now disabled.");
}

WorldGuardPlugin worldGuard;

public static Economy econ = null;
public static Chat chat = null;

private FileConfiguration customConfig = null;
private File customConfigFile = null;

public void reloadCustomConfig() {
    if (customConfigFile == null) {
    customConfigFile = new File(getDataFolder(), "db.yml");
    }
    customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
 
    // Look for defaults in the jar
    InputStream defConfigStream = this.getResource("db.yml");
    if (defConfigStream != null) {
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        customConfig.setDefaults(defConfig);
    }
}

public FileConfiguration getCustomConfig() {
    if (customConfig == null) {
        this.reloadCustomConfig();
    }
    return customConfig;
}

public void saveCustomConfig() {
    if (customConfig == null || customConfigFile == null) {
    return;
    }
    try {
        getCustomConfig().save(customConfigFile);
    } catch (IOException ex) {
        this.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
    }
}

private FileConfiguration languageConfig = null;
private File languageConfigFile = null;

public void reloadlanguageConfig() {
    if (languageConfigFile == null) {
    	languageConfigFile = new File(getDataFolder(), "language.yml");
    }
    languageConfig = YamlConfiguration.loadConfiguration(languageConfigFile);
 
    // Look for defaults in the jar
    InputStream defConfigStream = this.getResource("language.yml");
    if (defConfigStream != null) {
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        languageConfig.setDefaults(defConfig);
    }
}

public FileConfiguration getlanguageConfig() {
    if (languageConfig == null) {
        this.reloadlanguageConfig();
    }
    return languageConfig;
}

public void savelanguageConfig() {
    if (languageConfig == null || languageConfigFile == null) {
    return;
    }
    try {
        getlanguageConfig().save(languageConfigFile);
    } catch (IOException ex) {
        this.getLogger().log(Level.SEVERE, "Could not save config to " + languageConfigFile, ex);
    }
}


@Override
public void onEnable() {
	
	this.getServer().getPluginManager().registerEvents(new ServerChatPlayerListener(this), this);

	PluginDescriptionFile pdffile = this.getDescription();
	this.logger.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled!");
	
	
	getlanguageConfig().options().header("BuyLand Language File.");
	
	
	getlanguageConfig().addDefault("buyland.general.permission", "You do not have permission for that command.");	
	getlanguageConfig().addDefault("buyland.general.reload", "Config reloaded!");
	getlanguageConfig().addDefault("buyland.general.error1", "Error! Region name was incorrect.");
	
	getlanguageConfig().addDefault("buyland.sell.forsale", "This land is for sale.");
	getlanguageConfig().addDefault("buyland.sell.back1", "You have sold back the land for ");
	getlanguageConfig().addDefault("buyland.sell.back2", ". Your balance is: %s");
	getlanguageConfig().addDefault("buyland.sell.dontown", "You do not own this land!");
	
	getlanguageConfig().addDefault("buyland.buy.max", "You have bought the Maximum amount of land allowed.");
	getlanguageConfig().addDefault("buyland.buy.welcome1", "Welcome to ");
	getlanguageConfig().addDefault("buyland.buy.welcome2", "`s Land!");
	getlanguageConfig().addDefault("buyland.buy.cantafford", "%s to buy the land.");
	
	getlanguageConfig().addDefault("buyland.buy.bought", "You bought the land for %s and you now have %s");
	getlanguageConfig().addDefault("buyland.buy.dontown", "Sorry this land is not buyable.");
	
	getlanguageConfig().addDefault("buyland.price.price", "You currently have %s to purchase this land.");
	getlanguageConfig().addDefault("buyland.price.cost", "This land is buyable and costs: ");
	getlanguageConfig().addDefault("buyland.price.max1", "You have ");
	getlanguageConfig().addDefault("buyland.price.max2", " pieces of land. The Max is ");
	
	getlanguageConfig().addDefault("buyland.price.dontown", "Sorry this land is not buyable.");
	
	getlanguageConfig().options().copyDefaults(true);
	this.savelanguageConfig();
	
	getCustomConfig().options().header("BuyLand DB File. Used for keeping track of how many plots a user has.");
	getCustomConfig().addDefault("user", 0);
	getCustomConfig().options().copyDefaults(true);
	this.saveCustomConfig();
		
	final FileConfiguration config = this.getConfig();
	config.options().header("BuyLand... Besure to make prices have .00 or it may break.");
	config.addDefault("buyland.defaultprice", 100.00);
	config.addDefault("buyland.percentsellback", 1.00);
	config.addDefault("buyland.maxamountofland", 1);
	config.options().copyDefaults(true);
	saveConfig();
	
	getWorldGuard();
		
    if (!setupEconomy() ) {
      this.logger.info("Could not load due to Vault not being loaded.");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }
    
    setupChat();
	
}

//This is for Vault.
private boolean setupEconomy() {
    if (getServer().getPluginManager().getPlugin("Vault") == null) {
        return false;
    }
    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
        return false;
    }
    econ = rsp.getProvider();
    return econ != null;
}

private boolean setupChat() {
    RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
    chat = rsp.getProvider();
    return chat != null;
}



public WorldGuardPlugin getWorldGuard()
{
    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
    if ((plugin == null) || (!(plugin instanceof WorldGuardPlugin)))
    {
        return null; //throws a NullPointerException, telling the Admin that WG is not loaded.
    }
    return (WorldGuardPlugin)plugin;
} 


public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	
	Player player = null;
	if (sender instanceof Player) {
		player = (Player) sender;
	}

	if (args.length == 0){
		
		player.sendMessage(ChatColor.RED + "BuyLand is a product of chriztopia.com"); 
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Try using /buyland [region_name] or /priceland [region_name] or /sellland [region_name]");  
	}
		   if (args.length > 0){
			   
//RELOADBUYLADN COMMAND
if (cmd.getName().equalsIgnoreCase("reloadbuyland")){
	if (player.hasPermission("buyland.reload") || player.hasPermission("buyland.*")){			  
					 reloadConfig();  
					 reloadCustomConfig();
					 reloadlanguageConfig();
						String convertedgeneral2 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.reload"));
						
					 player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedgeneral2);
				   }else{
						String convertedgeneral = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.permission"));
						
						player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedgeneral);
					}
			   }
			   

//SELLLAND COMMAND	
	if (cmd.getName().equalsIgnoreCase("sellland")){ 
if (player.hasPermission("buyland.sell") || player.hasPermission("buyland.*")){
        World world1 = player.getWorld();
        RegionManager regionManager = this.getWorldGuard().getRegionManager(world1);

        if(regionManager.getRegionExact(args[0]) == null){
			String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
			
        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
     
        }else{
        
        ProtectedRegion set2 = regionManager.getRegionExact(args[0]);

		DefaultDomain owner = set2.getOwners();
	String owner2 = owner.toPlayersString();

	String pn = player.getName().toLowerCase();
	
	
	if (owner2.contains(pn)){
	        Boolean bflag = set2.getFlag(DefaultFlag.BUYABLE);
        Double pflag = set2.getFlag(DefaultFlag.PRICE);
        
	if (bflag == null){
		bflag = false;
	}
	
	if (pflag == null){
		pflag = this.getConfig().getDouble("buyland.defaultprice");
	}
	
	Double finalp = pflag * this.getConfig().getDouble("buyland.percentsellback");
	String convertedl1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.sell.back1"));
	String convertedl2 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.sell.back2"));
	
		
		EconomyResponse r = econ.depositPlayer(player.getName(), finalp);
         player.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedl1 + finalp + convertedl2, econ.format(r.balance)));

		String convertedforsale = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.sell.forsale"));
		

   	  DefaultDomain dd = new DefaultDomain();
	    dd.removePlayer(pn);
	 set2.setOwners(dd);
	 set2.setFlag(DefaultFlag.BUYABLE, true);
	 set2.setFlag(DefaultFlag.GREET_MESSAGE, convertedforsale);
	 
		String nm = player.getName();
		int numofland = this.getCustomConfig().getInt(nm);
		
   	 int finalland = numofland - 1;
	 
   	 this.getCustomConfig().set(nm, finalland);
   	 this.saveCustomConfig();
   	this.reloadCustomConfig();
	 
	    try
	    {
	    	regionManager.save();
	    }
	     catch (Exception exp)
	    { }
         
         
	}else{
		String convertedownland = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.sell.dontown"));
		
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedownland);
	}
}
	}else{
		String convertedgeneral = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.permission"));
		
		
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedgeneral);
	}
		   }
	
//BUYLAND COMMAND!
	if (cmd.getName().equalsIgnoreCase("buyland")){
	if (player.hasPermission("buyland.buy") || player.hasPermission("buyland.*")){
        World world1 = player.getWorld();
        RegionManager regionManager = this.getWorldGuard().getRegionManager(world1);
        if(regionManager.getRegionExact(args[0]) == null){
        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
			
        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
     
        }else{
        ProtectedRegion set2 = regionManager.getRegionExact(args[0]);
        
        Boolean bflag = set2.getFlag(DefaultFlag.BUYABLE);
        Double pflag = set2.getFlag(DefaultFlag.PRICE);
        
	if (bflag == null){
		bflag = false;
	}
	if (pflag == null){
	pflag = this.getConfig().getDouble("buyland.defaultprice");
	}
	
	String nm = player.getName();
	int numofland = this.getCustomConfig().getInt(nm);
	int maxofland = this.getConfig().getInt("buyland.maxamountofland");
	

if (numofland +1 > maxofland){
	String convertedmax = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.buy.max"));
	
	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedmax);
}else{
		
 String test = bflag.toString();

 if (test == "true"){
     EconomyResponse r = econ.withdrawPlayer(player.getName(), pflag);
     if(r.transactionSuccess()) {
    	 
    	 
    	 String convertedb = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.buy.bought"));
    	    		
    	 
         sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedb, econ.format(r.amount), econ.format(r.balance)));
         String p1 = player.getName();
    	 set2.setFlag(DefaultFlag.BUYABLE, false);

    		String convertedw1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.buy.welcome1"));
    		String convertedw2 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.buy.welcome2"));
    		
    	 
    	 set2.setFlag(DefaultFlag.GREET_MESSAGE, convertedw1 + p1 + convertedw2);
    	 
    	 int finalland = numofland + 1;
    	 
    	 this.getCustomConfig().set(nm, finalland);
    	 this.saveCustomConfig();
    	 this.reloadCustomConfig();
    	 
    	  DefaultDomain dd = new DefaultDomain();
    	       
    	    dd.addPlayer(p1);
    	   
    	 set2.setOwners(dd);
         
     } else {
    	 
    	 String converteda1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.buy.cantafford"));
         sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + converteda1, r.errorMessage));
     }

	    try
	    {
	    	regionManager.save();
	    }

	     catch (Exception exp)


	    { }
	 
	    
 }else{
	  String convertednonbuy = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.buy.dontown"));
		
	 
	 player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertednonbuy);
 }

 
	}
        }
	}else{
		String convertedgeneral = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.permission"));
		
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedgeneral);
	}
	
	
		   }
			   //END of buyLAND
			   
			   

//PRICELAND COMMAND
if (cmd.getName().equalsIgnoreCase("priceland")){ 
if (player.hasPermission("buyland.price") || player.hasPermission("buyland.*")){					
				        World world1 = player.getWorld();
				        RegionManager regionManager = this.getWorldGuard().getRegionManager(world1);
				        
				        if(regionManager.getRegionExact(args[0]) == null){
				        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
							
				        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
				     
				        }else{
				        
				        ProtectedRegion set2 = regionManager.getRegionExact(args[0]);
				        Boolean bflag = set2.getFlag(DefaultFlag.BUYABLE);
				        Double pflag = set2.getFlag(DefaultFlag.PRICE);
				        
					if (bflag == null){
						bflag = false;
					}
					
					if (pflag == null){
						pflag = this.getConfig().getDouble("buyland.defaultprice");
					}
				
				 String test = bflag.toString();

				 if (test == "true"){
					 Double aflag = 0.00;
					 EconomyResponse r = econ.withdrawPlayer(player.getName(), aflag);
					 
					    String convertedcosts = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.price.cost"));
						
					 
					    player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedcosts + pflag);
						
					    String convertedprice = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.price.price"));
						
					    player.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedprice, econ.format(r.balance)));
				         
				         String nm = player.getName();
				         int numofland = this.getCustomConfig().getInt(nm);
				         int maxofland = this.getConfig().getInt("buyland.maxamountofland");
				         
				         String convertedpricemax1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.price.max1"));
				         String convertedpricemax2 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.price.max2"));
		
								
				         
				         player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedpricemax1 + numofland + convertedpricemax2 + maxofland + ".");
				         
				 }else{
						String convertednotforsale = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.price.downown"));
						
					 player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertednotforsale);
				 }
				        }
				 
					}else{
						String convertedgeneral = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.permission"));
						
						player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedgeneral);
					}
		   }

//LAND
	return true;
}
	return true;

}
}


