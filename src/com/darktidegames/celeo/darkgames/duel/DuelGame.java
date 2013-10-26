package com.darktidegames.celeo.darkgames.duel;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class DuelGame
{

	final Duel duel;
	List<Player> players = null;

	public DuelGame(Duel duel)
	{
		this.duel = duel;
		players = new ArrayList<Player>();
	}

	public void addPlayer(Player player)
	{
		if (hasPlayer(player))
			return;
		if (!duel.plugin.wr.isIgnoringPlayer(player.getName()))
			duel.plugin.wr.togglePlayerIgnoreFor(player.getName());
	}

	public boolean hasPlayer(Player player)
	{
		return players.contains(player);
	}

	public void removePlayer(Player player)
	{
		if (!hasPlayer(player))
			return;
		players.remove(player);
		teleportPlayerOut(player);
		player.getInventory().setArmorContents(null);
		player.getInventory().clear();
		if (duel.plugin.wr.isIgnoringPlayer(player.getName()))
			duel.plugin.wr.togglePlayerIgnoreFor(player.getName());
	}

	public void teleportPlayerOut(Player player)
	{
		player.teleport(duel.getExit());
	}

}