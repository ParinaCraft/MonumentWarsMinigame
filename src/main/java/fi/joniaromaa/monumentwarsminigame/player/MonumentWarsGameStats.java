package fi.joniaromaa.monumentwarsminigame.player;

import fi.joniaromaa.parinacorelibrary.api.storage.dataset.StorageDataSet;
import fi.joniaromaa.parinacorelibrary.api.storage.dataset.StorageDataUpdateType;
import fi.joniaromaa.parinacorelibrary.api.storage.dataset.StorageDataUpdater;
import fi.joniaromaa.parinacorelibrary.api.user.dataset.UserDataStorage;

@StorageDataSet(schema = "monument_wars", table = "user_stats")
public class MonumentWarsGameStats implements UserDataStorage
{
	@StorageDataUpdater(value = "time_played", type = StorageDataUpdateType.INCREMENT)
	private int timePlayed;

	@StorageDataUpdater(value = "total_exp", type = StorageDataUpdateType.INCREMENT)
	private int totalExp;
	
	@StorageDataUpdater(value = "kills", type = StorageDataUpdateType.INCREMENT)
	private int kills;
	@StorageDataUpdater(value = "final_kills", type = StorageDataUpdateType.INCREMENT)
	private int finalKills;
	
	@StorageDataUpdater(value = "deaths", type = StorageDataUpdateType.INCREMENT)
	private int deaths;
	@StorageDataUpdater(value = "final_deaths", type = StorageDataUpdateType.INCREMENT)
	private int finalDeaths;
	
	@StorageDataUpdater(value = "blocks_placed", type = StorageDataUpdateType.INCREMENT)
	private int blocksPlaced;
	@StorageDataUpdater(value = "blocks_broken", type = StorageDataUpdateType.INCREMENT)
	private int blocksBroken;
	
	@StorageDataUpdater(value = "capture_points_capped", type = StorageDataUpdateType.INCREMENT)
	private int capturePointsCapped;
	
	@StorageDataUpdater(value = "monuments_damaged", type = StorageDataUpdateType.INCREMENT)
	private int monumentsDamaged;
	@StorageDataUpdater(value = "monuments_destroyed", type = StorageDataUpdateType.INCREMENT)
	private int monumentsDestroyed;
	
	@StorageDataUpdater(value = "plays", type = StorageDataUpdateType.INCREMENT)
	private int plays;
	@StorageDataUpdater(value = "wins", type = StorageDataUpdateType.INCREMENT)
	private int wins;
	
	public void increaseTimePlayed()
	{
		this.timePlayed++;
	}
	
	public void incresementExp()
	{
		this.totalExp += 25;
	}
	
	public void increaseKills()
	{
		this.kills++;
	}
	
	public void increaseFinalKills()
	{
		this.finalKills++;
	}
	
	public void increaseDeaths()
	{
		this.deaths++;
	}
	
	public void increaseFinalDeaths()
	{
		this.finalDeaths++;
	}
	
	public void increateBlocksPlaced()
	{
		this.blocksPlaced++;
	}
	
	public void increaseBlocksBroken()
	{
		this.blocksBroken++;
	}
	
	public void increaseMonumentsDamaged()
	{
		this.monumentsDamaged++;
	}
	
	public void increaseMonumentsDestroyed()
	{
		this.monumentsDestroyed++;
		this.totalExp += 25;
	}
	
	public void increaseCapturePointCapped()
	{
		this.capturePointsCapped++;
	}
	
	public void lostGame()
	{
		this.plays++;
	}
	
	public void wonGame()
	{
		this.totalExp += 75;
		
		this.plays++;
		this.wins++;
	}
}
