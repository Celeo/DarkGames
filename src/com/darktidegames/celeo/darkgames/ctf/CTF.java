package com.darktidegames.celeo.darkgames.ctf;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.darktidegames.celeo.darkgames.DarkGames;
import com.darktidegames.celeo.darkgames.Game;
import com.darktidegames.empyrean.C;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class CTF extends Game implements Listener, CommandExecutor
{

	/** Min number of players per team in a game */
	public int minPlayersInTeam = 2;
	/** Score to win */
	public int scoreToWin = 10;
	/** Place to teleport players to after the game ends */
	public Location warpOut = null;

	/** Join cooldowns */
	public Map<Player, Long> noJoin = new HashMap<Player, Long>();

	public CTFGame theGame = null;
	public Arena theArena = null;

	public CTF(DarkGames plugin)
	{
		super(plugin, "CTF");
	}

	@Override
	public void onEnable()
	{
		load();
		plugin.getCommand("ctf").setExecutor(this);
		getServer().getPluginManager().registerEvents(this, plugin);
		getLogger().info("CTF: Enabled");
	}

	private void setup()
	{
		theGame = new CTFGame(this);
		theArena = new Arena(this, "ctf_arena");
		theArena.setEntrance(C.stringToLocation(plugin.getConfig().getString("ctf.layouts.arenas.ctf_arena."
				+ "entrance")));
		theArena.setBlueBase(C.stringToLocation(plugin.getConfig().getString("ctf.layouts.arenas.ctf_arena."
				+ "blueBase")));
		theArena.setRedBase(C.stringToLocation(plugin.getConfig().getString("ctf.layouts.arenas.ctf_arena."
				+ "redBase")));
		theArena.setBlueJail(C.stringToLocation(plugin.getConfig().getString("ctf.layouts.arenas.ctf_arena."
				+ "blueJail")));
		theArena.setRedJail(C.stringToLocation(plugin.getConfig().getString("ctf.layouts.arenas.ctf_arena."
				+ "redJail")));
		theArena.setBlueFlag(C.stringToLocation(plugin.getConfig().getString("ctf.layouts.arenas.ctf_arena."
				+ "blueFlag")));
		theArena.setRedFlag(C.stringToLocation(plugin.getConfig().getString("ctf.layouts.arenas.ctf_arena."
				+ "redFlag")));
		theGame.setArena(theArena);
		theArena.reset();
		getLogger().info("CTF: Setup the game");
	}

	private void load()
	{
		reloadConfig();
		minPlayersInTeam = plugin.getConfig().getInt("settings.minPlayersInTeam", 2);
		if (plugin.getConfig().getString("ctf.layouts.warpOut") == null)
			warpOut = null;
		else
			warpOut = C.stringToLocation(plugin.getConfig().getString("ctf.layouts.warpOut"));
		scoreToWin = plugin.getConfig().getInt("settings.scoreToWin", 10);
		getLogger().info("CTF: Settings loaded from configuration");
		setup();
	}

	@Override
	public void onDisable()
	{
		for (Player player : theGame.getAllPlayers())
		{
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
		}
		getLogger().info("CTF: Disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player))
			return false;
		Player player = (Player) sender;
		if (args == null || args.length == 0
				|| (args.length == 1 && args[0].equalsIgnoreCase("info")))
		{
			if (theGame.hasPlayer(player))
				theGame.readOut(player);
			else
				player.sendMessage("§e/ctf [join|leave|info]");
			return true;
		}
		String param = args[0].toLowerCase();
		if (param.equals("join"))
		{
			if (!hasPerms(player, "ctf.join"))
				return true;
			if (theGame.hasPlayer(player))
			{
				player.sendMessage("§cYou are already in a game");
				return true;
			}
			boolean locOkay = false;
			for (String region : plugin.wg.getRegionManager(player.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(player.getLocation())))
			{
				if (region.equals("arenajoin"))
					locOkay = true;
				if (region.equals("ctf_arena"))
					locOkay = true;
			}
			if (noJoin.containsKey(player) && noJoin.get(player) != null)
			{
				long left = noJoin.get(player).longValue() + 600000;
				long now = System.currentTimeMillis();
				if (left > now)
				{
					player.sendMessage("§cYou are barred from joining for another "
							+ ((left - now) / 1000) + " seconds");
					return true;
				}
				noJoin.remove(player);
			}
			if (player.hasPermission("ctf.bypassloc"))
				locOkay = true;
			if (!locOkay)
			{
				player.sendMessage("§7You must go to the arena portal to use this command.");
				return true;
			}
			if (!playerInventoryIsEmpty(player))
			{
				player.sendMessage("§7Your inventory is not completely empty.");
				return true;
			}
			theGame.addPlayer(player);
			player.sendMessage("§aJoining game");
			return true;
		}
		if (param.equals("-testlockout"))
		{
			if (!hasPerms(player, "ctf.admin"))
				return true;
			noJoin.put(player, Long.valueOf(System.currentTimeMillis()));
			player.sendMessage("§aDone");
			return true;
		}
		if (param.equals("-seelockout"))
		{
			if (!hasPerms(player, "ctf.admin"))
				return true;
			if (noJoin.containsKey(player) && noJoin.get(player) != null)
			{
				long left = noJoin.get(player).longValue() + 300000;
				long now = System.currentTimeMillis();
				if (left > now)
					player.sendMessage("§cYou are barred from joining for another "
							+ ((left - now) / 1000) + " seconds");
				else
					player.sendMessage("§aYou are able to join the game");
			}
			else
				player.sendMessage("§7Not in map");
			return true;
		}
		if (param.equals("leave"))
		{
			if (!theGame.hasPlayer(player))
			{
				player.sendMessage("§cYou are not in a game");
				return true;
			}
			if (theGame.getTeam(player).jailed.contains(player))
			{
				player.sendMessage("§7You were jailed in the game. You will not be able to rejoin for 10 minutes.");
				noJoin.put(player, Long.valueOf(System.currentTimeMillis()));
			}
			player.sendMessage("§aLeaving game");
			theGame.removePlayer(player);
			return true;
		}
		if (param.equals("-reset"))
		{
			theArena.reset();
			player.sendMessage("§aDone");
			return true;
		}
		if (param.equals("-reload"))
		{
			if (!hasPerms(player, "ctf.admin"))
				return true;
			load();
			player.sendMessage("§aLoaded from configuration file");
			return true;
		}
		player.sendMessage("§e/ctf [join|leave|info]");
		return true;
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

	public boolean playerInventoryIsEmpty(Player player)
	{
		for (ItemStack i : player.getInventory().getContents())
			if (i != null)
				return false;
		for (ItemStack i : player.getInventory().getArmorContents())
			if (i != null && i.getTypeId() != 0)
				return false;
		return true;
	}

	public Location getExit()
	{
		return warpOut;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		theGame.onPlayerDeath(event);
	}

	@EventHandler
	public void onPlayerItemPickup(PlayerPickupItemEvent event)
	{
		theGame.onPlayerItemPickup(event);
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		theGame.onPlayerDropItem(event);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player)
			if (event instanceof EntityDamageByEntityEvent)
				if (((EntityDamageByEntityEvent) event).getDamager() instanceof Player)
					theGame.onPlayerAttackAnother((EntityDamageByEntityEvent) event);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		theGame.onPlayerInteract(event);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		theGame.onPlayerQuit(event);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		theGame.onPlayerRespawn(event);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		theGame.onBlockBreak(event);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		theGame.onBlockPlace(event);
	}

}