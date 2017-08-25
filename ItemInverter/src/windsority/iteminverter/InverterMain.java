package windsority.iteminverter;

import org.bukkit.plugin.java.JavaPlugin;

public class InverterMain extends JavaPlugin{
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		getCommand("inverter").setExecutor(new InverterCmd(this));
		getServer().getPluginManager().registerEvents(new ItemListener(this), this);
	}
	
	@Override
	public void onDisable() {
		
	}
		
}
