package com.darktidegames.celeo.darkgames;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.darktidegames.celeo.darkgames.ctf.CTF;
import com.darktidegames.celeo.darkgames.duel.Duel;
import com.darktidegames.celeo.warreport.WarReportPlugin;
import com.darktidegames.empyrean.C;
import com.darktidegames.empyrean.WarsPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class DarkGames extends JavaPlugin
{

	/** WorldGuard connection */
	public WorldGuardPlugin wg = null;
	/** Emyprean connection */
	public WarsPlugin emp = null;
	/** WarReport connection */
	public WarReportPlugin wr = null;

	/** CTF */
	public CTF ctf = null;
	/** Duels */
	public Duel duel = null;

	private Logger combatLogger = Logger.getLogger("DarkGames.PVP");
	public List<String> monitoring = new ArrayList<String>();

	@Override
	public void onLoad()
	{
		getDataFolder().mkdirs();
		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
		getLogger().info("Pre-enable setup complete");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player))
			return false;
		Player player = (Player) sender;
		if (args == null || args.length != 1)
			return false;
		if (!player.hasPermission("wars.monitorpvp"))
			return false;
		if (args[0].equalsIgnoreCase("-list"))
		{
			player.sendMessage("§7Monitoring: "
					+ C.listToString(monitoring).replace(",", ", "));
			return true;
		}
		if (monitoring.contains(args[0]))
		{
			monitoring.remove(args[0]);
			player.sendMessage("§7No longer monitoring " + args[0]);
		}
		else
		{
			monitoring.add(args[0]);
			player.sendMessage("§7Now monitoring " + args[0]);
		}
		return true;
	}

	@Override
	public void onEnable()
	{

		getDataFolder().mkdirs();

		Plugin test = getServer().getPluginManager().getPlugin("WorldGuard");
		if (test == null)
			getLogger().warning("Could not connect to WorldGuard!");
		else
			wg = (WorldGuardPlugin) test;
		test = getServer().getPluginManager().getPlugin("Empyrean");
		if (test != null)
			emp = (WarsPlugin) test;
		test = getServer().getPluginManager().getPlugin("WarReport");
		if (test != null)
			wr = (WarReportPlugin) test;

		// ctf = new CTF(this);
		// ctf.onEnable();

		// duel = new Duel(this);
		// duel.onEnable();

		getServer().getPluginManager().registerEvents(new PVP(this), this);

		try
		{
			combatLogger.setUseParentHandlers(false);
			File temp = new File(getDataFolder().getAbsolutePath()
					+ "/combat.log");
			if (!temp.exists())
				temp.createNewFile();
			FileHandler handler = new FileHandler(temp.getAbsolutePath(), true);
			handler.setFormatter(new Formatter()
			{
				@Override
				public String format(LogRecord logRecord)
				{
					return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:S z").format(new Date(logRecord.getMillis()))
							+ " " + logRecord.getMessage() + "\n";
				}
			});
			combatLogger.addHandler(handler);
			getLogger().info("Combat logged setup");
		}
		catch (Exception e)
		{
			combatLogger.setUseParentHandlers(true);
			getLogger().warning("Could not properly setup combat logger");
		}

		monitoring = getConfig().getStringList("monitoring");
		if (monitoring == null)
			monitoring = new ArrayList<String>();

		getLogger().info("Enabled");
	}

	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		getConfig().set("monitoring", monitoring);
		saveConfig();
		getLogger().info("Disabled");
	}

	public boolean hasPerms(Player player, String node)
	{
		if (!player.hasPermission(node))
		{
			player.sendMessage("§cYou cannot use that command");
			return false;
		}
		return true;
	}

	public void logCombat(String message)
	{
		combatLogger.info(message.replace("§7", ""));
	}

}