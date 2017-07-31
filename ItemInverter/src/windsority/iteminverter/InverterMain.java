package windsority.iteminverter;

import org.bukkit.plugin.java.JavaPlugin;

public class InverterMain extends JavaPlugin{
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		this.getCommand("inverter").setExecutor(new InverterCmd(this));
	}
	
	@Override
	public void onDisable() {
		
	}
		
}
