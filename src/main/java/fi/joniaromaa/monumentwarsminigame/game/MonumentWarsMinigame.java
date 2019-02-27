package fi.joniaromaa.monumentwarsminigame.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.github.paperspigot.Title;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import fi.joniaromaa.minigameframework.MinigamePlugin;
import fi.joniaromaa.minigameframework.config.MinigameConfig;
import fi.joniaromaa.minigameframework.config.MinigameMapConfig;
import fi.joniaromaa.minigameframework.event.game.TeamCapturePointCapEvent;
import fi.joniaromaa.minigameframework.event.game.TeamCapturePointNormalizedEvent;
import fi.joniaromaa.minigameframework.game.AbstractMinigame;
import fi.joniaromaa.minigameframework.game.helpers.CapturePoint;
import fi.joniaromaa.minigameframework.player.AbstractMinigamePlayer;
import fi.joniaromaa.minigameframework.player.BukkitUser;
import fi.joniaromaa.minigameframework.team.AbstractMinigameTeam;
import fi.joniaromaa.monumentwarsminigame.MonumentWarsPlugin;
import fi.joniaromaa.monumentwarsminigame.config.MonumentWarsMapConfig;
import fi.joniaromaa.monumentwarsminigame.config.MonumentWarsMinigameConfig;
import fi.joniaromaa.monumentwarsminigame.config.MonumentWarsTeamConfig;
import fi.joniaromaa.monumentwarsminigame.game.team.MonumentWarsMinigameTeam;
import fi.joniaromaa.monumentwarsminigame.game.team.MonumentWarsTeamMonument;
import fi.joniaromaa.monumentwarsminigame.player.MonumentWarsMinigamePlayer;
import fi.joniaromaa.parinacorelibrary.api.user.dataset.minigames.UserMonumentWarsStatsDataStorage;
import fi.joniaromaa.parinacorelibrary.bukkit.builder.ItemStackBuilder;
import fi.joniaromaa.parinacorelibrary.bukkit.data.BorderVector;
import fi.joniaromaa.parinacorelibrary.bukkit.scoreboard.ScoreboardDynamicScore;
import fi.joniaromaa.parinacorelibrary.bukkit.scoreboard.ScoreboardManager;
import fi.joniaromaa.parinacorelibrary.bukkit.scoreboard.ScoreboardViewer;
import fi.joniaromaa.parinacorelibrary.common.utils.TimeUtils;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class MonumentWarsMinigame extends AbstractMinigame<MonumentWarsMinigameTeam, MonumentWarsMinigamePlayer> implements Listener
{
	private CapturePoint capturePoint;
	
	@Getter private MonumentWarsGameStatus status;
	private int timePlayed;
	
	public MonumentWarsMinigame(int gameId, MinigameConfig config, MinigameMapConfig mapConfig, World world, Collection<BukkitUser> users, boolean privateGame)
	{
		super(gameId, config, mapConfig, world, users, privateGame);
		
		this.scoreboardManager = new ScoreboardManager(MonumentWarsPlugin.getPlugin(), this::setupScoreboard);
		
		this.capturePoint = new CapturePoint(this, this.getMapConfig().getCapturePoint().getLocation().toLocation(this.getWorld()), this.getMapConfig().getCapturePoint().getRange(), this.getMapConfig().getCapturePoint().getCaptureTime());
		
		this.status = MonumentWarsGameStatus.RUNNING;
		this.timePlayed = 0;
		
		MonumentWarsPlugin.getPlugin().getServer().getPluginManager().registerEvents(this, MonumentWarsPlugin.getPlugin());
	}
	
	private void setupScoreboard(ScoreboardViewer viewer)
	{
		MonumentWarsMinigamePlayer player = this.getMinigamePlayer(viewer.getPlayer());

		Scoreboard scoreboard = viewer.getScoreboard();
		
		Objective sideBar = scoreboard.registerNewObjective("sideBar", "dummy");
		sideBar.setDisplayName(ChatColor.AQUA + ChatColor.BOLD.toString() + "Monument" + ChatColor.GREEN + ChatColor.BOLD.toString() + "Wars" + (this.isPrivateGame() ? ChatColor.YELLOW + " [P]": ""));
		sideBar.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		List<MonumentWarsMinigameTeam> teams = new ArrayList<>(this.getTeams());
		teams.sort((o1, o2) ->
		{
			if (player != null && o2 == player.getTeam())
			{
				return -1;
			}
			else
			{
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		int i = 5;
		for(MonumentWarsMinigameTeam team : teams)
		{
			boolean myTeam = player != null ? team == player.getTeam() : false;
			
			for(MonumentWarsTeamMonument monument : team.getMonuments())
			{
				viewer.addDynamicScore(new ScoreboardDynamicScore(viewer, sideBar, (s) ->
				{
					StringBuilder stringBuilder = new StringBuilder();
					
					stringBuilder.append(ChatColor.valueOf(team.getColor().name()).toString()) //Use color here to have diff score name
						.append("  ")
						.append(ChatColor.AQUA);
					
					if (myTeam)
					{
						stringBuilder.append(ChatColor.BOLD);
					}
					
					stringBuilder.append(monument.getName())
						.append(": ")
						.append(monument.getHealthString(myTeam));
					
					s.set(stringBuilder.toString());
				}, i++));
			}
			
			viewer.addDynamicScore(new ScoreboardDynamicScore(viewer, sideBar, (s) ->
			{
				StringBuilder stringBuilder = new StringBuilder(ChatColor.valueOf(team.getColor().name()).toString());
				
				if (myTeam)
				{
					stringBuilder.append(ChatColor.BOLD);
				}
				
				stringBuilder.append(team.getName()).append(ChatColor.GREEN);
				
				if (myTeam)
				{
					stringBuilder.append(ChatColor.BOLD);
				}
				
				stringBuilder.append(" (")
					.append(team.getAlivePlayersCount())
					.append(" elossa)");
				
				s.set(stringBuilder.toString());
			}, i++));

			sideBar.getScore(ChatColor.valueOf(team.getColor().name()).toString()).setScore(i++);
			
			Team scoreboardTeam = scoreboard.registerNewTeam(team.getName());
			for(MonumentWarsMinigamePlayer teamPlayer : team.getTeamMembers())
			{
				scoreboardTeam.addEntry(teamPlayer.getBukkitPlayer().getName());
			}
			
			scoreboardTeam.setAllowFriendlyFire(false);
			scoreboardTeam.setCanSeeFriendlyInvisibles(true);
			scoreboardTeam.setPrefix(ChatColor.valueOf(team.getColor().name()) + "[" + team.getName().charAt(0) + "] ");
			
			Team scoreboardTeamInvis = scoreboard.registerNewTeam(team.getName() + "-Invis");
			scoreboardTeamInvis.setAllowFriendlyFire(false);
			scoreboardTeamInvis.setCanSeeFriendlyInvisibles(true);
			scoreboardTeamInvis.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
			scoreboardTeamInvis.setPrefix(ChatColor.valueOf(team.getColor().name()) + "[" + team.getName().charAt(0) + "] ");
		}

		Team spectatorScoreboardTeam = scoreboard.registerNewTeam("Spectator");
		spectatorScoreboardTeam.setAllowFriendlyFire(false);
		spectatorScoreboardTeam.setCanSeeFriendlyInvisibles(true);
		spectatorScoreboardTeam.setPrefix(ChatColor.GRAY + "[S] ");

		sideBar.getScore("   ").setScore(4);
		sideBar.getScore(ChatColor.AQUA + "Capture " + ChatColor.GREEN + "Point").setScore(3);
		viewer.addDynamicScore(new ScoreboardDynamicScore(viewer, sideBar, this::scoreboardControlPointHolder, 2));
		sideBar.getScore(" ").setScore(1);
		sideBar.getScore(ChatColor.AQUA + "parina" + ChatColor.GREEN + "craft.net").setScore(0);
	}
	
	private void scoreboardControlPointHolder(ScoreboardDynamicScore score)
	{
		StringBuilder stringBuilder = new StringBuilder("  ");

		AbstractMinigameTeam<?> teamHolding = this.capturePoint.getTeamHolding();
		if (this.capturePoint.getCapturingTime() == 0)
		{
			if (teamHolding == null)
			{
				stringBuilder.append(ChatColor.WHITE).append("None");
			}
			else
			{
				int timeLeft = 15 * 20 - this.capturePoint.getHoldingTime() % (15 * 20);
				
				stringBuilder.append(ChatColor.valueOf(teamHolding.getColor().name()))
					.append(teamHolding.getName())
					.append(ChatColor.GREEN)
					.append(" (")
					.append(TimeUtils.getHumanReadableSimplePeriod((int)Math.ceil(timeLeft / 20D)))
					.append(")");
			}
		}
		else
		{
			AbstractMinigameTeam<?> capturingTeam = this.capturePoint.getCapturingTeam();
			if (capturingTeam == null)
			{
				stringBuilder.append(ChatColor.WHITE)
					.append("Neutralisoidaan");
			}
			else
			{
				stringBuilder.append(ChatColor.valueOf(capturingTeam.getColor().name()))
					.append(capturingTeam.getName())
					.append(" valtaa");
			}
		}
		
		score.set(stringBuilder.toString());
	}

	@Override
	protected List<MonumentWarsMinigameTeam> buildTeams()
	{
		List<MonumentWarsMinigameTeam> teams = new ArrayList<>();
		
		for(MonumentWarsTeamConfig team : this.getMapConfig().getTeams())
		{
			teams.add(new MonumentWarsMinigameTeam(this, team));
		}

		return teams;
	}

	@Override
	protected MonumentWarsMinigamePlayer createPlayer(BukkitUser user)
	{
		return new MonumentWarsMinigamePlayer(this, user.getBukkitPlayer());
	}

	@Override
	public Optional<Location> onPlayerSpawn(Player player)
	{
		return Optional.of(this.getMapConfig().getPreGameSpawnPoint().toLocation(this.getWorld()));
	}

	@Override
	public void onCriticalException(Throwable e)
	{
		this.stats.clear(); //Avoid bad stats saving
		
		this.kick("Critical error");
		
		this.cleanup();
	}

	@Override
	public void start()
	{
		for(MonumentWarsMinigamePlayer player : this.getAlivePlayers())
		{
			Player bukkitPlayer = player.getBukkitPlayer();
			bukkitPlayer.teleport(player.getTeam().getSpawnPoint());
			bukkitPlayer.setGameMode(GameMode.SURVIVAL);
			bukkitPlayer.setHealth(bukkitPlayer.getMaxHealth());
			bukkitPlayer.setFoodLevel(20);
			
			player.updateInventory();
			
			this.scoreboardManager.addPlayer(bukkitPlayer);
		}
		
		super.start();
		
		this.sendMessage(ChatColor.AQUA + ChatColor.BOLD.toString() + "Monument" + ChatColor.GREEN + ChatColor.BOLD.toString() + "Wars");
		this.sendMessage(ChatColor.YELLOW + "Teht�v�n�si on rikkoa vastustajan monumentit (End stone)");
		this.sendMessage(ChatColor.YELLOW + "Kun kaikki monumentit on tuhottu vastustaja ei en��n synny uudelleen");
		this.sendMessage(ChatColor.YELLOW + "Vastustajan kuoltua peli p��ttyy");
		this.sendMessage(ChatColor.YELLOW + "Keskipisteen valtaaminen antaa etuja pelien edetess� tiimille, jonka on sen vallannut");
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onTick()
	{
		this.timePlayed++;
		
		if (this.status == MonumentWarsGameStatus.RUNNING || this.status == MonumentWarsGameStatus.DEATHMATCH)
		{
			if (this.timePlayed == 20 * 60 * 25) //25mins, Show warning about decrasing health
			{
				this.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Viiden minuutin p��st� monumentit alkavat menett�� el�m�pisteit� hitaasti yhteen rikkomiskertaan asti");
			}
			else if (this.timePlayed == 20 * 60 * 29) //29mins, Show warning about decrasing health
			{
				this.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Minuutin minuutin p��st� monumentit alkavat menett�� el�m�pisteit� hitaasti yhteen rikkomiskertaan asti");
			}
			else if (this.timePlayed == 20 * 60 * 30) //30mins, show decreasing health warning
			{
				this.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Monumentit alkavat menett�� el�m�pisteit� pikkuhiljaa kunnes ne saavuttavat yhden rikkomiskerran");

				this.getWorld().getPlayers().forEach((p) -> p.playSound(p.getLocation(), Sound.WITHER_IDLE, Integer.MAX_VALUE, 1));
			}
			else if (this.timePlayed == 20 * 60 * 40) //40mins, Show warning about deathmatch
			{
				this.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Viiden minuutin p��st� monumentit tuhoutuvat itsest��n");
			}
			else if (this.timePlayed == 20 * 60 * 44) //44mins, Show warning about deathmatch
			{
				this.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Minuutin p��st� monumentit tuhoutuvat itsest��n");
			}
			else if (this.timePlayed == 20 * 60 * 45) //45min, start deathmatch
			{
				for(MonumentWarsMinigameTeam team : this.getTeams())
				{
					for(MonumentWarsTeamMonument monument : team.getMonuments())
					{
						monument.explode();
					}
					
					for(MonumentWarsMinigamePlayer minigamePlayer : team.getTeamMembers())
					{
						minigamePlayer.getBukkitPlayer().sendTitle(Title.builder()
								.title(ChatColor.RED + "Deathmatch")
								.subtitle("Et synny en��n uudelleen")
								.fadeIn(10)
								.stay(40)
								.fadeOut(10)
								.build());
						

						minigamePlayer.getBukkitPlayer().playSound(minigamePlayer.getBukkitPlayer().getLocation(), team.canRespawn() ? Sound.WITHER_IDLE : Sound.ENDERDRAGON_GROWL, Integer.MAX_VALUE, 1);
					}
				}
			}
			else if (this.timePlayed == 20 * 60 * 55) //55min, end game warning
			{
				this.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Viiden minuutin p��st� peli p��tty automaattisesti");
			}
			else if (this.timePlayed == 20 * 60 * 59) //59min, end game warning
			{
				this.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Minuutin p��st� peli p��tty automaattisesti");
			}
			
			if (this.timePlayed > 20 * 60 * 30) //After 30 mins, start slowly decreasing health
			{
				if (this.timePlayed % 100 == 0)
				{
					for(MonumentWarsMinigameTeam team : this.getTeams())
					{
						team.getMonuments().stream()
						.filter((m) -> m.isAlive())
						.max((o1, o2) -> Integer.compare(o1.getHealth(), o2.getHealth()))
						.ifPresent((m) ->
						{
							if (m.getHealth() > 1)
							{
								m.setHealth(m.getHealth() - 1);
							}
						});
					}
				}
			}
			
			for(MonumentWarsMinigamePlayer player : this.getAlivePlayers())
			{
				player.getStats().increaseTimePlayed();
				
				Player bukkitPlayer = player.getBukkitPlayer();
	
				Integer respawnTime = player.getRespawnTime();
				if (respawnTime != null)
				{
					if (--respawnTime > 0)
					{
						player.setRespawnTime(respawnTime);
					}
					else
					{
						player.setRespawnTime(null);
						
						this.removeSpectator(bukkitPlayer);
						
						bukkitPlayer.teleport(player.getTeam().getSpawnPoint());
						bukkitPlayer.setGameMode(GameMode.SURVIVAL);
						bukkitPlayer.setHealth(bukkitPlayer.getMaxHealth());
						bukkitPlayer.setFoodLevel(20);
						bukkitPlayer.setFireTicks(0);
						bukkitPlayer.setFallDistance(0);
						
						player.updateInventory();
						
						((CraftPlayer)bukkitPlayer).getHandle().invulnerableTicks = 60;
					}
				}
				else
				{
					for(MonumentWarsMinigameTeam otherTeam : this.getAliveTeams())
					{
						if (player.getTeam() != otherTeam)
						{
							for(MonumentWarsMinigamePlayer enemy : otherTeam.getTeamMembers())
							{
								ScoreboardViewer viewer = this.getScoreboardManager().getPlayer(enemy.getBukkitPlayer());
								if (viewer != null)
								{
									Team team = viewer.getScoreboard().getTeam(player.getTeam().getName() + (bukkitPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY) ? "-Invis" : ""));
									if (team != null && !team.hasEntry(bukkitPlayer.getName()))
									{
										team.addEntry(bukkitPlayer.getName());
									}
								}
							}
						}
					}

					if (!this.isPrivateGame()) //Skip whole thing even tho its not saved
					{
						player.setGiveExp(player.getGiveExp() + 1);
						if (player.getGiveExp() >= 20 * 60)
						{
							player.setGiveExp(0);
							player.getStats().incresementExp();
							
							player.getBukkitPlayer().sendMessage(ChatColor.AQUA + "25 Monument Wars Experience");
						}
					}
					
					PlayerInventory inventory = bukkitPlayer.getInventory();
					inventory.remove(Material.GLASS_BOTTLE); //Remove empty bottles, nobody likes them

					//Prevent exploits
					if (bukkitPlayer.getItemOnCursor().getType() == Material.AIR)
					{
						if (!inventory.contains(Material.ARROW, 4))
						{
							player.setGiveArrowCounter(player.getGiveArrowCounter() + 1);
							if (player.getGiveArrowCounter() >= 100) //Every 5s
							{
								player.setGiveArrowCounter(0);
								
								if (!inventory.contains(Material.ARROW) && inventory.getContents()[8] == null)
								{
									inventory.setItem(8, ItemStackBuilder.builder().type(Material.ARROW).amount(1).build());
								}
								else
								{
									inventory.addItem(ItemStackBuilder.builder().type(Material.ARROW).amount(1).build());
								}
							}
						}
						
						int blocksCount = inventory.all(Material.STAINED_CLAY).values().stream().mapToInt((i) -> i.getAmount()).sum();
						if (blocksCount < 64)
						{
							inventory.addItem(ItemStackBuilder.builder().type(Material.STAINED_CLAY).data(player.getTeam().getColor().getData()).amount(64 - blocksCount).build());
						}
					}
					
					player.setGiveGoldCounter(player.getGiveGoldCounter() + 1);
					if (player.getGiveGoldCounter() >= 40)
					{
						player.setGiveGoldCounter(0);
						player.setGold(player.getGold() + 1);
					}
				}

				((CraftPlayer)bukkitPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(new ChatComponentText(ChatColor.GOLD + "Gold: " + player.getGold() + " " + ChatColor.DARK_PURPLE + "Magic Shard: " + player.getMagicShards()), (byte)2));
			}
			
			this.capturePoint.tick();
			if (this.capturePoint.getTeamHolding() instanceof MonumentWarsMinigameTeam)
			{
				if (this.capturePoint.getHoldingTime() > 0 && this.capturePoint.getHoldingTime() % (20 * 15) == 0)
				{
					MonumentWarsMinigameTeam team = (MonumentWarsMinigameTeam)this.capturePoint.getTeamHolding();
					for(MonumentWarsMinigamePlayer player : team.getAliveTeamMembers())
					{
						player.setMagicShards(player.getMagicShards() + 1);
					}
				}
			}
			
			if (true)
			{
				if (this.getAlivePlayersCount() == 0 || this.timePlayed == 20 * 60 * 60) //None alive or game has been running for hour
				{
					for(MonumentWarsMinigamePlayer player : this.getPlayers())
					{
						player.getStats().lostGame();
					}
					
					for(MonumentWarsMinigamePlayer player : this.getAlivePlayers())
					{
						Player bukkitPlayer = player.getBukkitPlayer();
						
						((CraftPlayer)bukkitPlayer).getHandle().invulnerableTicks = Integer.MAX_VALUE;
					}
					
					this.sendTitle(Title.builder()
							.title(ChatColor.GOLD + ChatColor.BOLD.toString() + "Tasapeli")
							.subtitle(ChatColor.GREEN + "Peli on p��ttynyt!")
							.build());

					TextComponent playAgain = new TextComponent("Pelaa uudelleen! (Klikkaamatta t�st�)");
					playAgain.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/queue monument_wars_6v6").create()));
					playAgain.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/queue monument_wars_6v6"));
					playAgain.setBold(true);
					playAgain.setColor(ChatColor.AQUA);
					
					this.getWorld().getPlayers().forEach((p) -> p.spigot().sendMessage(playAgain));;
					
					this.timePlayed = 0;
					this.status = MonumentWarsGameStatus.ENDED;
				}
				else if (this.getAliveTeamsCount() > 1)
				{
					for(MonumentWarsMinigameTeam team : this.getTeams())
					{
						for (MonumentWarsTeamMonument monument : team.getMonuments())
						{
							if (monument.isAlive())
							{
								monument.tick();
							}
						}
					}
				}
				else if (this.getAliveTeamsCount() == 1)
				{
					MonumentWarsMinigameTeam winner = this.getAliveTeams().iterator().next();
					
					if (!this.isPrivateGame()) //Skip whole thing
					{
						for(MonumentWarsMinigameTeam team : this.getTeams())
						{
							for(MonumentWarsMinigamePlayer player : team.getTeamMembers())
							{
								if (team == winner)
								{
									player.getStats().wonGame();
									
									player.getBukkitPlayer().sendMessage(ChatColor.AQUA + "75 Monument Wars Experience (Voitto boonus)");
								}
								else
								{
									player.getStats().lostGame();
								}
							}
						}
					}
					
					for(MonumentWarsMinigamePlayer player : winner.getAliveTeamMembers())
					{
						Player bukkitPlayer = player.getBukkitPlayer();
						
						((CraftPlayer)bukkitPlayer).getHandle().invulnerableTicks = Integer.MAX_VALUE;
						
						UserMonumentWarsStatsDataStorage stats = player.getUser().getDataStorage(UserMonumentWarsStatsDataStorage.class).get();

						if (bukkitPlayer.isOp() || stats.getFinalKillsRank() <= 10 || stats.getWinsRank() <= 10 || stats.getTotalExpRank() <= 10)
						{
							EnderDragon enderDragon = this.getWorld().spawn(bukkitPlayer.getLocation(), EnderDragon.class);
							enderDragon.setPassenger(bukkitPlayer);
							enderDragon.setCustomNameVisible(true);
							enderDragon.setCustomName(winner.getChatColor() + ChatColor.BOLD.toString() + player.getUser().getDisplayName() + " ebun dr�gön!");
							enderDragon.setNoDamageTicks(Integer.MAX_VALUE);
						}
					}
					
					this.sendTitle(Title.builder()
							.title(winner.getChatColor() + ChatColor.BOLD.toString() + winner.getName())
							.subtitle(ChatColor.GREEN + "on voittanut pelin!")
							.build());
					
					TextComponent playAgain = new TextComponent("Pelaa uudelleen! (Klikkaamatta t�st�)");
					playAgain.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/queue monument_wars_6v6").create()));
					playAgain.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/queue monument_wars_6v6"));
					playAgain.setBold(true);
					playAgain.setColor(ChatColor.AQUA);
					
					this.getWorld().getPlayers().forEach((p) -> p.spigot().sendMessage(playAgain));;
					
					this.timePlayed = 0;
					this.status = MonumentWarsGameStatus.ENDED;
				}
			}
			
			super.onTick();
		}
		else if (this.status == MonumentWarsGameStatus.ENDED)
		{
			if (this.timePlayed >= 20 * 15 && this.timePlayed % 20 == 0) //After 10s start to move player off
			{
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Connect");
				out.writeUTF("MonumentWars");
				
				this.sendPluginMessage(MonumentWarsPlugin.getPlugin(), "BungeeCord", out.toByteArray());
			}
			
			if (this.getWorld().getPlayers().size() == 0 || this.timePlayed >= 20 * 20) //Nobody on, lets move on or too much time spent
			{
				this.status = MonumentWarsGameStatus.CLEANUP;
			}
			else
			{
				for(MonumentWarsMinigamePlayer player : this.getAlivePlayers())
				{
					Player bukkitPlayer = player.getBukkitPlayer();

					Entity vehicle = bukkitPlayer.getVehicle();
					if (vehicle instanceof LivingEntity)
					{
						EntityLiving livingEntityNms = ((CraftLivingEntity)vehicle).getHandle();
						if (livingEntityNms instanceof EntityInsentient)
						{
							EntityInsentient insentientNms = (EntityInsentient)livingEntityNms;
							
							insentientNms.yaw = bukkitPlayer.getLocation().getYaw() + 180;
						}
						
						vehicle.setVelocity(bukkitPlayer.getLocation().getDirection().multiply(0.5));
					}
				}
			}
		}
		else if (this.status == MonumentWarsGameStatus.CLEANUP)
		{
			MinigamePlugin.getPlugin().getGameManager().deleteGame(this);
		}
	}
	
	@Override
	public void onPlayerQuit(Player player)
	{
		MonumentWarsMinigamePlayer minigamePlayer = this.getMinigamePlayer(player);
		if (minigamePlayer != null)
		{
			minigamePlayer.setAlive(false);
		}
		
		super.onPlayerQuit(player);
	}
	
	@Override
	public void cleanup()
	{
		super.cleanup();
		
		TeamCapturePointCapEvent.getHandlerList().unregister(this);
		TeamCapturePointNormalizedEvent.getHandlerList().unregister(this);
		EntityExplodeEvent.getHandlerList().unregister(this);
		BlockExplodeEvent.getHandlerList().unregister(this);
	}

	@Override
	public void makeSpectator(Player player)
	{
		super.makeSpectator(player);

		if (!this.isPlaying(player.getUniqueId()))
		{
			ScoreboardViewer viewer = this.scoreboardManager.getPlayer(player);
			if (viewer != null)
			{
				viewer.getScoreboard().getTeam("Spectator").addEntry(player.getName());
			}
		}
	}
	
	//All the events down here is some hacky temp solution
	@EventHandler(priority = EventPriority.MONITOR)
	public void onTeamCapturePointEvent(TeamCapturePointCapEvent event)
	{
		if (event.getMinigame() != this)
		{
			return;
		}
		
		this.sendMessage(ChatColor.valueOf(event.getTeam().getColor().name()) + event.getTeam().getName() + ChatColor.GRAY + " valtasi Capture Pointin");
	
		for(AbstractMinigamePlayer<?> player : event.getPlayers())
		{
			if (player instanceof MonumentWarsMinigamePlayer)
			{
				MonumentWarsMinigamePlayer minigamePlayer = (MonumentWarsMinigamePlayer)player;
				
				minigamePlayer.getStats().increaseCapturePointCapped();
				minigamePlayer.setGiveExp(minigamePlayer.getGiveExp() + 20);
			}
		}
		
		for(AbstractMinigamePlayer<?> player : event.getTeam().getTeamMembers())
		{
			player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, Integer.MAX_VALUE, 1);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onTeamCapturePointNormalizedEvent(TeamCapturePointNormalizedEvent event)
	{
		if (event.getMinigame() != this)
		{
			return;
		}
		
		this.sendMessage(ChatColor.valueOf(event.getTeam().getColor().name()) + event.getTeam().getName() + ChatColor.GRAY + " menetti Capture Pointin");
		
		for(AbstractMinigamePlayer<?> player : this.capturePoint.getTeamHolding().getTeamMembers())
		{
			player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.GHAST_SCREAM2, Integer.MAX_VALUE, 1);

			if (player instanceof MonumentWarsMinigamePlayer)
			{
				MonumentWarsMinigamePlayer minigamePlayer = (MonumentWarsMinigamePlayer)player;
				
				minigamePlayer.setGiveExp(minigamePlayer.getGiveExp() + 20);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplodeEvent(EntityExplodeEvent event)
	{
		if (this.status == MonumentWarsGameStatus.ENDED)
		{
			return; //Allow block explode now
		}
		
		if (event.getLocation().getWorld() != this.getWorld())
		{
			return;
		}
		
		Iterator<Block> iterator = event.blockList().iterator();
		while (iterator.hasNext())
		{
			Block block = iterator.next();
			
			Vector vector = block.getLocation().toVector();
			for(BorderVector border : this.getMapConfig().getDenyBuildAreas())
			{
				if (border.contains(vector))
				{
					iterator.remove();
					break;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockExplodeEvent(BlockExplodeEvent event)
	{
		if (this.status == MonumentWarsGameStatus.ENDED)
		{
			return; //Allow block explode now
		}
		
		if (event.getBlock().getLocation().getWorld() != this.getWorld())
		{
			return;
		}
		
		Iterator<Block> iterator = event.blockList().iterator();
		while (iterator.hasNext())
		{
			Block block = iterator.next();
			
			Vector vector = block.getLocation().toVector();
			for(BorderVector border : this.getMapConfig().getDenyBuildAreas())
			{
				if (border.contains(vector))
				{
					iterator.remove();
					break;
				}
			}
		}
	}
	
	@Override
	public MonumentWarsMinigameConfig getConfig()
	{
		return (MonumentWarsMinigameConfig)super.getConfig();
	}
	
	@Override
	public MonumentWarsMapConfig getMapConfig()
	{
		return (MonumentWarsMapConfig)super.getMapConfig();
	}
}
