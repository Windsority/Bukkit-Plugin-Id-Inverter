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
	final String correctForm[] = {"Usage: /inverter add [Id] [公式名]", "Usage: /inverter remove [公式名]",
								  "Usage: /inverter list", "Usage: /inverter detail [公式名]",
								  "Usage: /inverter enable [玩家] [公式名]",
								  "Usage: /inverter disable [玩家] [公式名]"};
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
					sender.sendMessage("公式不存在！");
				return true;	
			}
			
			//correct form: 2. /inverter remove [formula]
			if (args[0].equalsIgnoreCase("remove")) {	
				if (removeFormula(args[1]))
					sender.sendMessage("清除成功！");
				else sender.sendMessage("公式不存在！");
				return true;	
			}
			
			if (!checkArg1(args[0], sender)) sender.sendMessage(arg1Message);
			return true;
			
		} else if (args.length == 3) {
			//correct form: 1. /inverter add [Id] [formula]
			if (args[0].equalsIgnoreCase("add")) {
				if (plugin.getConfig().contains("formula." + args[2])) {
					sender.sendMessage("此公式已存在！");
					return true;
				}
				
				//this commands can only be triggered by players
				if (!(sender instanceof Player)) {
					sender.sendMessage("你没有手!"); 
					return true;
				}
				
				Player player = (Player) sender;
				ItemStack item = player.getInventory().getItemInMainHand();
								
				if (item.getType().equals(Material.AIR)) {
					sender.sendMessage("你什么物品都没有指定！");
					return true;
				}
				
				if (!isNumeric(args[1])) sender.sendMessage("Id必须是数字！");
				int Id = Integer.valueOf(args[1]);
				if (Id > 452 || Id < 1) {
					sender.sendMessage("Id必须在[1~452]范围内！");
					return true;
				}
				
				if (addFormula(args[2], item, Id))
					sender.sendMessage("添加成功！");
				else sender.sendMessage("公式名非法！");
				
				return true;
			}
			
			//correct form: 2. /inverter enable [player] [formula]
			if (args[0].equalsIgnoreCase("enable")) {
				if (!enablePlayer(args[1].toLowerCase(), args[2]))
					sender.sendMessage("没有这条公式!");
				else sender.sendMessage(ChatColor.ITALIC + "已为" + args[1] + "启用公式" + args[2] + "!");
				return true;
			}
				
			//correct form: 3. /inverter disable [player] [formula]
			if (args[0].equalsIgnoreCase("disable")) {
				if (!disablePlayer(args[1].toLowerCase(), args[2]))
					sender.sendMessage("没有这条公式!");
				else sender.sendMessage(ChatColor.ITALIC + args[1] + "公式" + args[2] + "已停用！");
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
		@SuppressWarnings("deprecation")
		int sourceId = item.getType().getId();
		if (item.getItemMeta().hasDisplayName())
			plugin.getConfig().set("formula." + formulaName + ".itemName", item.getItemMeta().getDisplayName());
		else plugin.getConfig().set("formula." + formulaName + ".itemName", item.getType().name());
		plugin.getConfig().set("formula." + formulaName + ".sourceId", sourceId);
		plugin.getConfig().set("formula." + formulaName + ".targetId", targetId);
		plugin.saveConfig();
		return true;
	}
	
	public boolean removeFormula(String formulaName) {
		if (!plugin.getConfig().contains("formula." + formulaName))
			return false;

		ConfigurationSection usingSection = plugin.getConfig().getConfigurationSection("using");
		Set<String> players = usingSection.getKeys(false);
		for (String player : players) {
			String path = player + "." + formulaName;
			if (usingSection.contains(path)) {
				String command = "inverter disable " + player + " " + formulaName;
				System.out.println(command);
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
				usingSection.set(path, null);
			}
		}
		plugin.getConfig().set("formula." + formulaName, null);
		plugin.saveConfig();
		return true;
	}
	
	public void showList(CommandSender sender) {
		sender.sendMessage("-------公式-------");
		Set<String> formulas =  
				plugin.getConfig().getConfigurationSection("formula").getKeys(false);
		if (formulas.size() == 0) sender.sendMessage("无");
		else for (String formula : formulas) sender.sendMessage(formula);
	}
	
	public boolean showDetail(String formulaName, CommandSender sender) {
		String path = "formula." + formulaName;
		if (!plugin.getConfig().contains(path)) return false;
		
		int sourceId = plugin.getConfig().getInt(path + ".sourceId");
		String ItemName = plugin.getConfig().getString(path + ".itemName");
		int targetId = plugin.getConfig().getInt(path + ".targetId");
				
		sender.sendMessage("公式" + formulaName + ":");
		String message = "[Id " + Integer.valueOf(sourceId).toString() + ": " + ItemName + 
						 "] --转变为--> " + "[Id " + Integer.valueOf(targetId).toString() +  
						 ": " + ItemName + "]";
		sender.sendMessage(message);
		return true;
	}
	
	public boolean enablePlayer(String playerName, String formulaName) {
		if (!plugin.getConfig().contains("formula." + formulaName))
			return false;
		ConfigurationSection formulaSection = plugin.getConfig().getConfigurationSection("formula");
		ConfigurationSection usingSection = plugin.getConfig().getConfigurationSection("using");
		Set<String> formulae = formulaSection.getKeys(false);
		int sourceId = formulaSection.getInt(formulaName + ".sourceId");
		int targetId = formulaSection.getInt(formulaName + ".targetId");
		for (String formula : formulae) {
			if (formula == formulaName) continue;
			int currentSourceId = formulaSection.getInt(formula + ".sourceId");
			int currentTargetId = formulaSection.getInt(formula + ".targetId");
			if (sourceId == currentSourceId || targetId == currentTargetId) {
				if (usingSection.contains(playerName + "." + formula))
					usingSection.set(playerName + "." + formula, false);
				disablePlayer(playerName, formula);
			}
		}
		usingSection.set(playerName + "." + formulaName, true);
		plugin.saveConfig();
		Player player = plugin.getServer().getPlayer(playerName);
		if (player != null) {
			InventoryProcessor check = new InventoryProcessor(plugin);
			check.updatePlayerInventory(player);
		}
		return true;
	}
	
	public boolean disablePlayer(String playerName, String formulaName) {
		if (!plugin.getConfig().contains("formula." + formulaName))
			return false;
		String path = "using." + playerName + "." + formulaName;
		if (!plugin.getConfig().contains(path)) return true;
		plugin.getConfig().set(path, false);
		plugin.saveConfig();
		Player player = plugin.getServer().getPlayer(playerName);
		if (player != null) {
			InventoryProcessor check = new InventoryProcessor(plugin);
			check.restorePlayerInventory(player);
		}
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
