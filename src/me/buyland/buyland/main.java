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

@Override
public void onEnable() {
	
	
	//plugin.getServer().getPluginManager().registerEvents(this, this);
	this.getServer().getPluginManager().registerEvents(new ServerChatPlayerListener(this), this);

	PluginDescriptionFile pdffile = this.getDescription();
	this.logger.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled!");
	
	getCustomConfig().options().header("BuyLand DB File. Used for keeping track of how many plots a user has.");
	getCustomConfig().addDefault("user", 0);
	getCustomConfig().options().copyDefaults(true);
	this.saveCustomConfig();
		
	final FileConfiguration config = this.getConfig();
	config.options().header("BuyLand... Besure to make prices have .00 or it may break.");
	config.addDefault("buyland.defaultprice", 100.00);
	config.addDefault("buyland.percentsellback", 0.50);
	config.addDefault("buyland.maxamountofland", 1);
	config.options().copyDefaults(true);
	saveConfig();
	
	
	getWorldGuard();
		
    if (!setupEconomy() ) {
        //log.info(Level.SEVERE, String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
        getServer().getPluginManager().disablePlugin(this);
        return;
    }
    
    setupChat();
	
}

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
		
		player.sendMessage(ChatColor.GOLD + "BuyLand is a product of chriztopia.com"); 
		player.sendMessage(ChatColor.GOLD + "Try using /buyland [region_name] or /priceland [region_name] or /sellland [region_name]");  
	}
		   if (args.length > 0){
			   
			   if (cmd.getName().equalsIgnoreCase("reloadbuyland")){
				   if (player.isOp()){
					 reloadConfig();  
					 reloadCustomConfig();
				   }
			   
			   }
	if (cmd.getName().equalsIgnoreCase("sellland")){ 

        World world1 = player.getWorld();
        RegionManager regionManager = this.getWorldGuard().getRegionManager(world1);
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
		
		
		EconomyResponse r = econ.depositPlayer(player.getName(), finalp);
         player.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "You have sold back the land for " + finalp + ". Your balance is: %s", econ.format(r.balance)));


   	  DefaultDomain dd = new DefaultDomain();
	    dd.removePlayer(pn);
	 set2.setOwners(dd);
	 set2.setFlag(DefaultFlag.BUYABLE, true);
	 set2.setFlag(DefaultFlag.GREET_MESSAGE, "This Land Is For Sale!");
	 
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
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "You do not own this land!");
	}
	}
	

	   
	if (cmd.getName().equalsIgnoreCase("buyland")){ 
	
        World world1 = player.getWorld();
        RegionManager regionManager = this.getWorldGuard().getRegionManager(world1);
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
	
player.sendMessage("num: " + numofland + " max: " + maxofland);

if (numofland +1 > maxofland){
	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "You have bought the Maximum amount of land allowed.");
}else{
		
 String test = bflag.toString();

 if (test == "true"){
     EconomyResponse r = econ.withdrawPlayer(player.getName(), pflag);
     if(r.transactionSuccess()) {
         sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "You bought the land for %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
         String p1 = player.getName();
    	 set2.setFlag(DefaultFlag.BUYABLE, false);
    	 set2.setFlag(DefaultFlag.GREET_MESSAGE, "Welcome to " + p1 + "`s Land!");
    	 
    	 int finalland = numofland + 1;
    	 
    	 this.getCustomConfig().set(nm, finalland);
    	 this.saveCustomConfig();
    	 this.reloadCustomConfig();
    	 
    	  DefaultDomain dd = new DefaultDomain();
    	       
    	    dd.addPlayer(p1);
    	   
    	 set2.setOwners(dd);
         
     } else {
         sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "%s to buy the land.", r.errorMessage));
     }

	    try
	    {
	    	regionManager.save();
	    }

	     catch (Exception exp)


	    { }
	 
	    
 }else{
	 player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Sorry this land is not buyable.");
 }

 
	}
	}
			   //END of buyLAND
			   
			   
			   
			   
			  
					if (cmd.getName().equalsIgnoreCase("priceland")){ 
					
				        World world1 = player.getWorld();
				        RegionManager regionManager = this.getWorldGuard().getRegionManager(world1);
				        ProtectedRegion set2 = regionManager.getRegionExact(args[0]);
				        Boolean bflag = set2.getFlag(DefaultFlag.BUYABLE);
				        Double pflag = set2.getFlag(DefaultFlag.PRICE);
				        
					if (bflag == null){
						bflag = false;
					}
					
					if (pflag == null){
						pflag = this.getConfig().getDouble("buyland.defaultprice");
					}
						//set2.getPriority();
				 String test = bflag.toString();

				 if (test == "true"){
					 Double aflag = 0.00;
					 EconomyResponse r = econ.withdrawPlayer(player.getName(), aflag);
					    player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "This land is buyable and costs: " + pflag);
				         player.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "You currently have %s to purchase this land.", econ.format(r.balance)));
				         
				         String nm = player.getName();
				         int numofland = this.getCustomConfig().getInt(nm);
				         int maxofland = this.getConfig().getInt("buyland.maxamountofland");
				         player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "You have " + numofland + " pieces of land. The Max is " + maxofland + ".");
				         
				         
				         
				 }else{
					 player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Sorry this land is not buyable.");
				 }

				 
					}
					

			   
//LAND
	return true;
}
	return true;

}
}


