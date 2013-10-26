package com.darktidegames.celeo.darkgames.royal;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class RoyalGame
{

	public final Royal royal;
	public List<RoyalPlayer> players;

	public RoyalGame(Royal royal)
	{
		this.royal = royal;
		this.players = new ArrayList<RoyalPlayer>();
	}

	public boolean hasPlayer(Player player)
	{
		return getRoyalPlayer(player) != null;
	}

	public RoyalPlayer getRoyalPlayer(Player player)
	{
		for (RoyalPlayer rp : players)
			if (rp.player.equals(player))
				return rp;
		return null;
	}

	public void addPlayer(Player player)
	{
		if (!hasPlayer(player))
			players.add(new RoyalPlayer(player));
	}

	public boolean removePlayer(Player player)
	{
		return players.remove(getRoyalPlayer(player));
	}

}