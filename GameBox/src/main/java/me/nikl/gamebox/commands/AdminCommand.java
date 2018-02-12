package me.nikl.gamebox.commands;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxLanguage;
import me.nikl.gamebox.Language;
import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.utility.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * @author Niklas Eicker
 *
 *
 */
public class AdminCommand implements CommandExecutor {
    private GameBox plugin;
    private GameBoxLanguage lang;

    private HashMap<String, HashMap<String, List<String>>> missingLanguageKeys;


    public AdminCommand(GameBox plugin){
        this.plugin = plugin;
        this.lang = plugin.lang;

        missingLanguageKeys = new HashMap<>();

        new BukkitRunnable(){
            @Override
            public void run() {
                GameBox.debug(" running late check in Admin command");
                checkLanguageFiles();
            }
        }.runTask(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission(Permission.ADMIN.getPermission())){
            sender.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
            return true;
        }

        if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            sendHelpMessages(sender);
            return true;
        }

        if(args[0].equalsIgnoreCase("givetoken") || args[0].equalsIgnoreCase("taketoken") || args[0].equalsIgnoreCase("settoken")){
            if(args.length != 3){
                sender.sendMessage(lang.PREFIX + " /gba [givetoken:taketoken:settoken] [player name] [count (integer)]");
                return true;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            if(player == null || !player.hasPlayedBefore()){
                sender.sendMessage(lang.PREFIX + ChatColor.RED + " can't find player " + args[1]);
                return true;
            }
            int count;
            try{
                count = Integer.parseInt(args[2]);
            } catch (NumberFormatException exception){
                sender.sendMessage(lang.PREFIX + ChatColor.RED + " last argument has to be an integer!");
                return true;
            }
            if(count < 0){
                sender.sendMessage(lang.PREFIX + ChatColor.RED + " the number can't be negative!");
                return true;
            }

            // all arguments are valid

            // handle cached online players
            GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
            if(gbPlayer != null && gbPlayer.isLoaded()){
                switch (args[0].toLowerCase()){
                    case "givetoken":
                        gbPlayer.setTokens(gbPlayer.getTokens() + count);
                        sender.sendMessage(lang.PREFIX + lang.CMD_GAVE_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(count)));
                        return true;

                    case "taketoken":
                        if(gbPlayer.getTokens() >= count){
                            gbPlayer.setTokens(gbPlayer.getTokens() - count);
                            sender.sendMessage(lang.PREFIX + lang.CMD_TOOK_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(count)));
                            return true;
                        } else {
                            sender.sendMessage(lang.PREFIX + lang.CMD_NOT_ENOUGH_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(gbPlayer.getTokens())));
                            return true;
                        }

                    case "settoken":
                        gbPlayer.setTokens(count);
                        sender.sendMessage(lang.PREFIX + lang.CMD_SET_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(count)));
                        return true;

                    default: // can't happen due to the check at the beginning of the command
                        return false;

                }
            }

            // handle offline or not cached players
            switch (args[0].toLowerCase()){
                case "givetoken":
                    plugin.getApi().giveToken(player, count);
                    sender.sendMessage(lang.PREFIX + lang.CMD_GAVE_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(count)));
                    return true;

                case "taketoken":
                    plugin.getApi().takeToken(player, count, new DataBase.Callback<Integer>() {
                        @Override
                        public void onSuccess(Integer done) {
                            sender.sendMessage(lang.PREFIX + lang.CMD_TOOK_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(count)));
                        }

                        @Override
                        public void onFailure(@Nullable Throwable throwable, @Nullable Integer value) {
                            if(value != null){
                                sender.sendMessage(lang.PREFIX + lang.CMD_NOT_ENOUGH_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(value)));
                            } else {
                                sender.sendMessage(lang.PREFIX + " Error...");
                                if(throwable != null){
                                    throwable.printStackTrace();
                                }
                            }
                        }
                    });
                    return true;

                case "settoken":
                    plugin.getDataBase().setToken(player.getUniqueId(), count);
                    sender.sendMessage(lang.PREFIX + lang.CMD_SET_TOKEN.replace("%player%", player.getName()).replace("%token%", String.valueOf(count)));
                    return true;

                default: // can't happen due to the check at the beginning of the command
                    return false;

            }
            // unreachable

        } // end of give/take/set token cmd
        else if(args[0].equalsIgnoreCase("token")){
            if(args.length != 2){
                sender.sendMessage(lang.PREFIX + " /gba token [player name]");
                return true;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            if(player == null || !player.hasPlayedBefore()){
                sender.sendMessage(lang.PREFIX + ChatColor.RED + " can't find player " + args[1]);
                return true;
            }

            // handle cached players
            cachedPlayer:
            if(player.isOnline()){
                GBPlayer gbPlayer = plugin.getPluginManager().getPlayer(player.getUniqueId());
                if(gbPlayer == null){
                    break cachedPlayer;
                }
                sender.sendMessage(lang.PREFIX + lang.CMD_TOKEN_INFO.replace("%player%", player.getName()).replace("%token%", String.valueOf(gbPlayer.getTokens())));
                return true;
            }

            plugin.getDataBase().getToken(player.getUniqueId(), new DataBase.Callback<Integer>() {
                @Override
                public void onSuccess(Integer done) {
                    sender.sendMessage(lang.PREFIX + lang.CMD_TOKEN_INFO
                            .replace("%player%", player.getName())
                            .replace("%token%", String.valueOf(done)));
                }

                @Override
                public void onFailure(@Nullable Throwable throwable, @Nullable Integer value) {
                    sender.sendMessage(lang.PREFIX + " Failed to get token for player: " + player.getName());
                    if(throwable != null) throwable.printStackTrace();
                }
            });
            return true;
        } else if(args[0].equalsIgnoreCase("reload")){
            if(plugin.reload()){
                sender.sendMessage(lang.PREFIX + lang.RELOAD_SUCCESS);
                return true;
            } else {
                sender.sendMessage(lang.PREFIX + lang.RELOAD_FAIL);
                Bukkit.getPluginManager().disablePlugin(plugin);
                return true;
            }
        } else if(args[0].equalsIgnoreCase("language")){
            // args length is min. 1
            if(args.length == 1){
                if(missingLanguageKeys.isEmpty()){
                    plugin.info(ChatColor.GREEN + " You have no missing messages in your language files :)");
                    return true;
                } else {
                    // ToDo: allow for cmd from op? (atm only printed to console)
                    printIncompleteLangFilesInfo();
                    return true;
                }
            } else if(args.length == 2){
                switch (args[1].toLowerCase()){
                    case "all":
                        printMissingKeys(sender);
                        return true;
                    default:
                        // ToDo print keys of only one specific file
                        sender.sendMessage(" Todo...");
                        return true;
                }
            }

            return true;
        } else if(args[0].equalsIgnoreCase("debug")){
            if(sender instanceof Player){
                return true;
            }
            GameBox.debug = !GameBox.debug;
            sender.sendMessage(lang.PREFIX + " Set debug mode to: " + GameBox.debug);
            return true;
        } else if(args[0].equalsIgnoreCase("resetHighScores")){
            plugin.getDataBase().resetHighScores();
        }
        sendHelpMessages(sender);
        return true;
    }

    public void printIncompleteLangFilesInfo() {
        // return if no keys are missing. This is only for calls from outside this class (e.g. from main class)
        if (missingLanguageKeys.isEmpty()) return;

        // print info about the incomplete files
        plugin.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
        plugin.info(ChatColor.BOLD + " There are missing keys in the following file(s):");
        plugin.info("");
        Iterator<String> iterator = missingLanguageKeys.keySet().iterator();
        String moduleID;
        while (iterator.hasNext()){
            moduleID = iterator.next();
            plugin.info(ChatColor.RED + "   -> " + ChatColor.RESET + plugin.getLanguage(moduleID).DEFAULT_PLAIN_NAME);
        }
        plugin.info("");
        plugin.info(" To get the specific missing keys of one file run ");
        plugin.info("      " + ChatColor.BLUE + "/gba language <>");
        plugin.info(" To get the specific missing keys of all files run ");
        plugin.info("      " + ChatColor.BLUE + "/gba language all");
        plugin.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - +");
    }

    private void sendHelpMessages(CommandSender sender) {
        plugin.info(ChatColor.BOLD.toString() + ChatColor.GOLD + " Change the number of tokens for online/offline players");
        plugin.info(ChatColor.DARK_GREEN + " /gba [givetoken:taketoken:settoken] [player name] [count (integer)]");
        plugin.info(ChatColor.BOLD.toString() + ChatColor.GOLD + " Get the number of tokens an online/offline player has");
        plugin.info(ChatColor.DARK_GREEN + " /gba [token] [player name]");
        plugin.info(ChatColor.BOLD.toString() + ChatColor.GOLD + " Reload Gamebox and all registered games");
        plugin.info(ChatColor.DARK_GREEN + " /gba reload");
        plugin.info(ChatColor.BOLD.toString() + ChatColor.GOLD + " Display information about your used language file");
        plugin.info(ChatColor.DARK_GREEN + " /gba language");
    }

    private void printMissingKeys(CommandSender sender){
        if(missingLanguageKeys.isEmpty()) return;

        plugin.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
        Iterator<String> iterator = missingLanguageKeys.keySet().iterator();
        String moduleID;
        while(iterator.hasNext()){
            moduleID = iterator.next();

            printMissingModuleKeys(sender, moduleID);

            if(iterator.hasNext()) {
                plugin.info(" ");
                plugin.info(" ");
            } else {
                plugin.info(ChatColor.RED + "+ - + - + - + - + - + - + - + - + - + - + - + - + - + - +");
            }
        }
    }

    private void printMissingModuleKeys(CommandSender sender, String moduleID) {
        HashMap<String, List<String>> currentKeys = missingLanguageKeys.get(moduleID);
        List<String> keys;
        plugin.info(" Missing from " + ChatColor.BLUE + plugin.getLanguage(moduleID).DEFAULT_PLAIN_NAME
                + ChatColor.RESET + " language file:");

        if(currentKeys.keySet().contains("string")){
            plugin.info(" ");
            plugin.info(ChatColor.BOLD + "   Strings:");
            keys = currentKeys.get("string");
            for(String key : keys){
                plugin.info(ChatColor.RED + "   -> " + ChatColor.RESET + key);
            }
        }

        if(currentKeys.keySet().contains("list")){
            plugin.info(" ");
            plugin.info(ChatColor.BOLD + "   Lists:");
            keys = currentKeys.get("list");
            for(String key : keys){
                plugin.info(ChatColor.RED + "   -> " + ChatColor.RESET + key);
            }
        }
    }


    private void checkLanguageFiles() {
		/*
		ToDo: check for missing keys in all used language files and add some support for creating
		Files that contain all customized messages and the default ones for previously unset keys
		*/
        HashMap<String, List<String>> currentKeys;
        for(String moduleID : plugin.getGameRegistry().getModuleIDs()){
            currentKeys = collectMissingKeys(moduleID);
            if(!currentKeys.isEmpty()){
                missingLanguageKeys.put(moduleID, currentKeys);
            }
        }
    }

    private HashMap<String, List<String>> collectMissingKeys(String moduleID){
        Language language = plugin.getLanguage(moduleID);

        List<String> missingStringKeys = language.findMissingStringMessages();
        List<String> missingListKeys = language.findMissingListMessages();

        HashMap<String, List<String>> toReturn = new HashMap<>();

        if(!missingListKeys.isEmpty()){
            toReturn.put("list", missingListKeys);
        }

        if(!missingStringKeys.isEmpty()){
            toReturn.put("string", missingStringKeys);
        }

        return toReturn;
    }

    public HashMap<String, HashMap<String, List<String>>> getMissingLanguageKeys(){
        return this.missingLanguageKeys;
    }

    // Todo (method in language)
    private void createDiffFile(String moduleID){

    }
}
