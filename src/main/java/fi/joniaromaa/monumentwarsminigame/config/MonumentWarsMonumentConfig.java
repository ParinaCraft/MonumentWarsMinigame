package fi.joniaromaa.monumentwarsminigame.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BlockVector;

import lombok.Getter;

public class MonumentWarsMonumentConfig
{
	@Getter private String name;
	@Getter private BlockVector location;
	@Getter private int health;
	
	public MonumentWarsMonumentConfig(ConfigurationSection config)
	{
		this.name = config.getString("name");
		this.location = (BlockVector)config.get("location");
		this.health = config.getInt("health");
	}
}
