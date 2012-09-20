package me.buyland.buyland;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


import java.util.logging.Level;
import java.util.logging.Logger;


import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
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
	//getlanguageConfig().addDefault("buyland.general.error2", "Error! Enter a Number/Price.");
	
	getlanguageConfig().addDefault("buyland.admin.forsale", "This Region has been placed back for sale.");
	
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
	config.addDefault("buyland.resetlandonsale", true);
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
      //  logger.warning(("File does not exist."));
    }
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

public static Selection getWorldEditSelection(Player ply) {
    Plugin we = Bukkit.getPluginManager().getPlugin("WorldEdit");
    if(we != null && we instanceof WorldEditPlugin) {
        return ((WorldEditPlugin) we).getSelection(ply);
    }
    return null;
}

public void AddProtectedRegion(String p1, String world, int x1, int y1, int z1, int x2, int y2, int z2, String name)
{       

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
	
    pr.setFlag(DefaultFlag.GREET_MESSAGE, convertedforsale);
    
    DefaultDomain dd = new DefaultDomain();
    // add the player to the region      
    dd.addPlayer(p1);
    // set the player as the owner
    pr.setOwners(dd);

    logger.info("BuyLand: Added region: " + name);
    
    try
    {
        rm.save();
    }

     catch (Exception exp)


    { }

}

@SuppressWarnings("deprecation")
private void saveSchematic(Location loc1, Location loc2, String schematic, Player player) throws EmptyClipboardException, IOException, DataException {
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
 
    //te.logUtil.debug(min + " - " + max);
 
    editSession.enableQueue();
    CuboidClipboard clipboard = new CuboidClipboard(max.subtract(min).add(new Vector(1, 1, 1)), min);
    clipboard.copy(editSession);
    clipboard.saveSchematic(saveFile);
    editSession.flushQueue();
}



public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	
	Player player = null;
	if (sender instanceof Player) {
		player = (Player) sender;
	}

	if (args.length == 0){
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
					   
					   }else{
//General HELP
							PluginDescriptionFile pdffile = this.getDescription();
							
							
		player.sendMessage(ChatColor.RED + "BuyLand: V" +  pdffile.getVersion() + ChatColor.GOLD + " is a product of chriztopia.com");
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/buyland [region_name] or /priceland [region_name] or /sellland [region_name]");  
		if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.*")){
//Admin HELP
			player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/abl forsale [region_name] or /abl save [region_name] or /abl price [region_name] [cost]");  
			player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "/abl reset [region_name]");  
			
		}
					   }
					   }
		   if (args.length > 0){
			   
//AdminBuyLand ABL COMMAND	
if (cmd.getName().equalsIgnoreCase("abl") || (cmd.getName().equalsIgnoreCase("adminbuyland"))){ 
if (player.hasPermission("buyland.admin") || player.hasPermission("buyland.*")){
	if (args[0].equalsIgnoreCase("save")){
//StartSaveArg
		int x1 = getWorldEditSelection(player).getMaximumPoint().getBlockX();
		int y1 = getWorldEditSelection(player).getMaximumPoint().getBlockY();
		int z1 = getWorldEditSelection(player).getMaximumPoint().getBlockZ();
		int x2 = getWorldEditSelection(player).getMinimumPoint().getBlockX();
		int y2 = getWorldEditSelection(player).getMinimumPoint().getBlockY();
		int z2 = getWorldEditSelection(player).getMinimumPoint().getBlockZ();
		String d1 = player.getWorld().getName();
		String p1 = player.getName();
//DEBUG		player.sendMessage("Player Name: " + p1 +" WorldNAME: " + d1 + " X1: " + x1 + " Y1: " + y1 + " Z1: " + z1 + " X2: " + x2 + " Y2: " + y2 + " Z2: " + z2 + " Testregion");
		AddProtectedRegion(p1, d1, x1, y1, z1, x2, y2, z2, args[1]);		
		player.sendMessage(ChatColor.RED + "BuyLand: " + ChatColor.WHITE + "Region Added!");
//EndSaveArg
	}
	
	if (args[0].equalsIgnoreCase("price")){
		
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
	
	if (args[0].equalsIgnoreCase("reset")){
//StartResetArg
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

		ResetMap(file, v1, world);
	player.sendMessage(ChatColor.RED + "Buyland: " + ChatColor.WHITE + "Region Reset to Default!");
//EndResetArg
        }
			}
	
	
				if (args[0].equalsIgnoreCase("forsale")){
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
				 
				 set2.setFlag(DefaultFlag.BUYABLE, true);
				 set2.setFlag(DefaultFlag.GREET_MESSAGE, convertedforsale);
			
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
				ResetMap(file, v1, world);
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
//Start Schematics
    	 World world = player.getWorld();
    	// RegionManager regionManager = this.getWorldGuard().getRegionManager(world);
    	// ProtectedRegion set2 = regionManager.getRegionExact(args[0]);
    	 int x = set2.getMinimumPoint().getBlockX();
    	 int y = set2.getMinimumPoint().getBlockY();
    	 int z = set2.getMinimumPoint().getBlockZ();
    	 Location loc1 = new Location(world, x,y,z);
    	 int x1 = set2.getMaximumPoint().getBlockX();
    	 int y1 = set2.getMaximumPoint().getBlockY();
    	 int z1 = set2.getMaximumPoint().getBlockZ();
    	 Location loc2 = new Location(world, x1,y1,z1);
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


