package fi.joniaromaa.monumentwarsminigame.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

import fi.joniaromaa.minigameframework.MinigamePlugin;
import fi.joniaromaa.monumentwarsminigame.game.MonumentWarsMinigame;
import fi.joniaromaa.monumentwarsminigame.player.MonumentWarsMinigamePlayer;
import fi.joniaromaa.parinacorelibrary.bukkit.data.BorderVector;
import net.md_5.bungee.api.ChatColor;

public class BlockListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlaceEvent(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		Block block = event.getBlock();

		MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
		{
			if (m instanceof MonumentWarsMinigame)
			{
				MonumentWarsMinigame monumentWars = (MonumentWarsMinigame)m;
				
				Vector vector = block.getLocation().toVector();
				if (!monumentWars.getMapConfig().getGameAreaBorder().contains(vector))
				{
					player.sendMessage(ChatColor.RED + "Et voi rakentaa pelialueen ulkopuolelle");
					
					event.setCancelled(true);
				}
				else
				{
					for(BorderVector border : monumentWars.getMapConfig().getDenyBuildAreas())
					{
						if (border.contains(vector))
						{
							player.sendMessage(ChatColor.RED + "Et voi rakentaa tähän");
							
							event.setCancelled(true);
							break;
						}
					}
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlaceEventMonitor(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();

		MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
		{
			if (m instanceof MonumentWarsMinigame)
			{
				MonumentWarsMinigame monumentWars = (MonumentWarsMinigame)m;
				
				MonumentWarsMinigamePlayer minigamePlayer = monumentWars.getMinigamePlayer(player);
				if (minigamePlayer != null)
				{
					minigamePlayer.getStats().increateBlocksPlaced();
					minigamePlayer.setGiveExp(minigamePlayer.getGiveExp() + 1);
				}
			}
		});
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreakEventMonitor(BlockBreakEvent event)
	{
		Player player = event.getPlayer();

		MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
		{
			if (m instanceof MonumentWarsMinigame)
			{
				MonumentWarsMinigame monumentWars = (MonumentWarsMinigame)m;
				
				MonumentWarsMinigamePlayer minigamePlayer = monumentWars.getMinigamePlayer(player);
				if (minigamePlayer != null)
				{
					minigamePlayer.getStats().increaseBlocksBroken();
					minigamePlayer.setGiveExp(minigamePlayer.getGiveExp() + 1);
				}
			}
		});
	}
}
