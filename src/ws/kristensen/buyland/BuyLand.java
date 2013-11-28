package ws.kristensen.buyland;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
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
import com.sk89q.worldedit.LocalWorld;
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
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/*
Commands

    -- /buyland [regionname] - Buys the Region
    -- /sellland [regionname] - Sells the Region
    -- /priceland [regionname] - Prices the Region Works for both rentland and buyland
    -- /rentland [regionname] [Time] [Sec/Min/Hr/Day/Wk] - Rents a region
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

    private final HashMap<Location, String> signsMap = new HashMap<Location, String>();

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
    private File              rentDbConfigFile   = null;
	private FileConfiguration rentDbConfig       = null;
	
    public FileConfiguration signGetConfig() {
        if (signConfig == null) {
            if (signConfigFile == null) {
                signConfigFile = new File(getDataFolder(), "signs.yml");
            }
            this.signUpdateSettingsVersion();
            this.signReloadConfig();
            //make sure this option is set
            signConfig.options().copyDefaults(true);
        }
        return signConfig;
    }
    private void signUpdateSettingsVersion() {
        sendMessageInfo(null, signConfigFile.getName());
        signConfig = YamlConfiguration.loadConfiguration(signConfigFile);
        //see if it is the original style of the config
        if (signConfig.getString("sign.placeholder.location") == null) {
            sendMessageInfo(null, "sign - need to fix");

            signConfig.set("general.configVersion", "2");

            //Get the sign config section from the file
            ConfigurationSection signConfigSection = signConfig.getConfigurationSection("sign");
            //get each sign and fix it.
            for (String key : signConfigSection.getKeys(false)) {
                //save the location string
                String location = signConfigSection.getString(key);
                //remove the old style key
                signConfigSection.set(key, null);
                //add the new structure
                signConfigSection.set("sign." + key + ".location", location);
                signConfigSection.set("sign." + key + ".state", "default");
            }
            signSaveConfig();
        } else {
            sendMessageInfo(null, "sign - no need to fix");
        }

    }
    public void signReloadConfig() {
        signConfig = YamlConfiguration.loadConfiguration(signConfigFile);
     
        // Look for defaults in the jar
        InputStream defConfigStream = this.getResource("signs.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            signConfig.setDefaults(defConfig);
        }
        
        //Make sure the minimum settings in the file are there with these defaults
        signConfig.options().header("BuyLand Sign DB File. Used to keep track of signs.");

        signConfig.addDefault("sign.placeholder.location", "here");
        signConfig.addDefault("sign.placeholder.state", "FOR_SALE");
        signConfig.addDefault("sign.placeholder.inactiveexempt", false);
        
        //Add each of the signs to the Hash for easy lookup.  format: location = regionName:state
        signsMap.clear();
        for (String regionName : signConfig.getConfigurationSection("sign").getKeys(false)) {
            if (!regionName.equalsIgnoreCase("placeholder")) {
                signsMap.put(locationFromString(signConfig.getString("sign." + regionName + ".location")), regionName + ":" + signConfig.getString("sign." + regionName + ".state"));
            }
        }
    }
    public void signSaveConfig() {
        if (signConfig == null || signConfigFile == null) {
            return;
        }
        try {
            signGetConfig().save(signConfigFile);
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

        customConfig.addDefault("user.own", 0);
        customConfig.addDefault("user.earned", 0.00);
        customConfig.addDefault("user.spent", 0.00);
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

    public FileConfiguration languageGetConfig() {
        if (languageConfig == null) {
            this.languageReloadConfig();
            //make sure this option is set
            languageConfig.options().copyDefaults(true);
        }
        return languageConfig;
    }
    public void languageReloadConfig() {
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
        languageConfig.addDefault("buyland.rent.cantafford", "%s to rent the land.");
        languageConfig.addDefault("buyland.rent.error1", "Sorry Rentable land can not be bought nor sold.");
        languageConfig.addDefault("buyland.rent.error2", "Sorry this region is not rentable.");
        languageConfig.addDefault("buyland.rent.max", "You have rented the Maximum amount of land allowed.");
        languageConfig.addDefault("buyland.rent.price", "The cost to rent this region for 1 %s is: %s");
        languageConfig.addDefault("buyland.rent.notsneak", "Sneak right-click the sign to rent the land.");

        languageConfig.addDefault("buyland.sell.forsale", "This land is for sale.");
        languageConfig.addDefault("buyland.sell.back1", "You have sold back the land for ");
        languageConfig.addDefault("buyland.sell.back2", ". Your balance is: %s");
        languageConfig.addDefault("buyland.sell.dontown", "You do not own this land!");
        languageConfig.addDefault("buyland.sell.notsneak", "You must be sneaking when you right-click a sign to sell land!");
        languageConfig.addDefault("buyland.sell.permission", "You do not have permission to sell a region.");
        languageConfig.addDefault("buyland.sell.instructions.line1", "Sign Instructions:");
        languageConfig.addDefault("buyland.sell.instructions.line2", "sneak right-click to buy region.");

        languageConfig.addDefault("buyland.member.removemember", "Removed Member!");
        languageConfig.addDefault("buyland.member.addmember", "Added Member!");
        
        languageConfig.addDefault("buyland.buy.max", "You have bought the Maximum amount of land allowed.");
        languageConfig.addDefault("buyland.buy.welcome1", "Welcome to ");
        languageConfig.addDefault("buyland.buy.welcome2", "`s Land!");
        languageConfig.addDefault("buyland.buy.cantafford", "%s to buy the land.");
        languageConfig.addDefault("buyland.buy.permission", "You do not have permission to buy a region.");
        languageConfig.addDefault("buyland.buy.notsneak", "Sneak right-click the sign to buy the land.");
        languageConfig.addDefault("buyland.buy.bought", "You bought the land for %s and you now have %s");
        languageConfig.addDefault("buyland.buy.dontown", "Sorry this land is not buyable.");
        
        languageConfig.addDefault("buyland.price.price", "You currently have %s to purchase this land.");
        languageConfig.addDefault("buyland.price.cost", "This land is buyable and costs: ");
        languageConfig.addDefault("buyland.price.max1", "You have ");
        languageConfig.addDefault("buyland.price.max2", " pieces of land. The Max is ");
        languageConfig.addDefault("buyland.price.dontown", "Sorry this land is not buyable.");
        
        languageConfig.addDefault("buyland.flag.cantafford", "%s to buy the region flag.");
        languageConfig.addDefault("buyland.flag.buy", "You bought the flag for %s. Your balance is: %s");
        languageConfig.addDefault("buyland.flag.sell", "You have sold the flag for %s. Your balance is: %s");
        languageConfig.addDefault("buyland.flag.instructions.line1", "Sign Instructions:");
        languageConfig.addDefault("buyland.flag.instructions.line2", "right-click to view available region flags.");
        languageConfig.addDefault("buyland.flag.instructions.line3", "left-click to view region flags options.");
        languageConfig.addDefault("buyland.flag.instructions.line4", "sneak right-click to toggle flag option.");
        languageConfig.addDefault("buyland.flag.instructions.line5", "sneak left-click to exit flag options.");
        
        languageConfig.addDefault("buyland.sign.instructions.header", "Sign Instructions:");
        
        languageConfig.addDefault("buyland.sign.instructions.forsale.left", "left-click break sign with rights.");
        languageConfig.addDefault("buyland.sign.instructions.forsale.right", "right-click does nothing.");
        languageConfig.addDefault("buyland.sign.instructions.forsale.sneakleft", "sneak left-click does nothing.");
        languageConfig.addDefault("buyland.sign.instructions.forsale.sneakright", "sneak right-click to buy region.");
        
        languageConfig.addDefault("buyland.sign.instructions.own.left", "left-click break sign with rights.");
        languageConfig.addDefault("buyland.sign.instructions.own.right", "right-click does nothing.");
        languageConfig.addDefault("buyland.sign.instructions.own.sneakleft", "sneak left-click view flag options.");
        languageConfig.addDefault("buyland.sign.instructions.own.sneakright", "sneak right-click to sell region.");
        
        languageConfig.addDefault("buyland.sign.instructions.forrent.left", "left-click break sign with rights.");
        languageConfig.addDefault("buyland.sign.instructions.forrent.right", "right-click to cycle through time frames.");
        languageConfig.addDefault("buyland.sign.instructions.forrent.sneakleft", "sneak left-click does nothing");
        languageConfig.addDefault("buyland.sign.instructions.forrent.sneakright", "sneak right-click to rent region.");
        
        languageConfig.addDefault("buyland.sign.instructions.rent.left", "left-click break sign with rights.");
        languageConfig.addDefault("buyland.sign.instructions.rent.right", "right-click to cycle through time frames.");
        languageConfig.addDefault("buyland.sign.instructions.rent.sneakleft", "sneak left-click view flag options.");
        languageConfig.addDefault("buyland.sign.instructions.rent.sneakright", "sneak right-click to extend rent.");
        
        languageConfig.addDefault("buyland.sign.instructions.flags.left", "left-click to view region flags options.");
        languageConfig.addDefault("buyland.sign.instructions.flags.right", "right-click to view available region flags.");
        languageConfig.addDefault("buyland.sign.instructions.flags.sneakleft", "sneak left-click to exit flag options.");
        languageConfig.addDefault("buyland.sign.instructions.flags.sneakright", "sneak right-click to toggle flag option.");

        languageConfig.addDefault("buyland.sign.break.adminonly", "Only an admin or with permission can break this sign.");
        languageConfig.addDefault("buyland.sign.break.notowner", "Only an admin or the owner can break this sign.");
    }
    public void languageSaveConfig() {
        if (languageConfig == null || languageConfigFile == null) {
            return;
        }
        
        try {
            languageGetConfig().save(languageConfigFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + languageConfigFile, ex);
        }
    }

    public FileConfiguration rentGetConfig() {
        if (rentConfig == null) {
            this.rentReloadConfig();
            //make sure this option is set
            rentConfig.options().copyDefaults(true);
        }
        return rentConfig;
    }
    public void rentReloadConfig() {
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
    public void rentSaveConfig() {
        if (rentConfig == null || rentConfigFile == null) {
            return;
        }

        try {
            rentGetConfig().save(rentConfigFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + rentConfigFile, ex);
        }
    }

    public FileConfiguration rentDbGetConfig() {
        if (rentDbConfig == null) {
            this.rentDbReloadConfig();
        }
        //make sure this option is set
        rentDbConfig.options().copyDefaults(true);
        
        return rentDbConfig;
    }
    public void rentDbReloadConfig() {
        if (rentDbConfigFile == null) {
        	rentDbConfigFile = new File(getDataFolder(), "rentdb.yml");
        }
        rentDbConfig = YamlConfiguration.loadConfiguration(rentDbConfigFile);
     
        // Look for defaults in the jar
        InputStream defConfigStream = this.getResource("rentdb.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            rentDbConfig.setDefaults(defConfig);
        }
        
        //Make sure the minimum settings in the file are there with these defaults
        rentDbConfig.options().header("BuyLand Rent DB File. Used for keeping track of how many rentable plots a user has.");
        
        rentDbConfig.addDefault("user.renting", 0);
        rentDbConfig.addDefault("user.earned", 0.00);
        rentDbConfig.addDefault("user.spent", 0.00);
    }
    public void rentDbSaveConfig() {
        if (rentDbConfig == null || rentDbConfigFile == null) {
            return;
        }
        
        try {
            rentDbGetConfig().save(rentDbConfigFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + rentDbConfigFile, ex);
        }
    }

    public FileConfiguration pluginGetConfig() {
        //this is a special case as the plugin config is built into bukkit.
        //this is normally accessable by calling this.getConfig(); as is done below.
        this.pluginReloadConfig();        
        
        return this.getConfig();
    }
    public void pluginReloadConfig() {
        final FileConfiguration config = this.getConfig();
        config.options().header("BuyLand... Besure to make prices have .00 or it may break. Double");

        //Unused config
        //config.addDefault("buyland.maxamountofland", 1);
        //config.addDefault("buyland.breaksignonbuy", false);

        //general config
        config.addDefault("general.regionPriority", 1);
        config.addDefault("general.configVersion", 2);
        config.addDefault("general.sign.showBuyLand", true);
        
        //buyland stuff
        config.addDefault("buyland.allowedWorldGuardFlags.example", 0.0);
        config.addDefault("buyland.onCreate.denyEntry", false);
        config.addDefault("buyland.onCreate.greetMessage.display", true);
        config.addDefault("buyland.onCreate.greetMessage.erase", false); //only available if display = false
        config.addDefault("buyland.onCreate.saveSchematic", true);
        config.addDefault("buyland.onCreate.removelwcprotection", false);
        config.addDefault("buyland.onCreate.worldGuardFlags.default", "");
        config.addDefault("buyland.onBuyFromBank.breakSign", false);
        config.addDefault("buyland.onBuyFromBank.denyEntry", false);
        config.addDefault("buyland.onBuyFromBank.greetMessage.display", true);
        config.addDefault("buyland.onBuyFromBank.greetMessage.erase", false); //only available if display = false
        config.addDefault("buyland.onBuyFromBank.placeSchematic", false);
        config.addDefault("buyland.onBuyFromBank.saveSchematic", true);
        config.addDefault("buyland.onBuyFromBank.price.default", 100.00);
        config.addDefault("buyland.onBuyFromBank.price.perBlock", 1.00);
        config.addDefault("buyland.onBuyFromBank.price.usePerBlock", false);
        config.addDefault("buyland.onBuyFromBank.removelwcprotection", false);
        config.addDefault("buyland.onBuyFromBank.worldGuardFlags.default", "");
        config.addDefault("buyland.onSaleToBank.denyEntry", false);
        config.addDefault("buyland.onSaleToBank.greetMessage.display", true);
        config.addDefault("buyland.onSaleToBank.greetMessage.erase", false); //only available if display = false
        config.addDefault("buyland.onSaleToBank.placeSchematic", true);
        config.addDefault("buyland.onSaleToBank.saveSchematic", false);
        config.addDefault("buyland.onSaleToBank.price.percent", 1.00);
        config.addDefault("buyland.onSaleToBank.removelwcprotection", false);
        config.addDefault("buyland.onSaleToBank.worldGuardFlags.default", "");
        config.addDefault("buyland.offlineLimit.days", 30);
        config.addDefault("buyland.offlineLimit.enable", true);
        config.addDefault("buyland.offlineLimit.checkMembers", false); //default to false so it acts the same way as before.
        
        //rentland stuff
        config.addDefault("rentland.allowedWorldGuardFlags.example", 0.0);
        config.addDefault("rentland.onPlayerJoin.notifyOfTimeLeft", true);
        config.addDefault("rentland.onCreate.denyEntry", false);
        config.addDefault("rentland.onCreate.greetMessage.display", true);
        config.addDefault("rentland.onCreate.greetMessage.erase", false); //only available if display = false
        config.addDefault("rentland.onCreate.saveSchematic", true);
        config.addDefault("rentland.onCreate.removelwcprotection", false);
        config.addDefault("rentland.onCreate.worldGuardFlags.default", "");
        config.addDefault("rentland.onCreate.price.perMinDefault", 1.0);
        config.addDefault("rentland.onRentBegin.denyEntry", false);
        config.addDefault("rentland.onRentBegin.greetMessage.display", true);
        config.addDefault("rentland.onRentBegin.greetMessage.erase", false); //only available if display = false
        config.addDefault("rentland.onRentBegin.removelwcprotection", false);
        config.addDefault("rentland.onRentBegin.placeSchematic", false);
        config.addDefault("rentland.onRentBegin.saveSchematic", true);
        config.addDefault("rentland.onRentBegin.worldGuardFlags.default", "");
        config.addDefault("rentland.onRentExtend.denyEntry", false);
        config.addDefault("rentland.onRentExtend.removelwcprotection", false);
        config.addDefault("rentland.onRentExtend.placeSchematic", false);
        config.addDefault("rentland.onRentExtend.saveSchematic", false);
        config.addDefault("rentland.onRentExtend.worldGuardFlags.default", "");
        config.addDefault("rentland.onRentExpire.denyEntry", false);
        config.addDefault("rentland.onRentExpire.greetMessage.display", true);
        config.addDefault("rentland.onRentExpire.greetMessage.erase", false); //only available if display = false
        config.addDefault("rentland.onRentExpire.removelwcprotection", false);
        config.addDefault("rentland.onRentExpire.placeSchematic", true);
        config.addDefault("rentland.onRentExpire.saveSchematic", false);
        config.addDefault("rentland.onRentExpire.broadcast.available", true);
        config.addDefault("rentland.onRentExpire.worldGuardFlags.default", "");
        config.addDefault("rentland.onRentBegin.maxRegions", 1);
        
        config.options().copyDefaults(true);        
    }
    
    @Override
    public void onDisable() {
        rentSaveConfig();
        //PluginDescriptionFile pdffile = this.getDescription();
        //this.logger.info(pdffile.getName() + " is now disabled.");
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
                                                            //                /rentland [regionname] x [Sec/Min/Hr/Day/Wk]     - Rents a region.
        getCommand("priceland").setExecutor(clPriceland);
        this.getServer().getPluginManager().registerEvents(elPlayerInteract, this);  //Handle sign left and right clicks
        this.getServer().getPluginManager().registerEvents(elPlayerJoin, this);      //Handle player joins
        this.getServer().getPluginManager().registerEvents(elSignChange, this);      //Handle sign create / alter

    	//PluginDescriptionFile pdffile = this.getDescription();
    	//this.logger.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled!");
    	
    	//setup the config files on disk    	
    	languageGetConfig();   //Load or create the defaults
    	languageSaveConfig();  //Save to disk
    	
    	rentGetConfig();       //Load or create the defaults
    	rentSaveConfig();      //Save to disk
    	
    	signGetConfig();       //Load or create the defaults
    	signSaveConfig();      //Save to disk
    	
    	getCustomConfig();     //Load or create the defaults
    	saveCustomConfig();    //Save to disk
    	
    	rentDbGetConfig();     //Load or create the defaults
    	rentDbSaveConfig();    //Save to disk
    		
    	pluginGetConfig();     //Load or create the defaults
    	saveConfig();          //Save to disk

    	//fix the files if necessary
        onEnable_fixRegionNames();
        onEnable_fixConfigSettings();

    	//Load the general plugin config
    	final FileConfiguration config = pluginGetConfig();

    	//initialize the worldGuard variable above.
    	getWorldGuard();
    	
    	//Run a background process every 1200 ticks (1 minute) after first waiting 20 ticks for the plugin to start up
    	//Auto Sell a Region if the player has not been on for the specified amount of days
    	new BukkitRunnable() {
    	    public void run() {
    	        //See if we want to limit offline time
    	        if(config.getBoolean("buyland.offlineLimit.enable") == true) {
    	            //Loop through each world
    	            for (World world: Bukkit.getWorlds()) {
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
    	                                //Player has never been on this server or is exempt from the offline limit.
    	                                //
    	                                //Do not sell the region.
    	                            } else {
    	                                //Ticket #75
    	                                //See if this region is exempt from this requirement
    	                                if (!signGetConfig().getBoolean("sign." + protectedRegion.getId() + ".inactiveexempt")) {
                                            //get when the player was last seen by bukkit
                                            long timePlayerLastSeen = Bukkit.getOfflinePlayer(ownerNames).getLastPlayed();
                                            //Get the current time on the server
                                            long timeCurrent = System.currentTimeMillis();
        	                                //Calculate the time away from the server
            	                            long timeAwayFromServer = timeCurrent - timePlayerLastSeen;
            	                            //Get the maximum amount of time logged off before the region is sold
            	                            long maximumAllowedTimeAwayFromServer = getConfig().getLong("buyland.offlineLimit.days") * (24 * 60 * 60 * 1000L);
            	                            //See if they have been away long enough
            	                            if (timeAwayFromServer > maximumAllowedTimeAwayFromServer) {
            	                                //See if this is a rental
            	                                if (isRentRegion(protectedRegion)) {
                                                    //It is rentable, do nothing here
                                                } else {
                                                    //Ticket #63
                                                    //Make sure no members of the region have signed in either
                                                    boolean noMemberHasBeenSeen = true;
                                                    if (getConfig().getBoolean("buyland.offlineLimit.checkMembers") == true) {
                                                        for (String memberName : protectedRegion.getMembers().getPlayers()) {
                                                            timePlayerLastSeen = Bukkit.getOfflinePlayer(memberName).getLastPlayed();
                                                            if ((timeCurrent - timePlayerLastSeen) <= maximumAllowedTimeAwayFromServer) {
                                                                noMemberHasBeenSeen = false;
                                                            }
                                                        }
                                                    }
    
                                                    if (noMemberHasBeenSeen) {
                                                        //neither owner nor members have signed in within timeframe
                                                        //Sell the region
                                                        ownSellRegion(null, true, world, protectedRegion.getId());
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
    	        ConfigurationSection rentalConfigSection = rentGetConfig().getConfigurationSection("rent");
    	        //Loop through each rental region
    	        for (String regionName : rentalConfigSection.getKeys(false)) {
    	            //see if there is a time section defined for the region
    	            if (rentalConfigSection.contains(regionName + ".time")) {
    	                //Do nothing as the section already has the values we need
    	            } else {
    	                //Set some default values for the region rental information since it doesn't exist 
            	    	rentGetConfig().addDefault("rent."+ regionName  + ".time", 0);
            	    	rentGetConfig().addDefault("rent."+ regionName  + ".rentable", true);
            	    	rentGetConfig().addDefault("rent."+ regionName  + ".world", "world");
            	    	rentGetConfig().addDefault("rent."+ regionName  + ".costpermin", 1.0);
            	    	rentGetConfig().options().copyDefaults(true);
            	    	rentSaveConfig();
    	            }

    	            
    	            //See if it is not rentable and time has expired for the rental
    	            if (rentalConfigSection.getBoolean(regionName + ".rentable") == false && System.currentTimeMillis() > rentalConfigSection.getLong(regionName + ".time")) {
                        String worldName = rentalConfigSection.getString(regionName + ".world");
                        World world = Bukkit.getWorld(worldName);

                        //Reset the rental region if needed
                        if (rentResetExpiredRegion(null, world, regionName) == true) {
                            //Send message to everyone
                            if (config.getBoolean("rentland.onRentExpire.broadcast.available") == true) {
                                broadcastMessageInfo(regionName + " is now rentable!");
                            }                            
                        }
    	            }
    	        } 
    	    }
    	}.runTaskTimer(this, 20L, 1200L); 
    			
    	//Make sure vault economy works properly.  If not, disable self
        if (!vaultSetupEconomy()) {
            this.logger.info("Could not load due to Vault not being loaded.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //Setup vault chat
        vaultSetupChat();
        //Setup vault permissions
        vaultSetupPermissions();	
        
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
    /**
     * converts the mixed case region names into lowercase region names in the rent and sign files.
     * Then save the config files.
     */
    private void onEnable_fixRegionNames() {
        //Fix names in the Rent config file
        
        //Get the rent config section from the file
        ConfigurationSection rentalConfigSection = rentGetConfig().getConfigurationSection("rent");
        //Loop through each rental region
        for (String regionName : rentalConfigSection.getKeys(false)) {
            if (regionName != regionName.toLowerCase()) {
                //set the config values for the region name
                rentGetConfig().set("rent." + regionName.toLowerCase() + ".time", rentGetConfig().get("rent." + regionName + ".time"));
                rentGetConfig().set("rent." + regionName.toLowerCase() + ".rentable", rentGetConfig().getBoolean("rent." + regionName + ".rentable"));
                rentGetConfig().set("rent." + regionName.toLowerCase() + ".world", rentGetConfig().get("rent." + regionName + ".world"));
                rentGetConfig().set("rent." + regionName.toLowerCase() + ".costpermin", rentGetConfig().get("rent." + regionName + ".costpermin"));

                //remove the old regionName
                rentGetConfig().set("rent." + regionName, null);
            }
        }
        rentSaveConfig();
        
        //Get the sign config section from the file
        ConfigurationSection signConfigSection = signGetConfig().getConfigurationSection("sign");
        //Loop through each sign region
        for (String regionName : signConfigSection.getKeys(false)) {
            if (regionName != regionName.toLowerCase()) {
                //set the config values for the region name
                signGetConfig().set("sign." + regionName.toLowerCase(), signGetConfig().get("sign." + regionName));

                //remove the old regionName
                signGetConfig().set("sign." + regionName, null);
            }
        }
        signSaveConfig();
        
    }
    private void onEnable_fixConfigSettings() {
        final FileConfiguration config = pluginGetConfig();

        //Convert the general section
        if (config.getString("buyland.landpriority") != null) {
            config.set("general.regionPriority", config.getInt("buyland.landpriority"));
            config.set("buyland.landpriority", null);
        }

        //Convert the buyland section
        if (config.getString("buyland.offlinelimitenable") != null) {
            config.set("buyland.offlineLimit.enable", config.getBoolean("buyland.offlinelimitenable"));
            config.set("buyland.offlinelimitenable", null);
        }
        if (config.getString("buyland.offlinelimitindays") != null) {
            config.set("buyland.offlineLimit.days", config.getInt("buyland.offlinelimitindays"));
            config.set("buyland.offlinelimitindays", null);
        }

        if (config.getString("buyland.defaultprice") != null) {
            config.set("buyland.onBuyFromBank.price.default", config.getDouble("buyland.defaultprice"));
            config.set("buyland.defaultprice", null);
        }
        if (config.getString("buyland.defaultpriceperblock") != null) {
            config.set("buyland.onBuyFromBank.price.perBlock", config.getDouble("buyland.defaultpriceperblock"));
            config.set("buyland.defaultpriceperblock", null);
        }
        if (config.getString("buyland.usepriceperblock") != null) {
            config.set("buyland.onBuyFromBank.price.usePerBlock", config.getBoolean("buyland.usepriceperblock"));
            config.set("buyland.usepriceperblock", null);
        }
        
        if (config.getString ("buyland.percentsellback") != null) {
            config.set("buyland.onSaleToBank.price.percent", config.getDouble("buyland.percentsellback"));
            config.set("buyland.percentsellback", null);
        }
        if (config.getString ("buyland.breaksignonbuy") != null) {
            config.set("buyland.onSaleToBank.breaksign", config.getBoolean("buyland.breaksignonbuy"));
            config.set("buyland.breaksignonbuy", null);
        }

        //Split one config into two related to buy and rent
        if (config.getString("buyland.denyentrytoland") != null) {
            Boolean value = config.getBoolean("buyland.denyentrytoland");
            config.set("buyland.onCreate.denyEntry", value);
            config.set("buyland.onBuyFromBank.denyEntry", value);
            config.set("buyland.onSaleToBank.denyEntry", value);
            config.set("rentland.onCreate.denyEntry", value);
            config.set("rentland.onRentBegin.denyEntry", value);
            config.set("rentland.onRentExpire.denyEntry", value);
            config.set("buyland.denyentrytoland", null);
        }
        if (config.getString("buyland.landgreeting") != null) {
            Boolean value = config.getBoolean("buyland.landgreeting");
            config.set("buyland.onCreate.greetMessage.display", value);
            config.set("buyland.onBuyFromBank.greetMessage.display", value);
            config.set("buyland.onSaleToBank.greetMessage.display", value);
            config.set("rentland.onCreate.greetMessage.display", value);
            config.set("rentland.onRentBegin.greetMessage.display", value);
            config.set("rentland.onRentExpire.greetMessage.display", value);
            config.set("buyland.landgreeting", null);
        }
        if (config.getString("buyland.landgreetingerasemsg") != null) {
            Boolean value = config.getBoolean("buyland.landgreetingerasemsg");
            config.set("buyland.onCreate.greetMessage.erase", value);
            config.set("buyland.onBuyFromBank.greetMessage.erase", value);
            config.set("buyland.onSaleToBank.greetMessage.erase", value);
            config.set("rentland.onCreate.greetMessage.erase", value);
            config.set("rentland.onRentBegin.greetMessage.erase", value);
            config.set("rentland.onRentExpire.greetMessage.erase", value);
            config.set("buyland.landgreetingerasemsg", null);
        }
        if (config.getString("buyland.resetlandonsale") != null) {
            Boolean value = config.getBoolean("buyland.resetlandonsale");
            config.set("buyland.onCreate.saveSchematic", value);
            config.set("buyland.onBuyFromBank.saveSchematic", value);
            config.set("buyland.onBuyFromBank.placeSchematic", !value);
            config.set("buyland.onSaleToBank.saveSchematic", !value);
            config.set("buyland.onSaleToBank.placeSchematic", value);
            config.set("rentland.onCreate.saveSchematic", value);
            config.set("rentland.onRentBegin.saveSchematic", value);
            config.set("rentland.onRentBegin.placeSchematic", !value);
            config.set("rentland.onRentExpire.saveSchematic", !value);
            config.set("rentland.onRentExpire.placeSchematic", value);
            config.set("buyland.resetlandonsale", null);
        }
        if (config.getString("buyland.removelwcprotection") != null) {
            Boolean value = config.getBoolean("buyland.removelwcprotection");
            config.set("buyland.onCreate.removelwcprotection", value);
            config.set("buyland.onBuyFromBank.removelwcprotection", value);
            config.set("buyland.onSaleToBank.removelwcprotection", value);
            config.set("rentland.onCreate.removelwcprotection", value);
            config.set("rentland.onRentBegin.removelwcprotection", value);
            config.set("rentland.onRentExpire.removelwcprotection", value);
            config.set("buyland.removelwcprotection", null);
        }
        
        //convert the rentland section
        if (config.getString("buyland.defaultrentcostpermin") != null) {
            config.set("rentland.onCreate.price.perMinDefault", config.getDouble("buyland.defaultrentcostpermin"));
            config.set("buyland.defaultrentcostpermin", null);
        }
        if (config.getString("buyland.maxamountofrentland") != null) {
            config.set("rentland.onRentBegin.maxRegions", config.getInt("buyland.maxamountofrentland"));
            config.set("buyland.maxamountofrentland", null);
        }
        if (config.getString("buyland.rentbroadcastmsg") != null) {
            config.set("rentland.onRentExpire.broadcast.available", config.getBoolean("buyland.rentbroadcastmsg"));
            config.set("buyland.rentbroadcastmsg", null);
        }
        if (config.getString("buyland.notifyplayerofrenttime") != null) {
            config.set("rentland.onPlayerJoin.notifyOfTimeLeft", config.getBoolean("buyland.notifyplayerofrenttime"));
            config.set("buyland.notifyplayerofrenttime", null);
        }
        
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }

    /**
     * this will register a buyland sign
     * This makes sure there is only buyland sign
     * 
     * @param world World that the region belongs to
     * @param argRegionName String name of the region
     * @return boolean true if the region was successfully sold, false otherwise.
     */
    protected boolean signRegister(World world, String argRegionName, Location newSignLocation) {
        //make sure it is the proper case
        argRegionName = argRegionName.toLowerCase();
        
        //Make sure location passed in is a valid BuyLand sign
        Block signBlockLocation = newSignLocation.getBlock();
        //see if location is a sign
        if (signBlockLocation.getType() == Material.SIGN || signBlockLocation.getType() == Material.SIGN_POST || signBlockLocation.getType() == Material.WALL_SIGN) {
            //See if we have an existing sign in the system for the region
            //Inactivate it if it exists
            if (signGetConfig().contains("sign." + argRegionName + ".location")) {
                //Get the block at the location from the config
                signBlockLocation = locationFromString(signGetConfig().getString("sign." + argRegionName + ".location")).getBlock();
                if (!newSignLocation.equals(signBlockLocation.getLocation())) {
                    //see if location is a sign
                    if (signBlockLocation.getType() == Material.SIGN || signBlockLocation.getType() == Material.SIGN_POST || signBlockLocation.getType() == Material.WALL_SIGN) {
                        //get the sign object
                        Sign sign = (Sign) signBlockLocation.getState();
                        //Inactivate the old existing sign
                        sign.setLine(0, "Inactivated");
                        sign.setLine(1, "BuyLand Sign");
                        sign.setLine(2, "for region");
                        sign.setLine(3, argRegionName);
                        //Save change to sign
                        sign.update();
                        //try to notify the owner of the region
                        try {
                            //Remove the old sign
                            signBlockLocation.breakNaturally();
                            
                            Player player = Bukkit.getPlayer(getWorldGuard().getRegionManager(world).getRegion(argRegionName).getOwners().getPlayers().toArray()[0].toString());
                            sendMessageInfo(player, String.format("The original Buyland sign for '%s' region located at: (%s) was removed.",
                                                                  argRegionName,
                                                                  locationToString(signBlockLocation.getLocation(),": ", ", ")
                                                                 ));
                        } catch (Exception  e) {}
                    }
                }
            }

            //Store the passed in sign in the config file replacing one if it is there.
            signGetConfig().set("sign." + argRegionName + ".location", locationToString(newSignLocation));
            signGetConfig().set("sign." + argRegionName + ".state", signGetState(newSignLocation, argRegionName).name());
            
            //Save the sign config
            signSaveConfig();
            signReloadConfig();

            //Report that sign infomation was stored in config
            return true;
        }        
        // We never created the sign in the config.  Let caller know.
        return false;
    }
    protected boolean signIsDefined(String argRegionName) {
        return signGetConfig().contains("sign." + argRegionName);
    }
    protected boolean signIsValidHeader(String[] lines) {
        if (lines[0].contains("[BuyLand]") || lines[0].equalsIgnoreCase("[BuyLand]")) {
            return true;
        }
        return false;
    }
    protected boolean signIsBuyland(Block signBlock) {
        //See if the block is even a sign type
        if (signBlock.getType() == Material.SIGN || signBlock.getType() == Material.SIGN_POST || signBlock.getType() == Material.WALL_SIGN) {
            //See if the block location is in the HashMap for signs
            if (signsMap.get(signBlock.getLocation()) != null)
                return true;
            
            //Abort if the item clicked on is not an instance of a sign
            if(signBlock.getState() instanceof Sign) {
                //Save the sign object for use
                Sign sign = (Sign) signBlock.getState();
                //See if this is a buyland sign
                if (sign.getLine(0).contains("[BuyLand]") || sign.getLine(0).equalsIgnoreCase("[BuyLand]")) {
                    return true;
                }                
            }
        }
        //didn't return some other way, so it is not a buyland sign
        return false;
    }
    protected Location signGetLocation(String argRegionName) {
        return locationFromString(signGetConfig().getString("sign." + argRegionName + ".location"));
    }
    protected String signGetRegion(Location location) {
        if (signsMap.containsKey(location)) {
            return signsMap.get(location).split(":")[0];
        } else {
            return null;
        }
    }
    protected signState signGetState(Location location, String regionName) {
        signState state = signGetState(location);
        if (state == null) {
            //this must be a new sign.
            //Get the region manager for the world
            RegionManager regionManager = getWorldGuard().getRegionManager(location.getWorld());
            //Try to get the protected region indicated
            ProtectedRegion protectedRegion = regionManager.getRegionExact(regionName);
            //Make sure the region exists
            if (protectedRegion != null) {
                if (isRentRegion(protectedRegion)) {
                    state = signState.FOR_RENT;
                } else {
                    state = signState.FOR_SALE;
                }
            }
        }
        return state;
    }
    protected signState signGetState(Location location) {
        if (signsMap.containsKey(location)) {
            try {
                return signState.valueOf(signsMap.get(location).split(":")[1]);                
            } catch (Exception e) {}
        }
        return null;
    }
    protected void signSetState(Location location, signState state) {
        if (signsMap.containsKey(location)) {
            //get region name
            String regionName = signsMap.get(location).split(":")[0];
            //update the hashmap
            signsMap.put(location, regionName + ":" + state.name());
            //update the file
            signGetConfig().set("sign." + regionName + ".state", state.name());
            //save the config file
            signSaveConfig();
        }
    }
    protected void signSetState(String argRegionName, signState state) {
        //get location
        Location location = locationFromString(signGetConfig().getString("sign." + argRegionName.toLowerCase() + ".location"));
        //update the hashmap
        signsMap.put(location, argRegionName.toLowerCase() + ":" + state.name());
        //update the file
        signGetConfig().set("sign." + argRegionName + ".state", state.name());
        //save the config file
        signSaveConfig();
    }
    protected boolean signDoStateAction(Player player, ProtectedRegion protectedRegion, signState state, String action) {
        String argRegionName = protectedRegion.getId();
        String playerName = "";
        if (player != null)
            playerName = player.getName();

        //See if a sign is defined for region
        if (signIsDefined(argRegionName)) {
            Block signBlock = signGetLocation(argRegionName).getBlock();
            
            if (signBlock.getType() == Material.SIGN || signBlock.getType() == Material.SIGN_POST || signBlock.getType() == Material.WALL_SIGN) {
                //get the sign from the region information
                Sign sign = (Sign) signBlock.getState();

                Boolean showBuyLandLine = getConfig().getBoolean("general.sign.showBuyLand");
                
                //define the sign lines for easier use
                int buylandLine         = 0;
                int signCaptionLine     = 1;
                int signRegionLine      = 2;
                int signDurationLine    = 3;    /* coordinate this with BlEventListenserPlayerInteract.java */
                int signPlayerLine      = 3;
                int signPriceLine       = 3;

                if (!showBuyLandLine) {
                    signCaptionLine  = 0;
                    signRegionLine   = 1;
                    signDurationLine = 2;
                    signPlayerLine   = 2;
                    signPriceLine    = 3;
                }

                
                int signFlagCaptionLine = 0;
                int signFlagNameLine    = 1;
                int signFlagValueLine   = 2;
                int signFlagPriceLine   = 3;

                //get the action that we are going to perform
                if (action.equalsIgnoreCase("set") || action.equalsIgnoreCase("redraw")) {
                    if (action.equalsIgnoreCase("set")) {
                        //Change the state of the sign
                        signSetState(signBlock.getLocation(), state);
                    }
                    
                    //Force [BuyLand] on first line
                    if (getConfig().getBoolean("general.sign.showBuyLand"))
                        sign.setLine(buylandLine, "[BuyLand]");
                    
                    if (state.equals(signState.OWNED)) {
                        sign.setLine(signCaptionLine, "Sell Back");
                        sign.setLine(signRegionLine, argRegionName);
                        sign.setLine(signPlayerLine, playerName);
                        sign.setLine(signPriceLine, BuyLand.econ.format(ownGetRegionSellPrice(protectedRegion)));
                    } else if (state.equals(signState.OWN_FLAGS) ||
                               state.equals(signState.RENT_FLAGS)) {
                        sign.setLine(signFlagCaptionLine, "Set Region Flags");
                        sign.setLine(signFlagNameLine, "");
                        sign.setLine(signFlagValueLine, "");
                        sign.setLine(signFlagPriceLine, "");
                        sign.update();
                        //get the first allowed region flag and first option
                        signDoStateAction(player, protectedRegion, state, "RotateFlag");
                        sign = (Sign) signBlock.getState();
                    } else if (state.equals(signState.FOR_SALE)) {
                        sign.setLine(signCaptionLine, "For Sale");
                        sign.setLine(signRegionLine, argRegionName);
                        sign.setLine(signDurationLine, "");
                        sign.setLine(signPriceLine, BuyLand.econ.format(ownGetRegionPurchasePrice(protectedRegion)));
                    } else if (state.equals(signState.RENTED)) {
                        sign.setLine(signCaptionLine, "Extend Rent");
                        sign.setLine(signRegionLine, argRegionName);
                        sign.setLine(signPriceLine, BuyLand.econ.format(rentGetRegionRentalPrice(argRegionName, "Hour")));
                        sign.setLine(signDurationLine, "1 Hour");
                    } else if (state.equals(signState.RENT_EXPIRING)) {
                        sign.setLine(signCaptionLine, "End Rent");
                        sign.setLine(signRegionLine, argRegionName);
                        sign.setLine(signPriceLine, "");
                        sign.setLine(signDurationLine, "");
                    } else if (state.equals(signState.FOR_RENT)) {
                        sign.setLine(signCaptionLine, "For Rent");
                        sign.setLine(signRegionLine, argRegionName);
                        sign.setLine(signPriceLine, BuyLand.econ.format(rentGetRegionRentalPrice(argRegionName, "Hour")));
                        sign.setLine(signDurationLine, "1 Hour");
                    }
                } else if (action.equalsIgnoreCase("RotateFlag")) {
                    ConfigurationSection csAllowedFlags;
                    if (state == signState.OWN_FLAGS) {
                        csAllowedFlags = getConfig().getConfigurationSection("buyland.allowedWorldGuardFlags");
                    } else if (state == signState.RENT_FLAGS) {
                        csAllowedFlags = getConfig().getConfigurationSection("rentland.allowedWorldGuardFlags");
                    } else {
                        csAllowedFlags = getConfig().getConfigurationSection("general.allowedWorldGuardFlags");
                    }
                    //get the name of the next flag
                    String flagName = worldGuardGetNextFlag(sign.getLine(signFlagNameLine), csAllowedFlags);
                    //get the current flag value 
                    String currentFlagValue = worldGuardGetFlagCurrentValue(protectedRegion, flagName);
                    
                    //declare the default price of the flag
                    Double regionFlagPrice = 0.0;
                    
                    //update the flag price and flagValue
                    if (currentFlagValue.equalsIgnoreCase(notSetFlagString)) {
                        //Get the price of the flag
                        if (state == signState.OWN_FLAGS) {
                            regionFlagPrice = ownGetRegionFlagPurchasePrice(flagName);
                        } else if (state == signState.RENT_FLAGS) {
                            regionFlagPrice = rentGetRegionFlagPurchasePrice(flagName);
                        }

                        //get the next flag value
                        currentFlagValue = worldGuardGetNextFlagValue(flagName, "");
                    } else {
                        //Get the price of the flag
                        if (state == signState.OWN_FLAGS) {
                            regionFlagPrice = ownGetRegionFlagSellPrice(flagName);
                        } else if (state == signState.RENT_FLAGS) {
                            regionFlagPrice = rentGetRegionFlagSellPrice(flagName);
                        }
                        
                        //update the flagValue to indicate it is selected
                        currentFlagValue = "*" + currentFlagValue;
                    }
                    
                    //update the sign
                    sign.setLine(signFlagNameLine, flagName);
                    sign.setLine(signFlagValueLine,  currentFlagValue);
                    sign.setLine(signFlagPriceLine, BuyLand.econ.format(regionFlagPrice));
                } else if (action.equalsIgnoreCase("RotateFlagValue")) {
                    //set the line to the next flag option
                    String nextValue = worldGuardGetNextFlagValue(sign.getLine(signFlagNameLine), sign.getLine(signFlagValueLine).replace("*", ""));
                    if (worldGuardIsFlagValueSet(protectedRegion, sign.getLine(signFlagNameLine), nextValue)) {
                        nextValue = "*" + nextValue;
                    }
                    sign.setLine(signFlagValueLine, nextValue);
                } else if (action.equalsIgnoreCase("ToggleFlag")) {
                    //get the flag info from the sign
                    String flagName = worldGuardGetFlagFullName(sign.getLine(signFlagNameLine));
                    String flagValue = sign.getLine(signFlagValueLine);
                    String fullFlagValue = worldGuardGetFlagFullValue(flagName, flagValue.replace("*", ""));
                    
                    //See which way we need to toggle the option
                    if (flagValue.substring(0,1).equalsIgnoreCase("*")) {
                        //Remove the flag that is currently set
                        
                        //get the credit for selling the flag
                        Double regionFlagPrice = 0.0;
                        if (state == signState.OWN_FLAGS) {
                            regionFlagPrice = ownGetRegionFlagSellPrice(flagName);
                        } else if (state == signState.RENT_FLAGS) {
                            regionFlagPrice = rentGetRegionFlagSellPrice(flagName);
                        }

                        //Sell it back
                        EconomyResponse economyResponse = BuyLand.econ.depositPlayer(playerName, regionFlagPrice);

                        //see if something is already set
                        String testValue = worldGuardGetFlagCurrentValue(protectedRegion, flagName, false);
                        if (testValue.contains(",")) {
                            //This is a list, remove the name from the list
                            testValue = testValue.replace(fullFlagValue, "").replaceAll(", ,", ",").replaceAll(",,", ",").trim().replace("[", "").replace("]", "");
                            if (testValue.substring(0,2).equals(", ")) {
                                testValue = testValue.substring(2, testValue.length());
                            }
                            //Set the flag with the value
                            worldGuardSetFlag(player, flagName, testValue, protectedRegion);
                        } else {
                            //This is not a list, just remove it
                            worldGuardSetFlag(player, flagName, null, protectedRegion); 
                        }

                        if (state == signState.OWN_FLAGS) {
                            regionFlagPrice = ownGetRegionFlagPurchasePrice(flagName);
                        } else if (state == signState.RENT_FLAGS) {
                            regionFlagPrice = rentGetRegionFlagPurchasePrice(flagName);
                        }
                        
                        //update the sign
                        sign.setLine(signFlagValueLine, fullFlagValue);
                        sign.setLine(signFlagPriceLine, BuyLand.econ.format(regionFlagPrice));

                        //Notify the player
                        if (regionFlagPrice != 0.0) {
                            sendMessageInfo(player, String.format(ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.flag.sell")), 
                                                                  BuyLand.econ.format(regionFlagPrice), 
                                                                  BuyLand.econ.format(economyResponse.balance)));
                        }
                    } else {
                        //Set the flag with the value
                        
                        //get the cost of purchasing the flag
                        Double regionFlagPrice = 0.0;
                        if (state == signState.OWN_FLAGS) {
                            regionFlagPrice = ownGetRegionFlagPurchasePrice(flagName);
                        } else if (state == signState.RENT_FLAGS) {
                            regionFlagPrice = rentGetRegionFlagPurchasePrice(flagName);
                        }
                        
                        //Try to buy it with the players funds
                        EconomyResponse economyResponse = BuyLand.econ.withdrawPlayer(playerName, regionFlagPrice);
    
                        if (!economyResponse.transactionSuccess()) {
                            //Purchase is unsuccessful
                            sendMessageInfo(player, String.format(ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.flag.cantafford")), economyResponse.errorMessage));
                        } else {
                            //see if the flag type is a list
                            if (flagName.equals("deny-spawn")) {
                                //This is a list, get what is already there and add to it.
                                String testValue = worldGuardGetFlagCurrentValue(protectedRegion, flagName, false);
                                testValue = testValue.replace(notSetFlagString, "").trim().replace("[", "").replace("]", "");
                                if (!testValue.isEmpty())
                                    testValue += ", ";
                                testValue += fullFlagValue;
                                worldGuardSetFlag(player, flagName, testValue, protectedRegion);
                            } else {
                                worldGuardSetFlag(player, flagName, fullFlagValue, protectedRegion);
                            }
                            //update the sign
                            sign.setLine(signFlagValueLine, "*" + fullFlagValue);
                            
                            //Notify the player
                            if (regionFlagPrice != 0.0) {
                                sendMessageInfo(player, String.format(ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.flag.buy")), 
                                                                      BuyLand.econ.format(economyResponse.amount), 
                                                                      BuyLand.econ.format(economyResponse.balance)));
                            }
                        }
                    }
                } else if (action.equalsIgnoreCase("RotateRentTimeframe")) {
                    String durationPortion = "Hour";
                    switch (sign.getLine(signDurationLine).split(" ")[1].toLowerCase()) {
                        case "second":  durationPortion = "Minute"; break;
                        case "minute":  durationPortion = "Hour";   break;
                        case "hour":    durationPortion = "Day";    break;
                        case "day":     durationPortion = "Week";   break;
                        case "week":    durationPortion = "Second"; break;
                    }
                    String rentalPrice = BuyLand.econ.format(rentGetRegionRentalPrice(argRegionName, durationPortion));
                    sign.setLine(signPriceLine, rentalPrice);
                    sign.setLine(signDurationLine, "1 " + durationPortion);
                    if (showBuyLandLine) {
                        //notify user of price since they can not see it
                        sendMessageInfo(player, String.format(ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.rent.price")),
                                                              durationPortion,
                                                              rentalPrice));
                    }
                }
                sign.update();
                return true;
            }
        }
        return false;
    }
    protected void signSetExempt(Player player, String argRegionName) {
        if (player.hasPermission("buyland.offlinelimit.exempt")) {
            signGetConfig().set("sign." + argRegionName + ".inactiveexempt", true);
        } else {
            signGetConfig().set("sign." + argRegionName + ".inactiveexempt", false);            
        }
        signSaveConfig();
        signReloadConfig();
    }
    /**
     * This will redraw all buyland signs so they are in the correct format
     * This looks at the general.sign.showBuyLand config setting to do this.
     * @return int count of signs updated
     */
    protected int signUpdateAll(Player player) {
        int signUpdateCount = 0;

        //Get all sign locations
        for (Location location : signsMap.keySet()) {
            //Get the potential sign block
            Block signBlock = location.getBlock();
            if (signBlock.getType() == Material.SIGN || signBlock.getType() == Material.SIGN_POST || signBlock.getType() == Material.WALL_SIGN) {
                //Get the recorded state of the sign.
                signState state = signGetState(location);
                //Get the region name
                String regionName = signGetRegion(location);
                //get the valid region
                RegionManager regionManager = getWorldGuard().getRegionManager(location.getWorld());
                ProtectedRegion protectedRegion = regionManager.getRegionExact(regionName);
                if (protectedRegion != null) {
                    //Update the sign to the correct state
                    sendMessageInfo(player, "Updating sign for region:" + regionName + " at:" + locationToString(location));
                    if (signDoStateAction(player, protectedRegion, state, "redraw")) {
                        signUpdateCount++;
                    }
                }
            }
        }
        return signUpdateCount;
    }

    
    /**
     * Indicates if the passed in protectedRegion is a rent region
     *  
     * @param protectedRegion ProtectedRegion that is in question
     * @return boolean true if it is a rentable region, false if it is a buyable region
     */
    protected boolean isRentRegion(ProtectedRegion protectedRegion) {
        String argRegionName = protectedRegion.getId();
        if(rentGetConfig().contains("rent." + argRegionName.toLowerCase() + ".rentable")) {
            //This is a rentable region
            if (!protectedRegion.getFlag(DefaultFlag.BUYABLE)) {
                //This rentable region is not buyable
                return true;
            }
        }
        return false;
    }
    /**
     * This will reset the expired rented region to allow for re-renting the region.
     * 
     * @param world World where the region exists
     * @param argRegionName String of the region to be reset
     */
    protected boolean rentResetExpiredRegion(CommandSender sender, World world, String argRegionName) {
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
            long end = rentGetConfig().getLong("rent." + argRegionName + ".time");
            //get the current time
            long start = System.currentTimeMillis();

            //see if the region needs to be reset because the rent time has expired
            if (rentGetConfig().getBoolean("rent." + argRegionName + ".rentable") == false && start > end) {
                //get 
                if (rentGetConfig().contains("rent." + argRegionName + ".time")) {
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
                    rentGetConfig().set("rent." + argRegionName + ".time", 0);
                    //set the rentable flag to true
                    rentGetConfig().set("rent." + argRegionName + ".rentable", true);
                    
                    //Set the greeting message based on config
                    if (getConfig().getBoolean("rentland.onRentExpire.greetMessage.display") == true) {
                        //set the for rent message
                        protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.rent.forrent")));
                    }else if (getConfig().getBoolean("rentland.onRentExpire.greetMessage.erase") == true){
                        protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, null);
                    }
        
                    //LWC - Remove protection from area based on config
                    if (getConfig().getBoolean("rentland.onRentExpire.removelwcprotection") == true) {
                        LWCProtectionRemove(protectedRegionMinimum, protectedRegionMaximum);
                    }
        
                    //Reset the land to original when the land is sold based on config
                    if (getConfig().getBoolean("rentland.onRentExpire.placeSchematic") == true) {
                        worldEditPlaceSchematic(protectedRegionMinimum, argRegionName);
                    }

                    //Save a schematic of the land region for restore based on config
                    if (getConfig().getBoolean("rentland.onRentExpire.saveSchematic") == true) {
                        worldEditSaveSchematic(protectedRegionMinimum, protectedRegionMaximum, argRegionName, (Player) sender);
                    }
    
                    //Set region flags per config
                    ConfigurationSection cs = getConfig().getConfigurationSection("rentland.onRentExpire.worldGuardFlags." + argRegionName);
                    if (cs == null) {
                        cs = getConfig().getConfigurationSection("rentland.onRentExpire.worldGuardFlags.default");
                    }
                    if (cs != null) {
                        worldGuardSetFlags(sender, protectedRegion, cs);
                    }
                    
                    //Deny entry based on config
                    if (getConfig().getBoolean("rentland.onRentExpire.denyEntry") == true) {
                        protectedRegion.setFlag(DefaultFlag.ENTRY, State.DENY);
                    } else {
                        protectedRegion.setFlag(DefaultFlag.ENTRY, null);
                    }
        
                    //get the owner of the region
                    DefaultDomain owners = protectedRegion.getOwners();

                    //Update the number of regions rented by the player and amount spent
                    rentUpdateRegionCount(owners.toUserFriendlyString(), -1, 0.00, 0.00);                            

                    //Save config
                    rentDbSaveConfig();
                    rentDbReloadConfig();                        
        
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
                    rentSaveConfig();
                    rentReloadConfig();
        
                    //change sign to indicate the region is available for rent
                    signDoStateAction(null, protectedRegion, signState.FOR_RENT, "set");

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
     *        W, Wk,  Week    <br/>
     * @return boolean true if the region was successfully sold, false otherwise.
     */
    protected boolean rentRegion(Player player, World world, String argRegionName, long timeUnitsToAdd, String argTimeType) {
        //get the properly cased region name for use on the sign, etc
        argRegionName = argRegionName.toLowerCase();

        //Get the player Name
        String playerName = player.getName();
        String playerNameLowerCase = playerName.toLowerCase();

        RegionManager regionManager = getWorldGuard().getRegionManager(world);
        //Get the protected region
        ProtectedRegion protectedRegion = regionManager.getRegionExact(argRegionName);

        //Make sure the region exists
        if (protectedRegion == null) {
            //Region does not exist.
            sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.general.error1")));
        } else {
            //Make sure region is rentable
            if (!rentGetConfig().contains("rent." + argRegionName + ".time")) {
                sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.rent.notbe")));
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
                long end = rentGetConfig().getLong("rent." + argRegionName + ".time");
                //get the current time
                long start = System.currentTimeMillis();

                //see if the region needs to be reset because the rent time has expired
                if (rentResetExpiredRegion(player, world, argRegionName)) {
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
                if (argTimeType.equalsIgnoreCase("w") || argTimeType.equalsIgnoreCase("wk") || argTimeType.equalsIgnoreCase("week")) {
                    rentMultiplier = 7 * 24 * 60 * 1;
                    timeMultiplier = 7 * 24 * 60 * 60 * 1000L;
                    timeWording    = "Week";                                        
                }

                //Get the cost of the rent for the period
                double priceToRentRegionForPeriod = rentGetRegionRentalPrice(argRegionName, timeUnitsToAdd, rentMultiplier); 
                
                //Get the new amount of time to add to the region 
                long time = timeUnitsToAdd * timeMultiplier;
                
                if (start < end) {
                    //The land is still rented and has more time on it

                    //See if the player is an owner of the region
                    if (!owners.toPlayersString().contains(playerName.toLowerCase())) {
                        sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.rent.tenant")) + BuyLand.elapsedTimeToString(start, end));       
                    } else {
                        //set the player know how much time is left
                        sendMessageInfo(player, "Time left: " + BuyLand.elapsedTimeToString(start, end));
                        //get how many time units to add

                        //Try to rent it with the players funds
                        EconomyResponse economyResponse = BuyLand.econ.withdrawPlayer(playerName, priceToRentRegionForPeriod);
                        if (!economyResponse.transactionSuccess()) {
                            sendMessageInfo(player, String.format( ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.rent.cantafford")), economyResponse.errorMessage));
                        } else {
                            //purchase successful
                            sendMessageInfo(player, String.format("Adding " + argTimeQuantity + " " + timeWording + "(s) to " + argRegionName + ". Cost: %s Balance: %s", BuyLand.econ.format(economyResponse.amount), BuyLand.econ.format(economyResponse.balance)), false);

                            //Get the time left on the region
                            long timepull = rentGetConfig().getLong("rent." + argRegionName +".time");
                            
                            //Add it to the region
                            rentGetConfig().set("rent." + argRegionName +".time", timepull + time);

                            //LWC - Remove protection from area based on config
                            if (getConfig().getBoolean("rentland.onRentExtend.removelwcprotection") == true) {
                                LWCProtectionRemove(protectedRegionMinimum, protectedRegionMaximum);
                            }

                            //Save a schematic of the land region for restore based on config
                            if (getConfig().getBoolean("rentland.onRentExtend.saveSchematic") == true) {
                                worldEditSaveSchematic(protectedRegionMinimum, protectedRegionMaximum, argRegionName, player);
                            }
                            
                            //Reset the land to original based on config
                            if (getConfig().getBoolean("rentland.onRentExtend.placeSchematic") == true) {
                                worldEditPlaceSchematic(protectedRegionMinimum, argRegionName);
                            }
                            
                            //Deny entry based on config
                            if (getConfig().getBoolean("rentland.onRentExtend.denyEntry") == true) {
                                protectedRegion.setFlag(DefaultFlag.ENTRY, State.DENY);
                            } else {
                                protectedRegion.setFlag(DefaultFlag.ENTRY, null);
                            }

                            //Set region flags per config
                            ConfigurationSection cs = getConfig().getConfigurationSection("rentland.onRentExtend.worldGuardFlags." + argRegionName);
                            if (cs == null) {
                                cs = getConfig().getConfigurationSection("rentland.onRentExtend.worldGuardFlags.default");
                            }
                            if (cs != null) {
                                worldGuardSetFlags(player, protectedRegion, cs);
                            }
                            
                            //Save the config files
                            rentSaveConfig();
                            rentReloadConfig();
                            
                            //Update the number of regions rented by the player and amount spent
                            rentUpdateRegionCount(playerNameLowerCase, 0, 0.00, priceToRentRegionForPeriod);                            
                            rentDbSaveConfig();

                            //update sign if it exists
                            signDoStateAction(player, protectedRegion, signState.RENTED, "set");
                            
                            //return that the region was rented
                            return true;
                        }
                    }
                } else {
                    //The land is for rent

                    //See if the player can rent more regions
                    if (!rentCanPlayerGetAnotherRegion(player)) {
                        //player has reached rent max rents
                        sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.rent.max")));
                    } else {
                        EconomyResponse economyResponse = BuyLand.econ.withdrawPlayer(playerName, priceToRentRegionForPeriod);
                        if (!economyResponse.transactionSuccess()) {
                            sendMessageInfo(player, String.format(ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.rent.cantafford")), economyResponse.errorMessage));
                        } else {
                            //Notify user that rent succeeded
                            sendMessageInfo(player, String.format("Renting " + argRegionName + " for " + argTimeQuantity + " " + timeWording + "(s). Cost: %s Balance: %s", BuyLand.econ.format(economyResponse.amount), BuyLand.econ.format(economyResponse.balance)));

                            //Update flags on region
                            rentGetConfig().set("rent." + argRegionName +".time", System.currentTimeMillis() + time);
                            rentGetConfig().set("rent." + argRegionName +".world", world.getName());
                            rentGetConfig().set("rent." + argRegionName +".rentable", false);
                            
                            //Update the number of regions rented by the player and amount spent
                            rentUpdateRegionCount(playerNameLowerCase, +1, 0.00, priceToRentRegionForPeriod);                            
                            rentDbSaveConfig();
                            
                            //Update owner of rented domain 
                            DefaultDomain dd = new DefaultDomain();
                                          dd.addPlayer(playerName);
                            protectedRegion.setOwners(dd);

                            //set greeting message for region based on config
                            if (getConfig().getBoolean("rentland.onRentBegin.greetMessage.display") == true) {
                                protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.rent.rentby")) + playerName);
                            } else if (getConfig().getBoolean("rentland.onRentBegin.greetMessage.erase") == true) {
                                protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, null);
                            }

                            //LWC - Remove protection from area based on config
                            if (getConfig().getBoolean("rentland.onRentBegin.removelwcprotection") == true) {
                                LWCProtectionRemove(protectedRegionMinimum, protectedRegionMaximum);
                            }

                            //Save a schematic of the land region for restore based on config
                            if (getConfig().getBoolean("rentland.onRentBegin.saveSchematic") == true) {
                                worldEditSaveSchematic(protectedRegionMinimum, protectedRegionMaximum, argRegionName, player);
                            }
                            
                            //Reset the land to original based on config
                            if (getConfig().getBoolean("rentland.onRentBegin.placeSchematic") == true) {
                                worldEditPlaceSchematic(protectedRegionMinimum, argRegionName);
                            }
                            
                            //Deny entry based on config
                            if (getConfig().getBoolean("rentland.onRentBegin.denyEntry") == true) {
                                protectedRegion.setFlag(DefaultFlag.ENTRY, State.DENY);
                            } else {
                                protectedRegion.setFlag(DefaultFlag.ENTRY, null);
                            }

                            //Flag the region as rented
                            protectedRegion.setFlag(DefaultFlag.BUYABLE, false);

                            //Set region flags per config
                            ConfigurationSection cs = getConfig().getConfigurationSection("rentland.onRentBegin.worldGuardFlags." + argRegionName);
                            if (cs == null) {
                                cs = getConfig().getConfigurationSection("rentland.onRentBegin.worldGuardFlags.default");
                            }
                            if (cs != null) {
                                worldGuardSetFlags(player, protectedRegion, cs);
                            }
                            
                            //change sign to indicate the region is rented
                            signDoStateAction(player, protectedRegion, signState.RENTED, "set");

                            //Save the region
                            try {
                                regionManager.save();
                            } catch (Exception exp) {
                            }

                            //Save the config files
                            rentSaveConfig();
                            rentReloadConfig();
                            
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
     * Return the rental price of a given region
     * 
     * @param argRegionName String name of the region of which to get the price
     * @param rentTimeFrame String name of the time frame to get the price
     * @return double price of the region
     */
    public Double rentGetRegionRentalPrice (String argRegionName, String rentTimeFrame) {
        double rentMultiplier       = 0;
                        
        //get the correct multiplier
        switch (rentTimeFrame.toLowerCase()) {
            case "second":  rentMultiplier = 1 / 2;             break;
            case "minute":  rentMultiplier = 1;                 break;
            case "hour":    rentMultiplier = 1 * 60;            break;
            case "day":     rentMultiplier = 1 * 60 * 24;       break;
            case "week":    rentMultiplier = 1 * 60 * 24 * 7;   break;
            default:        rentMultiplier = 1 * 60;            break;
        }

        //return the region price
        return rentGetRegionRentalPrice (argRegionName, 1.0, rentMultiplier);
    }
    /**
     * Return the rental price of a given region
     * 
     * @param argRegionName String name of the region of which to get the price
     * @return Double price of the region
     */
    public Double rentGetRegionRentalPrice (String argRegionName, double timeUnitsToAdd, double rentMultiplier) {
        //get the cost per minute
        Double regionPrice = rentGetConfig().getDouble("rent." + argRegionName + ".costpermin");
        //multiply it by the time units to add
        regionPrice *= timeUnitsToAdd;
        //multiply it by the rentMultiplier - this sets the proper timeframe
        regionPrice *= rentMultiplier;
        //return the region price
        return regionPrice;
    }
    /**
     * Return the purchase price of an rental region flag
     * 
     * @param String flagName of which to get a price
     * @return Double price of the rental region flag
     */
    public Double rentGetRegionFlagPurchasePrice (String flagName) {
        String regionFlagPrice = getConfig().getString("rentland.allowedWorldGuardFlags." + flagName);

        try {
            return Double.parseDouble(regionFlagPrice);
        } catch (NumberFormatException e) {
            return 0.0;
        }               
    }
    /**
     * Return the sell price of an rental region flag
     * 
     * @param String flagName of which to get a price
     * @return Double price of the rental region flag
     */
    public Double rentGetRegionFlagSellPrice (String flagName) {
        String regionFlagPrice = getConfig().getString("rentland.allowedWorldGuardFlags." + flagName);

        try {
            return Double.parseDouble(regionFlagPrice);
        } catch (NumberFormatException e) {
            return 0.0;
        }               
    }
    /**
     * This will update the regions rented count for a given player.
     * 
     * @param playerName String name of the player
     * @param rentingDifference int number of regions to increase or decrease.
     * @param earnedDifference double amount of currency the player earned with this update
     * @param spentDifference double amount of currency the player spent with this update
     */
    private void rentUpdateRegionCount(String playerName, int rentingDifference, double earnedDifference, double spentDifference) {
        //make sure it is lower case
        playerName = playerName.toLowerCase();
        //Make sure we are on the new format
        if (!rentDbGetConfig().isSet(playerName + ".renting")) {
            //save the current value
            int currentValue = rentDbGetConfig().getInt(playerName);
            //remove the current entry
            rentDbGetConfig().set(playerName, null);
            
            //convert to the new format since this path does not exist
            rentDbGetConfig().set(playerName + ".renting", currentValue);
            rentDbGetConfig().set(playerName + ".earned", 0.00);
            rentDbGetConfig().set(playerName + ".spent", 0.00);
            
        }
        
        //Record the new number of regions the player is renting
        int rentingAmount = rentDbGetConfig().getInt(playerName + ".renting") + rentingDifference;
        if (rentingAmount < 0) {
            rentingAmount = 0;
        }
        rentDbGetConfig().set(playerName + ".renting", rentingAmount);
        //Record the new amount earned by the player
        rentDbGetConfig().set(playerName + ".earned", rentDbGetConfig().getDouble(playerName + ".earned") + earnedDifference);
        //Record the new amount spent by the player
        rentDbGetConfig().set(playerName + ".spent", rentDbGetConfig().getDouble(playerName + ".spent") + spentDifference);
    }
    /**
     * A boolean value indicating if the player can rent another region.
     * 
     * @param player Player trying to rent a region
     * @return boolean true if they can rent another region, false otherwise
     */
    public boolean rentCanPlayerGetAnotherRegion(Player player) {
        String playerName = player.getName().toLowerCase();
        
        //Get the number of regions the player is already renting
        int currentNumberPlayerRentedRegions = rentDbGetConfig().getInt(playerName);
        if (currentNumberPlayerRentedRegions < 0) currentNumberPlayerRentedRegions = 0;
        //Get the maximum number of rentable regions
        int maxNumberOfPlayerRentedRegions = getConfig().getInt("rentland.onRentBegin.maxRegions");
        //See if the player can rent more land
        if (currentNumberPlayerRentedRegions + 1 <= maxNumberOfPlayerRentedRegions) {
            return true;
        }
        return false;
    }



    /**
     * Indicates if the passed in protectedRegion is a ownable region
     *  
     * @param protectedRegion ProtectedRegion that is in question
     * @return boolean true if it is an ownable region, false otherwise
     */
    protected boolean isOwnRegion(ProtectedRegion protectedRegion) {
        Boolean isRegionBuyable = protectedRegion.getFlag(DefaultFlag.BUYABLE);
        if (isRegionBuyable != null) {
            String argRegionName = protectedRegion.getId();
            if(!rentGetConfig().contains("rent." + argRegionName.toLowerCase() + ".rentable")) {
                return true;
            }        
        }
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
    protected boolean ownSellRegion(Player player, boolean fromAdmin, World world, String argRegionName) {
        //get the properly cased region name for use on the sign, etc
        argRegionName = argRegionName.toLowerCase();

        //Get the manager for the world
        RegionManager regionManager = getWorldGuard().getRegionManager(world);
        //Get the protected region
        ProtectedRegion protectedRegion = regionManager.getRegionExact(argRegionName);

        //make sure the region exists
        if (protectedRegion == null) {
            //Region does not exist.
            sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.general.error1")));
        } else {
            //make sure player owns region, or is initiated by an admin
            if (!(fromAdmin || protectedRegion.getOwners().toPlayersString().contains(player.getName().toLowerCase()))) {
                sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.sell.dontown")));
            } else {
                //see if the land is rentable 
                if (rentGetConfig().contains("rent." + argRegionName + ".rentable")) {
                    // can't sell a region while it is rentable
                    sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.rent.error1")));
                } else {
                    if (protectedRegion.getFlag(DefaultFlag.BUYABLE) == true) {
                        //is already for sale, do nothing
                        sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.sell.forsale")));
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
                        Double regionPrice = ownGetRegionSellPrice(protectedRegion);
        
                        //Sell it back
                        EconomyResponse economyResponse = BuyLand.econ.depositPlayer(ownerName, regionPrice);
        
                        //get the player to notify of the updated balance
                        Player notifyPerson = Bukkit.getPlayer(ownerName);
                        
                        if (notifyPerson != null) {
                            //Notify player of sale
                            sendMessageInfo(notifyPerson,
                                            String.format(ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.sell.back1"))
                                                          + regionPrice +
                                                          ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.sell.back2"))
                                                          ,
                                                          BuyLand.econ.format(economyResponse.balance)
                                                         )
                                           );
                        }
                        
                        //Remove player as owner of region
                        for (String owner : protectedRegion.getOwners().getPlayers()) {
                            //remove the player as owner
                            protectedRegion.getOwners().removePlayer(owner);

                            //Update the number of regions the player currently owns plus amount spent and earned
                            ownUpdateRegionCount(owner, -1, regionPrice, 0.00);
                        }
        
                        //Make sure there are no members of the region
                        for (String memberName : protectedRegion.getMembers().getPlayers()) { 
                            protectedRegion.getMembers().removePlayer(memberName);
                        }

                        //set the land priority
                        protectedRegion.setPriority(getConfig().getInt("general.regionPriority"));

                        //Set the land greeting message based on config
                        if (getConfig().getBoolean("buyland.onSaleToBank.greetMessage.display") == true) {
                            protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.sell.forsale")));
                        } else {
                            if (getConfig().getBoolean("buyland.onSaleToBank.greetMessage.erase") == true) {
                                protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, null);
                            }
                        }
        
                        //LWC - Remove protection from area based on config
                        if (getConfig().getBoolean("buyland.onSaleToBank.removelwcprotection") == true) {
                            LWCProtectionRemove(protectedRegionMinimum, protectedRegionMaximum);
                        }

                        //Reset the land to original when the land is sold based on config
                        if (getConfig().getBoolean("buyland.onSaleToBank.placeSchematic") == true) {
                            worldEditPlaceSchematic(protectedRegionMinimum, argRegionName);
                        }

                        //Save a schematic of the land region for restore based on config
                        if (getConfig().getBoolean("buyland.onSaleToBank.saveSchematic") == true) {
                            worldEditSaveSchematic(protectedRegionMinimum, protectedRegionMaximum, argRegionName, player);
                        }
        
                        //Protect land from entry based on config
                        if (getConfig().getBoolean("buyland.onSaleToBank.denyEntry") == true) {
                            protectedRegion.setFlag(DefaultFlag.ENTRY, State.DENY);
                        } else {
                            protectedRegion.setFlag(DefaultFlag.ENTRY, null);
                        }
        
                        //Set region flags per config
                        ConfigurationSection cs = getConfig().getConfigurationSection("buyland.onSaleToBank.worldGuardFlags." + argRegionName);
                        if (cs == null) {
                            cs = getConfig().getConfigurationSection("buyland.onSaleToBank.worldGuardFlags.default");
                        }
                        if (cs != null) {
                            worldGuardSetFlags(player, protectedRegion, cs);
                        }
                        
                        //Save the config files
                        saveCustomConfig();
                        reloadCustomConfig();
                        
                        //Change Sign to indicate it is for sale
                        signDoStateAction(player, protectedRegion, signState.FOR_SALE, "set");
                        
                        //Make the region buyable
                        protectedRegion.setFlag(DefaultFlag.BUYABLE, true);
        
                        //Notify the admin
                        broadcastMessageInfo(argRegionName + ": " + ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.admin.forsale")));
        
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
    protected boolean ownBuyRegion(Player player, World world, String argRegionName) {
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
            sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.general.error1")));
        } else {
            //Make sure it is not rentable
            if (rentGetConfig().contains("rent." + argRegionName + ".rentable")) {
                // can't buy a region while it is rentable
                sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.rent.error1")));
            } else {
                //Get the buyable flag for the region
                Boolean isRegionBuyable = protectedRegion.getFlag(DefaultFlag.BUYABLE);
                    if (isRegionBuyable == null) {
                        isRegionBuyable = false; 
                    }
    
                //See if the piece of land is for sale.
                if (!isRegionBuyable) {
                    sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.buy.dontown")));                                           
                } else {
                    //See if the player has rights to own another piece of land
                    if (!ownCanPlayerGetAnotherRegion(player)) {
                        sendMessageInfo(player, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.buy.max")));
                    } else {
                        //Get the cost of the region - do not trust price on the sign as it can possibly be changed
                        double regionPrice = ownGetRegionPurchasePrice(protectedRegion);
    
                        //Try to buy it with the players funds
                        EconomyResponse economyResponse = BuyLand.econ.withdrawPlayer(playerName, regionPrice);
    
                        if (!economyResponse.transactionSuccess()) {
                            //Purchase is unsuccessful
                            sendMessageInfo(player, String.format(ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.buy.cantafford")), economyResponse.errorMessage));
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
                            sendMessageInfo(player, String.format(ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.buy.bought")), 
                                                                  BuyLand.econ.format(economyResponse.amount), 
                                                                  BuyLand.econ.format(economyResponse.balance)));
                            
                            //set the land priority
                            protectedRegion.setPriority(getConfig().getInt("general.regionPriority"));

                            //set greeting message for region based on config
                            if (getConfig().getBoolean("buyland.onBuyFromBank.greetMessage.display") == true) {
                                protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, 
                                                        ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.buy.welcome1"))
                                                        + playerName + 
                                                        ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.buy.welcome2"))
                                                       );
                            } else {
                                if (getConfig().getBoolean("buyland.onBuyFromBank.greetMessage.erase") == true) {
                                    protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, null);
                                }
                            }

                            //LWC - Remove protection from area
                            if (getConfig().getBoolean("buyland.onBuyFromBank.removelwcprotection") == true) {
                                LWCProtectionRemove(protectedRegionMinimum, protectedRegionMaximum);
                            }

                            //Save a schematic of the land region for restore based on config
                            if (getConfig().getBoolean("buyland.onBuyFromBank.saveSchematic") == true) {
                                worldEditSaveSchematic(protectedRegionMinimum, protectedRegionMaximum, argRegionName, player);
                            }
                            
                            //Reset the land to original based on config
                            if (getConfig().getBoolean("buyland.onBuyFromBank.placeSchematic") == true) {
                                worldEditPlaceSchematic(protectedRegionMinimum, argRegionName);
                            }

                            //Deny entry based on config
                            if (getConfig().getBoolean("buyland.onBuyFromBank.denyEntry") == true) {
                                protectedRegion.setFlag(DefaultFlag.ENTRY, State.DENY);
                            } else {
                                protectedRegion.setFlag(DefaultFlag.ENTRY, null);
                            }
    
                            //Set region flags per config
                            ConfigurationSection cs = getConfig().getConfigurationSection("buyland.onBuyFromBank.worldGuardFlags." + argRegionName);
                            if (cs == null) {
                                cs = getConfig().getConfigurationSection("buyland.onBuyFromBank.worldGuardFlags.default");
                            }
                            if (cs != null) {
                                worldGuardSetFlags(player, protectedRegion, cs);
                            }
                            
                            //Update the number of regions the player currently owns plus amount spent and earned
                            ownUpdateRegionCount(playerName, +1, 0.00, regionPrice);
                           
                            saveCustomConfig();
    
                            //Set the owner of the land
                            DefaultDomain dd = new DefaultDomain();
                                          dd.addPlayer(playerName);
                            protectedRegion.setOwners(dd);
                            
                            //change sign to indicate the region is sold
                            signDoStateAction(player, protectedRegion, signState.OWNED, "set");
    
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
     * This will update the regions owned count for a given player.
     * 
     * @param playerName String name of the player
     * @param ownDifference int number of regions to increase or decrease.
     * @param earnedDifference double amount of currency the player earned with this update
     * @param spentDifference double amount of currency the player spent with this update
     */
    private void ownUpdateRegionCount(String playerName, int ownDifference, double earnedDifference, double spentDifference) {
        //make sure it is lower case
        playerName = playerName.toLowerCase();
        //Make sure we are on the new format
        if (!getCustomConfig().isSet(playerName + ".own")) {
            //save the current value
            int currentValue = getCustomConfig().getInt(playerName);
            //remove the current entry
            getCustomConfig().set(playerName, null);
            
            //convert to the new format since this path does not exist
            getCustomConfig().set(playerName + ".own", currentValue);
            getCustomConfig().set(playerName + ".earned", 0.00);
            getCustomConfig().set(playerName + ".spent", 0.00);
            
        }
        //Record the new number of regions the player owns
        int ownAmount = rentDbGetConfig().getInt(playerName + ".renting") + ownDifference;
        if (ownAmount < 0) {
            ownAmount = 0;
        }
        getCustomConfig().set(playerName + ".own", ownAmount);
        //Record the new amount earned by the player
        getCustomConfig().set(playerName + ".earned", getCustomConfig().getDouble(playerName + ".earned") + earnedDifference);
        //Record the new amount spent by the player
        getCustomConfig().set(playerName + ".spent", getCustomConfig().getDouble(playerName + ".spent") + spentDifference);
    }
    /**
     * Return the purchase price of a given region
     * 
     * @param protectedRegion ProtectedRegion of which to get the price
     * @return Double price of the region
     */
    public Double ownGetRegionPurchasePrice (ProtectedRegion protectedRegion) {
        Double regionPrice = protectedRegion.getFlag(DefaultFlag.PRICE);
        if (regionPrice == null) {
            if (this.getConfig().getBoolean("buyland.onBuyFromBank.price.usePerBlock") == true) {
                //get size of region
                int size = protectedRegion.volume();
                //  player.sendMessage("Area of blocks: " + size);
                double regionPricePerBlock = this.getConfig().getDouble("buyland.onBuyFromBank.price.perBlock");
                regionPrice = (double) (size * regionPricePerBlock);
                //player.sendMessage("regionPrice: " + regionPrice + " - regionPricePerBlock: " + regionPricePerBlock);
            } else {
                regionPrice = this.getConfig().getDouble("buyland.onBuyFromBank.price.default");
            }
        }
        return regionPrice;
    }
    /**
     * Return the sell price of a given region
     * 
     * @param protectedRegion ProtectedRegion of which to get the price
     * @return Double price of the region
     */
    public Double ownGetRegionSellPrice (ProtectedRegion protectedRegion) {
        Double regionPrice = ownGetRegionPurchasePrice (protectedRegion);
        //Adjust based on percent
        regionPrice *= getConfig().getDouble("buyland.onSaleToBank.price.percent"); 
        return regionPrice;
    }
    /**
     * Return the purchase price of an own region flag
     * 
     * @param String flagName of which to get a price
     * @return Double price of the region flag
     */
    public Double ownGetRegionFlagPurchasePrice (String flagName) {
        String regionFlagPrice = getConfig().getString("buyland.allowedWorldGuardFlags." + flagName);

        try {
            return Double.parseDouble(regionFlagPrice);
        } catch (NumberFormatException e) {
            return 0.0;
        }               
    }
    /**
     * Return the sell price of an own region flag
     * 
     * @param String flagName of which to get a price
     * @return Double price of the region flag
     */
    public Double ownGetRegionFlagSellPrice (String flagName) {
        String regionFlagPrice = getConfig().getString("buyland.allowedWorldGuardFlags." + flagName);

        try {
            return Double.parseDouble(regionFlagPrice);
        } catch (NumberFormatException e) {
            return 0.0;
        }               
    }
    /**
     * Return the maximum number of regions a player can own
     * 
     * @param player Player to check
     * @return The maximum number of regions a player can own
     */
     public int ownGetPlayerMaxNumberOfRegions(Player player) {
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
    public boolean ownCanPlayerGetAnotherRegion(Player player) {
        String playerName = player.getName().toLowerCase();
        
        //Make sure we are on the new format
        if (!getCustomConfig().isSet(playerName + ".own")) {
            //save the current value
            int currentValue = getCustomConfig().getInt(playerName);
            //remove the current entry
            getCustomConfig().set(playerName, null);
            
            //convert to the new format since this path does not exist
            getCustomConfig().set(playerName + ".own", currentValue);
            getCustomConfig().set(playerName + ".earned", 0.00);
            getCustomConfig().set(playerName + ".spent", 0.00);
            
        }

        int currentNumberPlayerOwnedRegions = getCustomConfig().getInt(playerName + ".own");
        if (currentNumberPlayerOwnedRegions < 0) currentNumberPlayerOwnedRegions = 0;
        
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
     * Do the initial hook into the permissions provider.
     * 
     * @return boolean True if provider exists, false otherwise. 
     */
    private boolean vaultSetupPermissions() {
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
    private boolean vaultSetupChat() {
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
    private boolean vaultSetupEconomy() {
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
     * This is to remove flags for a given region
     * 
     * @param protectedRegion ProtectedRegion from which to remove the flags
     * @param argFlagNames String[] list of flags to remove
     * @return boolean that indicates if flags were removed
     */
    public boolean worldGuardSetFlags(CommandSender sender, ProtectedRegion protectedRegion, ConfigurationSection csFlags) {
        //Ticket #41 - implement code to remove the passed in flags from a given region.
        //      call this when the region is sold, bought, rented, re-rented
        boolean flagsWereAdjusted = false;
        
        //com.sk89q.worldguard.protection.flags.DefaultFlag
        for (String flagName : csFlags.getKeys(true)) {
            //get the value of the flag name from the configuration section
            String flagValue = csFlags.getString(flagName).trim();
            
            //Null out the value if there is no characters to it
            if (flagValue.trim() == "") {
                flagValue = null;
            }
            
            //Handle each flag
            if (worldGuardSetFlag(sender, flagName, flagValue, protectedRegion)) {
                flagsWereAdjusted = true;
            }

            /*
            //Handle the State flags
            switch (flagName.toLowerCase()) {
                case "allowed-cmds": protectedRegion.setFlags(worldEditFlagList(sender, DefaultFlag.ALLOWED_CMDS, flagValue)); flagsWereAdjusted = true; break;  //List Flag
                case "blocked-cmds": protectedRegion.setFlags(worldEditFlagList(sender, DefaultFlag.BLOCKED_CMDS, flagValue)); flagsWereAdjusted = true; break;  //List Flag 
                case "build": protectedRegion.setFlag(DefaultFlag.BUILD, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
//Don't allow   case "buyable": protectedRegion.setFlag(DefaultFlag.BUYABLE, worldEditFlagBoolean(flagValue)); flagsWereAdjusted = true; break; 
                case "chest-access": protectedRegion.setFlag(DefaultFlag.CHEST_ACCESS, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "construct": protectedRegion.setFlags(worldEditFlagList(sender, DefaultFlag.CONSTRUCT, flagValue)); flagsWereAdjusted = true; break; //RegionGroupFlag
                case "creeper-explosion": protectedRegion.setFlag(DefaultFlag.CREEPER_EXPLOSION, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "deny-spawn": protectedRegion.setFlags(worldEditFlagList(sender, DefaultFlag.DENY_SPAWN, flagValue)); flagsWereAdjusted = true; break;  //List Flag
                case "enderdragon-block-damage": protectedRegion.setFlag(DefaultFlag.ENDERDRAGON_BLOCK_DAMAGE, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "enderman-grief": protectedRegion.setFlag(DefaultFlag.ENDER_BUILD, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "enderpearl": protectedRegion.setFlag(DefaultFlag.ENDERPEARL, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "entity-item-frame-destroy": protectedRegion.setFlag(DefaultFlag.ENTITY_ITEM_FRAME_DESTROY, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "entity-painting-destroy": protectedRegion.setFlag(DefaultFlag.ENTITY_PAINTING_DESTROY, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "entry": protectedRegion.setFlag(DefaultFlag.ENTRY, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                //case "entry group": break; //RegionGroupFlag
                case "exit": protectedRegion.setFlag(DefaultFlag.EXIT, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                //case "exit group": break; //RegionGroupFlag
                case "farewell": protectedRegion.setFlag(DefaultFlag.FAREWELL_MESSAGE, worldEditFlagString(flagValue)); flagsWereAdjusted = true; break; 
                case "feed-amount": protectedRegion.setFlag(DefaultFlag.FEED_AMOUNT, worldEditFlagInteger(flagValue)); flagsWereAdjusted = true; break; 
                case "feed-delay": protectedRegion.setFlag(DefaultFlag.FEED_DELAY, worldEditFlagInteger(flagValue)); flagsWereAdjusted = true; break; 
                case "feed-max-hunger": protectedRegion.setFlag(DefaultFlag.MAX_FOOD, worldEditFlagInteger(flagValue)); flagsWereAdjusted = true; break; 
                case "feed-min-hunger": protectedRegion.setFlag(DefaultFlag.MIN_FOOD, worldEditFlagInteger(flagValue)); flagsWereAdjusted = true; break; 
                case "fire-spread": protectedRegion.setFlag(DefaultFlag.FIRE_SPREAD, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "game-mode": protectedRegion.setFlag(DefaultFlag.GAME_MODE, worldEditFlagGameMode(flagValue)); flagsWereAdjusted = true; break; 
                case "ghast-fireball": protectedRegion.setFlag(DefaultFlag.GHAST_FIREBALL, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "grass-growth": protectedRegion.setFlag(DefaultFlag.GRASS_SPREAD, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "greeting": protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, worldEditFlagString(flagValue)); flagsWereAdjusted = true; break; 
                case "heal-amount": protectedRegion.setFlag(DefaultFlag.HEAL_AMOUNT, worldEditFlagInteger(flagValue)); flagsWereAdjusted = true; break; 
                case "heal-delay": protectedRegion.setFlag(DefaultFlag.HEAL_DELAY, worldEditFlagInteger(flagValue)); flagsWereAdjusted = true; break; 
                case "heal-max-health": protectedRegion.setFlag(DefaultFlag.MAX_HEAL, worldEditFlagDouble(flagValue)); flagsWereAdjusted = true; break; 
                case "heal-min-health": protectedRegion.setFlag(DefaultFlag.MIN_HEAL, worldEditFlagDouble(flagValue)); flagsWereAdjusted = true; break; 
                case "ice-form": protectedRegion.setFlag(DefaultFlag.ICE_FORM, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "ice-melt": protectedRegion.setFlag(DefaultFlag.ICE_MELT, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "invincible": protectedRegion.setFlag(DefaultFlag.INVINCIBILITY, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "lava-fire": protectedRegion.setFlag(DefaultFlag.LAVA_FIRE, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "lava-flow": protectedRegion.setFlag(DefaultFlag.LAVA_FLOW, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "leaf-decay": protectedRegion.setFlag(DefaultFlag.LEAF_DECAY, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "lighter": protectedRegion.setFlag(DefaultFlag.LIGHTER, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "lightning": protectedRegion.setFlag(DefaultFlag.LIGHTNING, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "mob-damage": protectedRegion.setFlag(DefaultFlag.MOB_DAMAGE, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "mob-spawning": protectedRegion.setFlag(DefaultFlag.MOB_SPAWNING, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "notify-enter": protectedRegion.setFlag(DefaultFlag.NOTIFY_ENTER, worldEditFlagBoolean(flagValue)); flagsWereAdjusted = true; break; 
                case "notify-leave": protectedRegion.setFlag(DefaultFlag.NOTIFY_LEAVE, worldEditFlagBoolean(flagValue)); flagsWereAdjusted = true; break; 
                case "passthrough": protectedRegion.setFlag(DefaultFlag.PASSTHROUGH, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "pistons": protectedRegion.setFlag(DefaultFlag.PISTONS, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "potion-splash": protectedRegion.setFlag(DefaultFlag.POTION_SPLASH, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
//Don't allow   case "price": protectedRegion.setFlag(DefaultFlag.PRICE, worldEditFlagDouble(flagValue)); flagsWereAdjusted = true; break; 
                case "pvp": protectedRegion.setFlag(DefaultFlag.PVP, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "receive-chat": protectedRegion.setFlag(DefaultFlag.RECEIVE_CHAT, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break; 
                case "send-chat": protectedRegion.setFlag(DefaultFlag.SEND_CHAT, worldEditFlagState(flagValue)); flagsWereAdjusted = true;  break; 
                case "sleep": protectedRegion.setFlag(DefaultFlag.SLEEP, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "snow-fall": protectedRegion.setFlag(DefaultFlag.SNOW_FALL, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "snow-melt": protectedRegion.setFlag(DefaultFlag.SNOW_MELT, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "spawn": protectedRegion.setFlag(DefaultFlag.SPAWN_LOC, worldEditFlagLocation(flagValue)); flagsWereAdjusted = true; break;
                case "teleport": protectedRegion.setFlag(DefaultFlag.TELE_LOC, worldEditFlagLocation(flagValue)); flagsWereAdjusted = true; break;
                case "tnt": protectedRegion.setFlag(DefaultFlag.TNT, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "use": protectedRegion.setFlag(DefaultFlag.USE, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "vehicle-destroy": protectedRegion.setFlag(DefaultFlag.DESTROY_VEHICLE, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "vehicle-place": protectedRegion.setFlag(DefaultFlag.PLACE_VEHICLE, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "water-flow": protectedRegion.setFlag(DefaultFlag.WATER_FLOW, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;

                //Undocumented flags on http://wiki.sk89q.com/wiki/WorldGuard/Regions/Flags
                case "enable-shop": protectedRegion.setFlag(DefaultFlag.ENABLE_SHOP, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "exp-drops": protectedRegion.setFlag(DefaultFlag.EXP_DROPS, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "item-drop": protectedRegion.setFlag(DefaultFlag.ITEM_DROP, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "mushrooms": protectedRegion.setFlag(DefaultFlag.MUSHROOMS, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "mycelium-spread": protectedRegion.setFlag(DefaultFlag.MYCELIUM_SPREAD, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "other-explosion": protectedRegion.setFlag(DefaultFlag.OTHER_EXPLOSION, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
                case "vine-growth": protectedRegion.setFlag(DefaultFlag.VINE_GROWTH, worldEditFlagState(flagValue)); flagsWereAdjusted = true; break;
            } 
            */
        }
                
        //return whether or not a flag was adjusted.
        return flagsWereAdjusted;
    }
    /**
     * Set a single WorldGuard flag for ProtectedRegion 
     * 
     * @param sender CommandSender is either the player or null of the one trying to set the command.
     * @param flagName String name of the flag to set
     * @param setting String setting value
     * @param protectedRegion ProtectedRegion to apply the flag
     * @return boolean Boolean true if the flag was successfully set or cleared.  false if the setting was invalid and flag was not adjusted. 
     */
    public boolean worldGuardSetFlag(CommandSender sender, String flagName, String setting, ProtectedRegion protectedRegion) {
        Flag<?> flag = DefaultFlag.fuzzyMatchFlag(flagName);
        if (flag != null) {
            //we have a valid flag... do something with it
            if (setting == null) {
                //remove the flag from worldguard
                protectedRegion.setFlag(flag, null);                
            } else if (setting.trim().isEmpty() || setting.equalsIgnoreCase(notSetFlagString)) {
                //remove the flag from worldguard
                protectedRegion.setFlag(flag, null);
            } else {
                //Try to set the flag since we have a value in setting variable

                //create a HashMap so we can use the more generic setFlags function
                HashMap<Flag<?>, Object> flags = (HashMap<Flag<?>, Object>) protectedRegion.getFlags();
                
                //interpret the setting to the correct object or null
                Object objectSetting = worldGuardSetFlagParseInput(sender, flag, setting);
                if (objectSetting != null) {
                    flags.put(flag, objectSetting);

                    //Set the flag
                    protectedRegion.setFlags(flags);
                } else {
                    return false;
                }
            }
            //return that we did something.
            return true;
        } else {
            //invalid flag name
            sendMessageWarning(null, "Invalid flag worldGuardFlags.[regionName/default]." + flagName);
        }
        //return that we did nothing
        return false;
    }
    private String notSetFlagString = "-Flag Not Set-";
    /**
     * parse the input of a WorldGuard flag
     * 
     * @param sender CommandSender a Player or null
     * @param flag Flag<?> flag of what we want to set
     * @param setting String representation of what we want to set
     * @return Boolean null if input was not valid, something else if valid 
     */
    private Object worldGuardSetFlagParseInput (CommandSender sender, Flag<?> flag, String setting) {
        //see if we have a StateFlag
        if (flag instanceof StateFlag) {
            //Process a StateFlag 
            Object bool = worldGuardSetFlagBoolean(flag.getName(), setting.trim());
            if (bool != null) {
                if ((Boolean)bool) {
                    return StateFlag.State.ALLOW;
                } else {
                   return StateFlag.State.DENY;
                }
            } else {
                //Notify console
                sendMessageWarning(null, "Invalid setting for worldGuardFlags.[regionName/default]." + flag.getName() + " Allowed values: allow/deny");
            }
        } else if (flag instanceof BooleanFlag) {
            //Process a boolean
            Object bool = worldGuardSetFlagBoolean(flag.getName(), setting.trim());
            if (bool != null) {
                return (Boolean)bool == true;
            } else {
                //Notify console
                sendMessageWarning(null, "Invalid setting for worldGuardFlags.[regionName/default]." + flag.getName() + " Allowed values: true/false");
            }
        } else if (flag instanceof LocationFlag) {
            //Process a location
            Object location =  worldGuardSetFlagLocation(setting.trim());
            if (location != null) {
                
            } else {
                //Notify console
                sendMessageWarning(null, "Invalid setting for worldGuardFlags.[regionName/default]." + flag.getName() + " Allowd Valid Location: world:x:y:z  Given: " + setting.trim());
            }
        } else {
            //Everything else is handled by worldguard
            try {
                return flag.parseInput(getWorldGuard(), sender, setting.trim());
            } catch (InvalidFlagFormat e) {
                //Notify console
                sendMessageWarning(null, "Invalid setting for worldGuardFlags.[regionName/default]." + flag.getName() + ". [WorldGuard] " + e.getMessage());
            }
        }
        
        //Nothing matched, so return null
        return null;
    }
    /**
     * Returns a Boolean if convertable or a null if empty string or if value is not convertable to Boolean
     * 
     * @param flagValue String representing a boolean or empty string
     * @return Boolean value or null
     */
    private Boolean worldGuardSetFlagBoolean(String flagName, String flagValue) {
        if (flagValue.equalsIgnoreCase("allow") || 
            flagValue.equalsIgnoreCase("allowed") ||
            flagValue.equalsIgnoreCase("yes") ||
            flagValue.equalsIgnoreCase("true") ||
            flagValue.equalsIgnoreCase("on")
           ) {
            return true; 
        } else if (flagValue.equalsIgnoreCase("deny") || 
                   flagValue.equalsIgnoreCase("denied") || 
                   flagValue.equalsIgnoreCase("no") || 
                   flagValue.equalsIgnoreCase("false") || 
                   flagValue.equalsIgnoreCase("off")
                  ) { 
            return false;
        } else {
            //we do not have a valid value
            return null;
        }
    }
    /**
     * Returns a LocationFlag representing a location within the world.
     * expects flagValue to be like worldName,x,y,Z
     * 
     * @param flagValue String representing a location
     * @return LocationFlag value or null
     */
    private com.sk89q.worldedit.Location worldGuardSetFlagLocation(String flagValue) {
        //try to return a converted value as a Location
        try {
            Location loc = locationFromString(flagValue);
            Vector vector = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            LocalWorld world = (LocalWorld) loc.getWorld();
            com.sk89q.worldedit.Location location = new com.sk89q.worldedit.Location(world, vector); 
            return location;
        } catch (Exception e) {
            return null;
        }
    }
    private String worldGuardGetFlagFullName(String partialFlagName) {
        if (partialFlagName.length() >= 15) {
            partialFlagName = partialFlagName.substring(0,15);
            //name is probably cut off, find the full name
            for (int i = 0; i < DefaultFlag.flagsList.length; i++) {
                String fullFlagName = DefaultFlag.flagsList[i].getName();
                if (fullFlagName.substring(0, 15).equalsIgnoreCase(partialFlagName)) {
                    return fullFlagName;
                }
            }
        }
        return partialFlagName;
    }
    private String worldGuardGetFlagCurrentValue(ProtectedRegion protectedRegion, String flagName) {
        return worldGuardGetFlagCurrentValue(protectedRegion, flagName, true);
    }
    private String worldGuardGetFlagCurrentValue(ProtectedRegion protectedRegion, String flagName, boolean returnOnlyOneValue) {
        Map<Flag<?>,Object> flags = protectedRegion.getFlags();
        for (Flag<?> key : flags.keySet()) {
            if (key.getName().equalsIgnoreCase(flagName)) {
                String returnValue = flags.get(key).toString();
                if (returnOnlyOneValue)
                    returnValue = returnValue.replace("[", "").replace("]", "").replace(",","").split(" ")[0];
                return returnValue;
            }
        }
        return notSetFlagString;
    }
    private String worldGuardGetFlagFullValue(String flagName, String partialFlagValue) {
        if (partialFlagValue.trim().isEmpty()) {
            return null; //since it is an empty string
        } else if (partialFlagValue.length() >= 14) {
            partialFlagValue = partialFlagValue.substring(0,14);
            Flag<?> argFlag = DefaultFlag.fuzzyMatchFlag(flagName);
            if (argFlag != null) {
                if (argFlag instanceof SetFlag && argFlag.getName().equals("deny-spawn")) {
                    for (int i = 0; i < EntityType.values().length; i++) {
                        String fullFlagValue = EntityType.values()[i].name();
                        if (fullFlagValue.substring(0, Math.min(fullFlagValue.length(), 14)).equalsIgnoreCase(partialFlagValue)) {
                            //matches, return it
                            return fullFlagValue;
                        }
                    }
                }                
            }            
        }
        
        //return what was passed in since it would not have been cut off
        return partialFlagValue;
    }
    private String worldGuardGetNextFlag(String currentFlagName, ConfigurationSection csAllowedFlags) {
        if (currentFlagName.isEmpty()) {
            //return the first DefaultFlag that is in the csAllowedFlags list
            for (int i = 0; i < DefaultFlag.flagsList.length; i++) {
                Flag<?> flag = DefaultFlag.flagsList[i];
                if (csAllowedFlags.contains(flag.getName())) {
                    if (flag instanceof StateFlag ||
                        flag instanceof BooleanFlag ||
                        (flag instanceof SetFlag && flag.getName().equals("deny-spawn")) ||
                        flag.getName().equals("game-mode")
                       ) {
                        return flag.getName();
                    }
                }
            }
        } else {
            //Find the index of the currentFlagName
            for (int i = 0; i < DefaultFlag.flagsList.length; i++) {
                if (DefaultFlag.flagsList[i].getName().equalsIgnoreCase(currentFlagName)) {
                    //we have a starting point, loop through the list to the end and then from the beginning back to itself
                    for (int idx = i+1; idx <= i + DefaultFlag.flagsList.length; idx++) {
                        Flag<?> flag = DefaultFlag.flagsList[idx % DefaultFlag.flagsList.length];
                        if (csAllowedFlags.contains(flag.getName())) {
                            if (flag instanceof StateFlag ||
                                flag instanceof BooleanFlag ||
                                (flag instanceof SetFlag && flag.getName().equals("deny-spawn")) ||
                                flag.getName().equals("game-mode")
                               ) {
                                return flag.getName();
                            }
                        }
                    }
                }
            }
        }
        //No valid flag found. Return nothing.
        return "";
    }
    private String worldGuardGetNextFlagValue(String flagName, String currentFlagValue) {
        //get the full flag name
        flagName = worldGuardGetFlagFullName(flagName);
        Flag<?> argFlag = DefaultFlag.fuzzyMatchFlag(flagName);
        if (argFlag != null) {
            if (argFlag instanceof StateFlag) {
                //see if it matches any but the last one in the list (NOTE: the last in the list would roll to the first one)
                for (int i = 0; i < StateFlag.State.values().length-1; i++) {
                    //see if it matches the currentFlagValue
                    if (currentFlagValue.equalsIgnoreCase(StateFlag.State.values()[i].name())) {
                        //return the next one in the list
                        return StateFlag.State.values()[i+1].name();
                    }
                }
                //Return the default (default one) 
                return StateFlag.State.values()[0].name();
            }
            if (argFlag instanceof BooleanFlag) {
                //Progression is TRUE, FALSE
                if (currentFlagValue.equalsIgnoreCase("TRUE"))
                    return "FALSE";
                else
                    return "TRUE";
            }
            if (argFlag instanceof SetFlag && argFlag.getName().equals("deny-spawn")) {
                //see if it matches any but the last one in the list (NOTE: the last in the list would roll to the first one)
                for (int i = 0; i < EntityType.values().length-1; i++) {
                    //extract the value
                    String testValue = EntityType.values()[i].name();
                    //see if it matches the currentFlagValue
                    // test that the currentFlagValue is the first part of the testValue because currentFlagValue may be cut off
                    if (currentFlagValue.length() >= 14) {
                        if (testValue.toLowerCase().startsWith(currentFlagValue.toLowerCase())) {
                            return EntityType.values()[i+1].name();
                        }
                    } else if (testValue.equalsIgnoreCase(currentFlagValue)){
                        return EntityType.values()[i+1].name();
                    }
                }
                //Return the default (default one)
                return EntityType.values()[0].name();
            }
            if (argFlag.getName().equals("game-mode")) {
                //see if it matches any but the last one in the list (NOTE: the last in the list would roll to the first one)
                for (int i = 0; i < org.bukkit.GameMode.values().length-1; i++) {
                    //extract the value
                    String testValue = org.bukkit.GameMode.values()[i].name();
                    //see if it matches the currentFlagValue
                    // test that the currentFlagValue is the first part of the testValue because currentFlagValue may be cut off
                    if (currentFlagValue.length() >= 14) {
                        if (testValue.toLowerCase().startsWith(currentFlagValue.toLowerCase())) {
                            return org.bukkit.GameMode.values()[i+1].name();
                        }
                    } else if (testValue.equalsIgnoreCase(currentFlagValue)){
                        return org.bukkit.GameMode.values()[i+1].name();
                    }
                }
                //Return the default (default one)
                return org.bukkit.GameMode.values()[0].name();
            }
        }
        return "";
    }
    private boolean worldGuardIsFlagValueSet(ProtectedRegion protectedRegion, String flagName, String flagValue) {
        //get the full flag name
        flagName = worldGuardGetFlagFullName(flagName);
        for(Flag<?> flag : protectedRegion.getFlags().keySet()) {
            if (flag.getName().equalsIgnoreCase(flagName)) {
                String testFlagValue = protectedRegion.getFlag(flag).toString();
                if (testFlagValue.equalsIgnoreCase(flagValue)) {
                    return true;
                } else {
                    testFlagValue = " " + testFlagValue.toLowerCase().replace("[", " ").replace("]", " ").replace(",", "");
                    if (testFlagValue.contains(" " + flagValue.toLowerCase() + " ")) {
                        return true;
                    }
                    //found the flag, but did not match the list
                    return false;
                }
            }
        }
        if (flagValue.equalsIgnoreCase(notSetFlagString))
            return true;
        else
            return false;
    }


    /**
     * Get WorldEdit Selection of the player
     * 
     * @param player Player that has the selection
     * @return Selection that the player had
     */
    public static Selection worldEditGetSelectionOfPlayer(Player player) {
        Plugin we = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if(we != null && we instanceof WorldEditPlugin) {
            return ((WorldEditPlugin) we).getSelection(player);
        }
        return null;
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
     * Adds a protected region to the manager
     * 
     * @param regionMinimum Location of the minimum block of the region
     * @param regionMaximum Location of the maximum block of the region
     * @param regionName String name of the region
     * @param action String indicating if this is for a "buy" or "rent" action
     * @param player Player of the one that is creating the region
     */
    public void protectedRegionAdd(Location regionMinimum, Location regionMaximum, String regionName, String action, Player player) {
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

            //set the land priority
            protectedRegion.setPriority(this.getConfig().getInt("general.regionPriority"));

            if (action == "buy") {
                //Make the region buyable if action is buy
                protectedRegion.setFlag(DefaultFlag.BUYABLE, true);
                
                //Set the land greeting message based on config
                if (getConfig().getBoolean("buyland.onCreate.greetMessage.display")) {
                    protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.sell.forsale")));
                } else if (this.getConfig().getBoolean("buyland.onCreate.greetMessage.erase") == true) {
                    protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, null);
                }
                
                //LWC - Remove protection from area based on config
                if (getConfig().getBoolean("buyland.onCreate.removelwcprotection") == true) {
                    LWCProtectionRemove(regionMinimum, regionMaximum);
                }

                //Save a schematic of the land region for restore based on config
                if (getConfig().getBoolean("buyland.onCreate.saveSchematic") == true) {
                    worldEditSaveSchematic(regionMinimum, regionMaximum, regionName, player);
                }

                //Deny entry based on config
                if (this.getConfig().getBoolean("buyland.onCreate.denyEntry") == true) {
                    protectedRegion.setFlag(DefaultFlag.ENTRY, State.DENY);
                } else {
                    protectedRegion.setFlag(DefaultFlag.ENTRY, null);
                }

                //Set region flags per config
                ConfigurationSection cs = getConfig().getConfigurationSection("buyland.onCreate.worldGuardFlags." + regionName);
                if (cs == null) {
                    cs = getConfig().getConfigurationSection("buyland.onCreate.worldGuardFlags.default");
                }
                if (cs != null) {
                    worldGuardSetFlags(player, protectedRegion, cs);
                }

            } else { // "rent"
                //Make the region non-buyable if action is rent
                protectedRegion.setFlag(DefaultFlag.BUYABLE, false);

                //Set the land greeting message based on config
                if (getConfig().getBoolean("rentland.onCreate.greetMessage.display")) {
                    protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, ChatColor.translateAlternateColorCodes('&', languageGetConfig().getString("buyland.rent.forrent")));
                } else if (getConfig().getBoolean("rentland.onCreate.greetMessage.erase") == true) {
                    protectedRegion.setFlag(DefaultFlag.GREET_MESSAGE, null);
                }

                //Save a schematic of the land region for restore based on config
                if (getConfig().getBoolean("rentland.onCreate.saveSchematic") == true) {
                    worldEditSaveSchematic(regionMinimum, regionMaximum, regionName, player);
                }

                //LWC - Remove protection from area based on config
                if (getConfig().getBoolean("rentland.onCreate.removelwcprotection") == true) {
                    LWCProtectionRemove(regionMinimum, regionMaximum);
                }

                //Set region flags per config
                ConfigurationSection cs = getConfig().getConfigurationSection("rentland.onCreate.worldGuardFlags." + regionName);
                if (cs == null) {
                    cs = getConfig().getConfigurationSection("rentland.onCreate.worldGuardFlags.default");
                }
                if (cs != null) {
                    worldGuardSetFlags(player, protectedRegion, cs);
                }

                //Deny entry based on config
                if (getConfig().getBoolean("rentland.onCreate.denyEntry") == true) {
                    protectedRegion.setFlag(DefaultFlag.ENTRY, State.DENY);
                } else {
                    protectedRegion.setFlag(DefaultFlag.ENTRY, null);
                }

                //Get the default cost to rent per minute
                double defaultcostpermin = getConfig().getDouble("rentland.onCreate.price.perMinDefault");
        
                //set the config values for the region name
                this.rentGetConfig().set("rent." + regionName + ".time", 0);
                this.rentGetConfig().set("rent." + regionName + ".rentable", true);
                this.rentGetConfig().set("rent." + regionName + ".world", world.getName());
                this.rentGetConfig().set("rent." + regionName + ".costpermin", defaultcostpermin);
        
                rentSaveConfig();
                rentReloadConfig();
            }

            sendMessageInfo(null, "Added region: " + regionName);
            
            try {
                regionManager.save();
            } catch (Exception exp) {
                
            }
        }
    }
    
    /**
     * Return the wording of how long the difference is between start and end
     * 
     * @param start long start time of which we are getting the difference
     * @param end long end time of which we are getting the difference
     * @return String containing the length of the difference
     */
    public static String elapsedTimeToString(long start, long end) {
        String auxRet= "";
        long timeDifference = end - start;
        long millisecondLength = 1;
        long secondLength = millisecondLength * 1000;
        long minuteLength = secondLength * 60;
        long hourLength = minuteLength * 60;
        long dayLength = hourLength * 24;
        long weekLength = dayLength * 7;
        
        //weeks
        if (timeDifference > weekLength) {
            //add number of weeks to output
            auxRet += (timeDifference / weekLength) + " weeks ";
            //reduce remaining time difference
            timeDifference = timeDifference % weekLength;
        }
        
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
    public Location locationFromString(String stringLocation) {
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
     * Convert a location to a string using specific separators
     * 
     * @param location Location that is to be converted
     * @param worldSeparator String separator between WorldName and X
     * @param coordinateSeparator String separator between X and Y and Z
     * @return String in the format of WorldName;X:Y:Z (if worldSeparator = ; and coordinateSeparator = :)
     */
    public String locationToString (Location location, String worldSeparator, String coordinateSeparator) {
        return location.getWorld().getName() + worldSeparator + 
               String.valueOf(location.getX()) + coordinateSeparator + 
               String.valueOf(location.getY()) + coordinateSeparator + 
               String.valueOf(location.getZ());
    }
    
    /**
     * remove one item from an array of strings 
     * @param args String[] list containing the arguments to adjust
     * @param itemToRemove int index of the item to remove
     * @return String[] that is 1 smaller in length than the original args and contains all but the requested index.
     */
     protected String[] arrayRemoveItem(String[] args, int itemToRemove) {
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
     * Sends a message of type warning to all players.
     * 
     * @param msg String message to send to the players.
     */
    protected void broadcastMessageWarning(String msg) {
        //Prefix the message with Buyland: since it is going to everyone 
        msg = ChatColor.RED + "BuyLand: " + ChatColor.DARK_RED + msg;

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