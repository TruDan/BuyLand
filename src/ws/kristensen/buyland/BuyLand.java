package ws.kristensen.buyland;

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
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/*
Commands

    -- /buyland [regionname] - Buys the Region
    -- /sellland [regionname] - Sells the Region
    -- /priceland [regionname] - Prices the Region Works for both rentland and buyland
    -- /rentland [regionname] [Time] [Sec/Min/Hour/Day] - Rents a region
    -- /rentland [regionname] cost - Shows the cost of a region.
    -- /buyland addmember [regionname] [playername] - Add a member to a region.
    -- /buyland removemember [regionname] [playername] - Remove a member from a region.
    -- /buyland tp [regionname] - Teleports a player to the region.
    -- /buyland list - Lists all regions the player owns.
    -- /rentland addmember [regionname] [playername] - Add a member to a rented region.
    -- /rentland removemember [regionname] [playername] - Remove a member from a rented region. 

Admin Commands

    -- /reloadbuyland - Will Reload all Config Files... Config.yml, DB.yml, and Language.yml
    -- /abl or /adminbuyland - there are several commands for this listed below
    -- /abl forsale [regionname] - This will set the land back to default. Just as if the player sold the land back.
    -- /abl save [regionname] - Select your cuboid using WorldEdit then run this command to save a new region with buyable set to true automatically.
    -- /abl price [regionname] [cost] - Use this to set a price for each individual region. (if you dont use this command the default price from config will take over.)
    -- /abl reset [regionname] - This will simply reset the Land back to when a player bought the land. (This will not sell back the land only reset it to default.)
    -- /abl lwcremove [regionname] - Removes LWC Protections for that region.
    -- /abl list [player] - Lists all regions a player owns.
    -- /rentland save [regionname] - Select your cuboid using WorldEdit then run this command to setup a RentLand region.
    -- /rentland [regionname] reset - Resets a rentland region 

*/

public class BuyLand extends JavaPlugin {
	//public static BuyLand plugin;
	public final Logger logger = Logger.getLogger("Minecraft");
	
    private final BlCommandListenerAdmin         clAdmin        = new BlCommandListenerAdmin(this);
    private final BlCommandListenerBuyland       clBuyland      = new BlCommandListenerBuyland(this);
    private final BlCommandListenerReloadBuyland clReload       = new BlCommandListenerReloadBuyland(this);
    private final BlCommandListenerRentland      clRentland     = new BlCommandListenerRentland(this);
    private final BlCommandListenerSelllandSell  clSellland     = new BlCommandListenerSelllandSell(this);
    private final BlCommandListenerPriceland     clPriceland    = new BlCommandListenerPriceland(this);
    
    private final BlEventListenerPlayerInteract  elPlayerInteract   = new BlEventListenerPlayerInteract(this);
    private final BlEventListenerPlayerJoin      elPlayerJoin       = new BlEventListenerPlayerJoin(this);
    private final BlEventListenerSignChange      elSignChange       = new BlEventListenerSignChange(this);
    
	WorldGuardPlugin worldGuard;

	public static Permission permission = null;
	public static Economy    econ       = null;
	public static Chat       chat       = null;

	//---------------
    private File              signConfigFile     = null;
	private FileConfiguration signConfig         = null;
    private File              customConfigFile   = null;
	private FileConfiguration customConfig       = null;
    private File              languageConfigFile = null;
	private FileConfiguration languageConfig     = null;
    private File              rentConfigFile     = null;
	private FileConfiguration rentConfig         = null;
    private File              rentdbConfigFile   = null;
	private FileConfiguration rentdbConfig       = null;
	
    public FileConfiguration getSignConfig() {
        if (signConfig == null) {
            this.reloadSignConfig();
            //make sure this option is set
            signConfig.options().copyDefaults(true);
        }
        return signConfig;
    }
    public void reloadSignConfig() {
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
        
        //Make sure the minimum settings in the file are there with these defaults
        signConfig.options().header("BuyLand Sign DB File. Used to keep track of signs.");

        signConfig.addDefault("sign.placeholder", "location");
    }
    public void saveSignConfig() {
        if (signConfig == null || signConfigFile == null) {
            return;
        }
        try {
            getSignConfig().save(signConfigFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + signConfigFile, ex);
        }
    }

    public FileConfiguration getCustomConfig() {
        if (customConfig == null) {
            this.reloadCustomConfig();
            //make sure this option is set
            customConfig.options().copyDefaults(true);
        }
        return customConfig;
    }
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
        
        //Make sure the minimum settings in the file are there with these defaults
        customConfig.options().header("BuyLand DB File. Used for keeping track of how many plots a user has.");

        customConfig.addDefault("user", 0);
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

    public FileConfiguration getLanguageConfig() {
        if (languageConfig == null) {
            this.reloadLanguageConfig();
            //make sure this option is set
            languageConfig.options().copyDefaults(true);
        }
        return languageConfig;
    }
    public void reloadLanguageConfig() {
        if (languageConfigFile == null) {
        	languageConfigFile = new File(getDataFolder(), "language.yml");
        }
        languageConfig = YamlConfiguration.loadConfiguration(languageConfigFile);
     
        // Look for language file in the jar
        InputStream defConfigStream = this.getResource("language.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            languageConfig.setDefaults(defConfig);
        }

        //Make sure the minimum settings in the file are there with these defaults
        languageConfig.options().header("BuyLand Language File.");

        languageConfig.addDefault("buyland.general.permission", "You do not have permission for that command.");   
        languageConfig.addDefault("buyland.general.reload", "Config reloaded!");
        languageConfig.addDefault("buyland.general.error1", "Error! Region name was incorrect.");
        languageConfig.addDefault("buyland.general.error2", "Error! Enter a Number/Price.");
        languageConfig.addDefault("buyland.general.parameters", "Incorrect number of parameters.");

        languageConfig.addDefault("buyland.admin.forsale", "This Region has been placed back for sale.");

        languageConfig.addDefault("buyland.rent.forrent", "This land is for rent!");
        languageConfig.addDefault("buyland.rent.noperm", "You dont have permission to do that!");
        languageConfig.addDefault("buyland.rent.tenant", "This land currently has a tenant - Time left: ");
        languageConfig.addDefault("buyland.rent.rentby", "This land is being rented by ");
        languageConfig.addDefault("buyland.rent.notbe", "This land can not be rented.");
        languageConfig.addDefault("buyland.rent.cantafford", "%s to buy the land.");
        languageConfig.addDefault("buyland.rent.error1", "Sorry Rentable land can not be bought nor sold.");
        languageConfig.addDefault("buyland.rent.error2", "Sorry this region is not rentable.");
        languageConfig.addDefault("buyland.rent.max", "You have rented the Maximum amount of land allowed.");

        languageConfig.addDefault("buyland.sell.forsale", "This land is for sale.");
        languageConfig.addDefault("buyland.sell.back1", "You have sold back the land for ");
        languageConfig.addDefault("buyland.sell.back2", ". Your balance is: %s");
        languageConfig.addDefault("buyland.sell.dontown", "You do not own this land!");
        languageConfig.addDefault("buyland.sell.notsneak", "You must be sneaking when you click a sign to sell land!");
        languageConfig.addDefault("buyland.buy.permission", "You do not have permission to sell a region.");

        languageConfig.addDefault("buyland.member.removemember", "Removed Member!");
        languageConfig.addDefault("buyland.member.addmember", "Added Member!");
        
        languageConfig.addDefault("buyland.buy.max", "You have bought the Maximum amount of land allowed.");
        languageConfig.addDefault("buyland.buy.welcome1", "Welcome to ");
        languageConfig.addDefault("buyland.buy.welcome2", "`s Land!");
        languageConfig.addDefault("buyland.buy.cantafford", "%s to buy the land.");
        languageConfig.addDefault("buyland.buy.permission", "You do not have permission to buy a region.");
        
        
        languageConfig.addDefault("buyland.buy.bought", "You bought the land for %s and you now have %s");
        languageConfig.addDefault("buyland.buy.dontown", "Sorry this land is not buyable.");
        
        languageConfig.addDefault("buyland.price.price", "You currently have %s to purchase this land.");
        languageConfig.addDefault("buyland.price.cost", "This land is buyable and costs: ");
        languageConfig.addDefault("buyland.price.max1", "You have ");
        languageConfig.addDefault("buyland.price.max2", " pieces of land. The Max is ");
        languageConfig.addDefault("buyland.price.dontown", "Sorry this land is not buyable.");
    }
    public void saveLanguageConfig() {
        if (languageConfig == null || languageConfigFile == null) {
            return;
        }
        
        try {
            getLanguageConfig().save(languageConfigFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + languageConfigFile, ex);
        }
    }

    public FileConfiguration getRentConfig() {
        if (rentConfig == null) {
            this.reloadRentConfig();
            //make sure this option is set
            rentConfig.options().copyDefaults(true);
        }
        return rentConfig;
    }
    public void reloadRentConfig() {
        if (rentConfigFile == null) {
            rentConfigFile = new File(getDataFolder(), "rent.yml");
        }
        rentConfig = YamlConfiguration.loadConfiguration(rentConfigFile);
     
        // Look for defaults in the jar
        InputStream defConfigStream = this.getResource("rent.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            rentConfig.setDefaults(defConfig);
        }
        
        //Make sure the minimum settings in the file are there with these defaults
        rentConfig.options().header("Rent File");

        rentConfig.addDefault("rent.placeholder.time", 0);
        rentConfig.addDefault("rent.placeholder.rentable", true);
        rentConfig.addDefault("rent.placeholder.world", "world");
        rentConfig.addDefault("rent.placeholder.costpermin", 1.0);
    }
    public void saveRentConfig() {
        if (rentConfig == null || rentConfigFile == null) {
            return;
        }

        try {
            getRentConfig().save(rentConfigFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + rentConfigFile, ex);
        }
    }

    public FileConfiguration getrentdbConfig() {
        if (rentdbConfig == null) {
            this.reloadrentdbConfig();
        }
        //make sure this option is set
        rentdbConfig.options().copyDefaults(true);
        
        return rentdbConfig;
    }
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
        
        //Make sure the minimum settings in the file are there with these defaults
        rentdbConfig.options().header("BuyLand Rent DB File. Used for keeping track of how many rentable plots a user has.");
        
        rentdbConfig.addDefault("user", 0);
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

    public FileConfiguration getPluginConfig() {
        //this is a special case as the plugin config is built into bukkit.
        //this is normally accessable by calling this.getConfig(); as is done below.
        this.reloadPluginConfig();        
        
        return this.getConfig();
    }
    
    public void reloadPluginConfig() {
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
        //config.addDefault("buyland.maxamountofland", 1);
        config.addDefault("buyland.offlinelimitindays", 30);
        config.addDefault("buyland.offlinelimitenable", true);

        config.options().copyDefaults(true);
    }
    
    @Override
    public void onDisable() {
        saveRentConfig();
        PluginDescriptionFile pdffile = this.getDescription();
        this.logger.info(pdffile.getName() + " is now disabled.");
    }

    @Override
    public void onEnable() {
        //Register our commands to which we listen
        getCommand("adminbuyland").setExecutor(clAdmin);    //Admin Commands: /abl or /adminbuyland - There are several commands for this listed below
                                                            //                /abl forsale [regionname]      - This will set the land back to default. Just as if the player sold the land back.
                                                            //                /abl save [regionname]         - Select your cuboid using WorldEdit then run this command to save a new region with buyable set to true automatically.
                                                            //                /abl price [regionname] [cost] - Use this to set a price for each individual region. (if you dont use this command the default price from config will take over.)
                                                            //                /abl reset [regionname]        - This will simply reset the Land back to when a player bought the land. (This will not sell back the land only reset it to default.)
                                                            //                /abl lwcremove [regionname]    - Removes LWC Protections for that region.
                                                            //                /abl list [player]             - Lists all regions a player owns.
        getCommand("buyland").setExecutor(clBuyland);       //Sub Commands:   /buyland list - Lists all regions the player owns.<br/>
                                                            //                /buyland tp [Region Name] - Teleports a player to the region.<br/>
                                                            //                /buyland addmember [Region Name] [Player Name] - Add a member to a region.<br/>
                                                            //                /buyland removemember [Region Name] [Player Name] - Remove a member from a region.<br/>
                                                            //                /buyland [Region Name] - Buys the Region

        getCommand("sellland").setExecutor(clSellland);     //Sub Commands:   /sellland [regionname]                           - Sells a region.

        getCommand("reloadbuyland").setExecutor(clReload);  //Admin Commands: /reloadbuyland - Will Reload all Config Files... Config.yml, DB.yml, and Language.yml
        
        getCommand("rentland").setExecutor(clRentland);     //Admin Commands: /rentland save [regionname]    - Setup a RentLand region
                                                            //                /rentland [regionname] reset   - Resets a rentland region
                                                            //
                                                            //Sub Commands:   /rentland addmember [regionname] [playername]    - Add a member to a region.
                                                            //                /rentland removemember [regionname] [playername] - Remove a member from a region.
                                                            //                /rentland [regionname] cost                      - Shows the cost of a region.
                                                            //                /rentland [regionname] time                      - Displays time left for a rented region.
                                                            //                /rentland [regionname] x [Sec/Min/Hour/Day]      - Rents a region.
        getCommand("priceland").setExecutor(clPriceland);
        this.getServer().getPluginManager().registerEvents(elPlayerInteract, this);  //Handle sign left and right clicks
        this.getServer().getPluginManager().registerEvents(elPlayerJoin, this);      //Handle player joins
        this.getServer().getPluginManager().registerEvents(elSignChange, this);      //Handle sign create / alter

    	PluginDescriptionFile pdffile = this.getDescription();
    	this.logger.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled!");
    	
    	//setup the config files on disk    	
    	getLanguageConfig();   //Load or create the defaults
    	saveLanguageConfig();  //Save to disk
    	
    	getRentConfig();       //Load or create the defaults
    	saveRentConfig();      //Save to disk
    	
    	getSignConfig();       //Load or create the defaults
    	saveSignConfig();      //Save to disk
    	
    	getCustomConfig();     //Load or create the defaults
    	saveCustomConfig();    //Save to disk
    	
    	getrentdbConfig();     //Load or create the defaults
    	saverentdbConfig();    //Save to disk
    		
    	getPluginConfig();     //Load or create the defaults
    	saveConfig();          //Save to disk
    	
    	//Load the general plugin config
    	final FileConfiguration config = getPluginConfig();

    	//initialize the worldGuard variable above.
    	getWorldGuard();
    	
    	//Run a background process every 1200 ticks (1 minute) after first waiting 20 ticks for the plugin to start up
    	//Auto Sell a Region if the player has not been on for the specified amount of days
    	new BukkitRunnable() {
    	    public void run() {
    	        //See if we want to limit offline time
    	        if(config.getBoolean("buyland.offlinelimitenable") == true) {
    	            //Loop through each world
    	            for (World world: Bukkit.getWorlds()) {
    	                //make sure world is not null
    	                if (world == null) { return; }
    	
    	                //get a map of regions
    	                Map<String, ProtectedRegion> worldRegions = WGBukkit.getRegionManager(world).getRegions();
    
    	                for (ProtectedRegion protectedRegion : worldRegions.values()) {
    	                    if (protectedRegion.getFlag(DefaultFlag.BUYABLE) == null) {
    	                        //It is rentable, do nothing here
    	                    } else {
    	                        //See if the region is owned
    	                        if (protectedRegion.getFlag(DefaultFlag.BUYABLE) == false) {
    	                            //Get the owner names
    	                            String ownerNames = protectedRegion.getOwners().toUserFriendlyString();

    	                            //See if player has ever been on server
    	                            if (!Bukkit.getOfflinePlayer(ownerNames).hasPlayedBefore()) {
    	                                //Player has never been on this server
    	                                //Do not sell the region.
    	                                //TODO Make sure this is what we want to do
    	                            } else {
                                        //get when the player was last seen by bukkit
                                        long timePlayerLastSeen = Bukkit.getOfflinePlayer(ownerNames).getLastPlayed();
                                        //Get the current time on the server
                                        long timeCurrent = System.currentTimeMillis();
    	                                //Calculate the time away from the server
        	                            long timeAwayFromServer = timeCurrent - timePlayerLastSeen;
        	                            //Get the maximum amount of time logged off before the region is sold
        	                            long maximumAllowedTimeAwayFromServer = getConfig().getLong("buyland.offlinelimitindays") * (24 * 60 * 60 * 1000L);
        	                            //See if they have been away long enough
        	                            if (timeAwayFromServer > maximumAllowedTimeAwayFromServer) {
        	                                //See if this is a rental
        	                                if (getRentConfig().contains("rent." + protectedRegion.getId() + ".rentable")) {
                                                //It is rentable, do nothing here
                                            } else {
                                                //Sell region
                                                for (Player possibleAdmin : Bukkit.getOnlinePlayers()) {
                                                    if(possibleAdmin.isOp() || possibleAdmin.hasPermission("buyland.admin")) {
                                                        //use the first admin or op found to sell the region.
                                                        //Notify admin or op found
                                                        sendMessageInfo(possibleAdmin, "Owner: " + ownerNames + " Region: " + protectedRegion.getId());
                                                        //Sell the region
                                                        Bukkit.dispatchCommand(Bukkit.getPlayer(possibleAdmin.getName()), "abl forsale " + protectedRegion.getId());
                                                        //Stop looking for admins
                                                        break;
                                                    }
                                                }
                                            }
        	                            }
    	                            }
    	                        }
    	                    }				
    	                }	
    	            }
    	        } else {
    	            //Bukkit.getLogger().info("Auto Remove Disabled...");
    	        }
    	    }
    	}.runTaskTimer(this, 20L, 1200L);
    	
        //Run a background process every 1200 ticks (1 minute) after first waiting 20 ticks for the plugin to start up
        //Auto release rentals when time has expired
    	new BukkitRunnable() {
    	    public void run() {
    	        ConfigurationSection rentalConfigSection = getRentConfig().getConfigurationSection("rent");
    	        //Loop through each rental region
    	        for (String regionName : rentalConfigSection.getKeys(false)) {
    	            //see if there is a time section defined for the region
    	            if (rentalConfigSection.contains(regionName + ".time")) {
    	                //Do nothing as the section already has the values we need
    	            } else {
    	                //Set some default values for the region rental information since it doesn't exist 
            	    	getRentConfig().addDefault("rent."+ regionName  + ".time", 0);
            	    	getRentConfig().addDefault("rent."+ regionName  + ".rentable", true);
            	    	getRentConfig().addDefault("rent."+ regionName  + ".world", "world");
            	    	getRentConfig().addDefault("rent."+ regionName  + ".costpermin", 1.0);
            	    	getRentConfig().options().copyDefaults(true);
            	    	saveRentConfig();
    	            }

    	            
    	            //See if it is not rentable and time has expired for the rental
    	            if (rentalConfigSection.getBoolean(regionName + ".rentable") == false && System.currentTimeMillis() > rentalConfigSection.getLong(regionName + ".time")) {
                        String worldName = rentalConfigSection.getString(regionName + ".world");
                        World world = Bukkit.getWorld(worldName);

                        //Reset the rental region if needed
                        if (resetExpiredRentedRegion(world, regionName) == true) {
                            //Send message to everyone
                            if (config.getBoolean("buyland.rentbroadcastmsg") == true) {
                                broadcastMessageInfo(regionName + " is now rentable!");
                            }                            
                        }
    	            }
    	        } 
    	    }
    	}.runTaskTimer(this, 20L, 1200L); 
    			
    	//Make sure vault economy works properly.  If not, disable self
        if (!setupVaultEconomy()) {
            this.logger.info("Could not load due to Vault not being loaded.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //Setup vault chat
        setupVaultChat();
        //Setup vault permissions
        setupVaultPermissions();	
        
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
        
        fixRegionNames();
    }

    /**
     * converts the mixed case region names into lowercase region names in the rent and sign files.
     * Then save the config files.
     */
    private void fixRegionNames() {
        //Fix names in the Rent config file
        
        //Get the rent config section from the file
        ConfigurationSection rentalConfigSection = getRentConfig().getConfigurationSection("rent");
        //Loop through each rental region
        for (String regionName : rentalConfigSection.getKeys(false)) {
            if (regionName != regionName.toLowerCase()) {
                //set the config values for the region name
                getRentConfig().set("rent." + regionName.toLowerCase() + ".time", getRentConfig().get("rent." + regionName + ".time"));
                getRentConfig().set("rent." + regionName.toLowerCase() + ".rentable", getRentConfig().getBoolean("rent." + regionName + ".rentable"));
                getRentConfig().set("rent." + regionName.toLowerCase() + ".world", getRentConfig().get("rent." + regionName + ".world"));
                getRentConfig().set("rent." + regionName.toLowerCase() + ".costpermin", getRentConfig().get("rent." + regionName + ".costpermin"));

                //remove the old regionName
                getRentConfig().set("rent." + regionName, null);
            }
        }
        saveRentConfig();
        
        //Get the sign config section from the file
        ConfigurationSection signConfigSection = getSignConfig().getConfigurationSection("sign");
        //Loop through each sign region
        for (String regionName : signConfigSection.getKeys(false)) {
            if (regionName != regionName.toLowerCase()) {
                //set the config values for the region name
                getSignConfig().set("sign." + regionName.toLowerCase(), getSignConfig().get("sign." + regionName));

                //remove the old regionName
                getSignConfig().set("sign." + regionName, null);
            }
        }
        saveSignConfig();
        
    }

    /**
     * this will register a buyland sign
     * This makes sure there is only buyland sign
     * 
     * @param world World that the region belongs to
     * @param argRegionName String name of the region
     * @return boolean true if the region was successfully sold, false otherwise.
     */
    protected boolean registerBuyLandSign(World world, String argRegionName, Location newSignLocation, String[] lines) {
        //make sure it is the proper case
        argRegionName = argRegionName.toLowerCase();
        
        //Make sure location passed in is a valid BuyLand sign
        Block signBlockLocation = newSignLocation.getBlock();
        //see if location is a sign
        if (signBlockLocation.getType() == Material.SIGN_POST || signBlockLocation.getType() == Material.WALL_SIGN) {
            //see if it is a valid BuyLand sign
            if(lines[0] == "[BuyLand]" && (lines[1] == "For Sale" || lines[1] == "For Rent")) {
                //We have a valid sign passed in
                
                //See if we have an existing sign in the system for the region
                //Inactivate it if it exists
                if (getSignConfig().contains("sign." + argRegionName)) {
                    //Get the block at the location from the config
                    signBlockLocation = stringToLocation(getSignConfig().getString("sign." + argRegionName)).getBlock();
                    //see if location is a sign
                    if (signBlockLocation.getType() == Material.SIGN_POST || signBlockLocation.getType() == Material.WALL_SIGN) {
                        //get the sign object
                        Sign sign = (Sign) signBlockLocation.getState();
                        //see if existing sign is a valid BuyLand sign
                        if(sign.getLine(0) == "[BuyLand]") {
                            //Inactivate the old existing sign
                            sign.setLine(1, "Inactivated");
                            sign.setLine(1, "BuyLand Sign");
                            //Save change to sign
                            sign.update();
                        } 
                    }
                }

                //Store the passed in sign in the config file replacing one if it is there.
                getSignConfig().set("sign." + argRegionName, locationToString(newSignLocation));
                
                //Save the sign config
                saveSignConfig();
                reloadSignConfig();
                
                //Report that sign infomation was stored in config
                return true;
            } else {
                //not a valid buyland sign, do not try to add it
                return false;
            }
        }        
        // We never created the sign in the config.  Let caller know.
        return false;
    }
    
    /**
     * This will reset the expired rented region to allow for re-renting the region.
     * 
     * @param world World where the region exists
     * @param argRegionName String of the region to be reset
     */
    protected boolean resetExpiredRentedRegion(World world, String argRegionName) {
        //get the properly cased region name for use on the sign, etc
        argRegionName = argRegionName.toLowerCase();

        RegionManager regionManager = getWorldGuard().getRegionManager(world);
        //Get the protected region
        ProtectedRegion protectedRegion = regionManager.getRegionExact(argRegionName);

        //Make sure the region exists
        if (protectedRegion == null) {
            //possibly remove the region information from the rental config section
        } else {
            //get the end of the rent time for the region
            long end = getRentConfig().getLong("rent." + argRegionName + ".time");
            //get the current time
            long start = System.currentTimeMillis();

            //see if the region needs to be reset because the rent time has expired
            if (getRentConfig().getBoolean("rent." + argRegionName + ".rentable") == false && start > end) {
                //get 
                if (getRentConfig().contains("rent." + argRegionName + ".time")) {
                    //Get protected region min and max locations
                    Location protectedRegionMinimum = new Location(world, 
                                                                   protectedRegion.getMinimumPoint().getBlockX(), 
                                                                   protectedRegion.getMinimumPoint().getBlockY(), 
                                                                   protectedRegion.getMinimumPoint().getBlockZ()
                                                                  );
                    Location protectedRegionMaximum = new Location(world, 
                                                                   protectedRegion.getMaximumPoint().getBlockX(), 
                                                                   protectedRegion.getMaximumPoint().getBlockY(), 
                                                                   protectedRegion.getMaximumPoint().getBlockZ()
                                                                  );
    
                    //Reset the ending time of the rental
                    getRentConfig().set("rent." + argRegionName + ".time", 0);
                    //set the rentable flag to true
                    getRentConfig().set("rent." + argRegionName + ".rentable", true);
                    
                    //reset the land to original when the land is rented
                    worldEditPlaceSchematic(protectedRegionMinimum, argRegionName);
                    
                    //LWC - Remove protection from area based on config
                    if (getConfig().getBoolean("buyland.removelwcprotection") == true) {
                        LWCProtectionRemove(protectedRegionMinimum, protectedRegionMaximum);
                    }
        
                    //Deny entry based on config
                    if (getConfig().getBoolean("buyland.denyentrytoland") == true) {
                        protectedRegion.setFlag(DefaultFlag.ENTRY, State.DENY);
                    } else {
                        protectedRegion.setFlag(DefaultFlag.ENTRY, null);
                    }
        
                    //Set the greeting message based on config
                    if (getConfig().getBoolean("buyland.landgreeting") == true) {
                        //set the for rent message
                        protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.rent.forrent")));
                    }else if (getConfig().getBoolean("buyland.landgreetingerasemsg") == true){
                        protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, null);
                    }
        
                    //get the owner of the region
                    DefaultDomain owners = protectedRegion.getOwners();

                    //Get the number of regions the player already owns
                    int currentNumberPlayerRentedRegions = getrentdbConfig().getInt(owners.toUserFriendlyString());
                    getrentdbConfig().set(owners.toUserFriendlyString(), currentNumberPlayerRentedRegions - 1);
            
                    //Save config
                    saverentdbConfig();
                    reloadrentdbConfig();                        
        
                    //Remove owners and members                                
                    DefaultDomain dd = new DefaultDomain();
                                  dd.removePlayer(owners.toString());
                    protectedRegion.setOwners(dd);
                    protectedRegion.setMembers(dd);
        
                    //Remove any region owners
                    for (String regionOwner : protectedRegion.getOwners().getPlayers()) {    
                        protectedRegion.getOwners().removePlayer(regionOwner);
                    }
                    
                    //Remove any region members
                    for (String regionMember : protectedRegion.getMembers().getPlayers()) {   
                        protectedRegion.getMembers().removePlayer(regionMember);
                    }

                    //Save config files
                    saveRentConfig();
                    reloadRentConfig();
        
                    //change sign to indicate the region is available for rent
                    if (getSignConfig().contains("sign." + argRegionName)) {
                        Block signBlockLocation = stringToLocation(getSignConfig().getString("sign." + argRegionName)).getBlock();
                        
                        if (signBlockLocation.getType() == Material.SIGN_POST || signBlockLocation.getType() == Material.WALL_SIGN) {
                            Sign sign = (Sign) signBlockLocation.getState();
                            //sign.setLine(0, "[BuyLand]");     //This is set when sign is created
                            sign.setLine(1, "For Rent");
                            //sign.setLine(2, argRegionName);   //This is set when sign is created
                            //sign.setLine(3, "1 Min");         //This is set when sign is created
                            sign.update();
                        } else {
                            // No sign exists: dont create one since we don't know where to put it.
                        }
                    }

                    //Save the region
                    try {
                        regionManager.save();
                    } catch (Exception exp) {
                        
                    }

                    //indicate the region was reset
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * This will rent the region and make it unavailable for renting.
     * 
     * @param player Player that issued the command
     * @param world World that the region belongs to
     * @param argRegionName String name of the region
     * @param timeUnitsToAdd Long number of time units to rent the region
     * @param argTimeType String type indicating the possible time units<br/>
     *        S, Sec, Second, <br/>
     *        M, Min, Minute, <br/>
     *        H, Hr,  Hour,   <br/>
     *        D,      Day     <br/>
     * @return boolean true if the region was successfully sold, false otherwise.
     */
    protected boolean rentRegion(Player player, World world, String argRegionName, long timeUnitsToAdd, String argTimeType) {
        //get the properly cased region name for use on the sign, etc
        argRegionName = argRegionName.toLowerCase();

        //Get the player Name
        String playerName = player.getName();

        RegionManager regionManager = getWorldGuard().getRegionManager(world);
        //Get the protected region
        ProtectedRegion protectedRegion = regionManager.getRegionExact(argRegionName);

        //Make sure the region exists
        if (protectedRegion == null) {
            //Region does not exist.
            sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.general.error1")));
        } else {
            //Make sure region is rentable
            if (!getRentConfig().contains("rent." + argRegionName + ".time")) {
                sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.rent.notbe")));
            } else {
                //Get domain owners
                DefaultDomain owners = protectedRegion.getOwners();

                //Get protected region min and max locations
                Location protectedRegionMinimum = new Location(world, 
                                                               protectedRegion.getMinimumPoint().getBlockX(), 
                                                               protectedRegion.getMinimumPoint().getBlockY(), 
                                                               protectedRegion.getMinimumPoint().getBlockZ()
                                                              );
                Location protectedRegionMaximum = new Location(world, 
                                                               protectedRegion.getMaximumPoint().getBlockX(), 
                                                               protectedRegion.getMaximumPoint().getBlockY(), 
                                                               protectedRegion.getMaximumPoint().getBlockZ()
                                                              );

                //get the end of the rent time for the region
                long end = getRentConfig().getLong("rent." + argRegionName + ".time");
                //get the current time
                long start = System.currentTimeMillis();

                //see if the region needs to be reset because the rent time has expired
                if (resetExpiredRentedRegion(world, argRegionName)) {
                    //possibly notify user/everyone that the land is rentable
                }
                
                //Get the amount of time units and time unit
                String argTimeQuantity = String.valueOf(timeUnitsToAdd);
                double rentMultiplier = 0;
                long   timeMultiplier = 0;
                String timeWording    = "";

                //figure out what unit we are working with
                if (argTimeType.equalsIgnoreCase("s") || argTimeType.equalsIgnoreCase("sec") || argTimeType.equalsIgnoreCase("second")) {
                    rentMultiplier = 1 / 2;   
                    timeMultiplier = 1000L;  
                    timeWording    = "Second";                                        
                }
                if (argTimeType.equalsIgnoreCase("m") || argTimeType.equalsIgnoreCase("min") || argTimeType.equalsIgnoreCase("minute")) {
                    rentMultiplier = 1;
                    timeMultiplier = 60 * 1000L;
                    timeWording    = "Minute";                                        
                }
                if (argTimeType.equalsIgnoreCase("h") || argTimeType.equalsIgnoreCase("hr") || argTimeType.equalsIgnoreCase("hour")) {
                    rentMultiplier = 60 * 1;
                    timeMultiplier = 60 * 60 * 1000L;
                    timeWording    = "Hour";                                        
                }
                if (argTimeType.equalsIgnoreCase("d") || argTimeType.equalsIgnoreCase("day")) {
                    rentMultiplier = 24 * 60 * 1;
                    timeMultiplier = 24 * 60 * 60 * 1000L;
                    timeWording    = "Day";                                        
                }

                //Get the cost of the rent for the period
                double priceToRentRegionForPeriod = getRentConfig().getDouble("rent." + argRegionName +".costpermin") * (double) timeUnitsToAdd * rentMultiplier;
                
                //Get the new amount of time to add to the region 
                long time = timeUnitsToAdd * timeMultiplier;
                
                if (start < end) {
                    //The land is still rented and has more time on it

                    //See if the player is an owner of the region
                    if (!owners.toPlayersString().contains(playerName.toLowerCase())) {
                        sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.rent.tenant")) + BuyLand.elapsedTime(start, end));       
                    } else {
                        //set the player know how much time is left
                        sendMessageInfo(player, "Time left: " + BuyLand.elapsedTime(start, end));
                        //get how many time units to add

                        //Try to rent it with the players funds
                        EconomyResponse economyResponse = BuyLand.econ.withdrawPlayer(playerName, priceToRentRegionForPeriod);
                        if (!economyResponse.transactionSuccess()) {
                            sendMessageInfo(player, String.format( ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.rent.cantafford")), economyResponse.errorMessage));
                        } else {
                            //purchase successful
                            sendMessageInfo(player, String.format("Adding " + argTimeQuantity + " " + timeWording + "(s) to " + argRegionName + ". Cost: %s Balance: %s", BuyLand.econ.format(economyResponse.amount), BuyLand.econ.format(economyResponse.balance)), false);

                            //Get the time left on the region
                            long timepull = getRentConfig().getLong("rent." + argRegionName +".time");
                            
                            //Add it to the region
                            getRentConfig().set("rent." + argRegionName +".time", timepull + time);

                            //Save the config files
                            saveRentConfig();
                            reloadRentConfig();
                            
                            //No need to update sign if it exists
                            
                            //return that the region was rented
                            return true;
                        }
                    }
                } else {
                    //The land is for rent

                    //See if the player can rent more regions
                    if (!canPlayerRentAnotherRegion(player)) {
                        //player has reached rent max rents
                        sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.rent.max")));
                    } else {
                        EconomyResponse economyResponse = BuyLand.econ.withdrawPlayer(playerName, priceToRentRegionForPeriod);
                        if (!economyResponse.transactionSuccess()) {
                            sendMessageInfo(player, String.format(ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.rent.cantafford")), economyResponse.errorMessage));
                        } else {
                            //Notify user that rent succeeded
                            sendMessageInfo(player, String.format("Renting " + argRegionName + " for " + argTimeQuantity + " " + timeWording + "(s). Cost: %s Balance: %s", BuyLand.econ.format(economyResponse.amount), BuyLand.econ.format(economyResponse.balance)));

                            //Update flags on region
                            getRentConfig().set("rent." + argRegionName +".time", System.currentTimeMillis() + time);
                            getRentConfig().set("rent." + argRegionName +".world", world.getName());
                            getRentConfig().set("rent." + argRegionName +".rentable", false);
                            
                            //Update the number of regions rented by the player
                            getrentdbConfig().set(playerName, getrentdbConfig().getInt(playerName) + 1);
                            saverentdbConfig();
                            
                            //Update owner of rented domain 
                            DefaultDomain dd = new DefaultDomain();
                                          dd.addPlayer(playerName);
                            protectedRegion.setOwners(dd);

                            //Deny entry based on config
                            if (getConfig().getBoolean("buyland.denyentrytoland") == true) {
                                protectedRegion.setFlag(DefaultFlag.ENTRY, State.DENY);
                            } else {
                                protectedRegion.setFlag(DefaultFlag.ENTRY, null);
                            }

                            //set greeting message for region based on config
                            if (getConfig().getBoolean("buyland.landgreeting") == true) {
                                protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.rent.rentby")) + playerName);
                            } else if (getConfig().getBoolean("buyland.landgreetingerasemsg") == true) {
                                protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, null);
                            }

                            //Save a schematic of the land region for restore
                            worldEditSaveSchematic(protectedRegionMinimum, protectedRegionMaximum, argRegionName, player);
                            
                            //Update Sign to Extend Rent
                            

                            //Flag the region as rented
                            protectedRegion.setFlag(DefaultFlag.BUYABLE, false);

                            //change sign to indicate the region is rented
                            if (getSignConfig().contains("sign." + argRegionName)) {
                                Block signBlockLocation = stringToLocation(getSignConfig().getString("sign." + argRegionName)).getBlock();
                                
                                if (signBlockLocation.getType() == Material.SIGN_POST || signBlockLocation.getType() == Material.WALL_SIGN) {
                                    Sign sign = (Sign) signBlockLocation.getState();
                                    //sign.setLine(0, "[BuyLand]");     //This is set when sign is created
                                    sign.setLine(1, "Extend Rent");
                                    //sign.setLine(2, argRegionName);   //This is set when sign is created
                                    //sign.setLine(3, "1 Min");         //This is set when sign is created
                                    sign.update();
                                } else {
                                    // No sign exists: dont create one since we don't know where to put it.
                                }
                            }

                            //Save the region
                            try {
                                regionManager.save();
                            } catch (Exception exp) {
                            }

                            //Save the config files
                            saveRentConfig();
                            reloadRentConfig();
                            
                            //return that the region was rented
                            return true;
                        }
                    }
                }
            }
        }
        //return that the region was not rented
        return false;
    }
    
    /**
     * This will sell the region and place it back available for purchase.
     * This will notify the owner that the region was sold and what their new balance is.
     * This will also send a broadcast that the region is for sale.
     * 
     * @param player CommandSender or Player that issued the command
     * @param world World that the region belongs to
     * @param argRegionName String name of the region
     * @return boolean true if the region was successfully sold, false otherwise.
     */
    protected boolean sellRegion(Player player, boolean fromAdmin, World world, String argRegionName) {
        //get the properly cased region name for use on the sign, etc
        argRegionName = argRegionName.toLowerCase();

        //Get the manager for the world
        RegionManager regionManager = getWorldGuard().getRegionManager(world);
        //Get the protected region
        ProtectedRegion protectedRegion = regionManager.getRegionExact(argRegionName);

        //make sure the region exists
        if (protectedRegion == null) {
            //Region does not exist.
            sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.general.error1")));
        } else {
            //make sure player owns region, or is initiated by an admin
            if (!(fromAdmin || protectedRegion.getOwners().toPlayersString().contains(player.getName()))) {
                sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.sell.dontown")));
            } else {
                //see if the land is rentable 
                if (getRentConfig().contains("rent." + argRegionName + ".rentable")) {
                    // can't sell a region while it is rentable
                    sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.rent.error1")));
                } else {
                    if (protectedRegion.getFlag(DefaultFlag.BUYABLE) == true) {
                        //is already for sale, do nothing
                        sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.sell.forsale")));
                    } else {
                        //Get protected region min and max locations of the region
                        Location protectedRegionMinimum = new Location(world, 
                                                                       protectedRegion.getMinimumPoint().getBlockX(), 
                                                                       protectedRegion.getMinimumPoint().getBlockY(), 
                                                                       protectedRegion.getMinimumPoint().getBlockZ()
                                                                      );
                        Location protectedRegionMaximum = new Location(world, 
                                                                       protectedRegion.getMaximumPoint().getBlockX(), 
                                                                       protectedRegion.getMaximumPoint().getBlockY(), 
                                                                       protectedRegion.getMaximumPoint().getBlockZ()
                                                                      );
        
                        //Get the owner name
                        String ownerName = protectedRegion.getOwners().toUserFriendlyString();
                        
                        //Get the price of the region
                        Double regionPrice = getRegionPurchasePrice(protectedRegion);
        
                        //Get the sell back price of the region
                        Double finalRegionPrice = regionPrice * getConfig().getDouble("buyland.percentsellback");
                        
                        //Sell it back
                        EconomyResponse economyResponse = BuyLand.econ.depositPlayer(ownerName, finalRegionPrice);
        
                        //get the player to notify of the updated balance
                        Player notifyPerson = Bukkit.getPlayer(ownerName);
                        
                        if (notifyPerson != null) {
                            //Notify player of sale
                            sendMessageInfo(notifyPerson,
                                            String.format(ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.sell.back1"))
                                                          + finalRegionPrice +
                                                          ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.sell.back2"))
                                                          ,
                                                          BuyLand.econ.format(economyResponse.balance)
                                                         )
                                           );
                        }
                        
                        //Remove player as owner of region
                        for (String owner : protectedRegion.getOwners().getPlayers()) {
                            //remove the player as owner
                            protectedRegion.getOwners().removePlayer(owner);
                            //Get the number of regions the player currently owns
                            int currentNumberPlayerOwnedRegions = getCustomConfig().getInt(owner);
                            //Record the new number of regions the player owns
                            getCustomConfig().set(owner, currentNumberPlayerOwnedRegions - 1);
                        }
        
                        //Make sure there are no members of the region
                        for (String memberName : protectedRegion.getMembers().getPlayers()) { 
                            protectedRegion.getMembers().removePlayer(memberName);
                        }
        
                        //Set the land greeting message based on config
                        if (getConfig().getBoolean("buyland.landgreeting") == true) {
                            protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.sell.forsale")));
                        } else {
                            if (getConfig().getBoolean("buyland.landgreetingerasemsg") == true) {
                                protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, null);
                            }
                        }
        
                        //Protect land from entry based on config
                        if (getConfig().getBoolean("buyland.denyentrytoland") == true) {
                            protectedRegion.setFlag(DefaultFlag.ENTRY, State.DENY);
                        } else {
                            protectedRegion.setFlag(DefaultFlag.ENTRY, null);
                        }
        
                        //set the land priority
                        protectedRegion.setPriority(getConfig().getInt("buyland.landpriority"));
        
                        //Save the config files
                        saveCustomConfig();
                        reloadCustomConfig();
                        
                        //LWC - Remove protection from area based on config
                        if (getConfig().getBoolean("buyland.removelwcprotection") == true) {
                            LWCProtectionRemove(protectedRegionMinimum, protectedRegionMaximum);
                        }

                        //Change Sign to indicate it is for sale     
                        if (getSignConfig().contains("sign." + argRegionName)) {
                            Block signBlockLocation = stringToLocation(getSignConfig().getString("sign." + argRegionName)).getBlock();
                 
                            if (signBlockLocation.getType() == Material.SIGN_POST || signBlockLocation.getType() == Material.WALL_SIGN) {
                                Sign sign = (Sign) signBlockLocation.getState();
                                //sign.setLine(0, "[BuyLand]");     //This is set when sign is created
                                sign.setLine(1, "For Sale");
                                //sign.setLine(2, argRegionName);   //This is set when sign is created
                                sign.setLine(3, regionPrice.toString());
                                sign.update();
                            } else {
                                // No sign exists: dont create one since we don't know where to put it.
                            }
                        }
                        
                        //Reset the land to original when the land is sold based on config
                        if (getConfig().getBoolean("buyland.resetlandonsale") == true) {
                            worldEditPlaceSchematic(protectedRegionMinimum, argRegionName);
                        }
        
                        //Make the region buyable
                        protectedRegion.setFlag(DefaultFlag.BUYABLE, true);
        
                        //Notify the admin
                        broadcastMessageInfo(argRegionName + ": " + ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.admin.forsale")));
        
                        //Save the region
                        try {
                            regionManager.save();
                        } catch (Exception exp) { 
                            
                        }
                        //indicate that the land was successfully put up for sale
                        return true;
                    }
                }
            }
        }
    
        return false;
    }
    /**
     * This will buy the region and make it unavailable to purchase.
     * 
     * @param player CommandSender or Player that issued the command
     * @param world World that the region belongs to
     * @param argRegionName String name of the region
     * @return boolean true if the region was successfully bought, false otherwise.
     */
    protected boolean buyRegion(Player player, World world, String argRegionName) {
        String playerName = player.getName();
        //get the properly cased region name for use on the sign, etc
        argRegionName = argRegionName.toLowerCase();
        //Get the manager for the world
        RegionManager regionManager = getWorldGuard().getRegionManager(world);
        //Get the protected region
        ProtectedRegion protectedRegion = regionManager.getRegionExact(argRegionName);

        //Make sure the region exists
        if (protectedRegion == null) {
            //Region does not exist.
            sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.general.error1")));
        } else {
            //Make sure it is not rentable
            if (getRentConfig().contains("rent." + argRegionName + ".rentable")) {
                // can't buy a region while it is rentable
                sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.rent.error1")));
            } else {
                //Get the buyable flag for the region
                Boolean isRegionBuyable = protectedRegion.getFlag(DefaultFlag.BUYABLE);
                    if (isRegionBuyable == null) {
                        isRegionBuyable = false; 
                    }
    
                //See if the piece of land is for sale.
                if (!isRegionBuyable) {
                    sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.buy.dontown")));                                           
                } else {
                    //See if the player has rights to own another piece of land
                    if (!canPlayerOwnAnotherRegion(player)) {
                        sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.buy.max")));
                    } else {
                        //Get the number of regions the player already owns
                        int currentNumberPlayerOwnedRegions = getCustomConfig().getInt(playerName);
    
                        //Get the cost of the region - do not trust price on the sign as it can possibly be changed
                        double regionPrice = getRegionPurchasePrice(protectedRegion);
    
                        //Try to buy it with the players funds
                        EconomyResponse economyResponse = BuyLand.econ.withdrawPlayer(playerName, regionPrice);
    
                        if (!economyResponse.transactionSuccess()) {
                            //Purchase is unsuccessful
                            sendMessageInfo(player, String.format(ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.buy.cantafford")), economyResponse.errorMessage));
                        } else {
                            //Purchase is successful
                            
                            //Flag the region as purchased
                            protectedRegion.setFlag(DefaultFlag.BUYABLE, false);
    
                            //Get protected region min and max locations
                            Location protectedRegionMinimum = new Location(world, 
                                                                           protectedRegion.getMinimumPoint().getBlockX(), 
                                                                           protectedRegion.getMinimumPoint().getBlockY(), 
                                                                           protectedRegion.getMinimumPoint().getBlockZ()
                                                                          );
                            Location protectedRegionMaximum = new Location(world, 
                                                                           protectedRegion.getMaximumPoint().getBlockX(), 
                                                                           protectedRegion.getMaximumPoint().getBlockY(), 
                                                                           protectedRegion.getMaximumPoint().getBlockZ()
                                                                          );
    
                            //Notify the player
                            sendMessageInfo(player, String.format(ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.buy.bought")), 
                                                                  BuyLand.econ.format(economyResponse.amount), 
                                                                  BuyLand.econ.format(economyResponse.balance)));
                            
                            //Deny entry based on config
                            if (getConfig().getBoolean("buyland.denyentrytoland") == true) {
                                protectedRegion.setFlag(DefaultFlag.ENTRY, State.DENY);
                            } else {
                                protectedRegion.setFlag(DefaultFlag.ENTRY, null);
                            }
    
                            //set greeting message for region based on config
                            if (getConfig().getBoolean("buyland.landgreeting") == true) {
                                protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, 
                                                        ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.buy.welcome1"))
                                                        + playerName + 
                                                        ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.buy.welcome2"))
                                                       );
                            } else {
                                if (getConfig().getBoolean("buyland.landgreetingerasemsg") == true) {
                                    protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, null);
                                }
                            }
    
                            //Record the new number of regions the player owns
                            getCustomConfig().set(playerName, currentNumberPlayerOwnedRegions + 1);
                            saveCustomConfig();
    
                            //Set the owner of the land
                            DefaultDomain dd = new DefaultDomain();
                                          dd.addPlayer(playerName);
                            protectedRegion.setOwners(dd);
    
                            //LWC - Remove protection from area
                            if (getConfig().getBoolean("buyland.removelwcprotection") == true) {
                                LWCProtectionRemove(protectedRegionMinimum, protectedRegionMaximum);
                            }
    
                            //Save a schematic of the land region for restore based on config
                            if (getConfig().getBoolean("buyland.resetlandonsale") == true) {
                                worldEditSaveSchematic(protectedRegionMinimum, protectedRegionMaximum, argRegionName, player);
                            }
    
                            //change sign to indicate the region is sold
                            if (getSignConfig().contains("sign." + argRegionName)) {
                                Block signBlockLocation = stringToLocation(getSignConfig().getString("sign." + argRegionName)).getBlock();
                                
                                if (signBlockLocation.getType() == Material.SIGN_POST || signBlockLocation.getType() == Material.WALL_SIGN) {
                                    Sign sign = (Sign) signBlockLocation.getState();
                                    //sign.setLine(0, "[BuyLand]");   //This is set when sign is created
                                    sign.setLine(1, "Sell Back");
                                    //sign.setLine(2, argRegionName); //This is set when sign is created
                                    sign.setLine(3, playerName);
                                    sign.update();
                                } else {
                                    // No sign exists: dont create one since we don't know where to put it.
                                }
                            }
    
                            //Save the region changes
                            try {
                                regionManager.save();
                            } catch (Exception exp) {
                            }
                            
                            //return that the land was purchased
                            return true;
                        }
                    }
                }
            }
        }
    
        //Return that the land was NOT purchased
        return false;
    }    
    
    /**
     * Do the initial hook into the permissions provider.
     * 
     * @return boolean True if provider exists, false otherwise. 
     */
    private boolean setupVaultPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
    /**
     * Hook into vault chat service provider
     * 
     * @return boolean true if setup correctly, false otherwise
     */
    private boolean setupVaultChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }
        return (chat != null);
    }
    /**
     * Hook into vault economy plugin
     * 
     * @return boolean true if setup correctly, false otherwise
     */
    private boolean setupVaultEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }
        return (econ != null);
    }

    /**
     * Hook into LWC plugin
     * 
     * @return LWC object
     */
    public LWC getLWC() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("LWC");
        if ((plugin == null) || (!(plugin instanceof LWC))) {
            return null;  //throws a NullPointerException, telling the Admin that LWC is not loaded
        }
        return (LWC)plugin;
    }
    
    /**
     * Hook into WorldGuard plugin
     * 
     * @return WorldGuardPlugin plugin object
     */
    public WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if ((plugin == null) || (!(plugin instanceof WorldGuardPlugin))) {
            return null; //throws a NullPointerException, telling the Admin that WG is not loaded.
        }
        return (WorldGuardPlugin)plugin;
    } 

    /**
     * Get WorldEdit Selection of the player
     * 
     * @param player Player that has the selection
     * @return Selection that the player had
     */
    public static Selection getWorldEditSelectionOfPlayer(Player player) {
        Plugin we = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if(we != null && we instanceof WorldEditPlugin) {
            return ((WorldEditPlugin) we).getSelection(player);
        }
        return null;
    }
    
    /**
     * Adds a protected region to the manager
     * 
     * @param regionMinimum Location of the minimum block of the region
     * @param regionMaximum Location of the maximum block of the region
     * @param regionName String name of the region
     * @param action String indicating if this is for a "buy" or "rent" action
     */
    public void AddProtectedRegion(Location regionMinimum, Location regionMaximum, String regionName, String action) {
        if (action == "buy" || action == "rent") {
            //Get the requested world
            World world = regionMinimum.getWorld();

            // get the region manager 
            RegionManager regionManager = this.getWorldGuard().getRegionManager(world);

            //Convert Locations to the vectors
            BlockVector blockVectorMinimum = new BlockVector(regionMinimum.getBlockX(), regionMinimum.getBlockY(), regionMinimum.getBlockZ());
            BlockVector blockVectorMaximum = new BlockVector(regionMaximum.getBlockX(), regionMaximum.getBlockY(), regionMaximum.getBlockZ());

            //Create the protected region with two points to create a 3d cube in the world
            ProtectedCuboidRegion protectedRegion = new ProtectedCuboidRegion(regionName, blockVectorMinimum, blockVectorMaximum);

            //Add region to the manager
            regionManager.addRegion(protectedRegion);

            if (action == "buy") {
                //Make the region buyable if action is buy
                protectedRegion.setFlag(DefaultFlag.BUYABLE, true);
            }

            //Set the land greeting message based on config
            if (this.getConfig().getBoolean("buyland.landgreeting") == true) {
                if (action == "buy") {
                    protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.sell.forsale")));
                } else { // "rent"
                    protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, ChatColor.translateAlternateColorCodes('&', getLanguageConfig().getString("buyland.rent.forrent")));                
                }
            } else if (this.getConfig().getBoolean("buyland.landgreetingerasemsg") == true) {
                protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, null);
            }

            //Deny entry based on config
            if (this.getConfig().getBoolean("buyland.denyentrytoland") == true) {
                protectedRegion.setFlag(DefaultFlag.ENTRY, State.DENY);
            } else {
                protectedRegion.setFlag(DefaultFlag.ENTRY, null);
            }

            //set the land priority
            protectedRegion.setPriority(this.getConfig().getInt("buyland.landpriority"));

            if (action == "rent") {
                //do things specific to renting
                
                //Get the default cost to rent per minute
                double defaultcostpermin = this.getConfig().getDouble("buyland.defaultrentcostpermin");
        
                //set the config values for the region name
                this.getRentConfig().set("rent." + regionName + ".time", 0);
                this.getRentConfig().set("rent." + regionName + ".rentable", true);
                this.getRentConfig().set("rent." + regionName + ".world", world.getName());
                this.getRentConfig().set("rent." + regionName + ".costpermin", defaultcostpermin);
        
                //this.logger.info("BuyLand_Debug - Save Rent Config Start 14");
                saveRentConfig();
                //this.logger.info("BuyLand_Debug - Save Rent Config End 14");
                //this.logger.info("BuyLand_Debug - Reload Rent Config Start 14");
        
                reloadRentConfig();
                //this.logger.info("BuyLand_Debug - Reload Rent Config End 14");
            }

            //DefaultDomain dd = new DefaultDomain();
            // add the player to the region      
            //dd.addPlayer(playerName);
            // set the player as the owner
            //protectedRegion.setOwners(dd);

            logger.info("BuyLand: Added region: " + regionName);
            
            try {
                regionManager.save();
            } catch (Exception exp) {
                
            }
        }
    }
    
    /**
     * Remove the LWC protection from a location
     * 
     * @param minimumLocation Location of the minimum points of the region
     * @param maximumLocation Location of the maximum points of the region
     */
    public void LWCProtectionRemove (Location minimumLocation, Location maximumLocation) {
        //get link to lwc
        LWC lwc = getLWC();
    
        //Get points
        World world = minimumLocation.getWorld();
        
        int minX = minimumLocation.getBlockX();
        int minY = minimumLocation.getBlockY();
        int minZ = minimumLocation.getBlockZ();
    
        int maxX = maximumLocation.getBlockX();
        int maxY = maximumLocation.getBlockY();
        int maxZ = maximumLocation.getBlockZ();
    
        for (int x11=minX; x11<maxX; x11++) {
            for (int y11=minY; y11<maxY; y11++) {
                for (int z11=minZ; z11<maxZ; z11++) {
                    Protection protection = lwc.findProtection(world, x11, y11, z11);
                    if (protection != null) {
                        protection.remove();
                        //this.getServer().getLogger().info("Removed LWC Protection from Plot: " + args[0]);
                    }
                }
            }
        }
    }

    /**
     * Return the price of a given region
     * 
     * @param protectedRegion ProtectedRegion of which to get the price
     * @return Double price of the region
     */
    public Double getRegionPurchasePrice (ProtectedRegion protectedRegion) {
        Double regionPrice = protectedRegion.getFlag(DefaultFlag.PRICE);
        if (regionPrice == null) {
            if (this.getConfig().getBoolean("buyland.usepriceperblock") == true) {
                //get size of region
                int size = protectedRegion.volume();
                //  player.sendMessage("Area of blocks: " + size);
                double regionPricePerBlock = this.getConfig().getDouble("buyland.defaultpriceperblock");
                regionPrice = (double) (size * regionPricePerBlock);
                //player.sendMessage("regionPrice: " + regionPrice + " - regionPricePerBlock: " + regionPricePerBlock);
            } else {
                regionPrice = this.getConfig().getDouble("buyland.defaultprice");
            }
        }
        return regionPrice;
    }
    
    /**
     * Save the WorldEdit Schematic 
     * 
     * @param minimumLocation Location of the minimum points of the region
     * @param maximumLocation Location of the maximum points of the region
     * @param schematicRegionName String name of the region to be saved as a schematic
     * @param player Player object
     * @throws EmptyClipboardException
     * @throws IOException
     * @throws DataException
     */
    @SuppressWarnings("deprecation")
    public void worldEditSaveSchematic(Location minimumLocation, Location maximumLocation, String schematicRegionName, Player player) {
        try {
            //Hook into world edit
            WorldEditPlugin wep = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
            WorldEdit we = wep.getWorldEdit();
            BukkitPlayer localPlayer = wep.wrapPlayer(player);
            LocalSession localSession = we.getSession(localPlayer);
            EditSession editSession = localSession.createEditSession(localPlayer);
        
            //create vectors for worldedit
            Vector vectorMinimum = new Vector(minimumLocation.getBlockX(), minimumLocation.getBlockY(), minimumLocation.getBlockZ());
            Vector vectorMaximum = new Vector(maximumLocation.getBlockX(), maximumLocation.getBlockY(), maximumLocation.getBlockZ());
         
            //Get file handle
            File fileHandle = new File(getDataFolder() + File.separator + "data" + File.separator);
            
            File safeSaveFileHandle;
            try {
                safeSaveFileHandle = we.getSafeSaveFile(localPlayer, fileHandle, schematicRegionName, "schematic", new String[]{"schematic"});
            } catch (FilenameException ex) {
                //te.logUtil.warning(ex.getMessage());
                return;
            }
         
            editSession.enableQueue();
            CuboidClipboard clipboard = new CuboidClipboard(vectorMaximum.subtract(vectorMinimum).add(new Vector(1, 1, 1)), vectorMinimum);
            clipboard.copy(editSession);
            clipboard.saveSchematic(safeSaveFileHandle);
            editSession.flushQueue();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        }
    }

    /**
     * Place a saved schematic into the world.
     * 
     * @param placementLocation Location where the schematic should be placed. This is the minimumPoint of the region
     * @param schematicRegionName String name of the region.  This will match into a schematic 
     */
    public void worldEditPlaceSchematic(Location placementLocation, String schematicRegionName) {
        //Get the world from the location
        World world = placementLocation.getWorld();
        //Convert the location into a vector indicating where to place the schematic
        Vector vector = new Vector(placementLocation.getBlockX(), placementLocation.getBlockY(), placementLocation.getBlockZ());
        //Get the file handle for the schematic of the region
        File file = new File(getDataFolder() + File.separator + "data" + File.separator + schematicRegionName + ".schematic");
        if (file.exists()) {
            try {
                //apply the schematic to the world
                BukkitWorld BWf = new BukkitWorld(world);
                EditSession editSession = new EditSession(BWf, 2000000);
                CuboidClipboard c1 = SchematicFormat.MCEDIT.load(file);
                c1.place(editSession, vector, false);                
            } catch (DataException ex) {             //logger.warning("'worldEditResetMap()' DataException");
            } catch (IOException ex) {               //logger.warning("'worldEditResetMap()' IOException");
            } catch (MaxChangedBlocksException ex) { //logger.warning("'worldEditResetMap()' MaxChangedBlocksException");
            }
        } else {
            //Logger.warning(("File does not exist."));
        }
    }

    /**
     * A boolean value indicating if the player can rent another region.
     * 
     * @param player Player trying to rent a region
     * @return boolean true if they can rent another region, false otherwise
     */
    public boolean canPlayerRentAnotherRegion(Player player) {
        //Get the number of regions the player is already renting
        int currentNumberPlayerRentedRegions = getrentdbConfig().getInt(player.getName());
        //Get the maximum number of rentable regions
        int maxNumberOfPlayerRentedRegions = getConfig().getInt("buyland.maxamountofrentland");
        //See if the player can rent more land
        if (currentNumberPlayerRentedRegions + 1 <= maxNumberOfPlayerRentedRegions) {
            return true;
        }
        return false;
    }
    
    /**
     * Return the maximum number of regions a player can own
     * 
     * @param player Player to check
     * @return The maximum number of regions a player can own
     */
     public int getMaxNumberOfRegionsPlayerCanOwn(Player player) {
        int currentNumberPlayerOwnedRegions = getCustomConfig().getInt(player.getName());
        
        for (int loopVal = 50; loopVal >= currentNumberPlayerOwnedRegions; loopVal--) {
            if (player.hasPermission("buyland.maxland."+Integer.toString(loopVal))) {
                return loopVal;
            }
        }
        return currentNumberPlayerOwnedRegions;
    }
    
    /**
     * A boolean value indicating if the player can buy another region.
     * @param player Player trying to buy a region
     * @return boolean true if they have rights to own another region, false otherwise.
     */
    public boolean canPlayerOwnAnotherRegion(Player player) {
        int currentNumberPlayerOwnedRegions = getCustomConfig().getInt(player.getName());

        //   Loop through all the permission nodes from what the player currently owns to the max.
        //   Grab the first one that is higher.
        //   This code replaces the code that is not based on permissions:   int maximumPlayerOwnedRegions = this.getConfig().getInt("buyland.maxamountofland");
        for (int loopVal = currentNumberPlayerOwnedRegions+1; loopVal <= 50; loopVal++) {
            if (player.hasPermission("buyland.maxland."+Integer.toString(loopVal))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Return the wording of how long the difference is between start and end
     * 
     * @param start long start time of which we are getting the difference
     * @param end long end time of which we are getting the difference
     * @return String containing the length of the difference
     */
    public static String elapsedTime(long start, long end) {
        String auxRet= "";
        long timeDifference = end - start;
        long millisecondLength = 1;
        long secondLength = millisecondLength * 1000;
        long minuteLength = secondLength * 60;
        long hourLength = minuteLength * 60;
        long dayLength = hourLength * 24;
        
        //days
        if (timeDifference > dayLength) {
            //add number of days to output
            auxRet += (timeDifference / dayLength) + " days ";
            //reduce remaining time difference
            timeDifference = timeDifference % dayLength;
        }
    
        //hours
        if (timeDifference > hourLength) {
            //add number of hours to output
            auxRet += (timeDifference / hourLength) + " hours ";
            //reduce remaining time difference
            timeDifference = timeDifference % hourLength;
        }
    
        //minutes
        if (timeDifference > minuteLength) {
            //add number of minutes to output
            auxRet += (timeDifference / minuteLength) + " minutes ";
            //reduce remaining time difference
            timeDifference = timeDifference % minuteLength;
        }
    
        //seconds
        if (timeDifference > secondLength){
            //add number of seconds to output
            auxRet += (timeDifference / secondLength) + " seconds ";
            //reduce remaining time difference
            timeDifference = timeDifference % secondLength;
        }
    
        //milliseconds
        if (timeDifference > millisecondLength) {
            //add number of seconds to output
            auxRet += (timeDifference / millisecondLength) + " milliseconds ";
        }
    
        //return String of time difference
        return auxRet;
    }

    /**
     * Convert a String to a Location
     * 
     * @param stringLocation String in the format of WorldName:X:Y:Z
     * @return Location representing the string
     */
    public Location stringToLocation(String stringLocation) {
        String[] loc = stringLocation.split(":");

        World world = Bukkit.getWorld(loc[0]);
        Double x = Double.parseDouble(loc[1]);
        Double y = Double.parseDouble(loc[2]);
        Double z = Double.parseDouble(loc[3]);
    
        return new Location(world, x, y, z);
    }
    /**
     * Convert a Location to a String
     * 
     * @param location Location that is to be converted
     * @return String in the format of WorldName:X:Y:Z
     */
    public String locationToString (Location location) {
        return location.getWorld().getName() + ":" + String.valueOf(location.getX()) + ":" + String.valueOf(location.getY()) + ":" + String.valueOf(location.getZ());
    }

    /**
     * remove one item from an array of strings 
     * @param args String[] list containing the arguments to adjust
     * @param itemToRemove int index of the item to remove
     * @return String[] that is 1 smaller in length than the original args and contains all but the requested index.
     */
    protected String[] removeItemFromArgs(String[] args, int itemToRemove) {
        //if index is out of bounds, just return the args
        if (itemToRemove > args.length-1 || itemToRemove < 0) {
            return args;
        }
        //Remove the last item
        String[] argsOutput = java.util.Arrays.copyOfRange(args, 0, args.length-1);
        //loop through and insert the items after the itemToRemove index
        for (int index = itemToRemove; index < args.length-1; index++) {
            argsOutput[index] = args[index+1];
        }
        //return the cropped array
        return argsOutput;
    }

    /**
     * Sends a message of type info to all players.
     * 
     * @param msg String message to send to the players.
     */
    protected void broadcastMessageInfo(String msg) {
        //Prefix the message with Buyland: since it is going to everyone 
        msg = ChatColor.RED + "BuyLand: " + ChatColor.YELLOW + msg;

        //Send the message
        getServer().broadcastMessage(msg);
    }
    /**
     * Sends a message of type info either to the console or the player, depending on the sender variable
     * 
     * @param sender CommandSender is either the player, or null for console.
     * @param msg String message to send to the player/console.
     */
    protected void sendMessageInfo(CommandSender sender, String msg) {
        sendMessageInfo(sender, msg, true);
    }
    protected void sendMessageInfo(CommandSender sender, String msg, boolean includePrefix) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            //Prefix the message with Buyland: since it is going to the user 
            if (includePrefix) {
                msg = ChatColor.RED + "BuyLand: " + ChatColor.WHITE + msg;
            }

            //Send the message
            player.sendMessage(msg);
        } else {
            this.getLogger().info(msg);
        }
    }
    /**
     * Sends a message of type warning either to the console or the player, depending on the sender variable
     * 
     * @param sender CommandSender is either the player, or null for console.
     * @param msg String message to send to the player/console.
     */
    protected void sendMessageWarning(CommandSender sender, String msg) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            //Prefix the message with Buyland: since it is going to the user 
            msg = ChatColor.RED + "BuyLand: " + ChatColor.DARK_RED + msg;

            //Send the message
            player.sendMessage(msg);
        } else {
            this.getLogger().warning(msg);
        }
    }

    //  player.sendMessage(ChatColor.RED + "BuyLand: V" +  plugin.getDescription().getVersion() + ChatColor.GOLD + " is a product of chriztopia.com");
}