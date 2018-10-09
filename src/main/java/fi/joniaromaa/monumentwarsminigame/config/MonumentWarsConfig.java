package fi.joniaromaa.monumentwarsminigame.config;

import org.bukkit.configuration.file.FileConfiguration;

import fi.joniaromaa.minigameframework.config.MinigameConfig;
import fi.joniaromaa.minigameframework.config.MinigameManagerConfig;
import fi.joniaromaa.minigameframework.game.AbstractPreMinigame;
import fi.joniaromaa.monumentwarsminigame.pregame.MonumentWarsPreMinigame;

public class MonumentWarsConfig extends MinigameManagerConfig
{
	private int concurrentGameLimit;
	
	private MonumentWarsMinigameConfig minigameConfig;
	
	public MonumentWarsConfig(FileConfiguration config)
	{
		this.concurrentGameLimit = config.getInt("game.concurrent-game-limit");
		
		this.minigameConfig = new MonumentWarsMinigameConfig(config.getConfigurationSection("minigame"));
	}

	@Override
	public int getConcurrentGameLimit()
	{
		return this.concurrentGameLimit;
	}

	@Override
	public Class<? extends AbstractPreMinigame> getPreMinigameClass()
	{
		return MonumentWarsPreMinigame.class;
	}

	@Override
	public MinigameConfig getMinigameConfig()
	{
		return this.minigameConfig;
	}
}
