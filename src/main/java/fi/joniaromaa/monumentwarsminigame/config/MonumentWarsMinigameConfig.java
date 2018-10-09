package fi.joniaromaa.monumentwarsminigame.config;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import fi.joniaromaa.minigameframework.config.MinigameConfig;
import fi.joniaromaa.minigameframework.config.MinigameMapConfig;
import fi.joniaromaa.minigameframework.game.AbstractMinigame;
import fi.joniaromaa.monumentwarsminigame.MonumentWarsPlugin;
import fi.joniaromaa.monumentwarsminigame.game.MonumentWarsMinigame;

public class MonumentWarsMinigameConfig extends MinigameConfig
{
	private HashMap<String, MonumentWarsMapConfig> maps;
	
	private int gameType;
	private int teamSize;
	
	public MonumentWarsMinigameConfig(ConfigurationSection config)
	{
		this.maps = new HashMap<>();
		
		this.gameType = config.getInt("gametype");
		this.teamSize = config.getInt("team-size");
		
		List<String> maps = config.getStringList("maps");
		if (maps != null)
		{
			for(String mapId : maps)
			{
				this.maps.put(mapId, new MonumentWarsMapConfig(mapId, YamlConfiguration.loadConfiguration(Paths.get(MonumentWarsPlugin.getPlugin().getDataFolder().getPath(), "maps", mapId, "config.yml").toFile())));
			}
		}
	}

	@Override
	public int getGameType()
	{
		return this.gameType;
	}

	@Override
	public int getTeamSize()
	{
		return this.teamSize;
	}

	@Override
	public Collection<MinigameMapConfig> getMapConfigs()
	{
		return Collections.unmodifiableCollection(this.maps.values());
	}

	@Override
	public Class<? extends AbstractMinigame<?, ?>> getMinigameClass()
	{
		return MonumentWarsMinigame.class;
	}
}
