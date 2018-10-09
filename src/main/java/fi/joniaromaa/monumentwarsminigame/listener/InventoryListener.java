package fi.joniaromaa.monumentwarsminigame.listener;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import fi.joniaromaa.minigameframework.MinigamePlugin;
import fi.joniaromaa.monumentwarsminigame.game.MonumentWarsMinigame;

public class InventoryListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClickEvent(InventoryClickEvent event)
	{
		HumanEntity human = event.getWhoClicked();
		if (event.getClickedInventory() != human.getInventory()) //Was not their inventory (Crafting slot)
		{
			if (human instanceof Player)
			{
				Player player = (Player)human;
				
				MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
				{
					if (m instanceof MonumentWarsMinigame)
					{
						event.setCancelled(true);
					}
				});
			}
		}
	}
}
