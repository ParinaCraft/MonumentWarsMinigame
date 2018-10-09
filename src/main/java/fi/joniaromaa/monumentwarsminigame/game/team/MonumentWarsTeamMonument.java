package fi.joniaromaa.monumentwarsminigame.game.team;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.Event.Result;
import org.github.paperspigot.Title;

import fi.joniaromaa.minigameframework.utils.BlockUtils;
import fi.joniaromaa.minigameframework.utils.EntityUtils;
import fi.joniaromaa.monumentwarsminigame.MonumentWarsPlugin;
import fi.joniaromaa.monumentwarsminigame.config.MonumentWarsMonumentConfig;
import fi.joniaromaa.monumentwarsminigame.player.MonumentWarsMinigamePlayer;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

public class MonumentWarsTeamMonument
{
	@Getter private final MonumentWarsMinigameTeam team;
	@Getter private final String name;
	@Getter private final Block block;
	
	@Getter private int maxHealth;
	@Getter private int health;
	
	private ArmorStand healthTitleArmorStand;
	private ArmorStand healthArmorStand;
	
	public MonumentWarsTeamMonument(MonumentWarsMinigameTeam team, MonumentWarsMonumentConfig config)
	{
		this.team = team;
		
		this.name = config.getName();
		this.block = config.getLocation().toLocation(team.getGame().getWorld()).getBlock();
		
		this.maxHealth = config.getHealth();
		this.health = this.maxHealth;
		
		BlockUtils.addBlockBreakContract(this.block, (b, p) ->
		{
			if (!this.isAlive())
			{
				return Result.DEFAULT;
			}
			
			//Don't let team mebers rekt their own monument, woah!
			if (team.isTeamMember(p))
			{
				return Result.DENY;
			}
			
			MonumentWarsMinigamePlayer player = this.getTeam().getGame().getMinigamePlayer(p);
			
			player.getStats().increaseMonumentsDamaged();
			player.setGiveExp(player.getGiveExp() + 20);
			
			this.health--;
			if (this.health > 0)
			{
				this.updateArmorStandHealth();
				
				return Result.DENY;
			}
			else
			{
				player.getStats().increaseMonumentsDestroyed();
				
				if (!this.team.getGame().isPrivateGame())
				{
					player.getBukkitPlayer().sendMessage(ChatColor.AQUA + "25 Monument Wars Experience (Monumentin tuhoamis bonus)");
				}
				
				this.getTeam().getGame().sendMessage(ChatColor.valueOf(player.getTeam().getColor().name()) + player.getBukkitPlayer().getName() + ChatColor.GRAY + " tuhosi monumentin!");
				
				this.explode();
				
				for(MonumentWarsMinigamePlayer minigamePlayer : team.getTeamMembers())
				{
					minigamePlayer.getBukkitPlayer().sendTitle(Title.builder()
							.title(ChatColor.RED + this.getName() + " tuhottiin")
							.subtitle(team.canRespawn() ? "" : "Et synny enään uudelleen")
							.fadeIn(10)
							.stay(40)
							.fadeOut(10)
							.build());
					

					minigamePlayer.getBukkitPlayer().playSound(minigamePlayer.getBukkitPlayer().getLocation(), team.canRespawn() ? Sound.WITHER_IDLE : Sound.ENDERDRAGON_GROWL, Integer.MAX_VALUE, 1);
				}
				
				return Result.ALLOW;
			}
		}, MonumentWarsPlugin.getPlugin());
		
		this.healthTitleArmorStand = EntityUtils.addHologram(this.block.getLocation().add(0.5, -0.5, 0.5), ChatColor.WHITE + "--" + ChatColor.AQUA + " Health " + ChatColor.WHITE + "--");
		this.healthArmorStand = EntityUtils.addHologram(this.block.getLocation().add(0.5, -0.7, 0.5), "");
		this.updateArmorStandHealth();
	}
	
	public void explode()
	{
		this.health = 0;
		
		this.block.getWorld().createExplosion(this.block.getLocation(), 8F, true);
		
		this.healthTitleArmorStand.remove();
		this.healthTitleArmorStand = null;
		
		this.healthArmorStand.remove();
		this.healthArmorStand = null;
	}
	
	public void setHealth(int health)
	{
		this.health = health;
		
		this.updateArmorStandHealth();
	}
	
	public void tick()
	{
		float percantages = this.health / (float)this.maxHealth;
		
		if (Math.random() > percantages)
		{
			this.block.getWorld().playEffect(this.block.getLocation(), Effect.SMOKE, (int)(Math.random() * 8));
		}
	}
	
	private void updateArmorStandHealth()
	{
		float percantages = this.health / (float)this.maxHealth;
		float i = 0;
		
		StringBuilder stringBuilder = new StringBuilder(ChatColor.GREEN + ChatColor.BOLD.toString());
		for(; i < percantages; i += 0.1F)
		{
			stringBuilder.append("⬛");
		}
		
		stringBuilder.append(ChatColor.RED + ChatColor.BOLD.toString());
		
		for(; i < 1.0F; i += 0.1F)
		{
			stringBuilder.append("⬛");
		}
		
		this.healthArmorStand.setCustomName(stringBuilder.toString());
	}
	
	public void heal(int amount)
	{
		this.health += amount;
	}
	
	public boolean isAlive()
	{
		return this.health > 0;
	}
	
	public String getHealthString(boolean bold)
	{
		StringBuilder stringBuilder = new StringBuilder();
		
		float percantages = this.health / (float)this.maxHealth;
		if (percantages > 0.66)
		{
			stringBuilder.append(ChatColor.GREEN);
		}
		else if (percantages > 0.33)
		{
			stringBuilder.append(ChatColor.YELLOW);
		}
		else
		{
			stringBuilder.append(ChatColor.RED);
		}
		
		if (bold)
		{
			stringBuilder.append(ChatColor.BOLD);
		}
		
		stringBuilder.append((int)Math.floor(percantages * 100D)).append("%");
		
		return stringBuilder.toString();
	}
}
