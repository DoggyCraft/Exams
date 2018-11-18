package main.java.com.dogonfire.exams;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;

/*
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.anjocaido.groupmanager.data.User;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
*/

public class PermissionsManager
{
	private String				pluginName			= "null";
	private Exams				plugin;
	private Permission 			vaultPermission;
	
	public PermissionsManager(Exams p)
	{
		this.plugin = p;
			
		RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(Permission.class);
		vaultPermission = permissionProvider.getProvider();
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
		return vaultPermission.has(player, node);
	}

	public String getGroup(String playerName)
	{
		return vaultPermission.getPrimaryGroup(plugin.getServer().getPlayer(playerName));
	}

	public void setGroup(String playerName, String groupName)
	{
		Player player = plugin.getServer().getPlayer(playerName);
		vaultPermission.playerAddGroup(player, groupName);
	}
}