package com.dogonfire.exams;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;

public class PermissionsManager
{
	private String				pluginName			= "null";
	private Exams				plugin;
	private Permission 			vaultPermission;
	
	public PermissionsManager(Exams p)
	{
		this.plugin = p;
			
		if (p.examPricesEnabled) {
			RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(Permission.class);
			vaultPermission = permissionProvider.getProvider();
		}
	}

	public void load()
	{
		// Nothing to see here
	}

	public Plugin getPlugin()
	{
		return plugin;
	}

	public String getPermissionPluginName()
	{
		return pluginName;
	}

	public boolean hasPermission(Player player, String node)
	{
		if (this.plugin.examPricesEnabled) {
			return vaultPermission.has(player, node);
		}
		return false;
	}

	public String getGroup(String playerName)
	{
		if (this.plugin.examPricesEnabled) {
			return vaultPermission.getPrimaryGroup(plugin.getServer().getPlayer(playerName));
		}
		return "";
	}
	
	public String[] getGroups(String playerName)
	{
		if (this.plugin.examPricesEnabled) {
			return vaultPermission.getPlayerGroups(null, plugin.getServer().getPlayer(playerName));
		}
		return null;
	}

	public void addGroup(String playerName, String groupName)
	{
		if (this.plugin.examPricesEnabled) {
			Player player = plugin.getServer().getPlayer(playerName);
			vaultPermission.playerAddGroup(null, player, groupName);
		}
	}
	
	public void addGroups(String playerName, String[] groupNames)
	{
		for (String groupName : groupNames) {
			addGroup(playerName, groupName);
		}
	}
	
	public void removeGroup(String playerName, String groupName)
	{
		if (this.plugin.examPricesEnabled) {
			Player player = plugin.getServer().getPlayer(playerName);
			vaultPermission.playerRemoveGroup(null, player, groupName);
		}
	}
	
	public void removeGroups(String playerName, String[] groupNames)
	{
		if (this.plugin.examPricesEnabled) {
			for (String groupName : groupNames) {
				removeGroup(playerName, groupName);
			}
		}
	}
	
	public boolean inGroup(String playerName, String groupName)
	{
		if (this.plugin.examPricesEnabled) {
			Player player = plugin.getServer().getPlayer(playerName);
			return vaultPermission.playerInGroup(player, groupName);
		}
		return false;
	}
}