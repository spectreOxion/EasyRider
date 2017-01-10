package nu.nerd.easyrider.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import nu.nerd.easyrider.EasyRider;
import nu.nerd.easyrider.Util;
import nu.nerd.easyrider.db.SavedHorse;

// ----------------------------------------------------------------------------
/**
 * Executor for the /horse-tp command.
 */
public class HorseTPExecutor extends ExecutorBase {
    // ------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public HorseTPExecutor() {
        super("horse-tp", "help");
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender,
     *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            return false;
        }
        if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "This command expects a horse's UUID as its only argument.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be in game to use this command.");
            return true;
        }

        Player sendingPlayer = (Player) sender;
        String uuidPrefix = args[0];
        List<SavedHorse> horses = EasyRider.DB.findHorsesByUUID(uuidPrefix);
        if (horses.size() == 0) {
            sender.sendMessage(ChatColor.RED + "The UUID prefix " + uuidPrefix + " doesn't match any horses.");
        } else if (horses.size() > 1) {
            sender.sendMessage(ChatColor.RED + "The UUID prefix " + uuidPrefix + " matches more than one horse.");
        } else {
            SavedHorse savedHorse = horses.get(0);
            Location loc = savedHorse.getLocation();

            long start = System.nanoTime();
            Horse horse = Util.findHorse(savedHorse.getUuid(), loc, 2);
            if (EasyRider.CONFIG.DEBUG_FINDS) {
                EasyRider.PLUGIN.getLogger().info("findHorse() took " + (System.nanoTime() - start) * 0.001 + " microseconds.");
            }

            if (horse != null) {
                savedHorse.observe(horse);
                tpToHorse(sendingPlayer, horse);
            } else {
                if (loc == null) {
                    sender.sendMessage(ChatColor.GOLD + "The horse cannot be found and has no known last location.");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "The horse doesn't appear to be loaded.");
                    sender.sendMessage(ChatColor.GOLD + "Teleporting you to " + Util.formatLocation(loc) + ", " +
                                       savedHorse.getUuid().toString() + "'s last known location.");
                    sendingPlayer.teleport(loc);
                }
            }
        }
        return true;
    }

    // ------------------------------------------------------------------------
    /**
     * Teleport the player to a found Horse.
     * 
     * @param player the player.
     * @param horse the Horse.
     */
    protected void tpToHorse(Player player, Horse horse) {
        Location loc = horse.getLocation();
        player.sendMessage(ChatColor.GOLD +
                           "Teleporting you to " + horse.getUniqueId().toString() +
                           " at " + Util.formatLocation(loc) + ".");
        player.teleport(loc);
    }
} // class HorseTPExecutor