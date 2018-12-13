package main.java.com.dogonfire.exams;

import java.util.List;

//import java.util.Comparator;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands
{
	private Exams	plugin	= null;

	Commands(Exams p)
	{
		this.plugin = p;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Player player = null;

		if ((sender instanceof Player))
		{
			player = (Player) sender;
		}

		if (player == null)
		{
			if ((cmd.getName().equalsIgnoreCase("exams")) || (cmd.getName().equalsIgnoreCase("exam")))
			{
				if (args.length == 1)
				{
					if(args[0].equalsIgnoreCase("reload"))
					{
						plugin.reloadSettings();

						return true;
					}
					else if (args[0].equalsIgnoreCase("clean"))
					{
						commandClean(sender);
						
						return true;
					}
				}

				commandExamList(player);
			}

			return true;
		}

		if ((cmd.getName().equalsIgnoreCase("exams")) || (cmd.getName().equalsIgnoreCase("exam")))
		{
			if (args.length == 0)
			{
				commandHelp(sender);
				return true;
			}
			if (args.length == 1)
			{
				if (args[0].equalsIgnoreCase("reload"))
				{
					if ((!player.isOp()) && (!player.hasPermission("exams.reload")))
					{
						return false;
					}

					this.plugin.reloadSettings();
					sender.sendMessage(ChatColor.YELLOW + this.plugin.getDescription().getFullName() + ":" + ChatColor.AQUA + " Reloaded configuration.");
					return true;
				}
				if (args[0].equalsIgnoreCase("help"))
				{
					if ((!player.isOp()) && (!player.hasPermission("exams.list")))
					{
						return false;
					}

					commandList(sender);

					return true;
				}
				if (args[0].equalsIgnoreCase("clean"))
				{
					if (!player.isOp() && !player.hasPermission("exams.clean"))
					{
						return false;
					}

					commandClean(sender);
					return true;
				}
				if ((args[0].equalsIgnoreCase("a")) || (args[0].equalsIgnoreCase("b")) || (args[0].equalsIgnoreCase("c")) || (args[0].equalsIgnoreCase("d")))
				{
					commandAnswer(player, args[0].toLowerCase());
				}
				else
				{
					if (args[0].equalsIgnoreCase("list"))
					{
						if ((!player.isOp()) && (!player.hasPermission("exams.list")))
						{
							return false;
						}

						return true;
					}

					sender.sendMessage(ChatColor.RED + "Invalid Exams command! Try /exams help");
					return true;
				}
			}
			else
			{
				if (args.length == 2)
				{
					
					if (args[0].equalsIgnoreCase("info"))
					{
						if (!player.isOp() && !player.hasPermission("exams.info"))
						{
							return false;
						}

						commandInfo(sender, args[1]);
						return true;
					}
					if (args[0].equalsIgnoreCase("reset"))
					{
						if (!player.isOp() && !player.hasPermission("exams.reset"))
						{
							return false;
						}

						commandReset(sender, args[1]);
						return true;
					}
					if (args[0].equalsIgnoreCase("test"))
					{
						if (!player.isOp() && !player.hasPermission("exams.test"))
						{
							return false;
						}

						commandTest(sender, args[1]);
						return true;
					}
					if (args[0].equalsIgnoreCase("studentinfo"))
					{
						if (!player.isOp() && !player.hasPermission("exams.studentinfo"))
						{
							return false;
						}

						commandStudentInfo(sender, args[1]);
						return true;
					}

					sender.sendMessage(ChatColor.RED + "Invalid Exams command! Try /exams help");
					return true;
				}

				if (args.length > 3)
				{
					sender.sendMessage(ChatColor.RED + "Too many arguments! Check /exams help");
					return true;
				}
			}
		}
		return true;
	}

	private boolean commandInfo(CommandSender sender, String examName)
	{
		return true;
	}
	
	private boolean commandReset(CommandSender sender, String playerName)
	{
		String originalRank = plugin.getStudentManager().getOriginalRank(playerName);
		
		if(originalRank!=null)
		{			
			plugin.getPermissionsManager().setGroup(playerName, originalRank);	
		}

		plugin.getStudentManager().removeStudent(playerName);
		plugin.getStudentManager().resetExamTime(playerName);
		
		sender.sendMessage(ChatColor.YELLOW + this.plugin.getDescription().getFullName() + ":" + ChatColor.AQUA + " Reset of player " + ChatColor.YELLOW + playerName + ChatColor.AQUA + "'s studentdata was successful!");
		
		return true;
	}
	
	private boolean commandStudentInfo(CommandSender sender, String playerName)
	{
		sender.sendMessage(ChatColor.YELLOW + "Student data for: " + playerName);
		
		// Checks for exam
		String currentExam = plugin.getStudentManager().getExamForStudent(playerName);
		if(currentExam!=null)
		{			
			sender.sendMessage(ChatColor.AQUA + "In exam?" + ChatColor.WHITE + " - Yes");
			sender.sendMessage(ChatColor.AQUA + "Exam name:" + ChatColor.WHITE + " - " + currentExam);
			String originalRank = plugin.getStudentManager().getOriginalRank(playerName);
			if(originalRank!=null)
			{
				sender.sendMessage(ChatColor.AQUA + "Original rank:" + ChatColor.WHITE + " - " + originalRank);
			}
			String examTime = plugin.getStudentManager().getLastExamTime(playerName);
			if(examTime!=null)
			{
				sender.sendMessage(ChatColor.AQUA + "Last exam time:" + ChatColor.WHITE + " - " + examTime);
			}
			List<String> passedExams = plugin.getStudentManager().getPassedExams(playerName);
			String passedExamsByComma = String.join(", ", passedExams);
			if(passedExams!=null)
			{
				sender.sendMessage(ChatColor.AQUA + "Passed exams:" + ChatColor.WHITE + " - " + passedExamsByComma);
			}
		}
		else
		{
			String examTime = plugin.getStudentManager().getLastExamTime(playerName);
			if(examTime!=null)
			{
				sender.sendMessage(ChatColor.AQUA + "In exam?" + ChatColor.WHITE + " - No");
				sender.sendMessage(ChatColor.AQUA + "Last exam time:" + ChatColor.WHITE + " - " + examTime);
				List<String> passedExams = plugin.getStudentManager().getPassedExams(playerName);
				String passedExamsByComma = String.join(", ", passedExams);
				if(passedExams!=null)
				{
					sender.sendMessage(ChatColor.AQUA + "Passed exams:" + ChatColor.WHITE + " - " + passedExamsByComma);
				}
			}
			else
			{
				List<String> passedExams = plugin.getStudentManager().getPassedExams(playerName);
				String passedExamsByComma = String.join(", ", passedExams);
				if(passedExams!=null)
				{
					sender.sendMessage(ChatColor.AQUA + "In exam?" + ChatColor.WHITE + " - No");
					sender.sendMessage(ChatColor.AQUA + "Passed exams:" + ChatColor.WHITE + " - " + passedExamsByComma);
				}
				else
				{
					sender.sendMessage(ChatColor.AQUA + "No student data found for player:" + ChatColor.WHITE + " - " + playerName);
				}
			}
		}
		
		
		return true;
	}

	private boolean commandTest(CommandSender sender, String exam)
	{
		Player player = (Player)sender;

		String examName = exam;
		
		if (!plugin.getExamManager().examExists(examName))
		{
			player.sendMessage(ChatColor.RED + "There is no exam called '" + examName + "'!");
			return false;
		}
		
		String currentExam = plugin.getStudentManager().getExamForStudent(player.getName());

		if(currentExam==null)
		{
			plugin.getExamManager().handleNewExamPrerequisites(player, examName);
			
			return false;
		}

		if (!currentExam.equals(examName))
		{
			plugin.sendInfo(player, ChatColor.RED + "You are already signed up for the " + ChatColor.YELLOW + currentExam + ChatColor.RED + " exam!");
			return false;
		}
		
		if (plugin.getExamManager().isExamOpen(player.getWorld(), examName))
		{
			if (!plugin.getStudentManager().isDoingExam(player.getName()))
			{
				if (!plugin.getExamManager().generateExam(player.getName(), examName))
				{
					player.sendMessage(ChatColor.RED + "ERROR: Could not generate a " + ChatColor.YELLOW + examName + ChatColor.RED + "exam!");
					return false;
				}

				plugin.sendToAll(ChatColor.AQUA + player.getName() + " started on the exam for " + ChatColor.YELLOW + examName + ChatColor.AQUA + "!");
				plugin.sendMessage(player.getName(), "You started on the " + ChatColor.YELLOW + examName + ChatColor.AQUA + " exam.");
				plugin.sendMessage(player.getName(), "Click on the sign again to repeat the exam question.");
				plugin.sendMessage(player.getName(), "Good luck!");

				plugin.getExamManager().nextExamQuestion(player.getName());
			}

			plugin.getExamManager().doExamQuestion(player.getName());
		}
			
		return true;
	}

	private void commandAnswer(Player player, String answer)
	{
		if (!plugin.getStudentManager().isDoingExam(player.getName()))
		{
			player.sendMessage(ChatColor.RED + "You are not taking any exam!");
			return;
		}

		plugin.getStudentManager().answer(player.getName(), answer);

		if (plugin.getExamManager().nextExamQuestion(player.getName()))
		{
			plugin.getExamManager().doExamQuestion(player.getName());
		}
		else
		{
			plugin.getExamManager().calculateExamResult(player.getName());
			plugin.getStudentManager().removeStudent(player.getName());
		}
	}

	private boolean commandHelp(CommandSender sender)
	{
		sender.sendMessage(ChatColor.YELLOW + "------------------ " + plugin.getDescription().getFullName() + " ------------------");
		sender.sendMessage(ChatColor.AQUA + "By DogOnFire");
		sender.sendMessage(ChatColor.AQUA + "");
		sender.sendMessage(ChatColor.AQUA + "There are currently " + ChatColor.WHITE + plugin.getExamManager().getExams().size() + ChatColor.AQUA + " exams in " + this.plugin.serverName);
		sender.sendMessage(ChatColor.AQUA + "");
		sender.sendMessage(ChatColor.AQUA + "Use " + ChatColor.WHITE + "/exams help" + ChatColor.AQUA + " for a list of commands");

		return true;
	}

	private boolean commandList(CommandSender sender)
	{
		sender.sendMessage(ChatColor.YELLOW + "------------------ " + this.plugin.getDescription().getFullName() + " ------------------");
		sender.sendMessage(ChatColor.AQUA + "/exams" + ChatColor.WHITE + " - Basic info");
		//sender.sendMessage(ChatColor.AQUA + "/exams list" + ChatColor.WHITE + " - List of all exams");
		sender.sendMessage(ChatColor.AQUA + "/exams a" + ChatColor.WHITE + " - Answer A to an exam question");
		sender.sendMessage(ChatColor.AQUA + "/exams b" + ChatColor.WHITE + " - Answer A to an exam question");
		sender.sendMessage(ChatColor.AQUA + "/exams c" + ChatColor.WHITE + " - Answer A to an exam question");
		sender.sendMessage(ChatColor.AQUA + "/exams d" + ChatColor.WHITE + " - Answer A to an exam question");
		if ((sender.isOp()) || (sender.hasPermission("exams.reload")))
		{
			sender.sendMessage(ChatColor.AQUA + "/exams reload" + ChatColor.WHITE + " - Reloads the Exams system");
		}
		if ((sender.isOp()) || (sender.hasPermission("exams.clean")))
		{
			sender.sendMessage(ChatColor.AQUA + "/exams clean" + ChatColor.WHITE + " - Cleans up expired student data");
		}
		if ((sender.isOp()) || (sender.hasPermission("exams.reset")))
		{
			sender.sendMessage(ChatColor.AQUA + "/exams reset <player>" + ChatColor.WHITE + " - Resets student data for a player");
		}
		if ((sender.isOp()) || (sender.hasPermission("exams.test")))
		{
			sender.sendMessage(ChatColor.AQUA + "/exams test <exam>" + ChatColor.WHITE + " - Validates an exam");
		}
		if ((sender.isOp()) || (sender.hasPermission("exams.studentinfo")))
		{
			sender.sendMessage(ChatColor.AQUA + "/exams studentinfo <player>" + ChatColor.WHITE + " - Gets info about a student");
		}

		return true;
	}

	private boolean commandClean(CommandSender sender)
	{
		int students = 0;

		students = plugin.getExamManager().cleanStudentData();

		if(sender!=null)
		{
			sender.sendMessage(ChatColor.YELLOW + this.plugin.getDescription().getFullName() + ":" + ChatColor.AQUA + " Cleaned up data for " + ChatColor.YELLOW + students + ChatColor.AQUA + " students");
		}
		
		plugin.log("Cleaned up data for " + students + " students");

		return true;
	}

	private void commandExamList(CommandSender sender)
	{
	}
}