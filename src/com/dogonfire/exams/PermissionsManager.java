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
				Exams.log("Permission provider found, exam ranks enabled.");
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
	
	public static boolean inGroup(String playerName, String groupName)
	{
		if (Exams.instance().examRanksEnabled) {
			Player player = Bukkit.getServer().getPlayer(playerName);
			assert player != null;
			return vaultPermission.playerInGroup(player, groupName);
		}
		// If Vault can't find a permission provider, just return true.
		// We already warned, so...
		return true;
	}
}