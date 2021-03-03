package com.dogonfire.exams;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;

public class PermissionsManager
{
	static private Permission			vaultPermission;
	static private PermissionsManager	instance;
	
	public PermissionsManager()
	{
		instance = this;
		if (Exams.instance().examRanksEnabled) {
			RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);

			if (permissionProvider != null)
			{
				vaultPermission = ((Permission) permissionProvider.getProvider());
				Exams.instance().examRanksEnabled = true;
				Exams.log("Permission provider, exam ranks enabled.");
			}
			else
			{
				Exams.log("Permission provider not found, exam ranks disabled.");
				Exams.instance().examRanksEnabled = false;
			}
		}
	}

	public static boolean hasPermission(Player player, String node)
	{
		if (Exams.instance().examRanksEnabled) {
			return vaultPermission.has(player, node);
		}
		else {
			return player.hasPermission(node);
		}
	}

	public String getGroup(String playerName)
	{
		if (Exams.instance().examPricesEnabled) {
			return vaultPermission.getPrimaryGroup(null, Bukkit.getServer().getPlayer(playerName));
		}
		return "";
	}
	
	public static String[] getGroups(String playerName)
	{
		if (Exams.instance().examPricesEnabled) {
			return vaultPermission.getPlayerGroups(null, Bukkit.getServer().getPlayer(playerName));
		}
		return null;
	}

	public static void addGroup(String playerName, String groupName)
	{
		if (Exams.instance().examPricesEnabled) {
			Player player = Bukkit.getServer().getPlayer(playerName);
			vaultPermission.playerAddGroup(null, player, groupName);
		}
	}
	
	public static void addGroups(String playerName, String[] groupNames)
	{
		for (String groupName : groupNames) {
			addGroup(playerName, groupName);
		}
	}
	
	public static void removeGroup(String playerName, String groupName)
	{
		if (Exams.instance().examPricesEnabled) {
			Player player = Bukkit.getServer().getPlayer(playerName);
			vaultPermission.playerRemoveGroup(null, player, groupName);
		}
	}
	
	public static void removeGroups(String playerName, String[] groupNames)
	{
		if (Exams.instance().examPricesEnabled) {
			for (String groupName : groupNames) {
				removeGroup(playerName, groupName);
			}
		}
	}
	
	public static boolean inGroup(String playerName, String groupName)
	{
		if (Exams.instance().examPricesEnabled) {
			Player player = Bukkit.getServer().getPlayer(playerName);
			assert player != null;
			return vaultPermission.playerInGroup(player, groupName);
		}
		return false;
	}
}