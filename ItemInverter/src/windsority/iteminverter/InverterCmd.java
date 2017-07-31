package windsority.iteminverter;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
					sender.sendMessage("公式不存在！");; 
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
				
				if (addFormula(args[2], item, Integer.valueOf(args[1])))
					sender.sendMessage("添加成功！");
				else sender.sendMessage("公式名非法！");
				
				return true;
			}
			
			//correct form: 2. /inverter enable [player] [formula]
			if (args[0].equalsIgnoreCase("enable")) {
				enablePlayer(args[1], args[2]);
				return true;
			}
				
			//correct form: 3. /inverter disable [player] [formula]
			if (args[0].equalsIgnoreCase("disable")) {
				disablePlayer(args[1], args[2]);
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
		Set<String> players = plugin.getConfig().getConfigurationSection("using").getKeys(false);
		for (String player : players) {
			if (plugin.getConfig().getString("using." + player) == formulaName)
				plugin.getConfig().set("using." + player, null);
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
		
		ItemStack sourceItem = plugin.getConfig().getItemStack(path + ".item");
		String sourceItemName = sourceItem.getType().name();
		int targetId = plugin.getConfig().getInt(path + ".targetId");
		
		@SuppressWarnings("deprecation")
		String tarItemName = Material.getMaterial(targetId).name();
		
		sender.sendMessage("公式" + formulaName + ":");
		String message = "[" + sourceItemName + "] --转变为--> " + "[Id " + 
						 Integer.valueOf(targetId).toString() +  ": " + tarItemName + "]";
		sender.sendMessage(message);
		return true;
	}
	
	public static void enablePlayer(String playerName, String formulaName) {
		
	}
	
	public static void disablePlayer(String playerName, String formulaName) {
		
	}
	
}
