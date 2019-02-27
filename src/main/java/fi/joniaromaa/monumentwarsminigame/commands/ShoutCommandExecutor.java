package fi.joniaromaa.monumentwarsminigame.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fi.joniaromaa.minigameframework.MinigamePlugin;
import fi.joniaromaa.minigameframework.api.game.Minigame;
import fi.joniaromaa.monumentwarsminigame.game.MonumentWarsMinigame;
import fi.joniaromaa.monumentwarsminigame.player.MonumentWarsMinigamePlayer;
import fi.joniaromaa.parinacorelibrary.api.user.User;
import fi.joniaromaa.parinacorelibrary.api.user.dataset.minigames.UserMonumentWarsStatsDataStorage;
import net.md_5.bungee.api.ChatColor;

public class ShoutCommandExecutor implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player player = (Player)sender;
			
			Minigame minigame = MinigamePlugin.getPlugin().getGameManager().getMinigame(player).orElse(null);
			if (minigame instanceof MonumentWarsMinigame)
			{
				MonumentWarsMinigame monumentWars = (MonumentWarsMinigame)minigame;
				
				MonumentWarsMinigamePlayer minigamePlayer = monumentWars.getMinigamePlayer(player);
				if (minigamePlayer != null)
				{
					if (minigamePlayer.isAlive())
					{
						if (!monumentWars.isSpectator(player))
						{
							User user = minigamePlayer.getUser();
							
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append(ChatColor.GOLD)
								.append("[SHOUT] ")
								.append(user.getDataStorage(UserMonumentWarsStatsDataStorage.class).get().getPrefix())
								.append(' ')
								.append(minigamePlayer.getTeam().getChatColor())
								.append(user.getDisplayName())
								.append(' ')
								.append(ChatColor.GRAY)
								.append(String.join(" ", args));
							
							monumentWars.sendMessage(stringBuilder.toString());
						}
						else
						{
							player.sendMessage(ChatColor.RED + "Katsojat eivät voi käyttää tota komentoa");
						}
					}
					else
					{
						player.sendMessage(ChatColor.RED + "Et ole enään pelissä mukana :(");
					}
				}
			}
			else
			{
				player.sendMessage(ChatColor.RED + "Voit käyttää tätä komento vain pelissä");
			}
		}
		
		return true;
	}
}
