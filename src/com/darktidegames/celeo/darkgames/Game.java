package com.darktidegames.celeo.darkgames;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Base
 * 
 * @author Celeo
 */
public class Game
{

	/** DarkGames connection */
	public final DarkGames plugin;
	/** Name of the game */
	public final String name;

	/**
	 * 
	 * @param plugin
	 *            DarkGames
	 * @param name
	 *            String
	 */
	public Game(final DarkGames plugin, final String name)
	{
		this.plugin = plugin;
		this.name = name;
	}

	public void onEnable()
	{
	}

	public void onDisable()
	{
	}

	/**
	 * Shortcut for getting the Server object
	 * 
	 * @return Server
	 */
	public Server getServer()
	{
		return Bukkit.getServer();
	}

	/**
	 * Shortcut for getting a World object
	 * 
	 * @param name
	 *            String
	 * @return World
	 */
	public World getWorld(String name)
	{
		return Bukkit.getServer().getWorld(name);
	}

	/**
	 * Shortcut for getting the plugin Logger object
	 * 
	 * @return Logger
	 */
	public Logger getLogger()
	{
		return plugin.getLogger();
	}

	/**
	 * Shortcut for getting the plugin's configuration accessor
	 * 
	 * @return FileConfiguration
	 */
	public FileConfiguration getConfig()
	{
		return plugin.getConfig();
	}

	/**
	 * Shortcut for reloading the plugin's configuration from file
	 */
	public void reloadConfig()
	{
		plugin.reloadConfig();
	}

}