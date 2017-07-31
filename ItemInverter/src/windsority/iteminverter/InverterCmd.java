package windsority.iteminverter;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InverterCmd implements CommandExecutor{
	
	final String arg1Message = "Usage: /inverter <add|remove|list|detail|enable|disable>";
	final String cmdArg1[] = {"add", "remove", "list", "detail", "enable", "disable"};
	final String correctForm[] = {"Usage: /inverter add [Id] [formula]", "Usage: /inverter remove [formula]",
								  "Usage: /inverter list", "Usage: /inverter detail [formula]",
								  "Usage: /inverter enable [player] [formula]",
								  "Usage: /inverter disable [player] [formula]"};
	final InverterMain plugin;
	
	public InverterCmd(InverterMain plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (args.length == 0) {
			sender.sendMessage(arg1Message);
			return true;
		} else if (args.length > 3) {
			return false;
		}
		
		if (args.length == 1) {   
			//correct form: /inverter list
			if (args[0].equalsIgnoreCase("list")) {	
				showList(sender);
				return true;	
			}
			
			//check if user means other argument
			if (!checkArg1(args[0], sender)) sender.sendMessage(arg1Message);
			return true;
			
		} else if (args.length == 2) {
			//correct form: 1. /inverter detail [formula]
			if (args[0].equalsIgnoreCase("detail")) {	
				if (!showDetail(args[1], sender))
					sender.sendMessage("Formula doesn't exist!");
				return true;	
			}
			
			//correct form: 2. /inverter remove [formula]
			if (args[0].equalsIgnoreCase("remove")) {	
				if (removeFormula(args[1]))
					sender.sendMessage("remove succeed!");
				else sender.sendMessage("Formula doesn't exist！");
				return true;	
			}
			
			if (!checkArg1(args[0], sender)) sender.sendMessage(arg1Message);
			return true;
			
		} else if (args.length == 3) {
			//correct form: 1. /inverter add [Id] [formula]
			if (args[0].equalsIgnoreCase("add")) {
				if (plugin.getConfig().contains("formula." + args[2])) {
					sender.sendMessage("Formula already exists!");
					return true;
				}
				
				//this commands can only be triggered by players
				if (!(sender instanceof Player)) {
					sender.sendMessage("You have no hands"); 
					return true;
				}
				
				Player player = (Player) sender;
				ItemStack item = player.getInventory().getItemInMainHand();
								
				if (item.getType().equals(Material.AIR)) {
					sender.sendMessage("You are not setting anything!");
					return true;
				}
				
				if (!isNumeric(args[1])) sender.sendMessage("Id must be a number！");
				int Id = Integer.valueOf(args[1]);
				if (Id > 452 || Id < 1) {
					sender.sendMessage("Id must be in range[1~452]！");
					return true;
				}
				
				if (addFormula(args[2], item, Id))
					sender.sendMessage("Addition succeed!");
				else sender.sendMessage("Formula name wrong!");
				return true;
			}
			
			//correct form: 2. /inverter enable [player] [formula]
			if (args[0].equalsIgnoreCase("enable")) {
				if (!enablePlayer(args[1].toLowerCase(), args[2]))
					sender.sendMessage("Formula doesn't exist!");
				else sender.sendMessage(ChatColor.ITALIC + "Enable " + args[1] + " formula " + args[2] + "!");
				return true;
			}
				
			//correct form: 3. /inverter disable [player] [formula]
			if (args[0].equalsIgnoreCase("disable")) {
				if (!disablePlayer(args[1].toLowerCase(), args[2]))
					sender.sendMessage("Formula doesn't exist!");
				else sender.sendMessage(ChatColor.ITALIC + args[1] + " formula " + args[2] + " disabled!");
				return true;
			}
			
			if (!checkArg1(args[0], sender)) sender.sendMessage(arg1Message);
			return true;
		}
		
		return true;
	}
	
	private final boolean checkArg1(String arg1, CommandSender sender) {
		for (int idx = 0; idx < 6; ++idx)
			if (arg1.equalsIgnoreCase(cmdArg1[idx])) {	
				sender.sendMessage(correctForm[idx]); 
				return true;	
			}
		return false;
	}
	
	public boolean addFormula(String formulaName, ItemStack item, int targetId) {
		if (formulaName.matches(".")) return false;
		plugin.getConfig().set("formula." + formulaName + ".targetId", targetId);
		plugin.getConfig().set("formula." + formulaName + ".item", item);
		plugin.saveConfig();
		return true;
	}
	
	public boolean removeFormula(String formulaName) {
		if (!plugin.getConfig().contains("formula." + formulaName))
			return false;
		plugin.getConfig().set("formula." + formulaName, null);
		ConfigurationSection usingSection = plugin.getConfig().getConfigurationSection("using");
		Set<String> players = usingSection.getKeys(false);
		for (String player : players) {
			String path = player + "." + formulaName;
			if (usingSection.contains(path))
				usingSection.set(path, null);
		}
		plugin.saveConfig();
		return true;
	}
	
	public void showList(CommandSender sender) {
		sender.sendMessage("-------formula-------");
		Set<String> formulas =  
				plugin.getConfig().getConfigurationSection("formula").getKeys(false);
		if (formulas.size() == 0) sender.sendMessage("none");
		else for (String formula : formulas) sender.sendMessage(formula);
	}
	
	public boolean showDetail(String formulaName, CommandSender sender) {
		String path = "formula." + formulaName;
		if (!plugin.getConfig().contains(path)) return false;
		
		ItemStack sourceItem = plugin.getConfig().getItemStack(path + ".item");
		String sourceItemName = sourceItem.getType().name();
		int targetId = plugin.getConfig().getInt(path + ".targetId");
		
		@SuppressWarnings("deprecation")
		String tarItemName = Material.getMaterial(targetId).name();
		
		sender.sendMessage("formula" + formulaName + ":");
		String message = "[" + sourceItemName + "] --convert to--> " + "[Id " + 
						 Integer.valueOf(targetId).toString() +  ": " + tarItemName + "]";
		sender.sendMessage(message);
		return true;
	}
	
	public boolean enablePlayer(String playerName, String formulaName) {
		if (!plugin.getConfig().contains("formula." + formulaName))
			return false;
		ConfigurationSection formulaSection = plugin.getConfig().getConfigurationSection("formula");
		ConfigurationSection usingSection = plugin.getConfig().getConfigurationSection("using");
		Set<String> formulae = formulaSection.getKeys(false);
		ItemStack sourceItem = formulaSection.getItemStack(formulaName + ".item");
		for (String formula : formulae) {
			if (formula == formulaName) continue;
			ItemStack currentItem = formulaSection.getItemStack(formula + ".item");
			if (currentItem.equals(sourceItem)) {
				if (!usingSection.contains(playerName + "." + formula)) break;
				usingSection.set(playerName + "." + formula, false);
			}
		}
		usingSection.set(playerName + "." + formulaName, true);
		plugin.saveConfig();
		return true;
	}
	
	public boolean disablePlayer(String playerName, String formulaName) {
		if (!plugin.getConfig().contains("formula." + formulaName))
			return false;
		String path = "using." + playerName + "." + formulaName;
		if (!plugin.getConfig().contains(path)) return true;
		plugin.getConfig().set(path, false);
		return true;
	}
	
	public boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if(!isNum.matches())
            return false;
        return true;
 }
	
}
