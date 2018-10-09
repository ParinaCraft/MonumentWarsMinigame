package fi.joniaromaa.monumentwarsminigame.pregame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Wool;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import fi.joniaromaa.minigameframework.api.game.PreMinigameStatus;
import fi.joniaromaa.minigameframework.builder.MinigameWorldBuilder;
import fi.joniaromaa.minigameframework.config.MinigameConfig;
import fi.joniaromaa.minigameframework.config.MinigameMapConfig;
import fi.joniaromaa.minigameframework.game.AbstractPreMinigame;
import fi.joniaromaa.minigameframework.world.BlockBreakContractTypeType;
import fi.joniaromaa.minigameframework.world.WorldWeatherType;
import fi.joniaromaa.monumentwarsminigame.MonumentWarsPlugin;
import fi.joniaromaa.monumentwarsminigame.config.MonumentWarsMapConfig;
import fi.joniaromaa.monumentwarsminigame.config.MonumentWarsTeamConfig;
import fi.joniaromaa.parinacorelibrary.bukkit.builder.ItemStackBuilder;
import fi.joniaromaa.parinacorelibrary.bukkit.data.WordlessLocation;
import fi.joniaromaa.parinacorelibrary.bukkit.scoreboard.ScoreboardDynamicScore;
import fi.joniaromaa.parinacorelibrary.bukkit.scoreboard.ScoreboardManager;
import fi.joniaromaa.parinacorelibrary.bukkit.scoreboard.ScoreboardViewer;
import net.md_5.bungee.api.ChatColor;

public class MonumentWarsPreMinigame extends AbstractPreMinigame
{
	private World world;
	
	public MonumentWarsPreMinigame(int gameId, MinigameConfig config, MinigameMapConfig mapConfig)
	{
		super(gameId, config, mapConfig);
		
		this.scoreboardManager = new ScoreboardManager(MonumentWarsPlugin.getPlugin(), this::setupScoreboard);
	}
	
	private void setupScoreboard(ScoreboardViewer viewer)
	{
		Scoreboard scoreboard = viewer.getScoreboard();
		
		Objective sideBar = scoreboard.registerNewObjective("sideBar", "dummy");
		sideBar.setDisplayName(ChatColor.AQUA + ChatColor.BOLD.toString() + "Monument" + ChatColor.GREEN + ChatColor.BOLD.toString() + "Wars");
		sideBar.setDisplaySlot(DisplaySlot.SIDEBAR);

		sideBar.getScore("    ").setScore(7);
		viewer.addDynamicScore(new ScoreboardDynamicScore(viewer, sideBar, this::updateScoreboardPlayerCount, 6));
		sideBar.getScore("   ").setScore(5);
		viewer.addDynamicScore(new ScoreboardDynamicScore(viewer, sideBar, this::updateScoreboardTime, 4));
		sideBar.getScore("  ").setScore(3);
		sideBar.getScore(ChatColor.AQUA + "Kartta: " + ChatColor.GREEN + this.getMapConfig().getId()).setScore(2);
		sideBar.getScore(" ").setScore(1);
		sideBar.getScore(ChatColor.AQUA + "parina" + ChatColor.GREEN + "craft.net").setScore(0);
	}

	
	private void updateScoreboardPlayerCount(ScoreboardDynamicScore dynamicScore)
	{
		dynamicScore.set(ChatColor.AQUA + "Pelaajia: " + ChatColor.GREEN + this.getPlayersCount() + "/" + this.getPlayersLimit());
	}
	
	private void updateScoreboardTime(ScoreboardDynamicScore dynamicScore)
	{
		if (this.getStatus() == PreMinigameStatus.WAITING_FOR_PLAYERS)
		{
			dynamicScore.set(ChatColor.AQUA + "Odotetaan pelaajia");
		}
		else
		{
			dynamicScore.set(ChatColor.AQUA + "Peli alkaa: " + ChatColor.GREEN + this.getTimeLeftToStartInSecs() + "s");
		}
	}
	
	@Override
	public void setup() throws Exception
	{
		FileUtils.copyDirectory(Paths.get(MonumentWarsPlugin.getPlugin().getDataFolder().getPath(), "maps", this.getMapConfig().getId(), "world").toFile(), new File("monument_wars_minigame-" + this.getGameId()));
		
		this.world = MinigameWorldBuilder.builder()
				.worldName("monument_wars_minigame-" + this.getGameId())
				.voidOnlyGenerator()
				.saveChunks(false)
				.setWeatherType(WorldWeatherType.CLEAR)
				.doDaylightCycle(false)
				.doTileDrops(false)
				.doFireTick(false)
				.allowBlockPlace(true)
				.blockBreakContractType(BlockBreakContractTypeType.USER_PLACED)
				.build(MonumentWarsPlugin.getPlugin());
		
		WordlessLocation spawnPoint = this.getMapConfig().getPreGameSpawnPoint();
		
		this.world.setSpawnLocation((int)spawnPoint.getX(), (int)spawnPoint.getY(), (int)spawnPoint.getZ());
		this.world.setTime(6000);
		this.world.setKeepSpawnInMemory(true);
		this.world.setPVP(true);
		this.world.setSpawnFlags(false, false);

		super.setup();
	}
	
	@Override
	public void onPlayerJoin(Player player)
	{
		super.onPlayerJoin(player);
		
		PlayerInventory inventory = player.getInventory();
		inventory.setArmorContents(null);
		inventory.clear();
		
		int i = 0;
		for(MonumentWarsTeamConfig team : this.getMapConfig().getTeams())
		{
			inventory.setItem(i++, ItemStackBuilder.builder()
					.materialData(new Wool(team.getColor()))
					.displayName(ChatColor.valueOf(team.getColor().name()) + team.getName())
					.build());
		}
		
		for(Player other : MonumentWarsPlugin.getPlugin().getServer().getOnlinePlayers())
		{
			if (other.getWorld() != this.world)
			{
				player.hidePlayer(other);
				other.hidePlayer(player);
			}
		}
		
		inventory.setItem(8, ItemStackBuilder.builder()
				.type(Material.BED)
				.displayName(ChatColor.RED + "Lobbyyn")
				.build());
	}

	@Override
	public Optional<Location> onPlayerSpawn(Player player)
	{
		return Optional.of(this.getMapConfig().getPreGameSpawnPoint().toLocation(this.world));
	}

	@Override
	public void onCriticalException(Throwable e)
	{
		this.world.getPlayers().forEach((p) -> p.kickPlayer("Critical error"));
		
		MonumentWarsPlugin.getPlugin().getServer().unloadWorld(this.world, false);
		
		try
		{
			FileUtils.deleteDirectory(this.world.getWorldFolder());
		}
		catch (IOException e1) //Failed to delete the directory
		{
			e1.printStackTrace();
		}
	}
	
	public MonumentWarsMapConfig getMapConfig()
	{
		return (MonumentWarsMapConfig)super.getMapConfig();
	}

	@Override
	public World getGameWorld()
	{
		return this.world;
	}
}
