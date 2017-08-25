package windsority.iteminverter;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ItemListener implements Listener {
	
	final InverterMain plugin;
	
	public ItemListener(InverterMain plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				InventoryProcessor check = new InventoryProcessor(plugin);
				check.restoreInventory((Player)event.getWhoClicked(), event.getInventory());
				check.updatePlayerInventory((Player)event.getWhoClicked());
			}
		}.runTaskLater(plugin, 1L);
	}
	
	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				InventoryProcessor check = new InventoryProcessor(plugin);
				check.restoreInventory((Player)event.getWhoClicked(), event.getInventory());
				check.updatePlayerInventory((Player)event.getWhoClicked());
			}
		}.runTaskLater(plugin, 1L);
	}
	
	@EventHandler
	public void onItemDroping(PlayerDropItemEvent event) {
		if (event.isCancelled()) return ;
		Player player = event.getPlayer();
		Item itemEntity = event.getItemDrop();
		InventoryProcessor processor = new InventoryProcessor(plugin);
		itemEntity.setItemStack(processor.restoreItem(player, itemEntity.getItemStack()));
	}
	
	
	@EventHandler
	public void onItemPickUp(PlayerPickupItemEvent event) {
		if (event.isCancelled()) return ;
		Player player = event.getPlayer();
		Item itemEntity = event.getItem();
		InventoryProcessor processor = new InventoryProcessor(plugin);
		ItemStack itemPicked = processor.checkItem(player, itemEntity.getItemStack());
		player.getInventory().addItem(itemPicked);
		player.updateInventory();
		event.getItem().remove();
		event.setCancelled(true);
	}
	
}
