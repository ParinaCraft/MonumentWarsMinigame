package fi.joniaromaa.monumentwarsminigame.game.team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

import fi.joniaromaa.minigameframework.MinigamePlugin;
import fi.joniaromaa.minigameframework.api.game.Minigame;
import fi.joniaromaa.minigameframework.team.AbstractMinigameTeam;
import fi.joniaromaa.minigameframework.utils.EntityUtils;
import fi.joniaromaa.monumentwarsminigame.config.MonumentWarsMonumentConfig;
import fi.joniaromaa.monumentwarsminigame.config.MonumentWarsTeamConfig;
import fi.joniaromaa.monumentwarsminigame.game.MonumentWarsMinigame;
import fi.joniaromaa.monumentwarsminigame.inventory.MonumentWarsMagicShardShopUserInterface;
import fi.joniaromaa.monumentwarsminigame.inventory.MonumentWarsShopUserInterface;
import fi.joniaromaa.monumentwarsminigame.nms.DummyEntityVillager;
import fi.joniaromaa.monumentwarsminigame.nms.EntityNms;
import fi.joniaromaa.monumentwarsminigame.player.MonumentWarsMinigamePlayer;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

public class MonumentWarsMinigameTeam extends AbstractMinigameTeam<MonumentWarsMinigamePlayer>
{
	@Getter private Location spawnPoint;
	
	@Getter private Villager shopNpc;
	@Getter private Villager magicShardShopNpc;
	
	private List<MonumentWarsTeamMonument> monuments;
	
	public MonumentWarsMinigameTeam(MonumentWarsMinigame game, MonumentWarsTeamConfig config)
	{
		super(game, config.getName(), config.getColor());

		this.monuments = new ArrayList<>();
		
		this.spawnPoint = config.getSpawnPoint().toLocation(this.getGame().getWorld());
		
		DummyEntityVillager shopNpcNms = EntityNms.spawnEntity(config.getShopLocation().toLocation(this.getGame().getWorld()), DummyEntityVillager.class);
		shopNpcNms.setRightClickCallable((h) ->
		{
			if (h instanceof Player)
			{
				Player player = (Player)h;
				
				Minigame minigame = MinigamePlugin.getPlugin().getGameManager().getMinigame(player).orElse(null);
				if (minigame instanceof MonumentWarsMinigame)
				{
					MonumentWarsMinigamePlayer minigamePlayer = ((MonumentWarsMinigame)minigame).getMinigamePlayer(player);
					if (minigamePlayer != null)
					{
						player.openInventory(new MonumentWarsShopUserInterface(minigamePlayer).getInventory());
					}
					
					return true;
				}
			}
			
			return false;
		});
		
		this.shopNpc = (Villager)shopNpcNms.getBukkitEntity();
		this.shopNpc.setProfession(Profession.BLACKSMITH);

		EntityUtils.addHologram(this.shopNpc.getLocation().add(0, 1.9, 0), ChatColor.YELLOW + ChatColor.BOLD.toString() + "Kauppa");
		
		shopNpcNms.aK = this.shopNpc.getLocation().getYaw(); //Ffs
		
		DummyEntityVillager magicShardShopNpcNms = EntityNms.spawnEntity(config.getMagicShardShopLocation().toLocation(this.getGame().getWorld()), DummyEntityVillager.class);
		magicShardShopNpcNms.setRightClickCallable((h) ->
		{
			if (h instanceof Player)
			{
				Player player = (Player)h;
				
				Minigame minigame = MinigamePlugin.getPlugin().getGameManager().getMinigame(player).orElse(null);
				if (minigame instanceof MonumentWarsMinigame)
				{
					MonumentWarsMinigamePlayer minigamePlayer = ((MonumentWarsMinigame)minigame).getMinigamePlayer(player);
					if (minigamePlayer != null)
					{
						player.openInventory(new MonumentWarsMagicShardShopUserInterface(minigamePlayer).getInventory());
					}
					
					return true;
				}
			}
			
			return false;
		});
		
		this.magicShardShopNpc = (Villager)magicShardShopNpcNms.getBukkitEntity();
		this.magicShardShopNpc.setProfession(Profession.PRIEST);
		
		EntityUtils.addHologram(this.magicShardShopNpc.getLocation().add(0, 1.9, 0), ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "Magic Shard Kauppa");
		
		magicShardShopNpcNms.aK = this.magicShardShopNpc.getLocation().getYaw(); //Ffs
		
		for(MonumentWarsMonumentConfig monument : config.getMonuments())
		{
			this.monuments.add(new MonumentWarsTeamMonument(this, monument));
		}
	}
	
	@Override
	public MonumentWarsMinigame getGame()
	{
		return (MonumentWarsMinigame)super.getGame();
	}
	
	public boolean canRespawn()
	{
		return this.monuments.stream().anyMatch((m) -> m.isAlive());
	}
	
	public List<MonumentWarsTeamMonument> getMonuments()
	{
		return Collections.unmodifiableList(this.monuments);
	}
}
