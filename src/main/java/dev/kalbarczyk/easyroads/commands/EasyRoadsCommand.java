package dev.kalbarczyk.easyroads.commands;

import dev.kalbarczyk.easyroads.EasyRoads;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/**
 * Handles the commands for the EasyRoads plugin.
 * This class implements {@link CommandExecutor} and processes player commands related to EasyRoads.
 */
public record EasyRoadsCommand(EasyRoads plugin) implements CommandExecutor {

    /**
     * Constructs an instance of EasyRoadsCommand with the given plugin.
     *
     * @param plugin the EasyRoads plugin instance
     */
    public EasyRoadsCommand {
    }

    /**
     * Executes the command sent by the player.
     *
     * @param sender       the sender of the command
     * @param cmd          the command that was executed
     * @param commandLabel the alias used to execute the command
     * @param args         the arguments passed to the command
     * @return true if the command was handled successfully, false otherwise
     */
    @Override
    public boolean onCommand(
            final @Nonnull CommandSender sender,
            final @Nonnull Command cmd,
            final @Nonnull String commandLabel,
            final @Nonnull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        // Check if no arguments or 'help' is requested
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage(plugin.getConfigState().messages().helpHeader());
            //check for permissions
            if (player.hasPermission("easyroads.reload")) {
                player.sendMessage(plugin.getConfigState().messages().helpReload());
            }

            if (player.hasPermission("easyroads.list")) {
                player.sendMessage(plugin.getConfigState().messages().helpList());
            }

            player.sendMessage(plugin.getConfigState().messages().helpHelp());

            return true;
        }

        // Handle the 'reload' command
        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("easyroads.reload")) {
                player.sendMessage(plugin.getConfigState().messages().noPermission());
                return true;
            }
            plugin.reloadConfig();
            plugin.reloadConfiguration();
            player.sendMessage(plugin.getConfigState().messages().reloadSuccess());
            return true;
        }


        // Handle the 'list' command
        if (args[0].equalsIgnoreCase("list")) {
            if (!player.hasPermission("easyroads.list")) {
                player.sendMessage(plugin.getConfigState().messages().noPermission());
                return true;
            }
            player.sendMessage(plugin.getConfigState().messages().listHeader());
            plugin.getConfigState().roads().forEach(road -> player.sendMessage(road.toString()));
            return true;
        }

        // If command not recognized, show invalid command message
        player.sendMessage(plugin.getConfigState().messages().invalidCommand());
        return true;
    }
}
