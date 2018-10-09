package fi.joniaromaa.monumentwarsminigame.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;

import fi.joniaromaa.parinacorelibrary.bukkit.data.WordlessLocation;
import lombok.Getter;

public class MonumentWarsTeamConfig
{
	@Getter private String name;
	@Getter private DyeColor color;
	@Getter private WordlessLocation spawnPoint;
	
	@Getter private WordlessLocation shopLocation;
	@Getter private WordlessLocation magicShardShopLocation;
	
	@Getter private List<MonumentWarsMonumentConfig> monuments;
	
	public MonumentWarsTeamConfig(ConfigurationSection config)
	{
		this.monuments = new ArrayList<>();
		
		this.name = config.getString("name");
		this.color = DyeColor.valueOf(config.getString("color"));
		this.spawnPoint = (WordlessLocation)config.get("spawn-point");
		
		this.shopLocation = (WordlessLocation)config.get("shop-location"); 
		this.magicShardShopLocation = (WordlessLocation)config.get("magic-shard-shop-location");
		
		ConfigurationSection monuments = config.getConfigurationSection("monuments");
		for(String monument : monuments.getKeys(false))
		{
			this.monuments.add(new MonumentWarsMonumentConfig(monuments.getConfigurationSection(monument)));
		}
	}
}
