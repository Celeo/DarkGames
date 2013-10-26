package com.darktidegames.celeo.darkgames.ctf;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.sk89q.worldedit.bukkit.BukkitUtil;

public class CTFGame
{

	public final CTF ctf;
	public Team red;
	public Team blue;
	public boolean running;
	public Arena arena;
	public Player carryingRed;
	public Player carryingBlue;
	public List<Player> toJail;

	public CTFGame(final CTF ctf)
	{
		this.ctf = ctf;
		this.red = new Team(Team.Color.RED);
		this.blue = new Team(Team.Color.BLUE);
		this.running = false;
		this.arena = null;
		this.carryingRed = null;
		this.carryingBlue = null;
		this.toJail = new ArrayList<Player>();
		ctf.getServer().getScheduler().scheduleSyncRepeatingTask(ctf.plugin, new Runnable()
		{
			@Override
			public void run()
			{
				if (getAllPlayers().size() < ctf.minPlayersInTeam)
				{
					pauseGame();
				}
			}
		}, 600L, 600L);
	}

	public void setRunning(boolean running)
	{
		this.running = running;
	}

	public boolean addPlayer(Player player)
	{
		checkSize();
		if (red.players.size() <= blue.players.size())
			red.addPlayer(player);
		else
			blue.addPlayer(player);
		teleportPlayerIn(player);
		player.sendMessage("§6You are on the "
				+ (getTeam(player).color.equals(Team.Color.RED) ? "§4red" : "§9blue")
				+ " §6team");
		checkSize();
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		if (!ctf.plugin.wr.isIgnoringPlayer(player.getName()))
			ctf.plugin.wr.togglePlayerIgnoreFor(player.getName());
		return true;
	}

	private void checkSize()
	{
		int size = 0;
		size += red.players.size();
		size += blue.players.size();
		if (size >= ctf.minPlayersInTeam * 2 && !running)
		{
			running = true;
			return;
		}
		if (size < ctf.minPlayersInTeam && running)
		{
			pauseGame();
			return;
		}
		if (getAllPlayers().size() >= ctf.minPlayersInTeam && !running)
		{
			resumeGame();
			return;
		}
	}

	public void pauseGame()
	{
		running = false;
		broadcast("§6Number of players in the game is §4"
				+ getAllPlayers().size() + ", §6game pausing until §4"
				+ ctf.minPlayersInTeam + " §6total players join");
	}

	public void resumeGame()
	{
		running = true;
		broadcast("§6Game resumed");
	}

	public boolean hasPlayer(Player player)
	{
		return red.hasPlayer(player) || blue.hasPlayer(player);
	}

	public boolean removePlayer(Player player)
	{
		if (hasPlayer(player))
		{
			teleportPlayerOut(player);
			dropFlag(player);
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
		}
		if (ctf.plugin.wr.isIgnoringPlayer(player.getName()))
			ctf.plugin.wr.togglePlayerIgnoreFor(player.getName());
		return red.removePlayer(player) || blue.removePlayer(player);
	}

	public void setArena(Arena arena)
	{
		this.arena = arena;
	}

	public void onPlayerDeath(PlayerDeathEvent event)
	{
		if (!running)
			return;
		Player player = event.getEntity();
		if (!hasPlayer(player))
			return;
		if (carryingRed != null && carryingRed.equals(player))
		{
			carryingRed = null;
			dropFlag(player);
		}
		if (carryingBlue != null && carryingBlue.equals(player))
		{
			carryingBlue = null;
			dropFlag(player);
		}
		getTeam(player).jailed.add(player);
		player.sendMessage("§7You have been sent to the opposing team's jail");
		toJail.add(player);
		ctf.getServer().getScheduler().scheduleSyncDelayedTask(ctf.plugin, new Runnable()
		{
			@Override
			public void run()
			{
				checkJailed();
			}
		}, 80L);
	}

	private void checkJailed()
	{
		if (!red.jailed.isEmpty() && !red.players.isEmpty()
				&& red.jailed.equals(red.players))
		{
			blue.addScore();
			broadcast("§6All players from " + red.getNameWithColor()
					+ " §6where jailed! " + blue.getNameWithColor()
					+ " §6gets a point!");
			releaseAllJailed(red);
		}
		if (!blue.jailed.isEmpty() && !blue.players.isEmpty()
				&& blue.jailed.equals(blue.players))
		{
			red.addScore();
			broadcast("§6All players from " + blue.getNameWithColor()
					+ " §6where jailed! " + red.getNameWithColor()
					+ " §6gets a point!");
			releaseAllJailed(blue);
		}
		checkWinning();
	}

	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if (!running)
			return;
		Player player = event.getPlayer();
		if (!hasPlayer(player))
			return;
		ctf.plugin.emp.ignoreOneRespawn(player.getName());
		event.setRespawnLocation(getOtherTeam(player).color.equals(Team.Color.RED) ? arena.redJail : arena.blueJail);
	}

	public void onPlayerItemPickup(PlayerPickupItemEvent event)
	{
		if (!running)
			return;
		Player player = event.getPlayer();
		if (!hasPlayer(player))
			return;
		ItemStack i = event.getItem().getItemStack();
		if (i.getTypeId() != 35)
			return;
		if (i.getDurability() != (short) 11 && i.getDurability() != (short) 14)
			return;
		Team.Color flag = getFlagColor(i.getDurability());
		if (flag == null)
			return;
		if (getTeam(player).color.equals(flag))
		{
			arena.returnFlag(flag);
			player.getInventory().remove(35);
		}
		else
			pickupFlag(player, flag);
	}

	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		if (!running)
			return;
		Player player = event.getPlayer();
		if (!hasPlayer(player))
			return;
		ItemStack i = event.getItemDrop().getItemStack();
		if (i.getTypeId() != 35)
			return;
		Team.Color flag = getFlagColor(i.getDurability());
		if (carryingRed != null && carryingRed.equals(player)
				&& flag.equals(Team.Color.RED))
		{
			carryingRed = null;
			dropFlag(player);
			return;
		}
		if (carryingBlue != null && carryingBlue.equals(player)
				&& flag.equals(Team.Color.BLUE))
		{
			carryingBlue = null;
			dropFlag(player);
			return;
		}
	}

	public void onPlayerAttackAnother(EntityDamageByEntityEvent event)
	{
		if (!running)
			return;
		Player attacker = (Player) event.getDamager();
		Player defender = (Player) event.getEntity();
		if (!hasPlayer(attacker) || !hasPlayer(defender))
			return;
		if (!onTheirSide(attacker))
		{
			attacker.sendMessage("§7You cannot attack when not on your side");
			event.setCancelled(true);
		}
		if (onTheirSide(defender))
		{
			attacker.sendMessage("§7Players cannot be hurt on their side");
			event.setCancelled(true);
		}
		if (getTeam(attacker).jailed.contains(attacker)
				|| getTeam(defender).jailed.contains(defender))
			event.setCancelled(true);
	}

	public void onBlockBreak(BlockBreakEvent event)
	{
		if (!running)
			return;
		Player player = event.getPlayer();
		if (!hasPlayer(player))
			return;
		Team team = getTeam(player);
		Block block = event.getBlock();
		if (ctf.plugin.wg.getRegionManager(block.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(block.getLocation())).contains(arena.id
				+ team.color.name().toLowerCase() + "flag"))
		{
			event.setCancelled(true);
			player.sendMessage("§7You cannot take your own flag");
		}
	}

	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (!running)
			return;
		Player player = event.getPlayer();
		if (!hasPlayer(player))
			return;
		dropFlag(player);
	}

	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (!running)
			return;
		Player player = event.getPlayer();
		if (!hasPlayer(player))
			return;
		Block clicked = event.getClickedBlock();
		if (event.getAction().equals(Action.LEFT_CLICK_AIR)
				|| event.getAction().equals(Action.RIGHT_CLICK_AIR))
			return;
		if (clicked == null)
			return;
		if (clicked.getType().equals(Material.GLOWSTONE))
		{
			Team team = getTeam(player);
			if (team.jailed.contains(player))
			{
				player.sendMessage("§7You cannot free yourself from jail.");
				return;
			}
			Team other = getOtherTeam(player);
			for (String id : ctf.plugin.wg.getRegionManager(clicked.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(clicked.getLocation())))
			{
				if (id.equalsIgnoreCase(arena.id + other.color.name() + "jail"))
					releaseAllJailed(team);
			}
		}
		if (!ctf.plugin.wg.canBuild(player, clicked))
			return;
		ItemStack hand = player.getItemInHand();
		if (hand.getTypeId() == 35)
		{
			if (clicked.getTypeId() != 35)
			{
				player.sendMessage("§7You must click on your flag on its stand to gain the point!");
				event.setCancelled(true);
				return;
			}
			event.setCancelled(true);
			player.getInventory().remove(hand);
			Team.Color flagColor = getFlagColor(hand.getDurability());
			if (flagColor.equals(this.getFlagColor(clicked.getData())))
			{
				player.sendMessage("§7You must place the the other team's flag on your flag to gain the point");
				return;
			}
			if (flagColor.equals(Team.Color.RED)
					&& !getTeam(player).color.equals(Team.Color.RED))
			{
				blue.addScore();
				dropFlag(player);
				arena.returnFlag(flagColor);
				carryingRed = null;
				checkWinning();
				return;
			}
			if (flagColor.equals(Team.Color.BLUE)
					&& !getTeam(player).color.equals(Team.Color.BLUE))
			{
				red.addScore();
				dropFlag(player);
				arena.returnFlag(flagColor);
				carryingBlue = null;
				checkWinning();
				return;
			}
			event.setCancelled(true);
		}
	}

	private void releaseAllJailed(Team team)
	{
		List<Player> remove = new ArrayList<Player>();
		for (Player p : team.jailed)
			remove.add(p);
		for (Player p : remove)
		{
			team.jailed.remove(p);
			p.teleport(team.equals(red) ? arena.redBase : arena.blueBase);
			p.sendMessage("§aYou have been freed from jail!");
		}
		broadcast("§6All members from " + team.getNameWithColor()
				+ " §6have been freed from jail!");
	}

	public void broadcast(String message)
	{
		for (Player player : getAllPlayers())
			player.sendMessage(message);
	}

	public void onPlayerQuit(PlayerQuitEvent event)
	{
		removePlayer(event.getPlayer());
	}

	public void checkWinning()
	{
		broadcast("§4Red §7team: §6" + red.score);
		broadcast("§9Blue §7team: §6" + blue.score);
		if (red.score >= ctf.scoreToWin || blue.score >= ctf.scoreToWin)
		{
			Team.Color win = red.score >= ctf.scoreToWin ? red.color : blue.color;
			running = false;
			for (Player player : getAllPlayers())
			{
				player.sendMessage((win.equals(Team.Color.RED) ? "§4Red" : "§9Blue")
						+ " §6has won the game!");
				teleportPlayerOut(player);
			}
			red.clear();
			blue.clear();
			arena.reset();
			ctf.getLogger().info("CTF Complete. Red: " + red.getPlayerNames()
					+ ", Blue: " + blue.getPlayerNames());
		}
	}

	public List<Player> getAllPlayers()
	{
		List<Player> ret = new ArrayList<Player>();
		ret.addAll(red.players);
		ret.addAll(blue.players);
		return ret;
	}

	public boolean onTheirSide(Player player)
	{
		Team team = getTeam(player);
		if (team == null)
			return true;
		for (String id : ctf.plugin.wg.getRegionManager(player.getWorld()).getApplicableRegionsIDs(BukkitUtil.toVector(player.getLocation())))
			if (id.equalsIgnoreCase(arena.id + team.color.name()))
				return true;
		return false;
	}

	public boolean sameTeam(Player one, Player two)
	{
		if (!hasPlayer(one) || !hasPlayer(two))
			return false;
		return getTeam(one).color.equals(getTeam(two).color);
	}

	public Team.Color getFlagColor(int durability)
	{
		if (durability == 11)
			return Team.Color.BLUE;
		if (durability == 14)
			return Team.Color.RED;
		return null;
	}

	public Team getTeam(Player player)
	{
		if (red.hasPlayer(player))
			return red;
		return blue.hasPlayer(player) ? blue : null;
	}

	public Team getOtherTeam(Player player)
	{
		if (red.hasPlayer(player))
			return blue;
		return blue.hasPlayer(player) ? red : null;
	}

	private void pickupFlag(Player player, Team.Color flag)
	{
		player.getInventory().setArmorContents(getArmorForFlag(flag));
		if (flag.equals(Team.Color.RED))
			carryingRed = player;
		else if (flag.equals(Team.Color.BLUE))
			carryingBlue = player;
	}

	public ItemStack[] getArmorForFlag(Team.Color flag)
	{
		ItemStack[] ret = new ItemStack[4];
		LeatherArmorMeta meta = null;
		ItemStack item = null;
		for (int i = 0; i < ret.length; i++)
		{
			switch (i)
			{
			default:
			case 0:
				item = new ItemStack(Material.LEATHER_BOOTS);
				break;
			case 1:
				item = new ItemStack(Material.LEATHER_CHESTPLATE);
				break;
			case 2:
				item = new ItemStack(Material.LEATHER_HELMET);
				break;
			case 3:
				item = new ItemStack(Material.LEATHER_LEGGINGS);
				break;
			}
			meta = (LeatherArmorMeta) item.getItemMeta();
			meta.setColor(flag.equals(Team.Color.BLUE) ? Color.fromBGR(225, 134, 30) : Color.RED);
			item.setItemMeta(meta);
			ret[i] = item;
		}
		return ret;
	}

	@SuppressWarnings("deprecation")
	private void dropFlag(Player player)
	{
		player.getInventory().setArmorContents(new ItemStack[4]);
		player.updateInventory();
	}

	public void teleportPlayerIn(Player player)
	{
		player.teleport(arena.entrance);
	}

	public void teleportPlayerOut(Player player)
	{
		player.teleport(ctf.getExit());
	}

	public void readOut(Player player)
	{
		player.sendMessage("§7Game " + (running ? "§ais" : "§4is not")
				+ " §7running");
		player.sendMessage("§7Team §4red (" + red.score + "): §7"
				+ red.getPlayerNames() + " (" + red.players.size() + ")");
		player.sendMessage("§7Team §9blue (" + blue.score + "): §7"
				+ blue.getPlayerNames() + " (" + blue.players.size() + ")");
	}

}