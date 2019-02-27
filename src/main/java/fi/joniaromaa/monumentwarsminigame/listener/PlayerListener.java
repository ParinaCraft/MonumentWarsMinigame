package fi.joniaromaa.monumentwarsminigame.listener;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import fi.joniaromaa.minigameframework.MinigamePlugin;
import fi.joniaromaa.minigameframework.player.BukkitUser;
import fi.joniaromaa.minigameframework.user.dataset.UserPreferedMinigameTeamDataStorage;
import fi.joniaromaa.monumentwarsminigame.MonumentWarsPlugin;
import fi.joniaromaa.monumentwarsminigame.game.MonumentWarsGameStatus;
import fi.joniaromaa.monumentwarsminigame.game.MonumentWarsMinigame;
import fi.joniaromaa.monumentwarsminigame.game.team.MonumentWarsMinigameTeam;
import fi.joniaromaa.monumentwarsminigame.player.MonumentWarsMinigamePlayer;
import fi.joniaromaa.monumentwarsminigame.pregame.MonumentWarsPreMinigame;
import fi.joniaromaa.parinacorelibrary.api.ParinaCore;
import fi.joniaromaa.parinacorelibrary.api.user.User;
import fi.joniaromaa.parinacorelibrary.api.user.dataset.minigames.UserMonumentWarsStatsDataStorage;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.DamageSource;

public class PlayerListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerDropItemEvent(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		
		MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
		{
			if (m instanceof MonumentWarsMinigame)
			{
				Item item = event.getItemDrop();
				ItemStack itemStack = item.getItemStack();
				
				if (itemStack.getType() == Material.GOLDEN_APPLE || itemStack.getType() == Material.ENDER_PEARL || itemStack.getType() == Material.POTION)
				{
					return;
				}
				
				event.setCancelled(true);
			}
		});
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerMoveEvent(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();

		if (player.getLocation().getY() <= 0)
		{
			MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
			{
				if (m instanceof MonumentWarsMinigame)
				{
					((CraftPlayer)player).getHandle().damageEntity(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
				}
				else if (m instanceof MonumentWarsPreMinigame)
				{
					MonumentWarsPreMinigame preGame = (MonumentWarsPreMinigame)m;
					
					player.teleport(preGame.getMapConfig().getPreGameSpawnPoint().toLocation(preGame.getGameWorld()));
				}
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		
		MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
		{
			if (m instanceof MonumentWarsPreMinigame)
			{
				MonumentWarsPreMinigame monumentWats = (MonumentWarsPreMinigame)m;
				
				Iterator<Player> iterator = event.getRecipients().iterator();
				while (iterator.hasNext())
				{
					Player recipient = iterator.next();
					if (!monumentWats.isPlaying(recipient))
					{
						iterator.remove();
					}
				}
			}
			else if (m instanceof MonumentWarsMinigame)
			{
				MonumentWarsMinigame monumentWars = (MonumentWarsMinigame)m;

				MonumentWarsMinigamePlayer minigamePlayer = monumentWars.getMinigamePlayer(player);
				
				User user = null;
				if (minigamePlayer != null)
				{
					user = minigamePlayer.getUser();
				}
				else
				{
					user = ParinaCore.getApi().getUserManager().getUser(player.getUniqueId()).orElse(null);
				}
				
				StringBuilder stringBuilder = new StringBuilder();
				if (monumentWars.getStatus() != MonumentWarsGameStatus.ENDED)
				{
					if (!monumentWars.isSpectator(player))
					{
						if (minigamePlayer != null)
						{
							MonumentWarsMinigameTeam team = minigamePlayer.getTeam();
							if (team != null)
							{
								Iterator<Player> iterator = event.getRecipients().iterator();
								while (iterator.hasNext())
								{
									Player recipient = iterator.next();
									if (!team.isTeamMember(recipient))
									{
										iterator.remove();
									}
								}
								
								stringBuilder.append(ChatColor.AQUA)
									.append("[TEAM] ");
							}
							else
							{
								event.setCancelled(true); //?
								return;
							}
						}
						else
						{
							event.setCancelled(true); //?
							return;
						}
					}
					else
					{
						Iterator<Player> iterator = event.getRecipients().iterator();
						while (iterator.hasNext())
						{
							Player recipient = iterator.next();
							if (!monumentWars.isSpectator(recipient))
							{
								iterator.remove();
							}
						}
						
						stringBuilder.append(ChatColor.GRAY)
							.append("[SPECTATOR] ");
					}
				}
				else
				{
					List<Player> worldPlayer = monumentWars.getWorld().getPlayers();
					
					Iterator<Player> iterator = event.getRecipients().iterator();
					while (iterator.hasNext())
					{
						Player recipient = iterator.next();
						if (!worldPlayer.contains(recipient))
						{
							iterator.remove();
						}
					}
				}
				
				UserMonumentWarsStatsDataStorage stats = user != null ? user.getDataStorage(UserMonumentWarsStatsDataStorage.class).orElse(null) : null;
				if (stats != null)
				{
					stringBuilder.append(stats.getPrefix())
						.append(' ');
				}
				else
				{
					stringBuilder.append(ChatColor.GREEN)
						.append("[✮0] ");
				}
			
				if (!monumentWars.isSpectator(player))
				{
					if (minigamePlayer != null)
					{
						MonumentWarsMinigameTeam team = minigamePlayer.getTeam();
					
						stringBuilder.append(team.getChatColor());
					}
					else
					{
						event.setCancelled(true); //?
						return;
					}
				}
				else
				{
					stringBuilder.append(ChatColor.GRAY);
				}
				
				stringBuilder.append(user != null ? user.getDisplayName() : player.getName())
					.append(' ')
					.append(ChatColor.GRAY)
					.append(event.getMessage());
				
				event.setFormat(stringBuilder.toString());
			}
		});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onPlayerInteractEvent(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
		{
			if (m instanceof MonumentWarsPreMinigame)
			{
				MonumentWarsPreMinigame monumentWars = (MonumentWarsPreMinigame)m;
				
				BukkitUser bukkitUser = monumentWars.getPlayer(player);
				if (bukkitUser != null)
				{
					ItemStack itemStack = player.getItemInHand();
					if (itemStack.getType() == Material.WOOL)
					{
						ItemMeta meta = itemStack.getItemMeta();
						if (meta.getDisplayName() != null)
						{
							UserPreferedMinigameTeamDataStorage storage = bukkitUser.getUser().getDataStorage(UserPreferedMinigameTeamDataStorage.class).orElse(null);
							if (storage == null || !storage.getTeam().equals(meta.getDisplayName()))
							{
								bukkitUser.getUser().setDataStorage(new UserPreferedMinigameTeamDataStorage(ChatColor.stripColor(meta.getDisplayName())));
							}
							
							player.sendMessage(ChatColor.GRAY + "Liityt tiimiin " + meta.getDisplayName() + ChatColor.GRAY + ", jos siellä on tilaa!");
						}
					}
					else if (itemStack.getType() == Material.BED)
					{
						ByteArrayDataOutput out = ByteStreams.newDataOutput();
						out.writeUTF("Connect");
						out.writeUTF("MonumentWars");
						
						player.sendPluginMessage(MonumentWarsPlugin.getPlugin(), "BungeeCord", out.toByteArray());
					}
				}
			}
		});
	}
}
