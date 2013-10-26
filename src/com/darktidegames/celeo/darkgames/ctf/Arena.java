package com.darktidegames.celeo.darkgames.ctf;

import org.bukkit.Location;

public class Arena
{

	public final CTF plugin;
	public final String id;
	public Location entrance;
	public Location redBase;
	public Location blueBase;
	public Location redJail;
	public Location blueJail;
	public Location redFlag;
	public Location blueFlag;

	public Arena(CTF plugin, String id)
	{
		this.plugin = plugin;
		this.id = id;
	}

	public void returnFlag(Team.Color flag)
	{
		if (flag.equals(Team.Color.BLUE))
		{
			blueFlag.getWorld().getBlockAt(blueFlag).setTypeId(35);
			blueFlag.getWorld().getBlockAt(blueFlag).setData((byte) 11);
		}
		else
		{
			redFlag.getWorld().getBlockAt(redFlag).setTypeId(35);
			redFlag.getWorld().getBlockAt(redFlag).setData((byte) 14);
		}
		return;
	}

	public void setEntrance(Location entrance)
	{
		this.entrance = entrance;
	}

	public void setRedBase(Location redBase)
	{
		this.redBase = redBase;
	}

	public void setBlueBase(Location blueBase)
	{
		this.blueBase = blueBase;
	}

	public void setRedJail(Location redJail)
	{
		this.redJail = redJail;
	}

	public void setBlueJail(Location blueJail)
	{
		this.blueJail = blueJail;
	}

	public void setRedFlag(Location redFlag)
	{
		this.redFlag = redFlag;
	}

	public void setBlueFlag(Location blueFlag)
	{
		this.blueFlag = blueFlag;
	}

	public void reset()
	{
		redFlag.getWorld().getBlockAt(redFlag).setTypeId(35);
		redFlag.getWorld().getBlockAt(redFlag).setData((byte) 14);
		blueFlag.getWorld().getBlockAt(blueFlag).setTypeId(35);
		blueFlag.getWorld().getBlockAt(blueFlag).setData((byte) 11);
	}

}