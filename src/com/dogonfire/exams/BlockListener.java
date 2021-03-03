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
	static private BlockListener	instance;

	public BlockListener()
	{
		instance = this;
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

		if (!ExamManager.isExamSign(event.getBlock(), event.getLines()))
		{
			return;
		}

		if (!player.isOp() && !PermissionsManager.hasPermission(player, "exams.place"))
		{
			event.setCancelled(true);
            destroySign(event.getBlock());

			Exams.sendInfo(player, ChatColor.RED + "You cannot place Exam signs");

			return;
		}

		if (!ExamManager.handleNewExamSign(event))
		{
			event.setCancelled(true);
            destroySign(event.getBlock());
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!ExamManager.isExamSign(event.getClickedBlock()))
		{
			return;
		}

		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			return;
		}

		String examName = ExamManager.getExamFromSign(event.getClickedBlock());

		if (examName == null)
		{
			return;
		}
		
		if (!ExamManager.examExists(examName))
		{
			event.getPlayer().sendMessage(ChatColor.RED + "There is no exam called '" + examName + "'!");
			return;
		}
		
		String currentExam = StudentManager.getExamForStudent(player.getName());

		if(currentExam==null)
		{
			ExamManager.handleNewExamPrerequisites(player, examName);
			
			return;
		}

		if (!currentExam.equals(examName))
		{
			Exams.sendInfo(event.getPlayer(), ChatColor.RED + "You are already signed up for the " + ChatColor.YELLOW + currentExam + ChatColor.RED + " exam!");
			return;
		}
		
		if (ExamManager.isExamOpen(player.getWorld(), examName))
		{
			if (!StudentManager.isDoingExam(player.getName()))
			{
				if (!ExamManager.generateExam(player.getName(), examName))
				{
					player.sendMessage(ChatColor.RED + "ERROR: Could not generate a " + ChatColor.YELLOW + examName + ChatColor.RED + "exam!");
					return;
				}

				Exams.sendToAll(ChatColor.AQUA + player.getName() + " started on the exam for " + ChatColor.YELLOW + examName + ChatColor.AQUA + "!");
				Exams.sendMessage(player.getName(), "You started on the " + ChatColor.YELLOW + examName + ChatColor.AQUA + " exam.");
				Exams.sendMessage(player.getName(), "Click on the sign again to repeat the exam question.");
				Exams.sendMessage(player.getName(), "Good luck!");

				ExamManager.nextExamQuestion(player.getName());
			}

			ExamManager.doExamQuestion(player.getName());
		}
		else if (!StudentManager.isDoingExam(player.getName()))
		{
			Exams.sendInfo(event.getPlayer(), ChatColor.RED + "The exam has not started yet!");
			Exams.sendInfo(event.getPlayer(), ChatColor.RED + "Please come back at " + ChatColor.YELLOW + ExamManager.getExamStartTime(examName) + ChatColor.RED + " Minecraft time");
		}
		else
		{
			Exams.sendInfo(event.getPlayer(), ChatColor.RED + "The exam has ended!");
			ExamManager.calculateExamResult(event.getPlayer().getName());
			StudentManager.removeStudent(player.getName());
		}
	}
}