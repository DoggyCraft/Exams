package main.java.com.dogonfire.exams;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.bpermissions.api.util.Calculable;
import de.bananaco.bpermissions.api.util.CalculableType;
import net.milkbowl.vault.permission.Permission;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

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
	private PermissionManager	pex					= null;
	private Permission 			vaultPermission;
	private PluginManager 		pluginManager;
	
	public PermissionsManager(Exams p)
	{
		this.plugin = p;
		
		pluginManager = plugin.getServer().getPluginManager();
		
		RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		vaultPermission = permissionProvider.getProvider();
	}

	public void load()
	{
		pluginManager = plugin.getServer().getPluginManager();

		if (pluginManager.getPlugin("PermissionsEx") != null)
		{
			plugin.log("Using PermissionsEx.");
			pluginName = "PermissionsEx";
			pex = PermissionsEx.getPermissionManager();
		}
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