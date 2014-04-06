package ws.kristensen.buyland;

import org.bukkit.Location;
import org.bukkit.World;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import ws.kristensen.buyland.BuyLand;


import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class BlEventListenerSignChange extends JavaPlugin implements Listener  {
	public static BuyLand plugin;

	public BlEventListenerSignChange(BuyLand instance) {
		plugin = instance;
	}
	
    public boolean isSignWithinRegion(Location signLocation, Location protectedRegionMinimum, Location protectedRegionMaximum) {
        if (protectedRegionMinimum.getX() < signLocation.getX() && signLocation.getX() < protectedRegionMaximum.getX()) {
            if (protectedRegionMinimum.getY() < signLocation.getY() && signLocation.getY() < protectedRegionMaximum.getY()) {
                if (protectedRegionMinimum.getZ() < signLocation.getZ() && signLocation.getZ() < protectedRegionMaximum.getZ()) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
	public void onSignChange(SignChangeEvent event) {
        if (plugin.signIsValidHeader(event.getLines())) { 
        //if (event.getLine(0).contains("[BuyLand]") || event.getLine(0).equalsIgnoreCase("[BuyLand]")) {
           //Get the player making the change
           Player player = event.getPlayer();
           //get the world involved
           World world = player.getWorld();

           //get the sign location
           Location signLocation = event.getBlock().getLocation();

           //Get the region manager for the world
           RegionManager regionManager = plugin.getWorldGuard().getRegionManager(world);

           //Get the region name on the sign
           String regionName = event.getLine(1).toLowerCase();

           //Try to get the protected region indicated
           ProtectedRegion protectedRegion = regionManager.getRegionExact(regionName);
           
           //Make sure the region exists
           if (protectedRegion == null) {
               //Region does not exist.
               event.setLine(0, "ERROR!");
               event.setLine(1, "Invalid Region");
               event.setLine(2, "");
               event.setLine(3, "");
           } else {
               //See if player is the owner, a member, or is an admin
               if (protectedRegion.getOwners().contains(player.getName()) ||
                   protectedRegion.getMembers().contains(player.getName()) ||
                   player.hasPermission("buyland.signcreate") || 
                   player.hasPermission("buyland.admin") || 
                   player.hasPermission("buyland.all")
                  ) {
                   //try to create the sign
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
                   //See if the sign is within the region
                   if (isSignWithinRegion(signLocation, protectedRegionMinimum, protectedRegionMaximum)) { 
                       //Deny placing a sign within its own region!
                       event.setLine(0, "ERROR!");
                       event.setLine(1, "Place sign");
                       event.setLine(2, "outside region");
                       event.setLine(3, "");
                       plugin.sendMessageWarning(player, "Placing a sign inside its own region will cause errors!");
                       plugin.sendMessageWarning(player, "Please place it outside of the region!");
                   } else {
                       if (plugin.signRegister(world, regionName, signLocation)) {
                           //registration successful, now update it to the proper state
                           if (plugin.signDoStateAction(player, protectedRegion, plugin.signGetState(signLocation), "set")) {
                               Sign tempSign = (Sign) plugin.signGetLocation(protectedRegion.getId()).getBlock().getState();
                               event.setLine(0, tempSign.getLine(0));
                               event.setLine(1, tempSign.getLine(1));
                               event.setLine(2, tempSign.getLine(2));
                               event.setLine(3, tempSign.getLine(3));
                           }
                       } else {
                           event.setLine(0, "ERROR!");
                           event.setLine(1, "Sign");
                           event.setLine(2, "Register");
                           event.setLine(3, "Error");
                           plugin.sendMessageWarning(player, "Validation Error in recording Buyland sign.");
                       }
                   }
               } else {
                   //Change the sign to indicate the player has no permission to make a Buyland sign
                   plugin.sendMessageWarning(player, "You Do Not Have Permission To Create A BuyLand Sign!");
                   event.setLine(0, "ERROR!");
                   event.setLine(1, "No permission");
                   event.setLine(2, "to create a");
                   event.setLine(3, "buyland sign!");
               }
           }
       }
	}
}
