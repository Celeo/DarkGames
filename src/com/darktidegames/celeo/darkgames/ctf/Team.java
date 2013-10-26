package com.darktidegames.celeo.darkgames.ctf;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class Team
{

	final Color color;
	int score;
	List<Player> players;
	List<Player> jailed;

	public Team(Color color)
	{
		this.color = color;
		this.score = 0;
		this.players = new ArrayList<Player>();
		this.jailed = new ArrayList<Player>();
	}

	public boolean addPlayer(Player player)
	{
		if (hasPlayer(player))
			return false;
		players.add(player);
		return true;
	}

	public boolean hasPlayer(Player player)
	{
		return players.contains(player);
	}

	public boolean removePlayer(Player player)
	{
		return players.remove(player);
	}

	public void setScore(int score)
	{
		this.score = score;
	}

	public enum Color
	{
		RED, BLUE;
	}

	public void addScore()
	{
		score++;
	}

	public void clear()
	{
		players.clear();
		score = 0;
	}

	public String getPlayerNames()
	{
		String ret = "";
		String add = "";
		for (Player in : players)
		{
			add = in.getName();
			if (jailed.contains(in))
				add = "(J)" + add;
			if (ret.equals(""))
				ret = add;
			else
				ret += ", " + add;
		}
		return ret;
	}

	public String getNameWithColor()
	{
		if (color.equals(Team.Color.BLUE))
			return "§9Blue";
		else if (color.equals(Team.Color.RED))
			return "§4Red";
		return null;
	}

}