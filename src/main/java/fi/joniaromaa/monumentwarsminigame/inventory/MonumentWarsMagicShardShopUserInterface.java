package fi.joniaromaa.monumentwarsminigame.inventory;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fi.joniaromaa.monumentwarsminigame.MonumentWarsPlugin;
import fi.joniaromaa.monumentwarsminigame.player.MonumentWarsMinigamePlayer;
import fi.joniaromaa.parinacorelibrary.bukkit.builder.ItemStackBuilder;
import fi.joniaromaa.parinacorelibrary.bukkit.builder.PotionBuilder;
import fi.joniaromaa.parinacorelibrary.bukkit.inventory.PlayerInterfaceInventory;
import net.md_5.bungee.api.ChatColor;

public class MonumentWarsMagicShardShopUserInterface extends PlayerInterfaceInventory
{
	private final MonumentWarsMinigamePlayer player;
	
	public MonumentWarsMagicShardShopUserInterface(MonumentWarsMinigamePlayer player)
	{
		super(MonumentWarsPlugin.getPlugin(), 27, ChatColor.DARK_PURPLE + "Magic Shard Kauppa");
		
		this.player = player;
		
		this.updateShop();
	}
	
	public void updateShop()
	{
		this.setItem(10, PotionBuilder.builder().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 30, 0, false, false)).displayName(ChatColor.WHITE + "Näkymättömyys (30s)").build(), this::buyPotion);
		this.setItem(12, PotionBuilder.builder().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 30, 4, false, false)).displayName(ChatColor.GREEN + "Hyppy (30s)").build(), this::buyPotion);
		this.setItem(14, PotionBuilder.builder().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 1, false, false)).displayName(ChatColor.AQUA + "Nopeus (30s)").build(), this::buyPotion);
		
		this.setItem(16, ItemStackBuilder.builder().type(Material.ENDER_PEARL).displayName(ChatColor.DARK_PURPLE + "Äärisilmä (8 Magic Shard)").build(), this::buyEnderPearl);
	
		//this.setItem(6, ItemStackBuilder.builder().type(Material.GOLDEN_APPLE).displayName(ChatColor.GREEN + "Heal Monument (5)").build(), this::buyHeal);
	}
	
	private void buyPotion(InventoryClickEvent event)
	{
		ItemStack item = event.getCurrentItem();
		
		int cost = 2;
		if (this.player.getMagicShards() >= cost)
		{
			this.player.setMagicShards(this.player.getMagicShards() - cost);
			
			this.player.getBukkitPlayer().getInventory().addItem(item);
		}
	}
	
	private void buyEnderPearl(InventoryClickEvent event)
	{
		int cost = 8;
		if (this.player.getMagicShards() >= cost)
		{
			this.player.setMagicShards(this.player.getMagicShards() - cost);
			
			this.player.getBukkitPlayer().getInventory().addItem(ItemStackBuilder.builder()
					.type(Material.ENDER_PEARL)
					.build());
		}
	}
	
	/*private void buyHeal(InventoryClickEvent event)
	{
		int cost = 5;
		if (this.player.getMagicShards() >= cost)
		{
			this.player.getTeam().getMonuments().stream()
				.filter((m) -> m.isAlive())
				.min((o1, o2) -> Integer.compare(o1.getHealth(), o2.getHealth()))
				.ifPresent((m) ->
				{
					m.heal(5);
					
					this.player.setMagicShards(this.player.getMagicShards() - cost);
				});
		}
	}*/
}
