package com.darktidegames.celeo.darkgames;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.darktidegames.empyrean.C;

public class PVP implements Listener
{

	final DarkGames plugin;

	public PVP(DarkGames plugin)
	{
		this.plugin = plugin;
	}

	@SuppressWarnings("boxing")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTakePVPDamage(EntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		if (!(event.getDamager() instanceof Player))
			return;
		Player hurt = (Player) event.getEntity();
		Player damager = (Player) event.getDamager();
		boolean cancelled = event.isCancelled();
		int damage = event.getDamage();
		if (plugin.monitoring.contains(hurt.getName())
				|| plugin.monitoring.contains(damager.getName()))
			plugin.logCombat(String.format("%s: %s (%d) [%s] -> %s (%d) [%s] for %d", !cancelled, damager.getName(), damager.getHealth(), C.locationToString(damager.getLocation()), hurt.getName(), hurt.getHealth(), C.locationToString(hurt.getLocation()), damage));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		if (plugin.monitoring.contains(player.getName()))
			plugin.logCombat(String.format("%s died%s", player.getName(), player.getKiller() == null ? "" : ", killed by "
					+ player.getKiller().getName()));
	}

}