package fi.joniaromaa.monumentwarsminigame.inventory;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fi.joniaromaa.monumentwarsminigame.MonumentWarsPlugin;
import fi.joniaromaa.monumentwarsminigame.player.MonumentWarsMinigamePlayer;
import fi.joniaromaa.parinacorelibrary.bukkit.builder.ItemStackBuilder;
import fi.joniaromaa.parinacorelibrary.bukkit.inventory.PlayerInterfaceInventory;
import net.md_5.bungee.api.ChatColor;

public class MonumentWarsShopUserInterface extends PlayerInterfaceInventory
{
	private final MonumentWarsMinigamePlayer player;
	
	public MonumentWarsShopUserInterface(MonumentWarsMinigamePlayer player)
	{
		super(MonumentWarsPlugin.getPlugin(), 45, ChatColor.YELLOW + "Kauppa");
		
		this.player = player;
		
		this.updateShop();
	}
	
	private void updateShop()
	{
		this.setItem(12, this.createShopIcon(Material.WOOD_SWORD, Enchantment.DAMAGE_ALL), this::buyEnchantment);
		this.setItem(14, ItemStackBuilder.builder().type(Material.GOLDEN_APPLE).amount(2).displayName(ChatColor.YELLOW + "Kultaomppui 2x (2)").build(), this::buyGoldenApple);
		
		this.setItem(28, this.createShopIcon(Material.LEATHER_HELMET, Enchantment.PROTECTION_ENVIRONMENTAL), this::buyEnchantment);
		this.setItem(30, this.createShopIcon(Material.LEATHER_CHESTPLATE, Enchantment.PROTECTION_ENVIRONMENTAL), this::buyEnchantment);
		this.setItem(32, this.createShopIcon(Material.LEATHER_LEGGINGS, Enchantment.PROTECTION_ENVIRONMENTAL), this::buyEnchantment);
		this.setItem(34, this.createShopIcon(Material.LEATHER_BOOTS, Enchantment.PROTECTION_ENVIRONMENTAL), this::buyEnchantment);
	}
	
	private ItemStack createShopIcon(Material material, Enchantment enchantment)
	{
		int level = this.player.getEnchantmentLevel(material, enchantment) + 1;
		int maxLevel = this.getMaxUpgrade(material, enchantment);
		
		return ItemStackBuilder.builder()
				.type(material)
				.addEnchantment(enchantment, Math.min(level, maxLevel))
				.displayName(level > maxLevel ? ChatColor.RED + "Päivitetty täyteen" : ChatColor.YELLOW + "Päivitä lumous " + this.getLocalization(enchantment) + " (" + this.getCost(material, enchantment, level) + " Gold)")
				.build();
	}
	
	private String getLocalization(Enchantment enchantment)
	{
		if (enchantment.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
		{
			return "suojaus";
		}
		else if (enchantment.equals(Enchantment.DAMAGE_ALL))
		{
			return "terävyys";
		}
		
		return "tuntematon";
	}
	
	private void buyEnchantment(InventoryClickEvent event)
	{
		ItemStack item = event.getCurrentItem();
		
		for(Enchantment enchantment : item.getEnchantments().keySet())
		{
			if (this.tryUpgrade(item.getType(), enchantment))
			{
				Player bukkitPlayer = this.player.getBukkitPlayer();
				bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.ANVIL_USE, Integer.MAX_VALUE, 1);
				
				this.player.updateItemEnchantments();
			}
			
			break; //Only support the first enchantment
		}
	}
	
	private void buyGoldenApple(InventoryClickEvent event)
	{
		int cost = 2;
		if (this.player.getGold() >= cost)
		{
			this.player.setGold(this.player.getGold() - cost);
			
			Player bukkitPlayer = this.player.getBukkitPlayer();
			bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.EAT, Integer.MAX_VALUE, 1);
			
			bukkitPlayer.getInventory().addItem(ItemStackBuilder.builder().type(Material.GOLDEN_APPLE).amount(2).build());
		}
	}
	
	private boolean tryUpgrade(Material material, Enchantment enchantment)
	{
		int level = this.player.getEnchantmentLevel(material, enchantment) + 1;
		if (level <= this.getMaxUpgrade(material, enchantment))
		{
			int cost = this.getCost(material, enchantment, level);
			if (this.player.getGold() >= cost)
			{
				this.player.setGold(this.player.getGold() - cost);
				this.player.addEnchantment(material, enchantment, level);
				
				this.updateShop();
				
				return true;
			}
		}
		
		return false;
	}
	
	private int getMaxUpgrade(Material material, Enchantment enchantment)
	{
		switch(material)
		{
			case WOOD_SWORD:
			{
				if (enchantment.equals(Enchantment.DAMAGE_ALL))
				{
					return 10;
				}
				
				break;
			}
			case LEATHER_HELMET:
			case LEATHER_BOOTS:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			{
				if (enchantment.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
				{
					return 5;
				}
				
				break;
			}
			default:
				break;
		}
		
		return 0;
	}
	
	private int getCost(Material material, Enchantment enchantment, int level)
	{
		switch(material)
		{
			case WOOD_SWORD:
			{
				if (enchantment.equals(Enchantment.DAMAGE_ALL))
				{
					return level * 30;
				}
			}
			break;
			case LEATHER_HELMET:
			case LEATHER_BOOTS:
			{
				if (enchantment.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
				{
					return level * 10;
				}
			}
			break;
			case LEATHER_CHESTPLATE:
			{
				if (enchantment.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
				{
					return level * 30;
				}
			}
			break;
			case LEATHER_LEGGINGS:
			{
				if (enchantment.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
				{
					return level * 20;
				}
			}
			break;
			default:
			{
			}
			break;
		}
		
		return Integer.MAX_VALUE;
	}
}
