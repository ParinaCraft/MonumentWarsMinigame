package fi.joniaromaa.monumentwarsminigame.listener;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

import fi.joniaromaa.minigameframework.MinigamePlugin;
import fi.joniaromaa.monumentwarsminigame.game.MonumentWarsGameStatus;
import fi.joniaromaa.monumentwarsminigame.game.MonumentWarsMinigame;

public class EntityListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent event)
	{
		Entity entity = event.getEntity();
		if (entity instanceof Player)
		{
			Player player = (Player)entity;
			MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
			{
				if (m instanceof MonumentWarsMinigame)
				{
					event.setCancelled(true);
					
					((CraftHumanEntity)player).getHandle().getFoodData().eat(20, 20); //Set food level and saturation
				}
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDismountEvent(EntityDismountEvent event)
	{
		Entity livingEntity = event.getEntity();
		if (livingEntity instanceof Player)
		{
			Player player = (Player)livingEntity;
			
			MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
			{
				if (m instanceof MonumentWarsMinigame)
				{
					MonumentWarsMinigame monumentWars = (MonumentWarsMinigame)m;
					if (monumentWars.getStatus() == MonumentWarsGameStatus.ENDED)
					{
						event.setCancelled(true);
					}
				}
			});
		}
	}
}
