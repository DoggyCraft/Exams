package com.dogonfire.exams;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener
{
	private Exams	plugin;

	BlockListener(Exams p)
	{
		this.plugin = p;
	}

    private void destroySign(Block signBlock)
    {
        Material signType = signBlock.getType();
        signBlock.setType(Material.AIR);

        // Drop it
        switch (signType) {
            case OAK_WALL_SIGN:
                signBlock.getWorld().dropItem(signBlock.getLocation(), new ItemStack(Material.OAK_SIGN, 1));
                break;
            case SPRUCE_WALL_SIGN:
                signBlock.getWorld().dropItem(signBlock.getLocation(), new ItemStack(Material.SPRUCE_SIGN, 1));
                break;
            case BIRCH_WALL_SIGN:
                signBlock.getWorld().dropItem(signBlock.getLocation(), new ItemStack(Material.BIRCH_SIGN, 1));
                break;
            case JUNGLE_WALL_SIGN:
                signBlock.getWorld().dropItem(signBlock.getLocation(), new ItemStack(Material.JUNGLE_SIGN, 1));
                break;
            case ACACIA_WALL_SIGN:
                signBlock.getWorld().dropItem(signBlock.getLocation(), new ItemStack(Material.ACACIA_SIGN, 1));
                break;
            case DARK_OAK_WALL_SIGN:
                signBlock.getWorld().dropItem(signBlock.getLocation(), new ItemStack(Material.DARK_OAK_SIGN, 1));
                break;
            case CRIMSON_WALL_SIGN:
                signBlock.getWorld().dropItem(signBlock.getLocation(), new ItemStack(Material.CRIMSON_SIGN, 1));
                break;
            case WARPED_WALL_SIGN:
                signBlock.getWorld().dropItem(signBlock.getLocation(), new ItemStack(Material.WARPED_SIGN, 1));
                break;
            default:
                break;
        }
    }

	@EventHandler
	public void onSignChange(SignChangeEvent event)
	{
		Player player = event.getPlayer();

		if (!plugin.getExamManager().isExamSign(event.getBlock(), event.getLines()))
		{
			return;
		}

		if (!player.isOp() && !plugin.getPermissionsManager().hasPermission(player, "exams.place"))
		{
			event.setCancelled(true);
            destroySign(event.getBlock());

			plugin.sendInfo(player, ChatColor.RED + "You cannot place Exam signs");

			return;
		}

		if (!plugin.getExamManager().handleNewExamSign(event))
		{
			event.setCancelled(true);
            destroySign(event.getBlock());
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!plugin.getExamManager().isExamSign(event.getClickedBlock()))
		{
			return;
		}

		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			return;
		}

		String examName = plugin.getExamManager().getExamFromSign(event.getClickedBlock());

		if (examName == null)
		{
			return;
		}
		
		if (!plugin.getExamManager().examExists(examName))
		{
			event.getPlayer().sendMessage(ChatColor.RED + "There is no exam called '" + examName + "'!");
			return;
		}
		
		String currentExam = plugin.getStudentManager().getExamForStudent(player.getName());

		if(currentExam==null)
		{
			plugin.getExamManager().handleNewExamPrerequisites(player, examName);
			
			return;
		}

		if (!currentExam.equals(examName))
		{
			plugin.sendInfo(event.getPlayer(), ChatColor.RED + "You are already signed up for the " + ChatColor.YELLOW + currentExam + ChatColor.RED + " exam!");
			return;
		}
		
		if (plugin.getExamManager().isExamOpen(player.getWorld(), examName))
		{
			if (!plugin.getStudentManager().isDoingExam(player.getName()))
			{
				if (!plugin.getExamManager().generateExam(player.getName(), examName))
				{
					player.sendMessage(ChatColor.RED + "ERROR: Could not generate a " + ChatColor.YELLOW + examName + ChatColor.RED + "exam!");
					return;
				}

				plugin.sendToAll(ChatColor.AQUA + player.getName() + " started on the exam for " + ChatColor.YELLOW + examName + ChatColor.AQUA + "!");
				plugin.sendMessage(player.getName(), "You started on the " + ChatColor.YELLOW + examName + ChatColor.AQUA + " exam.");
				plugin.sendMessage(player.getName(), "Click on the sign again to repeat the exam question.");
				plugin.sendMessage(player.getName(), "Good luck!");

				plugin.getExamManager().nextExamQuestion(player.getName());
			}

			plugin.getExamManager().doExamQuestion(player.getName());
		}
		else if (!plugin.getStudentManager().isDoingExam(player.getName()))
		{
			plugin.sendInfo(event.getPlayer(), ChatColor.RED + "The exam has not started yet!");
			plugin.sendInfo(event.getPlayer(), ChatColor.RED + "Please come back at " + ChatColor.YELLOW + plugin.getExamManager().getExamStartTime(examName) + ChatColor.RED + " Minecraft time");
		}
		else
		{
			plugin.sendInfo(event.getPlayer(), ChatColor.RED + "The exam has ended!");
			plugin.getExamManager().calculateExamResult(event.getPlayer().getName());
			plugin.getStudentManager().removeStudent(player.getName());
		}
	}
}