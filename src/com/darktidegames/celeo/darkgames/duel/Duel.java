package com.darktidegames.celeo.darkgames.duel;

import org.bukkit.Location;
import org.bukkit.event.Listener;

import com.darktidegames.celeo.darkgames.DarkGames;
import com.darktidegames.celeo.darkgames.Game;

public class Duel extends Game implements Listener
{

	/** Min number of players per team in a game */
	public int minPlayersInTeam = 2;
	/** Score to win */
	public int scoreToWin = 10;
	/** Place to teleport players to after the game ends */
	Location warpOut = null;

	/** Duel */
	DuelGame game = null;

	public Duel(DarkGames plugin)
	{
		super(plugin, "Duel");
	}

	@Override
	public void onEnable()
	{
		load();
		getServer().getPluginManager().registerEvents(this, plugin);
		getLogger().info("Duel: Enabled");
	}

	private void load()
	{
	}

	@Override
	public void onDisable()
	{
		getLogger().info("Duel: Disabled");
	}

	public Location getExit()
	{
		return warpOut;
	}

}