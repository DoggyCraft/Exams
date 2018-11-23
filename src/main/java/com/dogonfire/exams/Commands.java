package main.java.com.dogonfire.exams;

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
					if ((!player.isOp()) && (!player.hasPermission("exams.clean")))
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

					sender.sendMessage(ChatColor.RED + "Invalid Exams command");
					return true;
				}
			}
			else
			{
				if (args.length == 2)
				{
					
					if (args[0].equalsIgnoreCase("info"))
					{
						if ((!player.isOp()) && (!player.hasPermission("exams.info")))
						{
							return false;
						}

						commandInfo(sender, args[1]);
						return true;
					}
					if (args[0].equalsIgnoreCase("reset"))
					{
						if ((!player.isOp()) && (!player.hasPermission("exams.reset")))
						{
							return false;
						}

						commandReset(sender, args[1]);
						return true;
					}

					sender.sendMessage(ChatColor.RED + "Invalid Exams command");
					return true;
				}

				if (args.length > 3)
				{
					sender.sendMessage(ChatColor.RED + "Too many arguments!");
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
		
		sender.sendMessage(ChatColor.YELLOW + this.plugin.getDescription().getFullName() + ":" + ChatColor.AQUA + " Reset of ExamTime for player " + ChatColor.YELLOW + playerName + ChatColor.AQUA + " was successful!");
		
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
			sender.sendMessage(ChatColor.AQUA + "/exams reset <player>" + ChatColor.WHITE + " - Resets ExamTime for a player");
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