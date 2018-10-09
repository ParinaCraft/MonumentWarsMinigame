package fi.joniaromaa.monumentwarsminigame.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import fi.joniaromaa.minigameframework.config.MinigameMapConfig;
import fi.joniaromaa.parinacorelibrary.bukkit.data.BorderVector;
import fi.joniaromaa.parinacorelibrary.bukkit.data.WordlessLocation;
import lombok.Getter;

public class MonumentWarsMapConfig extends MinigameMapConfig
{
	@Getter private final String id;
	
	private int playerLimit;
	
	private Map<Integer, Integer> preGameStartTimes;
	
	@Getter private WordlessLocation preGameSpawnPoint;
	
	private List<MonumentWarsTeamConfig> teams;
	
	@Getter private BorderVector gameAreaBorder;
	private List<BorderVector> denyBuildAreas;
	
	@Getter private MonumentWarsCapturePointConfig capturePoint;
	
	public MonumentWarsMapConfig(String id, ConfigurationSection config)
	{
		this.id = id;
		
		this.preGameStartTimes = new HashMap<>();
		
		this.teams = new ArrayList<>();
		this.denyBuildAreas = new ArrayList<>();

		ConfigurationSection game = config.getConfigurationSection("game");
		
		this.playerLimit = game.getInt("player-limit");
		
		ConfigurationSection preGameStartTimes = game.getConfigurationSection("pre-game-start-times");
		for(String key : preGameStartTimes.getKeys(false))
		{
			this.preGameStartTimes.put(new Integer(key), preGameStartTimes.getInt(key));
		}
		
		this.preGameSpawnPoint = (WordlessLocation)game.get("pre-game-spawn-point");
		
		ConfigurationSection area = game.getConfigurationSection("area");
		
		this.gameAreaBorder = (BorderVector)area.get("border");
		
		ConfigurationSection denyBuildAreas = area.getConfigurationSection("deny-build");
		for(String key : denyBuildAreas.getKeys(false))
		{
			this.denyBuildAreas.add((BorderVector)denyBuildAreas.get(key));
		}
		
		this.capturePoint = new MonumentWarsCapturePointConfig(game.getConfigurationSection("capture-point"));
		
		ConfigurationSection teams = game.getConfigurationSection("teams");
		for(String key : teams.getKeys(false))
		{
			this.teams.add(new MonumentWarsTeamConfig(teams.getConfigurationSection(key)));
		}
	}
	
	@Override
	public int getPlayerLimit()
	{
		return this.playerLimit;
	}

	@Override
	public Map<Integer, Integer> getPreGameStartTimes()
	{
		return Collections.unmodifiableMap(this.preGameStartTimes);
	}
	
	public List<BorderVector> getDenyBuildAreas()
	{
		return Collections.unmodifiableList(this.denyBuildAreas);
	}
	
	public List<MonumentWarsTeamConfig> getTeams()
	{
		return Collections.unmodifiableList(this.teams);
	}
}
