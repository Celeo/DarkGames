package com.darktidegames.celeo.darkgames.royal;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Role
{

	public final String name;
	public double attack;
	public double attackSpeed;
	public double armor;
	public double dodge;
	public ItemStack armor_head;
	public ItemStack armor_chest;
	public ItemStack armor_pants;
	public ItemStack armor_boots;
	public List<ItemStack> give;

	public static Role warrior = new Role("warrior", 1.50, .50, 2.0, 0.0, new ItemStack[4], new ArrayList<ItemStack>());
	public static Role ranger = new Role("ranger", 1.0, 1.0, 1.0, 0.0, new ItemStack[4], new ArrayList<ItemStack>());
	public static Role rouge = new Role("rouge", 1.0, 1.5, 0.75, 25.0, new ItemStack[4], new ArrayList<ItemStack>());

	public Role(String name)
	{
		this(name, 1.0, 1.0, 1.0, 0.0, new ItemStack[4], new ArrayList<ItemStack>());
	}

	public Role(String name, double attack, double attackSpeed, double armor, double dodge, ItemStack[] items, List<ItemStack> give)
	{
		this.name = name;
		this.attack = attack;
		this.attackSpeed = attackSpeed;
		this.armor = armor;
		this.dodge = dodge;
		this.armor_head = items[0];
		this.armor_chest = items[1];
		this.armor_pants = items[2];
		this.armor_boots = items[3];
		this.give = give;
	}

	public void setGiveItems(List<ItemStack> give)
	{
		this.give = give;
	}

	public void outfit(Player player)
	{
		player.getInventory().setHelmet(armor_head);
		player.getInventory().setChestplate(armor_chest);
		player.getInventory().setLeggings(armor_pants);
		player.getInventory().setBoots(armor_boots);
		for (ItemStack i : give)
			player.getInventory().addItem(i);
	}

	public static Role getDefautlRole()
	{
		return warrior;
	}

}