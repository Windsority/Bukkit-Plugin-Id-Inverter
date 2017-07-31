package windsority.iteminverter;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TempIdInverter implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		ItemMeta meta = item.getItemMeta();
		int id = Integer.valueOf(args[0]);
		
		@SuppressWarnings("deprecation")
		ItemStack nitem = new ItemStack(Material.getMaterial(id));
		
		nitem.setItemMeta(meta);
		player.getInventory().setItemInMainHand(nitem);
		
		return true;
	}
	
}
