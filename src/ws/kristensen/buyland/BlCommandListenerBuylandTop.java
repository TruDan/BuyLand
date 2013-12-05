package ws.kristensen.buyland;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Handles the subcommand:<br/>
 *      /buyland top [Owners/Renters/CashSpent] (Player Qty)
 * <hr/>
 * Show to top players<br/>
 * <br/> 
 * 
 */
public class BlCommandListenerBuylandTop implements CommandExecutor {
    private final BuyLand plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin BuyLand class so we can point back to the base class at protected functions.
     */
    public BlCommandListenerBuylandTop(BuyLand plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length >= 1) {
            plugin.sendMessageWarning(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.general.parameters")));
            plugin.sendMessageInfo(sender, "Usage: /buyland top [Owners/Renters/CashSpent] (Player Qty)");
        } else {
            //Extract the passed arguments
            String argType = args[0].toLowerCase();
            Integer argQty = 10;

            if (args.length == 2)
                argQty = Integer.valueOf(args[1]);

            //See if the person requesting the information is a player
            if (sender instanceof Player) {
                Player player = (Player)sender;

                //See if the player has permission to do the command
                if (player.hasPermission("buyland.top") || player.hasPermission("buyland.all")) {
                    if (argType.equalsIgnoreCase("owners")) {
                        FileConfiguration configOwn = plugin.customGetConfig();
                        HashMap<String, Integer> rankOwn = new HashMap<String, Integer>();
                        for (String key : configOwn.getKeys(true)) {
                            if (!key.substring(0, 4).equalsIgnoreCase("user") && key.contains(".own")) rankOwn.put(key.substring(0, key.length()-4), configOwn.getInt(key));
                        }
                        LinkedHashMap<String, Integer> result = sortHashMapByInteger(rankOwn);
                        plugin.sendMessageInfo(sender, "Top " + argQty + " region owners:");
                        for (String key : result.keySet()) {
                            if (argQty-- == 0)
                                break;
                            plugin.sendMessageInfo(sender, key + " - " + String.valueOf(result.get(key)) + " regions");
                        }
                    } else if (argType.equalsIgnoreCase("renters")) {
                        FileConfiguration configRent = plugin.rentDbGetConfig();
                        HashMap<String, Integer> rankRent = new HashMap<String, Integer>();
                        for (String key : configRent.getKeys(true)) {
                            if (!key.substring(0, 4).equalsIgnoreCase("user") && key.contains(".renting")) rankRent.put(key.substring(0, key.length()-8), configRent.getInt(key));
                        }                        
                        LinkedHashMap<String, Integer> result = sortHashMapByInteger(rankRent);
                        plugin.sendMessageInfo(sender, "Top " + argQty + " region renters:");
                        for (String key : result.keySet()) {
                            if (argQty-- == 0)
                                break;
                            plugin.sendMessageInfo(sender, key + " - " + String.valueOf(result.get(key)) + " regions");
                        }
                    } else if (argType.equalsIgnoreCase("cashspent")) {
                        FileConfiguration configOwn = plugin.customGetConfig();
                        FileConfiguration configRent = plugin.rentDbGetConfig();
                        HashMap<String, Double>  rankSpent = new HashMap<String, Double>();
                        for (String key : configOwn.getKeys(true)) {
                            if (!key.substring(0, 4).equalsIgnoreCase("user") && key.contains(".spent")) rankSpent.put(key.substring(0, key.length()-6), configOwn.getDouble(key));
                        }
                        for (String key : configRent.getKeys(true)) {
                            if (!key.substring(0, 4).equalsIgnoreCase("user") && key.contains(".spent")) rankSpent.put(key.substring(0, key.length()-6), configRent.getDouble(key));
                        }

                        LinkedHashMap<String, Double> result = sortHashMapByDouble(rankSpent);
                        plugin.sendMessageInfo(sender, "Top " + argQty + " spenders on regions:");
                        for (String key : result.keySet()) {
                            if (argQty-- == 0)
                                break;
                            plugin.sendMessageInfo(sender, key + " - " + String.valueOf(Math.floor(result.get(key) * 100.0) / 100.0));
                        }
                    }
                } else {
                    plugin.sendMessageInfo(sender, ChatColor.translateAlternateColorCodes('&', plugin.languageGetConfig().getString("buyland.buy.permission")));
                }
            } else {
                plugin.sendMessageInfo(sender, "Currently not available at console.");
            }            
        }

        //command was utilized.
        return true;
    }
    
    private LinkedHashMap<String, Double> sortHashMapByDouble(HashMap<String, Double> passedMap) {
        List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<Double>(passedMap.values());
        Collections.sort(mapValues, BlCommandListenerBuylandTop.DoubleDescending);
        Collections.sort(mapKeys);

        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();

        Iterator<Double> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)){
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((String)key, (Double)val);
                    break;
                }
            }
        }
        return sortedMap;
    }
    private LinkedHashMap<String, Integer> sortHashMapByInteger(HashMap<String, Integer> passedMap) {
        List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
        List<Integer> mapValues = new ArrayList<Integer>(passedMap.values());
        Collections.sort(mapValues, BlCommandListenerBuylandTop.IntegerDescending);
        Collections.sort(mapKeys);

        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();

        Iterator<Integer> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)){
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((String)key, (Integer)val);
                    break;
                }
            }
        }
        return sortedMap;
    }
    public static Comparator<Integer>IntegerDescending = new Comparator<Integer>() {
        public int compare(Integer b1,Integer b2) {
            return -1 * (b1 - b2);
        }
    };
    public static Comparator<Double>DoubleDescending = new Comparator<Double>() {
        public int compare(Double b1,Double b2) {
            return (int) (-1 * (b1 - b2));
        }
    };
}
