package fi.joniaromaa.monumentwarsminigame.player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.github.paperspigot.Title;

import fi.joniaromaa.minigameframework.player.AbstractMinigamePlayer;
import fi.joniaromaa.monumentwarsminigame.game.MonumentWarsMinigame;
import fi.joniaromaa.monumentwarsminigame.game.team.MonumentWarsMinigameTeam;
import fi.joniaromaa.parinacorelibrary.api.user.dataset.minigames.UserMonumentWarsStatsDataStorage;
import fi.joniaromaa.parinacorelibrary.bukkit.builder.ItemStackBuilder;
import fi.joniaromaa.parinacorelibrary.bukkit.builder.LeatherArmorBuilder;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

public class MonumentWarsMinigamePlayer extends AbstractMinigamePlayer<MonumentWarsMinigameTeam>
{
	@Getter @Setter private int gold;
	@Getter @Setter private int magicShards;
	
	@Getter @Setter private Integer respawnTime;
	@Getter @Setter private int giveArrowCounter;
	@Getter @Setter private int giveGoldCounter;
	@Getter @Setter private int giveExp;
	
	@Getter private Map<Material, Map<Enchantment, Integer>> enchantments;
	
	public MonumentWarsMinigamePlayer(MonumentWarsMinigame game, Player bukkitPlayer)
	{
		super(game, bukkitPlayer, new MonumentWarsGameStats());
		
		this.enchantments = new HashMap<>();
		this.addEnchantment(Material.GOLD_PICKAXE, Enchantment.DIG_SPEED, 4);
	}
	
	public void addEnchantment(Material material, Enchantment enchantment, Integer level)
	{
		Map<Enchantment, Integer> enchantments = this.enchantments.get(material);
		if (enchantments == null)
		{
			this.enchantments.put(material, enchantments = new HashMap<>());
		}
		
		enchantments.put(enchantment, level);
	}
	
	@Override
	public void onDied()
	{
		Entity lastDamager = this.getCombatTracker().getLastDamager();
		if (lastDamager instanceof Player)
		{
			MonumentWarsMinigamePlayer minigamePlayer = this.getGame().getMinigamePlayer(lastDamager.getUniqueId());
			if (minigamePlayer != null)
			{
				minigamePlayer.setGold(minigamePlayer.getGold() + 10);
				minigamePlayer.setGiveExp(minigamePlayer.getGiveExp() + 20);
				
				Player player = minigamePlayer.getBukkitPlayer();
				player.playSound(player.getLocation(), Sound.ORB_PICKUP, Integer.MAX_VALUE, 1);
				
				if (this.getTeam().canRespawn())
				{
					minigamePlayer.getStats().increaseKills();
				}
				else
				{
					minigamePlayer.getStats().increaseFinalKills();
				}
			}
		}

		if (this.getTeam().canRespawn())
		{
			this.getGame().sendMessage(this.getCombatTracker().buildDeathMessage());
			
			this.getStats().increaseDeaths();
		}
		else
		{
			this.getGame().sendMessage(this.getCombatTracker().buildDeathMessage() + ChatColor.AQUA + ChatColor.BOLD.toString() + " FINAL KILL!");
		
			this.getStats().increaseFinalDeaths();
		}
		
		this.getCombatTracker().clear();
		
		Player bukkitPlayer = this.getBukkitPlayer();
		bukkitPlayer.sendTitle(Title.builder()
				.title(ChatColor.RED + ChatColor.BOLD.toString() + "KUOLIT")
				.subtitle(ChatColor.YELLOW + "Synnyt hetkenkuluttua uudelleen")
				.fadeIn(10)
				.stay(40)
				.fadeOut(10)
				.build());
		
		if (this.getTeam().canRespawn())
		{
			this.giveArrowCounter = 0;
			this.respawnTime = 60; //3s
			
			bukkitPlayer.sendMessage(ChatColor.YELLOW + "Synnyt‰‰n uudelleen kolmessa sekunnissa");
			
			this.getGame().makeSpectator(this.getBukkitPlayer());
		}
		else
		{
			this.setAlive(false);
			
			this.getGame().makeSpectator(this.getBukkitPlayer());
		}
		
		if (bukkitPlayer.getLocation().getY() <= 0)
		{
			bukkitPlayer.teleport(this.getTeam().getSpawnPoint());
		}
	}
	
	@SuppressWarnings("deprecation")
	public void updateInventory()
	{
		PlayerInventory inventory = this.getBukkitPlayer().getInventory();
		inventory.clear();
		inventory.setArmorContents(null);
		
		inventory.setItem(0, ItemStackBuilder.builder()
				.type(Material.WOOD_SWORD)
				.amount(1)
				.unbrekable(true)
				.build());
		inventory.setItem(1, ItemStackBuilder.builder()
				.type(Material.FISHING_ROD)
				.amount(1)
				.unbrekable(true)
				.build());
		inventory.setItem(2, ItemStackBuilder.builder()
				.type(Material.BOW)
				.amount(1)
				.unbrekable(true)
				.build());
		inventory.setItem(3, ItemStackBuilder.builder()
				.type(Material.GOLD_PICKAXE)
				.amount(1)
				.unbrekable(true)
				.build());
		
		inventory.setItem(4, ItemStackBuilder.builder()
				.type(Material.STAINED_CLAY)
				.data(this.getTeam().getColor().getData())
				.amount(64)
				.build());
		
		inventory.setItem(8, ItemStackBuilder.builder()
				.type(Material.ARROW)
				.amount(4)
				.build());
		
		inventory.setHelmet(LeatherArmorBuilder.builder()
				.type(Material.LEATHER_HELMET)
				.color(this.getTeam().getColor().getColor())
				.unbrekable(true)
				.build());
		inventory.setChestplate(LeatherArmorBuilder.builder()
				.type(Material.LEATHER_CHESTPLATE)
				.color(this.getTeam().getColor().getColor())
				.unbrekable(true)
				.build());
		inventory.setLeggings(LeatherArmorBuilder.builder()
				.type(Material.LEATHER_LEGGINGS)
				.color(this.getTeam().getColor().getColor())
				.unbrekable(true)
				.build());
		inventory.setBoots(LeatherArmorBuilder.builder()
				.type(Material.LEATHER_BOOTS)
				.color(this.getTeam().getColor().getColor())
				.unbrekable(true)
				.build());
		
		this.updateItemEnchantments();
	}
	
	public void updateItemEnchantments()
	{
		PlayerInventory inventory = this.getBukkitPlayer().getInventory();
		
		Stream.concat(Arrays.stream(inventory.getContents()), Arrays.stream(inventory.getArmorContents())).forEach((i) ->
		{
			if (i != null && i.getType() != Material.AIR)
			{
				//Remove all enchantments?
				for(Enchantment enchantment : i.getEnchantments().keySet())
				{
					i.removeEnchantment(enchantment);
				}
				
				i.addUnsafeEnchantments(this.getEnchantments(i.getType()));
			}
		});
	}
	
	public int getEnchantmentLevel(Material material, Enchantment enchantment)
	{
		Map<Enchantment, Integer> enchantments = this.enchantments.get(material);
		if (enchantments != null)
		{
			return enchantments.get(enchantment);
		}
		
		return 0;
	}
	
	public Map<Enchantment, Integer> getEnchantments(Material material)
	{
		Map<Enchantment, Integer> enchantments = this.enchantments.get(material);
		if (enchantments != null)
		{
			return Collections.unmodifiableMap(enchantments);
		}
		
		return Collections.emptyMap();
	}
	
	public int getLevel()
	{
		UserMonumentWarsStatsDataStorage stats = this.getUser().getDataStorage(UserMonumentWarsStatsDataStorage.class);
		if (stats != null)
		{
			return stats.getLevel();
		}
		
		return 1;
	}
	
	@Override
	public MonumentWarsMinigame getGame()
	{
		return (MonumentWarsMinigame)super.getGame();
	}
	
	@Override
	public MonumentWarsGameStats getStats()
	{
		return (MonumentWarsGameStats)super.getStats();
	}
}
