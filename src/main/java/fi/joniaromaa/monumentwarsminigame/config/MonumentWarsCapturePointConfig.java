package fi.joniaromaa.monumentwarsminigame.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BlockVector;

import lombok.Getter;

public class MonumentWarsCapturePointConfig
{
	@Getter private BlockVector location;
	@Getter private int range;
	@Getter private int captureTime;
	
	public MonumentWarsCapturePointConfig(ConfigurationSection config)
	{
		this.location = (BlockVector)config.get("location");
		this.range = config.getInt("range");
		this.captureTime = config.getInt("capture-time");
	}
}
