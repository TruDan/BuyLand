package me.buyland.buyland;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import org.bukkit.plugin.RegisteredServiceProvider;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.FilenameException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class main extends JavaPlugin {
	
	public static main plugin;
	public final Logger logger = Logger.getLogger("Minecraft");
	public ServerChatPlayerListener playerListener = new ServerChatPlayerListener(this);

@Override
public void onDisable() {
	saveRentConfig();
	PluginDescriptionFile pdffile = this.getDescription();
	this.logger.info(pdffile.getName() + " is now disabled.");
}

WorldGuardPlugin worldGuard;

public static Economy econ = null;
public static Chat chat = null;

private LWC LWC1;
//---------------

private FileConfiguration signConfig = null;
private File signConfigFile = null;

public void reloadsignConfig() {
    if (signConfigFile == null) {
    signConfigFile = new File(getDataFolder(), "signs.yml");
    }
    signConfig = YamlConfiguration.loadConfiguration(signConfigFile);
 
    // Look for defaults in the jar
    InputStream defConfigStream = this.getResource("signs.yml");
    if (defConfigStream != null) {
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        signConfig.setDefaults(defConfig);
    }
}

public FileConfiguration getsignConfig() {
    if (signConfig == null) {
        this.reloadsignConfig();
    }
    return signConfig;
}

public void savesignConfig() {
    if (signConfig == null || signConfigFile == null) {
    return;
    }
    try {
        getsignConfig().save(signConfigFile);
    } catch (IOException ex) {
        this.getLogger().log(Level.SEVERE, "Could not save config to " + signConfigFile, ex);
    }
}

//----------
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
//


//
private FileConfiguration RentConfig = null;
private File RentConfigFile = null;

public void reloadRentConfig() {
    if (RentConfigFile == null) {
    	RentConfigFile = new File(getDataFolder(), "rent.yml");
    }
    RentConfig = YamlConfiguration.loadConfiguration(RentConfigFile);
 
    // Look for defaults in the jar
    InputStream defConfigStream = this.getResource("rent.yml");
    if (defConfigStream != null) {
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        RentConfig.setDefaults(defConfig);
    }
}

public FileConfiguration getRentConfig() {
    if (RentConfig == null) {
        this.reloadRentConfig();
    }
    return RentConfig;
}

public void saveRentConfig() {
    if (RentConfig == null || RentConfigFile == null) {
    return;
    }
    try {
        getRentConfig().save(RentConfigFile);
    } catch (IOException ex) {
        this.getLogger().log(Level.SEVERE, "Could not save config to " + RentConfigFile, ex);
    }
}
//--------------------------
//
private FileConfiguration rentdbConfig = null;
private File rentdbConfigFile = null;

public void reloadrentdbConfig() {
    if (rentdbConfigFile == null) {
    	rentdbConfigFile = new File(getDataFolder(), "rentdb.yml");
    }
    rentdbConfig = YamlConfiguration.loadConfiguration(rentdbConfigFile);
 
    // Look for defaults in the jar
    InputStream defConfigStream = this.getResource("rentdb.yml");
    if (defConfigStream != null) {
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        rentdbConfig.setDefaults(defConfig);
    }
}

public FileConfiguration getrentdbConfig() {
    if (rentdbConfig == null) {
        this.reloadrentdbConfig();
    }
    return rentdbConfig;
}

public void saverentdbConfig() {
    if (rentdbConfig == null || rentdbConfigFile == null) {
    return;
    }
    try {
        getrentdbConfig().save(rentdbConfigFile);
    } catch (IOException ex) {
        this.getLogger().log(Level.SEVERE, "Could not save config to " + rentdbConfigFile, ex);
    }
}


@Override
public void onEnable() {


	try {
	    Metrics metrics = new Metrics(this);
	   metrics.start();
	} catch (IOException e) {
	    // Failed to submit the stats :-(
	}
	this.getServer().getPluginManager().registerEvents(new ServerChatPlayerListener(this), this);


	PluginDescriptionFile pdffile = this.getDescription();
	this.logger.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled!");
	
	getlanguageConfig().options().header("BuyLand Language File.");
	
	//language file
	getlanguageConfig().addDefault("buyland.general.permission", "You do not have permission for that command.");	
	getlanguageConfig().addDefault("buyland.general.reload", "Config reloaded!");
	getlanguageConfig().addDefault("buyland.general.error1", "Error! Region name was incorrect.");
	getlanguageConfig().addDefault("buyland.general.error2", "Error! Enter a Number/Price.");
	
	getlanguageConfig().addDefault("buyland.admin.forsale", "This Region has been placed back for sale.");
	
	getlanguageConfig().addDefault("buyland.rent.forrent", "This land is for rent!");
	getlanguageConfig().addDefault("buyland.rent.noperm", "You dont have permission to do that!");
	getlanguageConfig().addDefault("buyland.rent.tenant", "This land currently has a tenant - Time left: ");
	getlanguageConfig().addDefault("buyland.rent.rentby", "This land is being rented by ");
	getlanguageConfig().addDefault("buyland.rent.notbe", "This land can not be rented.");
	getlanguageConfig().addDefault("buyland.rent.cantafford", "%s to buy the land.");
	getlanguageConfig().addDefault("buyland.rent.error1", "Sorry Rentable land can not be bought nor sold.");
	getlanguageConfig().addDefault("buyland.rent.error2", "Sorry this region is not rentable.");
	getlanguageConfig().addDefault("buyland.rent.max", "You have rented the Maximum amount of land allowed.");
	
	
	getlanguageConfig().addDefault("buyland.sell.forsale", "This land is for sale.");
	getlanguageConfig().addDefault("buyland.sell.back1", "You have sold back the land for ");
	getlanguageConfig().addDefault("buyland.sell.back2", ". Your balance is: %s");
	getlanguageConfig().addDefault("buyland.sell.dontown", "You do not own this land!");
	getlanguageConfig().addDefault("buyland.sell.notsneak", "You must be sneaking when you click a sign to sell land!");
	
	
	
	getlanguageConfig().addDefault("buyland.member.removemember", "Removed Member!");
	getlanguageConfig().addDefault("buyland.member.addmember", "Added Member!");

	
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
	
	getRentConfig().options().header("Rent File");
	getRentConfig().addDefault("rent.placeholder.time", 0);
	getRentConfig().addDefault("rent.placeholder.rentable", true);
	getRentConfig().addDefault("rent.placeholder.world", "world");
	getRentConfig().addDefault("rent.placeholder.costpermin", 1.0);

	getRentConfig().options().copyDefaults(true);
	this.saveRentConfig();
	
	getsignConfig().options().header("BuyLand Sign DB File. Used to keep track of signs.");
	getsignConfig().addDefault("sign.placeholder", "location");
	getsignConfig().options().copyDefaults(true);
	this.savesignConfig();
	
	getCustomConfig().options().header("BuyLand DB File. Used for keeping track of how many plots a user has.");
	getCustomConfig().addDefault("user", 0);
	getCustomConfig().options().copyDefaults(true);
	this.saveCustomConfig();
	
	getrentdbConfig().options().header("BuyLand Rent DB File. Used for keeping track of how many rentable plots a user has.");
	getrentdbConfig().addDefault("user", 0);
	getrentdbConfig().options().copyDefaults(true);
	this.saverentdbConfig();
	
		
	final FileConfiguration config = this.getConfig();
	config.options().header("BuyLand... Besure to make prices have .00 or it may break. Double");
	config.addDefault("buyland.defaultprice", 100.00);
	config.addDefault("buyland.percentsellback", 1.00);
	config.addDefault("buyland.resetlandonsale", true);
	config.addDefault("buyland.landpriority", 1);
	config.addDefault("buyland.usepriceperblock", false);
	config.addDefault("buyland.defaultpriceperblock", 1.00);
	config.addDefault("buyland.rentbroadcastmsg", true);
	config.addDefault("buyland.landgreeting", true);
	config.addDefault("buyland.landgreetingerasemsg", false);
	config.addDefault("buyland.breaksignonbuy", false);
	config.addDefault("buyland.denyentrytoland", false);
	config.addDefault("buyland.removelwcprotection", false);
	config.addDefault("buyland.defaultrentcostpermin", 1.0);
	config.addDefault("buyland.maxamountofrentland", 1);
	config.addDefault("buyland.notifyplayerofrenttime", true);
	config.addDefault("buyland.maxamountofland", 1);
	config.addDefault("buyland.offlinelimitindays", 30);
	config.addDefault("buyland.offlinelimitenable", true);
	
	config.options().copyDefaults(true);
	saveConfig();
	getWorldGuard();
	
	
	

	new BukkitRunnable()
	{
    public void run() {
    	
    	if(config.getBoolean("buyland.offlinelimitenable") == true){
    		
    		for (World w1 : Bukkit.getWorlds())
    		{
if (w1 == null){
	return;
}
	
	Map<String, ProtectedRegion> regionMap = WGBukkit.getRegionManager(w1).getRegions();

	
	for(ProtectedRegion region : regionMap.values()) {
		
		
if(region.getFlag(DefaultFlag.BUYABLE) == null){

}else{
		if(region.getFlag(DefaultFlag.BUYABLE) == false){
			
			DefaultDomain du = region.getOwners();
	    	String pn = du.toUserFriendlyString();

	    long lastseen = Bukkit.getOfflinePlayer(pn).getLastPlayed();
	    long current = System.currentTimeMillis();
	    
	    if(!Bukkit.getOfflinePlayer(pn).hasPlayedBefore()){
	    	
	    	return;
	    	
	    }
	    
	    
        long difference = current - lastseen;
        long con = getConfig().getLong("buyland.offlinelimitindays");
        long month = con * 24*60*60*1000L;

        if (difference > month){
        	
        	
		       	for (Player p2 : Bukkit.getOnlinePlayers()) {
	        		if(p2.isOp() || p2.hasPermission("buyland.admin")){
	        			
	        			if (getRentConfig().contains("rent." + region.getId() + ".rentable")){
	        				
	        		   
	        			}else{	
	        			
     				
        				p2.sendMessage("Owner: " + pn + " Region: " + region.getId());
	        			Bukkit.dispatchCommand(Bukkit.getPlayer(p2.getName()), "abl forsale " + region.getId());		
	        				        			
	        			}
	        
	
        	}
        }
		}
        
				
		}
	}				
	//}


//--------------------------------------	
	

	}	
	
    	}
	
    }else{
    	//Bukkit.getLogger().info("Auto Remove Disabled...");
		
    }
	
    }
	}.runTaskTimer(this, 20L, 1200L);
	
	new BukkitRunnable()
	{
    public void run() {
    	

ConfigurationSection yaml = getRentConfig().getConfigurationSection("rent");
for (String regionName : yaml.getKeys(false)) {
	
	
	if (yaml.contains(regionName + ".time")){
		
	}else{
		
  	//Protection May Cause Lag
	    	getRentConfig().addDefault("rent."+ regionName  + ".time", 0);
	    	getRentConfig().addDefault("rent."+ regionName  + ".rentable", true);
	    	getRentConfig().addDefault("rent."+ regionName  + ".world", "world");
	    	getRentConfig().addDefault("rent."+ regionName  + ".costpermin", 1.0);
	    	getRentConfig().options().copyDefaults(true);
	    	//getServer().getLogger().info("BuyLand_Debug - Save Rent Config Start 12");
	    	saveRentConfig();
	    	//getServer().getLogger().info("BuyLand_Debug - Save Rent Config END 12");
		
	}
	
      
    if (yaml.getBoolean(regionName + ".rentable") == false && System.currentTimeMillis() > yaml.getLong(regionName + ".time")) {
   
    	
    	if (config.getBoolean("buyland.rentbroadcastmsg") == true){
        	getServer().broadcastMessage(ChatColor.RED + "BuyLand: " + ChatColor.YELLOW + regionName + " is now rentable!");
    	}

    	getRentConfig().set("rent." + regionName + ".rentable", true);
    	String world1 = yaml.getString(regionName + ".world");
    	
    	World world = Bukkit.getWorld(world1);
    
		RegionManager regionManager = getWorldGuard().getRegionManager(world);
		ProtectedRegion set2 = regionManager.getRegionExact(regionName);
		int x = set2.getMinimumPoint().getBlockX();
		int y = set2.getMinimumPoint().getBlockY();
		int z = set2.getMinimumPoint().getBlockZ();
		Vector v1 = new Vector(x,y,z);
		
		File file = new File(getDataFolder() + File.separator + "data" + File.separator + regionName + ".schematic");
		ResetMap(file, v1, world);
//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\		

		
		
		//test101
		//LWC
		if (getConfig().getBoolean("buyland.removelwcprotection") == true){
		   	
		final Plugin plugin;
		plugin = Bukkit.getServer().getPluginManager().getPlugin("LWC");
		if(plugin != null && plugin instanceof LWCPlugin) {
			LWC1 = ((LWCPlugin) plugin).getLWC();
		}
		
		
	  
	  World world9 = Bukkit.getWorld(world1);
	  RegionManager regionManager2 = getWorldGuard().getRegionManager(world9);
	  if(regionManager2.getRegionExact(regionName) == null){
	  //	this.getServer().getLogger().info("NULL");
	  }else{

	  	
	  	ProtectedRegion set4 = regionManager2.getRegionExact(regionName);
	  	int minx = set4.getMinimumPoint().getBlockX();
	  	int miny = set4.getMinimumPoint().getBlockY();
	  	int minz = set4.getMinimumPoint().getBlockZ();

	  	int maxx = set4.getMaximumPoint().getBlockX();
	  	int maxy = set4.getMaximumPoint().getBlockY();
	  	int maxz = set4.getMaximumPoint().getBlockZ();

	  	
			for(int x11=minx; x11<maxx; x11++) {
				for(int y11=miny; y11<maxy; y11++) {
					for(int z11=minz; z11<maxz; z11++) {
						
		        		Protection protection = LWC1.findProtection(world9, x11, y11, z11);
		        		if(protection != null) {
		        			protection.remove();
		        		//this.getServer().getLogger().info("Removed LWC Protection from Plot: " + args[0]);
		        		}else{
		        		
		        		}
						
						
					}
				}
			}

	  }
		}
	//************************		
		
		
		
		
//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
if (config.getBoolean("buyland.landgreeting") == true){
set2.setFlag(DefaultFlag.GREET_MESSAGE, "This land is for rent!");
}else if (config.getBoolean("buyland.landgreetingerasemsg") == true){
set2.setFlag(DefaultFlag.GREET_MESSAGE, null);
}

if (config.getBoolean("buyland.denyentrytoland") == true){
set2.setFlag(DefaultFlag.ENTRY, State.DENY);
}else{
set2.setFlag(DefaultFlag.ENTRY, null);
}



    	DefaultDomain du = set2.getOwners();
    	
    	String pn = du.toString();
    	
		 String nm = du.toUserFriendlyString();
		 int numofland = getrentdbConfig().getInt(nm);
		 int finalland = numofland - 1;
		 getrentdbConfig().set(nm, finalland);
		 saverentdbConfig();
		 reloadrentdbConfig();
		
	   	  DefaultDomain dd = new DefaultDomain();
	   	
		 dd.removePlayer(pn);
		 set2.setOwners(dd);
		 set2.setMembers(dd);
		 
		// Check if region has any owners
		 if (set2.getOwners().size() == 0) {
		     //it doesnt exit
		 }else{
			 for (String p2 : set2.getOwners().getPlayers()) {	
				 set2.getOwners().removePlayer(p2);
	        		}
		 }
		 
			// Check if region has any members
		 if (set2.getMembers().size() == 0) {
		     //it doesnt exit
		 }else{
			 for (String p2 : set2.getMembers().getPlayers()) {	
				 set2.getMembers().removePlayer(p2);
	        		}
		 }

		 
	    try
	    {
	    	regionManager.save();
	    }
	     catch (Exception exp)
	    { }
       
		 saveRentConfig();
	
		 reloadRentConfig();
		
		
        	    }

        	} 


        }
	}.runTaskTimer(this, 20L, 1200L);

			
    if (!setupEconomy() ) {
      this.logger.info("Could not load due to Vault not being loaded.");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }
    setupChat();	
    setupPermissions();	
  

    
//Check for data folder then create it.   
    File f = new File(getDataFolder() + File.separator + "data" + File.separator + "placeholder.txt");
    if (!f.exists()) {
    f.getParentFile().mkdirs();
    try {
   
    f.createNewFile();
    } catch (IOException ex) {
    // Handle error
    }
    }
    
}


public static void ResetMap(File file, Vector v, World worldf) {
    if (file.exists()) {
        try {
            BukkitWorld BWf = new BukkitWorld(worldf);
            EditSession es = new EditSession(BWf, 2000000);
            CuboidClipboard c1 = SchematicFormat.MCEDIT.load(file);
            c1.place(es, v, false);
        } catch (DataException ex) {
          //  logger.warning("'ResetMap()' DataException");
        } catch (IOException ex) {
          //  logger.warning("'ResetMap()' IOException");
        } catch (MaxChangedBlocksException ex) {
         //   logger.warning("'ResetMap()' MaxChangedBlocksException");
        }
    } else {
       // Logger.warning(("File does not exist."));
    }
}

//public static Economy econ = null;

//This is for Vault.
public static Permission permission = null;

private boolean setupPermissions()
{
    RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
    if (permissionProvider != null) {
        permission = permissionProvider.getProvider();
    }
    return (permission != null);
}


private boolean setupChat()
{
    RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
    if (chatProvider != null) {
        chat = chatProvider.getProvider();
    }

    return (chat != null);
}

private boolean setupEconomy()
{
    RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    if (economyProvider != null) {
        econ = economyProvider.getProvider();
    }

    return (econ != null);
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

public static Selection getWorldEditSelection(Player ply) {
    Plugin we = Bukkit.getPluginManager().getPlugin("WorldEdit");
    if(we != null && we instanceof WorldEditPlugin) {
        return ((WorldEditPlugin) we).getSelection(ply);
    }
    return null;
}


public void AddProtectedRegion(String p1, String world, int x1, int y1, int z1, int x2, int y2, int z2, String name, String abc)
{
if (abc == "buy"){
	    World world1 = Bukkit.getWorld(world);
    // get the region manager 
    RegionManager rm = this.getWorldGuard().getRegionManager(world1);
    // make a cuboid with two points to create a 3d cube in the world
    BlockVector b2 = new BlockVector(x1, y1, z1);
    BlockVector b1 = new BlockVector(x2, y2, z2);
    // create the protected region
    ProtectedCuboidRegion pr = new ProtectedCuboidRegion(name, b1, b2);
    rm.addRegion(pr);
    
    pr.setFlag(DefaultFlag.BUYABLE, true);
    String convertedforsale = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.sell.forsale"));
	
    
    if (this.getConfig().getBoolean("buyland.landgreeting") == true){
    	
    pr.setFlag(DefaultFlag.GREET_MESSAGE, convertedforsale);
    }else if (this.getConfig().getBoolean("buyland.landgreetingerasemsg") == true){
    	pr.setFlag(DefaultFlag.GREET_MESSAGE, null);
    }
    
	if (this.getConfig().getBoolean("buyland.denyentrytoland") == true){
        pr.setFlag(DefaultFlag.ENTRY, State.DENY);
		}else{
			pr.setFlag(DefaultFlag.ENTRY, null);
		}
    
    
    pr.setPriority(this.getConfig().getInt("buyland.landpriority"));
    
   // DefaultDomain dd = new DefaultDomain();
    // add the player to the region      
  //  dd.addPlayer(p1);
    // set the player as the owner
  //  pr.setOwners(dd);

    logger.info("BuyLand: Added region: " + name);
    
    try
    {
        rm.save();
    }
     catch (Exception exp)
    { }
}


if (abc == "rent"){
    World world1 = Bukkit.getWorld(world);
// get the region manager 
RegionManager rm = this.getWorldGuard().getRegionManager(world1);
// make a cuboid with two points to create a 3d cube in the world
BlockVector b2 = new BlockVector(x1, y1, z1);
BlockVector b1 = new BlockVector(x2, y2, z2);
// create the protected region
ProtectedCuboidRegion pr = new ProtectedCuboidRegion(name, b1, b2);
rm.addRegion(pr);

//pr.setFlag(DefaultFlag.BUYABLE, true);
String forrent = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.forrent"));
if (this.getConfig().getBoolean("buyland.landgreeting") == true){
	

pr.setFlag(DefaultFlag.GREET_MESSAGE, forrent);
}else if (this.getConfig().getBoolean("buyland.landgreetingerasemsg") == true){
	pr.setFlag(DefaultFlag.GREET_MESSAGE, null);
}

if (this.getConfig().getBoolean("buyland.denyentrytoland") == true){
    pr.setFlag(DefaultFlag.ENTRY, State.DENY);
	}else{
		pr.setFlag(DefaultFlag.ENTRY, null);
	}


pr.setPriority(this.getConfig().getInt("buyland.landpriority"));

double defaultcostpermin = this.getConfig().getDouble("buyland.defaultrentcostpermin");

this.getRentConfig().set("rent." + name + ".time", 0);
this.getRentConfig().set("rent." + name + ".rentable", true);
this.getRentConfig().set("rent." + name + ".world", world);
this.getRentConfig().set("rent." + name + ".costpermin", defaultcostpermin);

//this.logger.info("BuyLand_Debug - Save Rent Config Start 14");
saveRentConfig();
//this.logger.info("BuyLand_Debug - Save Rent Config End 14");
//this.logger.info("BuyLand_Debug - Reload Rent Config Start 14");

reloadRentConfig();
//this.logger.info("BuyLand_Debug - Reload Rent Config End 14");

//DefaultDomain dd = new DefaultDomain();
// add the player to the region      
//dd.addPlayer(p1);
// set the player as the owner
//pr.setOwners(dd);

logger.info("BuyLand: Added region: " + name);

try
{
    rm.save();
}
 catch (Exception exp)
{ }
}


}

@SuppressWarnings("deprecation")
public void saveSchematic(Location loc1, Location loc2, String schematic, Player player) throws EmptyClipboardException, IOException, DataException {
    WorldEditPlugin wep = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
    WorldEdit we = wep.getWorldEdit();
    BukkitPlayer localPlayer = wep.wrapPlayer(player);
    LocalSession localSession = we.getSession(localPlayer);
    EditSession editSession = localSession.createEditSession(localPlayer);
 
    double x1 = Math.min(loc1.getX(), loc2.getX());
    double x2 = Math.max(loc1.getX(), loc2.getX());
    double y1 = Math.min(loc1.getY(), loc2.getY());
    double y2 = Math.max(loc1.getY(), loc2.getY());
    double z1 = Math.min(loc1.getZ(), loc2.getZ());
    double z2 = Math.max(loc1.getZ(), loc2.getZ());
 
    Location l1 = new Location(loc1.getWorld(), x1, y1, z1);
    Location l2 = new Location(loc1.getWorld(), x2, y2, z2);
    Vector min = new Vector(l1.getBlockX(), l1.getBlockY(), l1.getBlockZ());
    Vector max = new Vector(l2.getBlockX(), l2.getBlockY(), l2.getBlockZ());
 File d1 = new File(getDataFolder() + File.separator + "data" + File.separator);

    
    File saveFile;
    try {
        saveFile = we.getSafeSaveFile(localPlayer, d1, schematic, "schematic", new String[]{"schematic"});
    } catch (FilenameException ex) {
       // te.logUtil.warning(ex.getMessage());
        return;
    }
 
    editSession.enableQueue();
    CuboidClipboard clipboard = new CuboidClipboard(max.subtract(min).add(new Vector(1, 1, 1)), min);
    clipboard.copy(editSession);
    clipboard.saveSchematic(saveFile);
    editSession.flushQueue();
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

//stringtoloc
public Location stringToLoc(String string){
    String[] loc = string.split(":");
   // System.out.println(loc.toString());
    loc.toString();
    World world = Bukkit.getWorld(loc[0]);
    Double x = Double.parseDouble(loc[1]);
    Double y = Double.parseDouble(loc[2]);
    Double z = Double.parseDouble(loc[3]);

    return new Location(world, x, y, z);
}




public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	
	
	
	Player player = null;
	if (sender instanceof Player) {
		player = (Player) sender;
	

	if (args.length == 0){
		//RELOADBUYLADN COMMAND
		if (cmd.getName().equalsIgnoreCase("reloadbuyland")){
			if (player.hasPermission("buyland.reload") || player.hasPermission("buyland.all")){			  
							 reloadConfig();  
							 reloadCustomConfig();
							 reloadlanguageConfig();
							 reloadRentConfig();
							 reloadrentdbConfig();
							 reloadsignConfig();
							 
							String convertedgeneral2 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.reload"));
							 player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedgeneral2);
						   }else{
								String convertedgeneral = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.permission"));
								player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedgeneral);
						   }
					   
					   }else{
//General HELP
							PluginDescriptionFile pdffile = this.getDescription();
							
							if (cmd.getName().equalsIgnoreCase("rentland")){
								player.sendMessage(ChatColor.RED + "BuyLand: V" +  pdffile.getVersion() + ChatColor.GOLD + " is a product of chriztopia.com");
								
							if (player.hasPermission("buyland.rent") || player.hasPermission("buyland.all")){
								player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/rentland [region_name] cost - Check cost of rentable region.");  
								player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/rentland [region_name] 1 minute - Rent a region.");  
								}
							}
							
		if (cmd.getName().equalsIgnoreCase("buyland")){ 				
		player.sendMessage(ChatColor.RED + "BuyLand: V" +  pdffile.getVersion() + ChatColor.GOLD + " is a product of chriztopia.com");
		if (player.hasPermission("buyland.buy") || player.hasPermission("buyland.all")){
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/buyland [region_name] - Buy a region.");  
		}
		if (player.hasPermission("buyland.buy.addmember") || player.hasPermission("buyland.all")){
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/buyland addmember [region_name] [player_name] - Add Member to region.");  
		}
		if (player.hasPermission("buyland.buy.removemember") || player.hasPermission("buyland.all")){
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/buyland removemember [region_name] [player_name] - Remove Member from region.");  
		}
		if (player.hasPermission("buyland.price") || player.hasPermission("buyland.all")){
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/priceland [region_name] - Prices a region thats buyable.");  
		}
		if (player.hasPermission("buyland.sell") || player.hasPermission("buyland.all")){
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/sellland [region_name] - Sell a region.");  
		}
		if (player.hasPermission("buyland.tp") || player.hasPermission("buyland.all")){
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/buyland tp [region_name] - Teleport you to region.");  
		}
		if (player.hasPermission("buyland.list") || player.hasPermission("buyland.all")){
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/buyland list - Lists all owned regions.");  
		}

					   }
		
			
if (cmd.getName().equalsIgnoreCase("abl") || (cmd.getName().equalsIgnoreCase("adminbuyland"))){ 
	player.sendMessage(ChatColor.RED + "BuyLand: V" +  pdffile.getVersion() + ChatColor.GOLD + " is a product of chriztopia.com");
	
	if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")){
			player.sendMessage(" ");
			player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.YELLOW + "Admin Commands");
			player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/abl forsale [region_name] - Makes a premade region buyable.");  
			player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/abl save [region_name] - Select with WorldEdit first.");  
			player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/abl price [region_name] [cost] - Sets a price for buyable region.");  
			player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/abl list [player] [region_name] - Lists Owned region of player.");  
			
			player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/abl reset [region_name] - Resets buyable region.");  
			player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/rentland save [region_name] - Select with WorldEdit first.");  
			player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/rentland [region_name] reset - Reset rentable region.");  
		
}
		}
					   }
					   }
		   if (args.length > 0){
			   
//AdminBuyLand ABL COMMAND	
if (cmd.getName().equalsIgnoreCase("abl") || (cmd.getName().equalsIgnoreCase("adminbuyland"))){
	
	if (player.hasPermission("buyland.admin.list") || player.hasPermission("buyland.all") || player.hasPermission("buyland.admin")){	
	//Begin list
	
if (args[0].equalsIgnoreCase("list")){
		if(args.length < 2){
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Usage: /buyland list [player]");
	}else{ 
World w1 = player.getWorld();
		Map<String, ProtectedRegion> regionMap = WGBukkit.getRegionManager(w1).getRegions();
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + args[1] + " owns regions: ");
		for(ProtectedRegion region : regionMap.values()) {	
		if(region.isOwner(args[1])) {	
	if(region.getFlag(DefaultFlag.BUYABLE) == null){
	
	}else{
			if(region.getFlag(DefaultFlag.BUYABLE) == false){
				player.sendMessage(" " + region.getId());
					
			}
		}				
		}
		}	
	}
}
	}

if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")){
	if (args[0].equalsIgnoreCase("save")){
		if (args.length == 1 ){
        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
			
		}else{
//StartSaveArg
		int x1 = getWorldEditSelection(player).getMaximumPoint().getBlockX();
		int y1 = getWorldEditSelection(player).getMaximumPoint().getBlockY();
		int z1 = getWorldEditSelection(player).getMaximumPoint().getBlockZ();
		
		int x2 = getWorldEditSelection(player).getMinimumPoint().getBlockX();
		int y2 = getWorldEditSelection(player).getMinimumPoint().getBlockY();
		int z2 = getWorldEditSelection(player).getMinimumPoint().getBlockZ();
		
		
		String d1 = player.getWorld().getName();
		String p1 = player.getName();		
	     AddProtectedRegion(p1, d1, x1, y1, z1, x2, y2, z2, args[1], "buy");
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Region Added!");
//EndSaveArg
	}
	}
	

		
	
	
	if (args[0].equalsIgnoreCase("lwcremove")){
//lwcremove command
		//test101
   		//LWC
   		if (this.getConfig().getBoolean("buyland.removelwcprotection") == true){
   		   	
   		final Plugin plugin;
   		plugin = Bukkit.getServer().getPluginManager().getPlugin("LWC");
   		if(plugin != null && plugin instanceof LWCPlugin) {
   			LWC1 = ((LWCPlugin) plugin).getLWC();
   		}
   		
   		
   	  World world9 = player.getWorld();
   	  RegionManager regionManager2 = this.getWorldGuard().getRegionManager(world9);
   	  if(regionManager2.getRegionExact(args[1]) == null){
   	  //	this.getServer().getLogger().info("NULL");
   	  }else{

   	  	
   	  	ProtectedRegion set4 = regionManager2.getRegionExact(args[1]);
   	  	int minx = set4.getMinimumPoint().getBlockX();
   	  	int miny = set4.getMinimumPoint().getBlockY();
   	  	int minz = set4.getMinimumPoint().getBlockZ();

   	  	int maxx = set4.getMaximumPoint().getBlockX();
   	  	int maxy = set4.getMaximumPoint().getBlockY();
   	  	int maxz = set4.getMaximumPoint().getBlockZ();

   	  	
   			for(int x11=minx; x11<maxx; x11++) {
   				for(int y11=miny; y11<maxy; y11++) {
   					for(int z11=minz; z11<maxz; z11++) {
   						
   		        		Protection protection = LWC1.findProtection(world9, x11, y11, z11);
   		        		if(protection != null) {
   		        			protection.remove();
   		        		//this.getServer().getLogger().info("Removed LWC Protection from Plot: " + args[0]);
   		        		}else{
   		        		
   		        		}
   						
   						
   					}
   					
   				}
   			}

   	  }
   	  
			player.sendMessage(ChatColor.RED + "Buyland: " + ChatColor.WHITE + "Removed LWCProtections from region: " + args[1]);
   		}else{
   			player.sendMessage(ChatColor.RED + "Buyland: " + ChatColor.WHITE + "LWCProtections were not removed, you must enable it in the config.");
   			player.sendMessage(ChatColor.RED + "Buyland: " + ChatColor.WHITE + "Do not enable it if you do not have LWC installed!");
   	   		
   		}
   	//************************
		
		
		
		
	}
	
	if (args[0].equalsIgnoreCase("price")){
		
		if (args.length == 1 ){
        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
			
		}else{
		
		if (args.length == 2 ){
        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error2"));
        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
			
		}else{
		
		//StartPriceArg
		World world2 = player.getWorld();
	    RegionManager regionManager1 = this.getWorldGuard().getRegionManager(world2);
		ProtectedRegion set2 = regionManager1.getRegionExact(args[1]);
		if(regionManager1.getRegionExact(args[1]) == null){
        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
     
        }else{
		
		//DefaultDomain owner = set2.getOwners();
	     Double pflag1 = Double.valueOf(args[2]);
	     set2.setFlag(DefaultFlag.PRICE, pflag1);
	     
	     
	     
		    try
		    {
		    	regionManager1.save();
		    }
		     catch (Exception exp)
		    { }
	player.sendMessage(ChatColor.RED + "Buyland: " + ChatColor.WHITE + "Price Added!");
		//EndPriceArg
        }
        }
	}
			}
	
	if (args[0].equalsIgnoreCase("reset")){
//StartResetArg
		if (args.length == 1 ){
        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
			
		}else{
		World world = player.getWorld();
		RegionManager regionManager = this.getWorldGuard().getRegionManager(world);
		ProtectedRegion set2 = regionManager.getRegionExact(args[1]);

		if(regionManager.getRegionExact(args[1]) == null){
        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
     
        }else{
		int x = set2.getMinimumPoint().getBlockX();
		int y = set2.getMinimumPoint().getBlockY();
		int z = set2.getMinimumPoint().getBlockZ();
		Vector v1 = new Vector(x,y,z);
		//Location loc = new Location(world, x,y,z);
		File file = new File(getDataFolder() + File.separator + "data" + File.separator + args[1] + ".schematic");
		if (this.getConfig().getBoolean("buyland.resetlandonsale") == true){
		
		ResetMap(file, v1, world);
	player.sendMessage(ChatColor.RED + "Buyland: " + ChatColor.WHITE + "Region Reset to Default! Use forsale command to erase all owners and members!");
		}else{
			player.sendMessage(ChatColor.RED + "Buyland: " + ChatColor.WHITE + "Set resetlandonsale to true in the config file to use this feature!");
		}
//EndResetArg
        }
        }
			}
	
	
				if (args[0].equalsIgnoreCase("forsale")){
					if (args.length == 1 ){
			        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
			        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
						
					}else{
//FORSALE Arg
				World world1 = player.getWorld();
			    RegionManager regionManager = this.getWorldGuard().getRegionManager(world1);
				ProtectedRegion set2 = regionManager.getRegionExact(args[1]);
				
				if(regionManager.getRegionExact(args[1]) == null){
		        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
		        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
		     
		        }else{
				DefaultDomain owner = set2.getOwners();
				String owner2 = owner.toPlayersString();
				String pn = owner2;
				
				if (owner2.contains(pn)){
				    Boolean bflag = set2.getFlag(DefaultFlag.BUYABLE);
			        Double pflag = set2.getFlag(DefaultFlag.PRICE);
			        
				if (bflag == null){
					bflag = false;
				}
				
				if (pflag == null){
					pflag = this.getConfig().getDouble("buyland.defaultprice");
				}
				
			String convertedforsale = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.sell.forsale"));
					
			   	  DefaultDomain dd = new DefaultDomain();
				 dd.removePlayer(pn);
				 set2.setOwners(dd);
				 
				// Check if region has any owners
				 if (set2.getOwners().size() == 0) {
				     //it doesnt exit
				 }else{
					 for (String p2 : set2.getOwners().getPlayers()) {	
						 set2.getOwners().removePlayer(p2);
			        		}
				 }
				 
					// Check if region has any members
				 if (set2.getMembers().size() == 0) {
				     //it doesnt exit
				 }else{
					 for (String p2 : set2.getMembers().getPlayers()) {	
						 set2.getMembers().removePlayer(p2);
			        		}
				 }
				 
				 set2.setFlag(DefaultFlag.BUYABLE, true);
				 
			 		if (this.getConfig().getBoolean("buyland.denyentrytoland") == true){
			 	        set2.setFlag(DefaultFlag.ENTRY, State.DENY);
			 			}else{
			 				set2.setFlag(DefaultFlag.ENTRY, null);
			 			}
				 
				 if (this.getConfig().getBoolean("buyland.landgreeting") == true){
						
				 set2.setFlag(DefaultFlag.GREET_MESSAGE, convertedforsale);
				 }else if (this.getConfig().getBoolean("buyland.landgreetingerasemsg") == true){
				    	set2.setFlag(DefaultFlag.GREET_MESSAGE, null);
				    }
				 
				String nm = owner2;
				
				int numofland = this.getCustomConfig().getInt(nm);
				
			   	 int finalland = numofland - 1;
				 
			   	 if (nm.equalsIgnoreCase("")){
			   		this.getCustomConfig().set("user", finalland);
			   	 }else{
			   		this.getCustomConfig().set(nm, finalland);
			   	 }
			   	
			   	this.saveCustomConfig();
			   	this.reloadCustomConfig();

				String convertedsale = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.admin.forsale"));
				player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedsale);
//RESET to default
				World world = player.getWorld();
				//RegionManager regionManager = this.getWorldGuard().getRegionManager(world);
				//ProtectedRegion set2 = regionManager.getRegionExact(args[1]);
				int x = set2.getMinimumPoint().getBlockX();
				int y = set2.getMinimumPoint().getBlockY();
				int z = set2.getMinimumPoint().getBlockZ();
				Vector v1 = new Vector(x,y,z);
				//Location loc = new Location(world, x,y,z);
				File file = new File(getDataFolder() + File.separator + "data" + File.separator + args[1] + ".schematic");
				if (this.getConfig().getBoolean("buyland.resetlandonsale") == true){
				ResetMap(file, v1, world);
				
				
				
				
				
				//Change Sign   	
				 if(this.getsignConfig().contains("sign." + args[1])){
					 
					 String price = pflag.toString();
					 
					 //-----------------

			             Location signloc = stringToLoc(this.getsignConfig().getString("sign." + args[1]));
			             Block that = signloc.getBlock();
			             
			             if (that.getType() == Material.SIGN_POST || that.getType() == Material.WALL_SIGN){
			            	 
			                 Sign s = (Sign) signloc.getBlock().getState();

			                 s.setLine(0, "[BuyLand]");
			                 s.setLine(1, "For Sale");
			                 s.setLine(2, args[1]);
			                 s.setLine(3, price);
			                 s.update();
			            	 
			             }else{
			            	// Maybe create a sign if it doesn't exist?

			             }
			             

					 //-------------
					 
				 }
				
				
				
				
				}
//END RESET TO DEFAULT	
				    try
				    {
				    	regionManager.save();
				    }
				     catch (Exception exp)
				    { }
				}

//FORSALE END0
		        }
				}	
}
			}
			}


//RentLand COMMAND	

	if (cmd.getName().equalsIgnoreCase("rentland")){
	
		if (player.hasPermission("buyland.rent") || player.hasPermission("buyland.all")){

			//top


			
			
if (args.length == 2 && args[0].equalsIgnoreCase("save")){
			if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")){
					
				//StartSaveArg
						int x1 = getWorldEditSelection(player).getMaximumPoint().getBlockX();
						int y1 = getWorldEditSelection(player).getMaximumPoint().getBlockY();
						int z1 = getWorldEditSelection(player).getMaximumPoint().getBlockZ();
						
						int x2 = getWorldEditSelection(player).getMinimumPoint().getBlockX();
						int y2 = getWorldEditSelection(player).getMinimumPoint().getBlockY();
						int z2 = getWorldEditSelection(player).getMinimumPoint().getBlockZ();
						
						String d1 = player.getWorld().getName();
						String p1 = player.getName();		
					     AddProtectedRegion(p1, d1, x1, y1, z1, x2, y2, z2, args[1], "rent");
						player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Region Added!");
				//EndSaveArg
				}else{
					
					String rentperm = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.noperm"));
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.DARK_RED + rentperm);
				}
			
			}else if (args[0].equalsIgnoreCase("addmember")){
				if (player.hasPermission("buyland.rent.addmember") || player.hasPermission("buyland.all")){
				
	//addmember rent
					
					if(args.length < 3){
						player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Usage: /rentland addmember REGION PLAYER");
					}else{
					
				        World world2 = player.getWorld();
				        RegionManager regionManager2 = this.getWorldGuard().getRegionManager(world2);
				        if(regionManager2.getRegionExact(args[1]) == null){
				        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
				        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
				        	//player.sendMessage("args1 " + args[1]);
				        }else{
						
						
						World world3 = player.getWorld();
					    RegionManager regionManager1 = this.getWorldGuard().getRegionManager(world3);
						ProtectedRegion set = regionManager1.getRegionExact(args[1]);
				
						
						if(this.getRentConfig().contains("rent." + args[1] +".rentable")){
			
						
						  DefaultDomain owner = set.getOwners();
						  	String owner2 = owner.toPlayersString();
						  	String pn = player.getName().toLowerCase();
						  	
						  	
						  	if (owner2.contains(pn)){
			//----------
				    	            	set.getMembers().addPlayer(args[2]);
					    	            try {
											regionManager1.save();
										} catch (ProtectionDatabaseException e) {
											//  Auto-generated catch block
											e.printStackTrace();
										}
					    	            

					    	            
					    	        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.member.addmember"));
					    	        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
			//----------
							
	
						  	}else{
								String convertedownland = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.sell.dontown"));
								player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedownland);
							}
						  	
						}else{
							//NOT RENTABLE
							
					    	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.error2"));
					    	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);

						}
					    	 
					}
					
					
					}
					
					
			}else{
				String rentperm = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.noperm"));
				player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.DARK_RED + rentperm);
			}
				
					//end addmember rent
				}else if (args[0].equalsIgnoreCase("removemember")){
					
					if (player.hasPermission("buyland.rent.removemember") || player.hasPermission("buyland.all")){
						
					//removemember rent
					
					if(args.length < 3){
						player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Usage: /rentland removemember REGION PLAYER");
					}else{
					
				        World world2 = player.getWorld();
				        RegionManager regionManager2 = this.getWorldGuard().getRegionManager(world2);
				        if(regionManager2.getRegionExact(args[1]) == null){
				        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
				        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
				        	//player.sendMessage("args1 " + args[1]);
				        }else{
						
						
						World world3 = player.getWorld();
					    RegionManager regionManager1 = this.getWorldGuard().getRegionManager(world3);
						ProtectedRegion set = regionManager1.getRegionExact(args[1]);
				
						
						if(this.getRentConfig().contains("rent." + args[1] +".rentable")){
			
						
						  DefaultDomain owner = set.getOwners();
						  	String owner2 = owner.toPlayersString();
						  	String pn = player.getName().toLowerCase();
						  	
						  	
						  	if (owner2.contains(pn)){
			//----------
				    	     //REOVE MEMBER
					    	            

		    	            	set.getMembers().removePlayer(args[2]);
			    	            
			    	            try {
									regionManager1.save();
								} catch (ProtectionDatabaseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		    	            	
			    	        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.member.removemember"));
			    	        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
		    	            
			    	        	
						  		
						  		//----------
							
	
						  	}else{
								String convertedownland = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.sell.dontown"));
								player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedownland);
							}
						  	
						}else{
							//NOT RENTABLE
							
					    	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.error2"));
					    	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);

						}
					    	 
					}
					
					
					}
					
					
				}else{
					String rentperm = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.noperm"));
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.DARK_RED + rentperm);
				}
					
					//end removemember rent
				}else					
			
			{
        World world1 = player.getWorld();
        RegionManager regionManager = this.getWorldGuard().getRegionManager(world1);

        if(regionManager.getRegionExact(args[0]) == null){
        	//region does not exist
			String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
        }else{  
        
        	if (this.getRentConfig().contains("rent." + args[0] + ".time")){
				
    		long end = this.getRentConfig().getLong("rent." + args[0] + ".time");
    		long start = System.currentTimeMillis();
			if (args.length == 2 && args[1].equalsIgnoreCase("time")){
				if (start > end){
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Time left for " + args[0] + ": 0 - This land is rentable!");
				}else{
				
				player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Time left for " + args[0] + ": " + elapsedTime(start, end));
				}
			}else{
				
				if (args.length == 2 && args[1].equalsIgnoreCase("cost")){
					double s = this.getRentConfig().getDouble("rent." + args[0] +".costpermin") / 2;
					double m = this.getRentConfig().getDouble("rent." + args[0] +".costpermin");
					double h = this.getRentConfig().getDouble("rent." + args[0] +".costpermin") * 60;
					double d = this.getRentConfig().getDouble("rent." + args[0] +".costpermin") * 1440;
					
					
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "The cost of " + args[0] + " is: ");
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "1 Second = " + s);
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "1 Minute = " + m);
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "1 Hour = " + h);
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "1 Day = " + d);
				}else{		
				
if (args.length == 2 && args[1].equalsIgnoreCase("reset")){
if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.all")){
					
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + args[0] + " has been reset!");
					
					getRentConfig().set("rent." + args[0] + ".time", 0);
					getRentConfig().set("rent." + args[0] + ".rentable", true);
					
					ProtectedRegion set3 = regionManager.getRegionExact(args[0]);	
					//This is used for Loading Schematics   	
								   	   	World world = player.getWorld();
								   	   	int x = set3.getMinimumPoint().getBlockX();
								   	   	int y = set3.getMinimumPoint().getBlockY();
								   	   	int z = set3.getMinimumPoint().getBlockZ();
								   	   	Vector v1 = new Vector(x,y,z);
								   	   	File file = new File(getDataFolder() + File.separator + "data" + File.separator + args[0] + ".schematic");
								   	   	ResetMap(file, v1, world);
								   	   	
								   	//End of Schematics
								   	   	
								   	//test101
								   		//LWC
								   		if (this.getConfig().getBoolean("buyland.removelwcprotection") == true){
								   		   	
								   		final Plugin plugin;
								   		plugin = Bukkit.getServer().getPluginManager().getPlugin("LWC");
								   		if(plugin != null && plugin instanceof LWCPlugin) {
								   			LWC1 = ((LWCPlugin) plugin).getLWC();
								   		}
								   		
								   		
								   	  World world9 = player.getWorld();
								   	  RegionManager regionManager2 = this.getWorldGuard().getRegionManager(world9);
								   	  if(regionManager2.getRegionExact(args[0]) == null){
								   	  //	this.getServer().getLogger().info("NULL");
								   	  }else{

								   	  	
								   	  	ProtectedRegion set4 = regionManager2.getRegionExact(args[0]);
								   	  	int minx = set4.getMinimumPoint().getBlockX();
								   	  	int miny = set4.getMinimumPoint().getBlockY();
								   	  	int minz = set4.getMinimumPoint().getBlockZ();

								   	  	int maxx = set4.getMaximumPoint().getBlockX();
								   	  	int maxy = set4.getMaximumPoint().getBlockY();
								   	  	int maxz = set4.getMaximumPoint().getBlockZ();

								   	  	
								   			for(int x11=minx; x11<maxx; x11++) {
								   				for(int y11=miny; y11<maxy; y11++) {
								   					for(int z11=minz; z11<maxz; z11++) {
								   						
								   		        		Protection protection = LWC1.findProtection(world1, x11, y11, z11);
								   		        		if(protection != null) {
								   		        			protection.remove();
								   		        		//this.getServer().getLogger().info("Removed LWC Protection from Plot: " + args[0]);
								   		        		}else{
								   		        		
								   		        		}
								   						
								   						
								   					}
								   				}
								   			}

								   	  }
								   		}
								   	//************************
								   	   	
								   	   	
								   	   	
							        	DefaultDomain du = set3.getOwners();
							        	
										String forrent = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.forrent"));

										if (this.getConfig().getBoolean("buyland.landgreeting") == true){
											
									 		if (this.getConfig().getBoolean("buyland.denyentrytoland") == true){
									 	        set3.setFlag(DefaultFlag.ENTRY, State.DENY);
									 			}else{
									 				set3.setFlag(DefaultFlag.ENTRY, null);
									 			}
											
							        	set3.setFlag(DefaultFlag.GREET_MESSAGE, forrent);
										}else if (this.getConfig().getBoolean("buyland.landgreetingerasemsg") == true){
									    	set3.setFlag(DefaultFlag.GREET_MESSAGE, null);
									    }
							        	
							        	String pn = du.toString();
							        	
							        	
							   		 String nm = du.toUserFriendlyString();
									 int numofland = getrentdbConfig().getInt(nm);
									 int finalland = numofland - 1;
									 getrentdbConfig().set(nm, finalland);
									 saverentdbConfig();
									 //reloadrentdbConfig();
							    		
							    	   	  DefaultDomain dd = new DefaultDomain();
							    		    dd.removePlayer(pn);
							    		 set3.setOwners(dd);
							    		 set3.setMembers(dd);
							    		 
							    		 
							    		 
							    		 
					//this.logger.info("BuyLand_Debug - Save Rent Config Start 1");
							    		 saveRentConfig();
					//this.logger.info("BuyLand_Debug - Save Rent Config End 1");
					//this.logger.info("BuyLand_Debug - Reload Rent Config Start 1");
							    		
							    		 reloadRentConfig();
					// this.logger.info("BuyLand_Debug - Reload Rent Config End 1");
					 
							    		    try
							    		    {
							    		    	regionManager.save();
							    		    }
							    		     catch (Exception exp)
							    		    { }
				}else{
					
					String rentperm = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.noperm"));

					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.DARK_RED + rentperm);
				}

					
				}else{
				if (args.length == 1 ){
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Try /rentland [region] 1 second");
				}else{
				
			if (args.length == 2 ){
				player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Try /rentland [region] 1 second");
				
		}else{
    		
if (this.getRentConfig().getBoolean("rent." + args[0] + ".rentable") == false && start > end){
		ProtectedRegion set3 = regionManager.getRegionExact(args[0]);	
		
		getRentConfig().set("rent." + args[0] + ".time", 0);
		getRentConfig().set("rent." + args[0] + ".rentable", true);
		//This is used for Loading Schematics   	
					   	   	World world = player.getWorld();
					   	   	int x = set3.getMinimumPoint().getBlockX();
					   	   	int y = set3.getMinimumPoint().getBlockY();
					   	   	int z = set3.getMinimumPoint().getBlockZ();
					   	   	Vector v1 = new Vector(x,y,z);
					   	   	File file = new File(getDataFolder() + File.separator + "data" + File.separator + args[0] + ".schematic");
					   	   	ResetMap(file, v1, world);
					   	//End of Schematics
					   	   	
					   	//test101
					   		//LWC
					   		if (this.getConfig().getBoolean("buyland.removelwcprotection") == true){
					   		   	
					   		final Plugin plugin;
					   		plugin = Bukkit.getServer().getPluginManager().getPlugin("LWC");
					   		if(plugin != null && plugin instanceof LWCPlugin) {
					   			LWC1 = ((LWCPlugin) plugin).getLWC();
					   		}
					   		
					   		
					   	  World world9 = player.getWorld();
					   	  RegionManager regionManager2 = this.getWorldGuard().getRegionManager(world9);
					   	  if(regionManager2.getRegionExact(args[0]) == null){
					   	  //	this.getServer().getLogger().info("NULL");
					   	  }else{

					   	  	
					   	  	ProtectedRegion set4 = regionManager2.getRegionExact(args[0]);
					   	  	int minx = set4.getMinimumPoint().getBlockX();
					   	  	int miny = set4.getMinimumPoint().getBlockY();
					   	  	int minz = set4.getMinimumPoint().getBlockZ();

					   	  	int maxx = set4.getMaximumPoint().getBlockX();
					   	  	int maxy = set4.getMaximumPoint().getBlockY();
					   	  	int maxz = set4.getMaximumPoint().getBlockZ();

					   	  	
					   			for(int x11=minx; x11<maxx; x11++) {
					   				for(int y11=miny; y11<maxy; y11++) {
					   					for(int z11=minz; z11<maxz; z11++) {
					   						
					   		        		Protection protection = LWC1.findProtection(world1, x11, y11, z11);
					   		        		if(protection != null) {
					   		        			protection.remove();
					   		        		//this.getServer().getLogger().info("Removed LWC Protection from Plot: " + args[0]);
					   		        		}else{
					   		        		
					   		        		}
					   						
					   						
					   					}
					   				}
					   			}

					   	  }
					   		}
					   	//************************
					   	   	
				        	DefaultDomain du = set3.getOwners();
				        
							String forrent = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.forrent"));

							if (this.getConfig().getBoolean("buyland.landgreeting") == true){
						
						 		if (this.getConfig().getBoolean("buyland.denyentrytoland") == true){
						 	        set3.setFlag(DefaultFlag.ENTRY, State.DENY);
						 			}else{
						 				set3.setFlag(DefaultFlag.ENTRY, null);
						 			}
								
				        	set3.setFlag(DefaultFlag.GREET_MESSAGE, forrent);
							}else if (this.getConfig().getBoolean("buyland.landgreetingerasemsg") == true){
						    	set3.setFlag(DefaultFlag.GREET_MESSAGE, null);
						    }
				        	
				        	String pn = du.toString();
				        	
				   		 String nm = du.toUserFriendlyString();
						 int numofland = getrentdbConfig().getInt(nm);
						 int finalland = numofland - 1;
						 getrentdbConfig().set(nm, finalland);
						 saverentdbConfig();
						 reloadrentdbConfig();
				    		
				    	   	  DefaultDomain dd = new DefaultDomain();
				    		    dd.removePlayer(pn);
				    		 set3.setOwners(dd);
				    		 set3.setMembers(dd);
				    		 
								//this.logger.info("BuyLand_Debug - Save Rent Config Start 2");
					    		 saveRentConfig();
			//this.logger.info("BuyLand_Debug - Save Rent Config End 2");
			//this.logger.info("BuyLand_Debug - Reload Rent Config Start 2");
					    		
					    		 reloadRentConfig();
			// this.logger.info("BuyLand_Debug - Reload Rent Config End 2");
			 
				    		 
				    		    try
				    		    {
				    		    	regionManager.save();
				    		    }
				    		     catch (Exception exp)
				    		    { }
		
        }

//if (this.getRentConfig().getBoolean("rent." + args[0] + ".rentable") == true){
		if (start < end){

//----------------

ProtectedRegion set2 = regionManager.getRegionExact(args[0]);
DefaultDomain owner = set2.getOwners();
	String owner2 = owner.toPlayersString();
	String pn = player.getName().toLowerCase();
	
	if (owner2.contains(pn)){
    	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Time left: " + elapsedTime(start, end));
String s = args[1];	
long timea = Long.parseLong(s);


if (args[2].equalsIgnoreCase("s") || args[2].equalsIgnoreCase("sec") || args[2].equalsIgnoreCase("second")){

	
	double aflag = this.getRentConfig().getDouble("rent." + args[0] +".costpermin") / 2;
	double pflag = aflag * Double.valueOf(args[1]);
	EconomyResponse r = econ.withdrawPlayer(player.getName(), pflag);
	if(r.transactionSuccess()) {
	sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Adding " + args[1] + " Second(s) to " + args[0] + ". Cost: %s Balance: %s", econ.format(r.amount), econ.format(r.balance)));
		
	
long time = timea * 1000L;
long timepull = this.getRentConfig().getLong("rent." + args[0] +".time");
this.getRentConfig().set("rent." + args[0] +".time", timepull + time);

saveRentConfig();
reloadRentConfig();

} else {
String converteda1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.cantafford"));
sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + converteda1, r.errorMessage));
}

}

if (args[2].equalsIgnoreCase("m") || args[2].equalsIgnoreCase("min") || args[2].equalsIgnoreCase("minute")){
	double aflag = this.getRentConfig().getDouble("rent." + args[0] +".costpermin");
	double pflag = aflag * Double.valueOf(args[1]);

	EconomyResponse r = econ.withdrawPlayer(player.getName(), pflag);
	if(r.transactionSuccess()) {
		
sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Adding " + args[1] + " Minute(s) to " + args[0] + ". Cost: %s Balance: %s", econ.format(r.amount), econ.format(r.balance)));
		

	long time = timea * 60000L;
long timepull = this.getRentConfig().getLong("rent." + args[0] +".time");
this.getRentConfig().set("rent." + args[0] +".time", timepull + time);


//this.logger.info("BuyLand_Debug - Save Rent Config Start 4");
saveRentConfig();
//this.logger.info("BuyLand_Debug - Save Rent Config End 4");
//this.logger.info("BuyLand_Debug - Reload Rent Config Start 4");

reloadRentConfig();
//this.logger.info("BuyLand_Debug - Reload Rent Config End 4");


} else {
String converteda1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.cantafford"));
sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + converteda1, r.errorMessage));
}


}

if (args[2].equalsIgnoreCase("h") || args[2].equalsIgnoreCase("hour")){
	
	double aflag = this.getRentConfig().getDouble("rent." + args[0] +".costpermin") * 60;
	double pflag = aflag * Double.valueOf(args[1]);

	EconomyResponse r = econ.withdrawPlayer(player.getName(), pflag);
	if(r.transactionSuccess()) {
sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Adding " + args[1] + " Hour(s) to " + args[0] + ". Cost: %s Balance: %s", econ.format(r.amount), econ.format(r.balance)));
	
	
long time = timea * 3600000L;
long timepull = this.getRentConfig().getLong("rent." + args[0] +".time");
this.getRentConfig().set("rent." + args[0] +".time", timepull + time);


//this.logger.info("BuyLand_Debug - Save Rent Config Start 5");
saveRentConfig();
//this.logger.info("BuyLand_Debug - Save Rent Config End 5");
//this.logger.info("BuyLand_Debug - Reload Rent Config Start 5");

reloadRentConfig();
//this.logger.info("BuyLand_Debug - Reload Rent Config End 5");



} else {
String converteda1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.cantafford"));
sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + converteda1, r.errorMessage));
}

}

if (args[2].equalsIgnoreCase("d") || args[2].equalsIgnoreCase("day")){
	
	double aflag = this.getRentConfig().getDouble("rent." + args[0] +".costpermin") * 1440;
	double pflag = aflag * Double.valueOf(args[1]);

	EconomyResponse r = econ.withdrawPlayer(player.getName(), pflag);
	if(r.transactionSuccess()) {
	sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Adding " + args[1] + " Day(s) to " + args[0] + ". Cost: %s Balance: %s", econ.format(r.amount), econ.format(r.balance)));
	
	long time = timea * 86400000L;
	long timepull = this.getRentConfig().getLong("rent." + args[0] +".time");
	this.getRentConfig().set("rent." + args[0] +".time", timepull + time);

	//this.logger.info("BuyLand_Debug - Save Rent Config Start 6");
	 saveRentConfig();
//this.logger.info("BuyLand_Debug - Save Rent Config End 6");
//this.logger.info("BuyLand_Debug - Reload Rent Config Start 6");
	
	 reloadRentConfig();
//this.logger.info("BuyLand_Debug - Reload Rent Config End 6");

	

	} else {
	String converteda1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.cantafford"));
	sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + converteda1, r.errorMessage));
	}
	
	
}
	}else{
	
		String renthasten = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.tenant"));

   player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + renthasten + elapsedTime(start, end));		
	}
//------------------------					
				}else{

String s = args[1];	
long timea = Long.parseLong(s);

if (args[2].equalsIgnoreCase("s") || args[2].equalsIgnoreCase("sec") || args[2].equalsIgnoreCase("second")){

	String nm = player.getName();
	int numofland = this.getrentdbConfig().getInt(nm);
	int maxofland = this.getConfig().getInt("buyland.maxamountofrentland");
	

if (numofland +1 > maxofland){
	String convertedmax = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.max"));
	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedmax);
}else{

	
	double aflag = this.getRentConfig().getDouble("rent." + args[0] +".costpermin") / 2;
	double pflag = aflag * Double.valueOf(args[1]);
	EconomyResponse r = econ.withdrawPlayer(player.getName(), pflag);
	if(r.transactionSuccess()) {
		sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Renting " + args[0] + " for " + args[1] + " Second(s). Cost: %s Balance: %s", econ.format(r.amount), econ.format(r.balance)));
	
	long time = timea * 1000L;
this.getRentConfig().set("rent." + args[0] +".time", System.currentTimeMillis() + time);
this.getRentConfig().set("rent." + args[0] +".world", player.getWorld().getName());
this.getRentConfig().set("rent." + args[0] +".rentable", false);

//Set User As Owner

int finalland = numofland + 1;

this.getrentdbConfig().set(nm, finalland);
this.saverentdbConfig();

String p1 = player.getName();
ProtectedRegion set2 = regionManager.getRegionExact(args[0]);
DefaultDomain dd = new DefaultDomain();
dd.addPlayer(p1);
set2.setOwners(dd);

String rentby = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.rentby"));

if (this.getConfig().getBoolean("buyland.landgreeting") == true){
	
set2.setFlag(DefaultFlag.GREET_MESSAGE, rentby + p1);
}else if (this.getConfig().getBoolean("buyland.landgreetingerasemsg") == true){
	set2.setFlag(DefaultFlag.GREET_MESSAGE, null);
	
		if (this.getConfig().getBoolean("buyland.denyentrytoland") == true){
 	        set2.setFlag(DefaultFlag.ENTRY, State.DENY);
 			}else{
 				set2.setFlag(DefaultFlag.ENTRY, null);
 			}
}

set2.setFlag(DefaultFlag.BUYABLE, false);

try
{
	regionManager.save();
}
 catch (Exception exp)
{ }

//Start Schematics
World world2 = player.getWorld();
int x4 = set2.getMinimumPoint().getBlockX();
int y4 = set2.getMinimumPoint().getBlockY();
int z4 = set2.getMinimumPoint().getBlockZ();
Location loc1 = new Location(world2, x4,y4,z4);
int x1 = set2.getMaximumPoint().getBlockX();
int y1 = set2.getMaximumPoint().getBlockY();
int z1 = set2.getMaximumPoint().getBlockZ();
Location loc2 = new Location(world2, x1,y1,z1);
String name = args[0];
		try {
			saveSchematic(loc1, loc2, name, player);
		} catch (EmptyClipboardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
//END schematics

//this.logger.info("BuyLand_Debug - Save Rent Config Start 7");
saveRentConfig();
//this.logger.info("BuyLand_Debug - Save Rent Config End 7");
//this.logger.info("BuyLand_Debug - Reload Rent Config Start 7");

reloadRentConfig();
//this.logger.info("BuyLand_Debug - Reload Rent Config End 7");


} else {
String converteda1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.cantafford"));
sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + converteda1, r.errorMessage));
}
}
}

if (args[2].equalsIgnoreCase("m") || args[2].equalsIgnoreCase("min") || args[2].equalsIgnoreCase("minute")){
	String nm = player.getName();
	int numofland = this.getrentdbConfig().getInt(nm);
	int maxofland = this.getConfig().getInt("buyland.maxamountofrentland");
	

if (numofland +1 > maxofland){
	String convertedmax = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.max"));
	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedmax);
}else{

	
	double aflag = this.getRentConfig().getDouble("rent." + args[0] +".costpermin");
	double pflag = aflag * Double.valueOf(args[1]);

	EconomyResponse r = econ.withdrawPlayer(player.getName(), pflag);
	if(r.transactionSuccess()) {
sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Renting " + args[0] + " for " + args[1] + " Minute(s). Cost: %s Balance: %s", econ.format(r.amount), econ.format(r.balance)));

	
	long time = timea * 60000L;
this.getRentConfig().set("rent." + args[0] +".time", System.currentTimeMillis() + time);
this.getRentConfig().set("rent." + args[0] +".world", player.getWorld().getName());
this.getRentConfig().set("rent." + args[0] +".rentable", false);


//Set User As Owner
int finalland = numofland + 1;

this.getrentdbConfig().set(nm, finalland);
this.saverentdbConfig();


String p1 = player.getName();
ProtectedRegion set2 = regionManager.getRegionExact(args[0]);
DefaultDomain dd = new DefaultDomain();
dd.addPlayer(p1);
set2.setOwners(dd);

String rentby = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.rentby"));

if (this.getConfig().getBoolean("buyland.landgreeting") == true){
	
set2.setFlag(DefaultFlag.GREET_MESSAGE, rentby + p1);
}else if (this.getConfig().getBoolean("buyland.landgreetingerasemsg") == true){
	set2.setFlag(DefaultFlag.GREET_MESSAGE, null);
}

if (this.getConfig().getBoolean("buyland.denyentrytoland") == true){
    set2.setFlag(DefaultFlag.ENTRY, State.DENY);
	}else{
		set2.setFlag(DefaultFlag.ENTRY, null);
	}

set2.setFlag(DefaultFlag.BUYABLE, false);


try
{
	regionManager.save();
}
 catch (Exception exp)
{ }

//Start Schematics
World world3 = player.getWorld();
int x7 = set2.getMinimumPoint().getBlockX();
int y7 = set2.getMinimumPoint().getBlockY();
int z7 = set2.getMinimumPoint().getBlockZ();
Location loc1 = new Location(world3, x7,y7,z7);
int x1 = set2.getMaximumPoint().getBlockX();
int y1 = set2.getMaximumPoint().getBlockY();
int z1 = set2.getMaximumPoint().getBlockZ();
Location loc2 = new Location(world3, x1,y1,z1);
String name = args[0];
		try {
			saveSchematic(loc1, loc2, name, player);
		} catch (EmptyClipboardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
//END schematics

		//this.logger.info("BuyLand_Debug - Save Rent Config Start 8");
		 saveRentConfig();
//this.logger.info("BuyLand_Debug - Save Rent Config End 8");
//this.logger.info("BuyLand_Debug - Reload Rent Config Start 8");
		
		 reloadRentConfig();
//this.logger.info("BuyLand_Debug - Reload Rent Config End 8");



	} else {
	String converteda1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.cantafford"));
	sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + converteda1, r.errorMessage));
	}
}
}

if (args[2].equalsIgnoreCase("h") || args[2].equalsIgnoreCase("hour")){
	
	String nm = player.getName();
	int numofland = this.getrentdbConfig().getInt(nm);
	int maxofland = this.getConfig().getInt("buyland.maxamountofrentland");
	

if (numofland +1 > maxofland){
	String convertedmax = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.max"));
	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedmax);
}else{

	
	double aflag = this.getRentConfig().getDouble("rent." + args[0] +".costpermin") * 60;
	double pflag = aflag * Double.valueOf(args[1]);

	EconomyResponse r = econ.withdrawPlayer(player.getName(), pflag);
	if(r.transactionSuccess()) {
	sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Renting " + args[0] + " for " + args[1] + " Hour(s). Cost: %s Balance: %s", econ.format(r.amount), econ.format(r.balance)));

	
long time = timea * 3600000L;
this.getRentConfig().set("rent." + args[0] +".time", System.currentTimeMillis() + time);
this.getRentConfig().set("rent." + args[0] +".world", player.getWorld().getName());
this.getRentConfig().set("rent." + args[0] +".rentable", false);

//Set User As Owner
int finalland = numofland + 1;

this.getrentdbConfig().set(nm, finalland);
this.saverentdbConfig();

String p1 = player.getName();
ProtectedRegion set2 = regionManager.getRegionExact(args[0]);
DefaultDomain dd = new DefaultDomain();
dd.addPlayer(p1);
set2.setOwners(dd);

String rentby = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.rentby"));

if (this.getConfig().getBoolean("buyland.landgreeting") == true){
	
set2.setFlag(DefaultFlag.GREET_MESSAGE, rentby + p1);
}else if (this.getConfig().getBoolean("buyland.landgreetingerasemsg") == true){
	set2.setFlag(DefaultFlag.GREET_MESSAGE, null);
}

if (this.getConfig().getBoolean("buyland.denyentrytoland") == true){
    set2.setFlag(DefaultFlag.ENTRY, State.DENY);
	}else{
		set2.setFlag(DefaultFlag.ENTRY, null);
	}

set2.setFlag(DefaultFlag.BUYABLE, false);

try
{
	regionManager.save();
}
 catch (Exception exp)
{ }

//Start Schematics
World world4 = player.getWorld();
int x3 = set2.getMinimumPoint().getBlockX();
int y3 = set2.getMinimumPoint().getBlockY();
int z3 = set2.getMinimumPoint().getBlockZ();
Location loc1 = new Location(world4, x3,y3,z3);
int x1 = set2.getMaximumPoint().getBlockX();
int y1 = set2.getMaximumPoint().getBlockY();
int z1 = set2.getMaximumPoint().getBlockZ();
Location loc2 = new Location(world4, x1,y1,z1);
String name = args[0];
		try {
			saveSchematic(loc1, loc2, name, player);
		} catch (EmptyClipboardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
//END schematics

		//this.logger.info("BuyLand_Debug - Save Rent Config Start 9");
		 saveRentConfig();
//this.logger.info("BuyLand_Debug - Save Rent Config End 9");
//this.logger.info("BuyLand_Debug - Reload Rent Config Start 9");
		
		 reloadRentConfig();
//this.logger.info("BuyLand_Debug - Reload Rent Config End 9");


	} else {
	String converteda1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.cantafford"));
	sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + converteda1, r.errorMessage));
	}
}
}

if (args[2].equalsIgnoreCase("d") || args[2].equalsIgnoreCase("day")){
	
	String nm = player.getName();
	int numofland = this.getrentdbConfig().getInt(nm);
	int maxofland = this.getConfig().getInt("buyland.maxamountofrentland");
	

if (numofland +1 > maxofland){
	String convertedmax = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.max"));
	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedmax);
}else{

	
	double aflag = this.getRentConfig().getDouble("rent." + args[0] +".costpermin") * 1440;
	double pflag = aflag * Double.valueOf(args[1]);

	EconomyResponse r = econ.withdrawPlayer(player.getName(), pflag);
	if(r.transactionSuccess()) {
	sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Renting " + args[0] + " for " + args[1] + " Day(s). Cost: %s Balance: %s", econ.format(r.amount), econ.format(r.balance)));
	
	long time = timea * 86400000L;
	this.getRentConfig().set("rent." + args[0] +".time", System.currentTimeMillis() + time);
	this.getRentConfig().set("rent." + args[0] +".world", player.getWorld().getName());
	this.getRentConfig().set("rent." + args[0] +".rentable", false);

//Set User As Owner
	int finalland = numofland + 1;

	this.getrentdbConfig().set(nm, finalland);
	this.saverentdbConfig();
	
	String p1 = player.getName();
	ProtectedRegion set2 = regionManager.getRegionExact(args[0]);
	DefaultDomain dd = new DefaultDomain();
	dd.addPlayer(p1);
	set2.setOwners(dd);
	
	String rentby = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.rentby"));

	if (this.getConfig().getBoolean("buyland.landgreeting") == true){
		
	set2.setFlag(DefaultFlag.GREET_MESSAGE, rentby + p1);
	}else if (this.getConfig().getBoolean("buyland.landgreetingerasemsg") == true){
    	set2.setFlag(DefaultFlag.GREET_MESSAGE, null);
    }
	
	if (this.getConfig().getBoolean("buyland.denyentrytoland") == true){
        set2.setFlag(DefaultFlag.ENTRY, State.DENY);
		}else{
			set2.setFlag(DefaultFlag.ENTRY, null);
		}
	
	set2.setFlag(DefaultFlag.BUYABLE, false);
	
	try
	{
		regionManager.save();
	}
	 catch (Exception exp)
	{ }

	//Start Schematics
	World world5 = player.getWorld();
	int x5 = set2.getMinimumPoint().getBlockX();
	int y5 = set2.getMinimumPoint().getBlockY();
	int z5 = set2.getMinimumPoint().getBlockZ();
	Location loc1 = new Location(world5, x5,y5,z5);
	int x1 = set2.getMaximumPoint().getBlockX();
	int y1 = set2.getMaximumPoint().getBlockY();
	int z1 = set2.getMaximumPoint().getBlockZ();
	Location loc2 = new Location(world5, x1,y1,z1);
	String name = args[0];
			try {
				saveSchematic(loc1, loc2, name, player);
			} catch (EmptyClipboardException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	//END schematics
	

			//this.logger.info("BuyLand_Debug - Save Rent Config Start 10");
   		 saveRentConfig();
//this.logger.info("BuyLand_Debug - Save Rent Config End 10");
//this.logger.info("BuyLand_Debug - Reload Rent Config Start 10");
   		
   		 reloadRentConfig();
//this.logger.info("BuyLand_Debug - Reload Rent Config End 10");

			
	
	} else {
	String converteda1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.cantafford"));
	sender.sendMessage(String.format(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + converteda1, r.errorMessage));
	}
}
	}
}
		
		
	}
        
		}
				}

				//bottom rentland command
				}
			}
        	
			
			}else{
					
					String cannot = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.notbe"));

					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + cannot);
				}
		
	}
			
		}
		}
	}
	
	
//SELLLAND COMMAND	
	if (cmd.getName().equalsIgnoreCase("sellland")){ 
if (player.hasPermission("buyland.sell") || player.hasPermission("buyland.all")){
	 
	if (this.getRentConfig().contains("rent." + args[0] + ".rentable")){
		
    	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.error1"));
    	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);

	}else{
	
        World world1 = player.getWorld();
        RegionManager regionManager = this.getWorldGuard().getRegionManager(world1);

        if(regionManager.getRegionExact(args[0]) == null){
        	//region does not exist
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
	 set2.setMembers(dd);
	 
	 
	// Check if region has any owners
			 if (set2.getOwners().size() == 0) {
			     //it doesnt exit
			 }else{
				 for (String p2 : set2.getOwners().getPlayers()) {	
					 set2.getOwners().removePlayer(p2);
		        		}
			 }
			 
				// Check if region has any members
			 if (set2.getMembers().size() == 0) {
			     //it doesnt exit
			 }else{
				 for (String p2 : set2.getMembers().getPlayers()) {	
					 set2.getMembers().removePlayer(p2);
		        		}
			 }
	 
	 
	 
	 set2.setFlag(DefaultFlag.BUYABLE, true);
	 
	 if (this.getConfig().getBoolean("buyland.landgreeting") == true){
			
	 set2.setFlag(DefaultFlag.GREET_MESSAGE, convertedforsale);
	}else if (this.getConfig().getBoolean("buyland.landgreetingerasemsg") == true){
    	set2.setFlag(DefaultFlag.GREET_MESSAGE, null);
    }
	 
		if (this.getConfig().getBoolean("buyland.denyentrytoland") == true){
	        set2.setFlag(DefaultFlag.ENTRY, State.DENY);
			}else{
				set2.setFlag(DefaultFlag.ENTRY, null);
			}
	 
	 set2.setPriority(this.getConfig().getInt("buyland.landpriority"));
	 
		String nm = player.getName();
		int numofland = this.getCustomConfig().getInt(nm);
		
   	 int finalland = numofland - 1;
	 
   	 this.getCustomConfig().set(nm, finalland);
   	 this.saveCustomConfig();
   	this.reloadCustomConfig();
   	

//test101
	//LWC
   	if (this.getConfig().getBoolean("buyland.removelwcprotection") == true){
   		
	final Plugin plugin;
	plugin = Bukkit.getServer().getPluginManager().getPlugin("LWC");
	if(plugin != null && plugin instanceof LWCPlugin) {
		LWC1 = ((LWCPlugin) plugin).getLWC();
	}
	
	
    World world9 = player.getWorld();
    RegionManager regionManager2 = this.getWorldGuard().getRegionManager(world9);
    if(regionManager2.getRegionExact(args[0]) == null){
    //	this.getServer().getLogger().info("NULL");
    }else{

    	
    	ProtectedRegion set4 = regionManager2.getRegionExact(args[0]);
    	int minx = set4.getMinimumPoint().getBlockX();
    	int miny = set4.getMinimumPoint().getBlockY();
    	int minz = set4.getMinimumPoint().getBlockZ();

    	int maxx = set4.getMaximumPoint().getBlockX();
    	int maxy = set4.getMaximumPoint().getBlockY();
    	int maxz = set4.getMaximumPoint().getBlockZ();

    	
		for(int x=minx; x<maxx; x++) {
			for(int y=miny; y<maxy; y++) {
				for(int z=minz; z<maxz; z++) {
					
					
	        		Protection protection = LWC1.findProtection(world1, x, y, z);
	        		if(protection != null) {
	        			protection.remove();
	        		//this.getServer().getLogger().info("Removed LWC Protection from Plot: " + args[0]);
	        		}else{
	        		
	        		}
					
					
				}
			}
		}
 
    }
   	}
//************************
   	
   
//Change Sign   	
	 if(this.getsignConfig().contains("sign." + args[0])){
		 
		 String price = pflag.toString();
		 
		 //-----------------

             Location signloc = stringToLoc(this.getsignConfig().getString("sign." + args[0]));
             Block that = signloc.getBlock();
             
             if (that.getType() == Material.SIGN_POST || that.getType() == Material.WALL_SIGN){
            	 
                 Sign s = (Sign) signloc.getBlock().getState();

                 s.setLine(0, "[BuyLand]");
                 s.setLine(1, "For Sale");
                 s.setLine(2, args[0]);
                 s.setLine(3, price);
                 s.update();
            	 
             }else{
            	// Maybe create a sign if it doesn't exist?

             }
             

		 //-------------
		 
	 }
   	
   	if (this.getConfig().getBoolean("buyland.resetlandonsale") == true){
   	//This is used for Loading Schematics   	
   	   	World world = player.getWorld();
   	   	int x = set2.getMinimumPoint().getBlockX();
   	   	int y = set2.getMinimumPoint().getBlockY();
   	   	int z = set2.getMinimumPoint().getBlockZ();
   	   	Vector v1 = new Vector(x,y,z);
   	   	File file = new File(getDataFolder() + File.separator + "data" + File.separator + args[0] + ".schematic");
   	   	ResetMap(file, v1, world);
   	//End of Schematics
   	   	

   	}
   	
  //test101
  	//LWC
  	if (this.getConfig().getBoolean("buyland.removelwcprotection") == true){
  	   	
  	final Plugin plugin;
  	plugin = Bukkit.getServer().getPluginManager().getPlugin("LWC");
  	if(plugin != null && plugin instanceof LWCPlugin) {
  		LWC1 = ((LWCPlugin) plugin).getLWC();
  	}
  	
  	
    World world9 = player.getWorld();
    RegionManager regionManager2 = this.getWorldGuard().getRegionManager(world9);
    if(regionManager2.getRegionExact(args[0]) == null){
    //	this.getServer().getLogger().info("NULL");
    }else{

    	
    	ProtectedRegion set4 = regionManager2.getRegionExact(args[0]);
    	int minx = set4.getMinimumPoint().getBlockX();
    	int miny = set4.getMinimumPoint().getBlockY();
    	int minz = set4.getMinimumPoint().getBlockZ();

    	int maxx = set4.getMaximumPoint().getBlockX();
    	int maxy = set4.getMaximumPoint().getBlockY();
    	int maxz = set4.getMaximumPoint().getBlockZ();

    	
  		for(int x11=minx; x11<maxx; x11++) {
  			for(int y11=miny; y11<maxy; y11++) {
  				for(int z11=minz; z11<maxz; z11++) {
  					
  	        		Protection protection = LWC1.findProtection(world1, x11, y11, z11);
  	        		if(protection != null) {
  	        			protection.remove();
  	        		//this.getServer().getLogger().info("Removed LWC Protection from Plot: " + args[0]);
  	        		}else{
  	        		
  	        		}
  					
  					
  				}
  			}
  		}

    }
  	}
  //************************

   	
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
}
	}else{
		String convertedgeneral = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.permission"));
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedgeneral);
	}
		   }
	


//BUYLAND COMMAND!
	if (cmd.getName().equalsIgnoreCase("buyland")){
	if (player.hasPermission("buyland.buy") || player.hasPermission("buyland.all")){
		
		
		if(args.length > 1){
			
			
//Begin Home
			if (player.hasPermission("buyland.tp") || player.hasPermission("buyland.all")){
		if (args[0].equalsIgnoreCase("tp")){
			if(args.length < 2){
				player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Usage: /buyland tp REGION");
			}else{
				if(this.getsignConfig().contains("sign." + args[1])){
					
                Location tptosign = stringToLoc(this.getsignConfig().getString("sign." + args[1]));
				player.teleport(tptosign);
				}else{
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Teleportation has not been enabled for this region or it does not exist.");
						
				}
			}
		}
		}
			
		//Begin AddMEMBER
			if (player.hasPermission("buyland.buy.addmember") || player.hasPermission("buyland.all")){
		if (args[0].equalsIgnoreCase("addmember")){
			
			if(args.length < 3){
				player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Usage: /buyland addmember REGION PLAYER");
			}else{
			
			if (this.getRentConfig().contains("rent." + args[1] + ".rentable")){
	        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.error1"));
	        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
			
			}else{
			
	        World world1 = player.getWorld();
	        RegionManager regionManager = this.getWorldGuard().getRegionManager(world1);
	        if(regionManager.getRegionExact(args[1]) == null){
	        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
	        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
	        }
			
			
			World world2 = player.getWorld();
		    RegionManager regionManager1 = this.getWorldGuard().getRegionManager(world2);
			ProtectedRegion set = regionManager1.getRegionExact(args[1]);
	

			  DefaultDomain owner = set.getOwners();
			  	String owner2 = owner.toPlayersString();
			  	String pn = player.getName().toLowerCase();
			  	
			  	
			  	if (owner2.contains(pn)){
//----------
	    	            	set.getMembers().addPlayer(args[2]);
		    	            try {
								regionManager1.save();
							} catch (ProtectionDatabaseException e) {
								//  Auto-generated catch block
								e.printStackTrace();
							}
		    	            

		    	            
		    	        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.member.addmember"));
		    	        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
//----------
				}else{
					String convertedownland = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.sell.dontown"));
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedownland);
				}
		    	        	
			}
			}
		}
			}else{
				String convertedgeneral = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.permission"));
				
				player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedgeneral);
			}
			//END ADDMEMBER
		

			//Begin RemoveMEMBER
			if (player.hasPermission("buyland.buy.removemember") || player.hasPermission("buyland.all")){
		if (args[0].equalsIgnoreCase("removemember")){
				
				if(args.length < 3){
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Usage: /buyland removemember REGION PLAYER");
				}else{
				
				if (this.getRentConfig().contains("rent." + args[1] + ".rentable")){
		        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.error1"));
		        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
				
				}else{
				
		        World world1 = player.getWorld();
		        RegionManager regionManager = this.getWorldGuard().getRegionManager(world1);
		        if(regionManager.getRegionExact(args[1]) == null){
		        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
					
		        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
		        }
				
		        
		        
		        World world2 = player.getWorld();
			    RegionManager regionManager1 = this.getWorldGuard().getRegionManager(world2);
				ProtectedRegion set = regionManager1.getRegionExact(args[1]);
		
				
				  DefaultDomain owner = set.getOwners();
				  	String owner2 = owner.toPlayersString();
				  	String pn = player.getName().toLowerCase();
				  	
				  	
				  	if (owner2.contains(pn)){
//----------
				  		
				
		    	            	set.getMembers().removePlayer(args[2]);
			    	            
			    	            try {
									regionManager1.save();
								} catch (ProtectionDatabaseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		    	            	
			    	        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.member.removemember"));
			    	        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
		    	            
			    	        	
			    	        	
//----------
					}else{
						String convertedownland = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.sell.dontown"));
						player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedownland);
					}    	        	
			    	        	
				
				}
				}
		}
			}else{
				String convertedgeneral = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.permission"));
				
				player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedgeneral);
			}
				//END RemoveMEMBER
		}else{
			
				//Begin list
				
			if (args[0].equalsIgnoreCase("list")){
				if (player.hasPermission("buyland.list") || player.hasPermission("buyland.all")){
				if(args.length > 1){
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Usage: /buyland list");
				}else{ 
	World w1 = player.getWorld();
					Map<String, ProtectedRegion> regionMap = WGBukkit.getRegionManager(w1).getRegions();
					player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "You own regions: ");
					for(ProtectedRegion region : regionMap.values()) {	
					if(region.isOwner(player.getName())) {	
				if(region.getFlag(DefaultFlag.BUYABLE) == null){
				//	player.sendMessage("null" + region.getId());
				}else{
						if(region.getFlag(DefaultFlag.BUYABLE) == false){
							player.sendMessage(" " + region.getId());
								
						}
					}				
					}
					}	
					
					
				}
			}
			
				
			}else{
			
		
		if (this.getRentConfig().contains("rent." + args[0] + ".rentable")){
        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.rent.error1"));
        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
		
		}else{
		
        World world1 = player.getWorld();
        RegionManager regionManager = this.getWorldGuard().getRegionManager(world1);
        if(regionManager.getRegionExact(args[0]) == null){
        	String convertederror1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.error1"));
			
        	player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertederror1);
     
        }else{
        	
			//BETA AREA
			World world2 = player.getWorld();
		    RegionManager regionManager1 = this.getWorldGuard().getRegionManager(world2);
			ProtectedRegion set2 = regionManager1.getRegionExact(args[0]);
        
        Boolean bflag = set2.getFlag(DefaultFlag.BUYABLE);
        Double pflag = set2.getFlag(DefaultFlag.PRICE);
          
	if (bflag == null){
		bflag = false;
	}
	
	if (pflag == null){
		if (this.getConfig().getBoolean("buyland.usepriceperblock") == true){
			//BETA AREA
	        	int size = set2.volume();
	        //	player.sendMessage("Area of blocks: " + size);
	        	double ppb = this.getConfig().getDouble("buyland.defaultpriceperblock");
	        	pflag = (double) (size * ppb);
	        	//player.sendMessage("Pflag: " + pflag + " - ppb: " + ppb);
	        //BETA AREA
		}else{
	pflag = this.getConfig().getDouble("buyland.defaultprice");
		}
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
    	 
 		if (this.getConfig().getBoolean("buyland.denyentrytoland") == true){
 	        set2.setFlag(DefaultFlag.ENTRY, State.DENY);
 			}else{
 				set2.setFlag(DefaultFlag.ENTRY, null);
 			}

    		String convertedw1 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.buy.welcome1"));
    		String convertedw2 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.buy.welcome2"));
    		
    		if (this.getConfig().getBoolean("buyland.landgreeting") == true){
    	 set2.setFlag(DefaultFlag.GREET_MESSAGE, convertedw1 + p1 + convertedw2);
    		}else if (this.getConfig().getBoolean("buyland.landgreetingerasemsg") == true){
    	    	set2.setFlag(DefaultFlag.GREET_MESSAGE, null);
    	    }
 	   
    		
    		//Land Owned Number
    	 int finalland = numofland + 1;
    	 this.getCustomConfig().set(nm, finalland);
    	 this.saveCustomConfig();
    	 //Land Owned Number
    	 
    	 
    	  DefaultDomain dd = new DefaultDomain();
    	    dd.addPlayer(p1);
    	 set2.setOwners(dd);
//Start Schematics
    	 World world = player.getWorld();

    	 int x = set2.getMinimumPoint().getBlockX();
    	 int y = set2.getMinimumPoint().getBlockY();
    	 int z = set2.getMinimumPoint().getBlockZ();
    	 Location loc1 = new Location(world, x,y,z);
    	 int x1 = set2.getMaximumPoint().getBlockX();
    	 int y1 = set2.getMaximumPoint().getBlockY();
    	 int z1 = set2.getMaximumPoint().getBlockZ();
    	 Location loc2 = new Location(world, x1,y1,z1);
    	 String name = args[0];
    		if (this.getConfig().getBoolean("buyland.resetlandonsale") == true){
    	 		try {
    	 			saveSchematic(loc1, loc2, name, player);
    	 		} catch (EmptyClipboardException e) {
    	 			// TODO Auto-generated catch block
    	 			e.printStackTrace();
    	 		} catch (IOException e) {
    	 			// TODO Auto-generated catch block
    	 			e.printStackTrace();
    	 		} catch (DataException e) {
    	 			// TODO Auto-generated catch block
    	 			e.printStackTrace();
    	 		}
    		}
//END schematics
         
    		
    		
    		
    		
//change sign    		
if(this.getsignConfig().contains("sign." + args[0])){
    		 
    		 //-----------------
                 //Change Signs
                 Location signloc = stringToLoc(this.getsignConfig().getString("sign." + args[0]));
                 
                 Block that = signloc.getBlock();
                 
                 if (that.getType() == Material.SIGN_POST || that.getType() == Material.WALL_SIGN){
                 Sign s = (Sign) signloc.getBlock().getState();
           
                 s.setLine(0, "[BuyLand]");
                 s.setLine(1, "Sale Back");
                 s.setLine(2, args[0]);
                 s.setLine(3, player.getName());
                 s.update();
            	 
        	                  }else{
                	// Maybe create a sign if it doesnt exist?
            	  
                 }
    		 //-------------
      
}




//test101
	//LWC
	if (this.getConfig().getBoolean("buyland.removelwcprotection") == true){
	   	
	final Plugin plugin;
	plugin = Bukkit.getServer().getPluginManager().getPlugin("LWC");
	if(plugin != null && plugin instanceof LWCPlugin) {
		LWC1 = ((LWCPlugin) plugin).getLWC();
	}
	
	
  World world9 = player.getWorld();
  RegionManager regionManager2 = this.getWorldGuard().getRegionManager(world9);
  if(regionManager2.getRegionExact(args[0]) == null){
  //	this.getServer().getLogger().info("NULL");
  }else{

  	
  	ProtectedRegion set4 = regionManager2.getRegionExact(args[0]);
  	int minx = set4.getMinimumPoint().getBlockX();
  	int miny = set4.getMinimumPoint().getBlockY();
  	int minz = set4.getMinimumPoint().getBlockZ();

  	int maxx = set4.getMaximumPoint().getBlockX();
  	int maxy = set4.getMaximumPoint().getBlockY();
  	int maxz = set4.getMaximumPoint().getBlockZ();

  	
		for(int x11=minx; x11<maxx; x11++) {
			for(int y11=miny; y11<maxy; y11++) {
				for(int z11=minz; z11<maxz; z11++) {
					
	        		Protection protection = LWC1.findProtection(world1, x11, y11, z11);
	        		if(protection != null) {
	        			protection.remove();
	        		//this.getServer().getLogger().info("Removed LWC Protection from Plot: " + args[0]);
	        		}else{
	        		
	        		}
					
					
				}
			}
		}

  }
	}
//************************	
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
		}
		}
	}
	
		
	}else{
		String convertedgeneral = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.permission"));
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedgeneral);
	}
	
	

		   }
			   //END of buyLAND	
	//BuyLand Command

	
			   

//PRICELAND COMMAND
if (cmd.getName().equalsIgnoreCase("priceland")){ 
if (player.hasPermission("buyland.price") || player.hasPermission("buyland.all")){
	
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
							if (this.getConfig().getBoolean("buyland.usepriceperblock") == true){
								//BETA AREA
	                                int size = set2.volume();
	                                double ppb = this.getConfig().getDouble("buyland.defaultpriceperblock");
	                	        	pflag = (double) (size * ppb);
						        //BETA AREA
							}else{
						pflag = this.getConfig().getDouble("buyland.defaultprice");
							}
						}
					
					 String test = bflag.toString();
				        	
			        	if (this.getRentConfig().contains("rent." + args[0] + ".rentable")){
							double s = this.getRentConfig().getDouble("rent." + args[0] +".costpermin") / 2;
							double m = this.getRentConfig().getDouble("rent." + args[0] +".costpermin");
							double h = this.getRentConfig().getDouble("rent." + args[0] +".costpermin") * 60;
							double d = this.getRentConfig().getDouble("rent." + args[0] +".costpermin") * 1440;
							
							player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "This Region is rentable.");
							player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "The cost of " + args[0] + " is: ");
							player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "1 Second = " + s);
							player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "1 Minute = " + m);
							player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "1 Hour = " + h);
							player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "1 Day = " + d);
										        		
			        	}else{

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
				     String convertednotforsale = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.price.dontown"));
						
					 player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertednotforsale);
				
				 }
				 
				        
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

}else{
	
	
	if (cmd.getName().equalsIgnoreCase("reloadbuyland")){
					  
						 reloadConfig();  
						 reloadCustomConfig();
						 reloadlanguageConfig();
						 reloadRentConfig();
						 reloadrentdbConfig();
						 reloadsignConfig();
						 
						String convertedgeneral2 = ChatColor.translateAlternateColorCodes('&', this.getlanguageConfig().getString("buyland.general.reload"));
						 sender.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + convertedgeneral2);
					
				   
				   }else{
					 sender.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "This Command is for ingame only!");
				   }
	
	
	//------------------
}
	return true;
}
	

}