package com.darktidegames.celeo.darkgames.royal;

import org.bukkit.entity.Player;

public class RoyalPlayer
{

	public final Player player;
	public Role role = Role.getDefautlRole();
	
	public RoyalPlayer(Player player)
	{
		this.player = player;
	}
	
	public void resetItems()
	{
		player.getInventory().clear();
		role.outfit(player);
	}
	
	public String getName()
	{
		return player.getName();
	}
	
}