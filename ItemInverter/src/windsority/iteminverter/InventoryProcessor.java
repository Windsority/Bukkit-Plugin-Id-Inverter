package windsority.iteminverter;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryProcessor {

	final InverterMain plugin;
	public InventoryProcessor(InverterMain plugin) {
		this.plugin = plugin;
	}
	
	public ItemStack changeId(ItemStack item, int targetId) {
		@SuppressWarnings("deprecation")
		ItemStack targetItem = new ItemStack(Material.getMaterial(targetId));
		targetItem.setAmount(item.getAmount());
		targetItem.setItemMeta(item.getItemMeta());
		return targetItem;
	}
	
	@SuppressWarnings("deprecation")
	public void updatePlayerInventory(Player player) {
		String playerName = player.getName().toLowerCase();
		if (!plugin.getConfig().contains("using." + playerName)) return ;
		ConfigurationSection playerSection = 
				plugin.getConfig().getConfigurationSection("using." + playerName);
		Set<String> usingFormulae = playerSection.getKeys(false);
		for (String formula : usingFormulae) {
			if (playerSection.getBoolean(formula) == false) continue;
			
			int sourceId = plugin.getConfig().getInt("formula." + formula + ".sourceId");
			int targetId = plugin.getConfig().getInt("formula." + formula + ".targetId");
												
			for (int i = 0; i <= 35; ++i) {
				ItemStack inventoryItem = player.getInventory().getItem(i);
				if (inventoryItem == null) continue;
				int invItemId = inventoryItem.getType().getId();
				if (invItemId == sourceId)
					player.getInventory().setItem(i, changeId(inventoryItem, targetId));
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void restorePlayerInventory(Player player) {
		String playerName = player.getName().toLowerCase();
		if (!plugin.getConfig().contains("using." + playerName)) return ;
		ConfigurationSection playerSection = 
				plugin.getConfig().getConfigurationSection("using." + playerName);
		Set<String> usingFormulae = playerSection.getKeys(false);
		for (String formula : usingFormulae) {
			if (playerSection.getBoolean(formula) == true) continue;
			
			int sourceId = plugin.getConfig().getInt("formula." + formula + ".sourceId");
			int targetId = plugin.getConfig().getInt("formula." + formula + ".targetId");
			
			for (int i = 0; i <= 35; ++i) {
				ItemStack inventoryItem = player.getInventory().getItem(i);
				if (inventoryItem == null) continue;
				int invItemId = inventoryItem.getType().getId();
				if (invItemId == targetId)
					player.getInventory().setItem(i, changeId(inventoryItem, sourceId));
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void restoreInventory(Player player, Inventory inventory) {
		String playerName = player.getName().toLowerCase();
		if (!plugin.getConfig().contains("using." + playerName)) return ;
		ConfigurationSection playerSection = 
				plugin.getConfig().getConfigurationSection("using." + playerName);
		Set<String> usingFormulae = playerSection.getKeys(false);
		for (String formula : usingFormulae) {
			if (playerSection.getBoolean(formula) == false) continue;
			
			int sourceId = plugin.getConfig().getInt("formula." + formula + ".sourceId");
			int targetId = plugin.getConfig().getInt("formula." + formula + ".targetId");
			
			for (int i = 0; i < inventory.getSize(); ++i) {
				ItemStack inventoryItem = inventory.getItem(i);
				if (inventoryItem == null) continue;
				int invItemId = inventoryItem.getType().getId();
				if (invItemId == targetId)
					inventory.setItem(i, changeId(inventoryItem, sourceId));
			}
		}
	}	
	
	@SuppressWarnings("deprecation")
	public ItemStack checkItem(Player player, ItemStack item) {
		String playerName = player.getName().toLowerCase();
		if (!plugin.getConfig().contains("using." + playerName)) return item;
		ConfigurationSection playerSection = 
				plugin.getConfig().getConfigurationSection("using." + playerName);
		Set<String> usingFormulae = playerSection.getKeys(false);
		for (String formula : usingFormulae) {
			int sourceId = plugin.getConfig().getInt("formula." + formula + ".sourceId");
			int targetId = plugin.getConfig().getInt("formula." + formula + ".targetId");
			if (playerSection.getBoolean(formula) == false) {
				if (item.getType().getId() == targetId) {
					ItemStack retItem = new ItemStack(Material.getMaterial(sourceId));
					retItem.setItemMeta(item.getItemMeta());
					retItem.setAmount(item.getAmount());
					return retItem;
				}
			} else {
				if (item.getType().getId() == sourceId) {
					ItemStack retItem = new ItemStack(Material.getMaterial(targetId));
					retItem.setItemMeta(item.getItemMeta());
					retItem.setAmount(item.getAmount());
					return retItem;
				}
			}
		}
		return item;
	}
	
	@SuppressWarnings("deprecation")
	public ItemStack restoreItem(Player player, ItemStack item) {
		String playerName = player.getName().toLowerCase();
		if (!plugin.getConfig().contains("using." + playerName)) return item;
		ConfigurationSection playerSection = 
				plugin.getConfig().getConfigurationSection("using." + playerName);
		Set<String> usingFormulae = playerSection.getKeys(false);
		for (String formula : usingFormulae) {
			int sourceId = plugin.getConfig().getInt("formula." + formula + ".sourceId");
			int targetId = plugin.getConfig().getInt("formula." + formula + ".targetId");
			if (playerSection.getBoolean(formula) == true) {
				if (item.getType().getId() == targetId)
					return changeId(item, sourceId);
			}
		}
		return item;
	}
	
}
