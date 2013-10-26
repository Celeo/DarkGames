package com.darktidegames.celeo.darkgames.royal;

import org.bukkit.event.Listener;

import com.darktidegames.celeo.darkgames.DarkGames;
import com.darktidegames.celeo.darkgames.Game;

public class Royal extends Game implements Listener
{

	RoyalGame theGame;

	public Royal(DarkGames plugin)
	{
		super(plugin, "Royal");
		setup();
	}

	private void setup()
	{
		theGame = new RoyalGame(this);
	}

}