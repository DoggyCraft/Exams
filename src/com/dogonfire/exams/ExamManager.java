package com.dogonfire.exams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.NamespacedKey;

import net.milkbowl.vault.economy.Economy;

public class ExamManager
{
	private Exams				plugin;
	private FileConfiguration	examsConfig			= null;
	private File				examsConfigFile		= null;
	private Random				random				= new Random();
	private Economy				economy				= null;

	ExamManager(Exams p)
	{
		this.plugin = p;
		
		RegisteredServiceProvider<Economy> economyProvider = this.plugin.getServer().getServicesManager().getRegistration(Economy.class);

		if (economyProvider != null)
		{
			economy = ((Economy) economyProvider.getProvider());
			this.plugin.examPricesEnabled = true;
			this.plugin.log("Vault economy, exam prices enabled.");
		}
		else
		{
			this.plugin.log("Vault economy not found, exam prices disabled.");
			this.plugin.examPricesEnabled = false;
		}
	}

	public void load()
	{
		if (examsConfigFile == null)
		{
			examsConfigFile = new File(plugin.getDataFolder(), "exams.yml");
		}

		examsConfig = YamlConfiguration.loadConfiguration(examsConfigFile);
		
		// Fill out exams.yml with 2 example exams...
		if(!examsConfigFile.exists())
		{
			String testExam = "Citizen";

			List<String> questions = new ArrayList<String>();
			
			questions.add("Is it ok to grief?");
			questions.add("Is it ok to spam?");
			questions.add("Can I become admin?");
			questions.add("Does admins give out free stuff?");
			questions.add("Is this a RPG server?");
			questions.add("Are you allowed to insult people?");

			this.examsConfig.set(testExam + ".RankName", "Citizen");
			this.examsConfig.set(testExam + ".StartTime", 600);
			this.examsConfig.set(testExam + ".EndTime", 13000);
			this.examsConfig.set(testExam + ".Price", 100);
			this.examsConfig.set(testExam + ".NumberOfQuestions", 3);
			this.examsConfig.set(testExam + ".Questions", questions);
			
			for (String question : questions)
			{
				List<String> options = new ArrayList<String>();
				options.add("Yes");
				options.add("No");
				options.add("Maybe");
				options.add("I dont know");

				this.examsConfig.set(testExam + ".Questions." + question + ".Options", options);
				this.examsConfig.set(testExam + ".Questions." + question + ".CorrectOption", "B");
			}
					
			testExam = "Wizard";

			questions = new ArrayList<String>();
			questions.add("What does a speed potion consist of?");
			questions.add("How do you spawn 4 pigs?");
			questions.add("Where do you become wizard?");
			questions.add("How do you cast a fireball spell?");
			questions.add("In which world are wizards enabled?");
			questions.add("How do you slay a dragon?");

			this.examsConfig.set(testExam + ".RankName", "Wizard");
			this.examsConfig.set(testExam + ".RequiredRank", "Citizen");
			this.examsConfig.set(testExam + ".Command", "/give $PlayerName 38 1");
			this.examsConfig.set(testExam + ".StartTime", 600);
			this.examsConfig.set(testExam + ".EndTime", 13000);
			this.examsConfig.set(testExam + ".Price", 100);
			this.examsConfig.set(testExam + ".NumberOfQuestions", 3);
			this.examsConfig.set(testExam + ".Questions", questions);

			for (String question : questions)
			{
				List<String> options = new ArrayList<String>();
				options.add("Cobweb and spidereyes");
				options.add("Light and darkness");
				options.add("No idea");
				options.add("Blue monday");

				this.examsConfig.set(testExam + ".Questions." + question + ".Options", options);
				this.examsConfig.set(testExam + ".Questions." + question + ".CorrectOption", "A");
			}
			
			save();
			
			this.plugin.log("Couldn't load exams.yml, generated an example file");
		}

		try
		{
			examsConfig.load(new InputStreamReader(new FileInputStream(examsConfigFile), StandardCharsets.UTF_8));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InvalidConfigurationException e)
		{
			e.printStackTrace();
		}

		//examsConfig = YamlConfiguration.loadConfiguration(examsConfigFile);

		if (examsConfig.getKeys(false).size() > 0)
		{
			this.plugin.log("Loaded " + examsConfig.getKeys(false).size() + " exams.");
		}
	}

	private void save()
	{
		/*plugin.logDebug("Saving all exam configs...");
		File exams = new File(plugin.getDataFolder() + File.separator + "exams");
		for(File exam : exams.listFiles())
		{
			plugin.logDebug("Is this a directory?");
			if (exam.isDirectory()) 
			{
	            this.plugin.logDebug("Oops, that was a directory... skipping");
	        } 
			else 
			{
				plugin.logDebug("That wasn't a directory, trying to save it.");
				try
				{
					this.getExamConfig(exam.getName()).save(this.examsConfigFile);
				}
				catch (Exception ex)
				{
					this.plugin.log("Could not save config to " + this.examsConfigFile + ": " + ex.getMessage());
				}
	        }
		}*/
		
		if ((this.examsConfig == null) || (this.examsConfigFile == null))
		{
			return;
		}

		try
		{
			this.examsConfig.save(this.examsConfigFile);
		}
		catch (Exception ex)
		{
			this.plugin.log("Could not save config to " + this.examsConfigFile + ": " + ex.getMessage());
		}
	}

	public boolean isExamOpen(World world, String examName)
	{
		long time = world.getFullTime() % 24000L;

		long startTime = examsConfig.getLong(examName + ".StartTime");
		long endTime = examsConfig.getLong(examName + ".EndTime");

		if (startTime == endTime)
		{
			return true;
		}

		plugin.logDebug("Time is " + time);
		plugin.logDebug("Startime is " + startTime);
		plugin.logDebug("Endtime is " + endTime);

		return (time >= startTime) && (time <= endTime);
	}

	public boolean handleNewExamPrerequisites(Player player, String examName)
	{
		// Check for required RANK
		String requiredRank = plugin.getExamManager().getRequiredRankForExam(examName);
		if (requiredRank!=null && !plugin.getPermissionsManager().inGroup(player.getName(), requiredRank))
		{
			plugin.sendInfo(player, ChatColor.RED + "Only players with the " + ChatColor.YELLOW + requiredRank + ChatColor.RED + " rank, can take this exam!");
			return false;			
		}
		
		// Check for required PERMISSION
		String requiredPermission = plugin.getExamManager().getRequiredPermissionForExam(examName);
		if (requiredPermission!=null && !plugin.getPermissionsManager().hasPermission(player, requiredPermission))
		{
			plugin.sendInfo(player, ChatColor.RED + "Only players with the " + ChatColor.YELLOW + requiredPermission + ChatColor.RED + " permission, can take this exam!");
			return false;
		}

		// Check for required EXAM
		String requiredExam = plugin.getExamManager().getUnpassedRequiredExamForExam(player.getName(), examName);
		
		if (requiredExam!=null)
		{
			plugin.sendInfo(player, ChatColor.RED + "You must pass the " + ChatColor.YELLOW + requiredExam + ChatColor.RED + " exam before taking this exam!");
			return false;	 		
		}

		// Sign the player up for the EXAM
		if (plugin.getExamManager().signupForExam(player.getName(), examName, player))
		{
			plugin.sendMessage(player.getName(), ChatColor.AQUA + "Click the sign again to start this exam!");
			plugin.sendToAll(ChatColor.AQUA + player.getName() + " signed up for the " + ChatColor.YELLOW + examName + ChatColor.AQUA + " exam!");
		}
		else
		{
			return false;
		}

		return true;		
	}
	
	public boolean isWallSign(Block sign) {
		Material block = sign.getType();
		plugin.logDebug("Material: " + block.toString());
        switch (block) {
            case OAK_WALL_SIGN:
            case SPRUCE_WALL_SIGN:
            case BIRCH_WALL_SIGN:
            case JUNGLE_WALL_SIGN:
            case ACACIA_WALL_SIGN:
            case DARK_OAK_WALL_SIGN:
            case CRIMSON_WALL_SIGN:
            case WARPED_WALL_SIGN:
                return true;
            default:
                return false;
        }
	}
	
	public String getExamFromSign(Block clickedBlock)
	{
		if (!isWallSign(clickedBlock))
		{
			return null;
		}

		BlockState state = clickedBlock.getState();

		Sign sign = (Sign) state;

		String[] lines = sign.getLines();

		return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lines[2]));
	}
	
	public String getRequiredRankForExam(String examName)
	{
		return examsConfig.getString(examName + ".RequiredRank");
	}
	
	public String getRequiredPermissionForExam(String examName)
	{
		return examsConfig.getString(examName + ".RequiredPermission");
	}
	
	public String getUnpassedRequiredExamForExam(String playerName, String examName)
	{
		String requiredExamName = "";
		
		requiredExamName = examsConfig.getString(examName + ".RequiredExam");

		for(String passedExam : plugin.getStudentManager().getPassedExams(playerName))
		{
			if(passedExam.equals(requiredExamName))
			{
				return null;
			}
		}

		return requiredExamName;
	}

	public boolean isExamSign(Block clickedBlock)
	{
		if ((clickedBlock == null) || (!isWallSign(clickedBlock)))
		{
			return false;
		}

		BlockState state = clickedBlock.getState();

		Sign sign = (Sign) state;

		String[] lines = sign.getLines();

		return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lines[0])).equalsIgnoreCase("Exam");
	}

	public boolean isExamSign(Block clickedBlock, String[] lines)
	{
		if (!isWallSign(clickedBlock))
		{
			this.plugin.logDebug("Not an exam sign");
			return false;
		}

		clickedBlock.getState();

		if (!ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lines[0])).equalsIgnoreCase("Exam"))
		{
			this.plugin.logDebug("Not written exam on first line: " + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lines[0])));
			return false;
		}

		return true;
	}

	public void calculateExamResult(String playerName)
	{
		int correctAnswers = this.plugin.getStudentManager().getCorrectAnswersForStudent(playerName);
		String examName = this.plugin.getStudentManager().getExamForStudent(playerName);

		int score = 100 * correctAnswers / getExamNumberOfQuestions(examName);

		plugin.sendMessage(playerName, ChatColor.YELLOW + "");
		plugin.sendMessage(playerName, ChatColor.YELLOW + "");
		plugin.sendMessage(playerName, ChatColor.YELLOW + "");
		plugin.sendMessage(playerName, ChatColor.YELLOW + "");
		plugin.sendMessage(playerName, ChatColor.YELLOW + "------------- Exam done -------------");
		plugin.sendMessage(playerName, ChatColor.YELLOW + "");
		plugin.sendMessage(playerName, ChatColor.AQUA + " Exam score:  " + ChatColor.YELLOW + score + ChatColor.AQUA + " points");
		plugin.sendMessage(playerName, ChatColor.AQUA + " Points needed: " + ChatColor.YELLOW + plugin.requiredExamScore + ChatColor.AQUA + " points");

		plugin.getStudentManager().setLastExamTime(playerName);
		
		if (score >= plugin.requiredExamScore)
		{
			String newGroup = getExamRank(examName);
			
			if(newGroup!=null)
			{
				plugin.getPermissionsManager().removeGroup(playerName, "student");
				
				plugin.getPermissionsManager().addGroup(playerName, newGroup);
			}
			else
			{
				String[] oldGroups = plugin.getStudentManager().getOriginalRanks(playerName);
				 
				plugin.getPermissionsManager().addGroups(playerName, oldGroups);
			}

			String command = getExamCommand(examName);
			
			if(command!=null)
			{
				plugin.logDebug("Reading single command");
				plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("$PlayerName", playerName));
			}
			else
			{			
				plugin.logDebug("Reading multiple commands");

				List<String> commands = getExamCommands(examName);
			
				if(commands!=null)
				{
					for(String c : commands)
					{
						plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), c.replace("$PlayerName", playerName)); 
					}
				}
			}			
			
			plugin.getStudentManager().setPassedExam(playerName, examName);

			plugin.sendMessage(playerName, ChatColor.GREEN + "Congratulations, you passed the exam!");
			plugin.sendToAll(ChatColor.GREEN + playerName + " just PASSED the " + ChatColor.YELLOW + plugin.getStudentManager().getExamForStudent(playerName) + ChatColor.GREEN + " exam!");
			
			// giving player an advancement for passing an exam if set to true
			if (plugin.grantAdvancement) {
				Advancement a = Bukkit.getAdvancement(NamespacedKey.fromString(plugin.examAdvacementName));
				if(a != null){
					Player player = Bukkit.getServer().getPlayer(playerName);
					AdvancementProgress progress = player.getAdvancementProgress(a);
					if(progress.isDone() == false) {
						player.getAdvancementProgress(a).awardCriteria("Student");
		        	}
		        }else{
		        	plugin.logDebug("Failed to find Advancement");
		        }
			}
			
		}
		else
		{
			/* TODO: REMOVE THIS
			String oldGroup = plugin.getStudentManager().getOriginalRank(playerName);
			
			plugin.getPermissionsManager().addGroup(playerName, oldGroup);
			*/

			String[] oldGroups = plugin.getStudentManager().getOriginalRanks(playerName);
			
			plugin.getPermissionsManager().addGroups(playerName, oldGroups);

			plugin.sendMessage(playerName, ChatColor.RED + "Sorry, you did not pass the exam...");
			plugin.sendToAll(ChatColor.RED + playerName + " just FAILED the " + ChatColor.YELLOW + plugin.getStudentManager().getExamForStudent(playerName) + ChatColor.RED + " exam...");
			plugin.log(playerName + " failed the " + examName + " exam with " + score + " points");

			String command = getExamFailCommand(examName);
			
			if(command!=null)
			{
				plugin.logDebug("Reading single fail command");
				plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("$PlayerName", playerName));
			}
		}
	}

	public double getExamPrice(String examName)
	{
		return examsConfig.getDouble(examName + ".Price");
	}

	public int getExamNumberOfQuestions(String examName)
	{
		int number = examsConfig.getInt(examName + ".NumberOfQuestions");

		if (number == 0)
		{
			plugin.log("Found no NumberOfQuestions for exam '" + examName + "'. Setting NumberOfQuestions to 1.");
			number = 1;
		}

		return number;
	}

	public String getExamStartTime(String examName)
	{
		int time = examsConfig.getInt(examName + ".StartTime") % 24000;

		int hours = 6 + time / 1000;

		return hours + ":00";
	}
	
	public String[] getExamListRoles(String examName)
	{
		List<String> listRolesList = examsConfig.getStringList(examName + ".List");
		
		String[] listRoles = listRolesList.toArray(new String[0]);
		return listRoles;
	}

	public int cleanStudentData()
	{
		int n = 0;

		for (String studentName : plugin.getStudentManager().getStudents())
		{
			if (plugin.getStudentManager().hasOutdatedExamAttempt(studentName))
			{
				plugin.getStudentManager().deleteStudent(studentName);

				n++;
			}
		}
		return n;
	}

	public String getExamRank(String examName)
	{
		return examsConfig.getString(examName + ".RankName");
	}

	public String getExamCommand(String examName)
	{
		return examsConfig.getString(examName + ".Command");
	}

	public String getExamFailCommand(String examName)
	{
		return examsConfig.getString(examName + ".CommandOnFail");
	}

	public List<String> getExamCommands(String examName)
	{
		return examsConfig.getStringList(examName + ".Commands");
	}

	public boolean nextExamQuestion(String playerName)
	{
		String examName = plugin.getStudentManager().getExamForStudent(playerName);

		//plugin.log("getExamNumberOfQuestions is " + getExamNumberOfQuestions(examName));
		//plugin.log("plugin.getStudentManager().nextExamQuestion(playerName) is " + plugin.getStudentManager().nextExamQuestionIndex(playerName));
		
		if (plugin.getStudentManager().nextExamQuestionIndex(playerName) >= getExamNumberOfQuestions(examName))
		{
			plugin.log("getExamNumberOfQuestions: No more questions");
			return false;
		}

		int examQuestionIndex = plugin.getStudentManager().getExamQuestionIndexForStudent(playerName);

		String question = getExamQuestionText(examName, examQuestionIndex);

		if (question == null)
		{
			plugin.log("nextExamQuestion: No question found for exam " + examName);
			return false;
		}

		String correctOption = getExamQuestionCorrectOptionText(examName, examQuestionIndex);
		List<String> options = getExamQuestionOptionText(examName, examQuestionIndex);

		if (options==null || options.size() == 0)
		{
			plugin.log("nextExamQuestion: No options found for question '" + question + "'");
			return false;
		}
		
		plugin.log("nextExamQuestion: Question is '" + question + "'");
		plugin.log("nextExamQuestion: ExamQuestionIndex is " + examQuestionIndex);

		plugin.getStudentManager().setExamQuestionForStudent(playerName, question, options, correctOption);

		return true;
	}

	public String getExamQuestionText(String examName, int examQuestionIndex)
	{
		ConfigurationSection configSection = examsConfig.getConfigurationSection(examName + ".Questions");
		Set<String> questions = configSection.getKeys(false);

		if(examQuestionIndex >= questions.size())
		{
			plugin.log("ERROR: Could not find question text with index " + examQuestionIndex + ". There are only " + questions.size() + " questions!");
			return null;
		}
		
		return (String) questions.toArray()[examQuestionIndex];
	}

	public String getExamQuestionCorrectOptionText(String examName, int examQuestionIndex)
	{
		ConfigurationSection configSection = examsConfig.getConfigurationSection(examName + ".Questions");
		Set<String> questions = configSection.getKeys(false);

		if(examQuestionIndex >= questions.size())
		{
			plugin.log("ERROR: Could not find question correct option with index " + examQuestionIndex + ". There are only " + questions.size() + " questions!");
			return null;
		}

		String question = (String) questions.toArray()[examQuestionIndex];

		return examsConfig.getString(examName + ".Questions." + question + ".CorrectOption");
	}

	public List<String> getExamQuestionOptionText(String examName, int examQuestionIndex)
	{
		ConfigurationSection configSection = examsConfig.getConfigurationSection(examName + ".Questions");
		Set<String> questions = configSection.getKeys(false);

		if(examQuestionIndex >= questions.size())
		{
			plugin.log("ERROR: Could not find question option with index " + examQuestionIndex + ". There are only " + questions.size() + " questions!");
			return null;
		}
		
		String question = (String) questions.toArray()[examQuestionIndex];

		return examsConfig.getStringList(examName + ".Questions." + question + ".Options");
	}

	public boolean generateExam(String playerName, String examName)
	{
		ConfigurationSection configSection = examsConfig.getConfigurationSection(examName + ".Questions");
		Set<String> questionKeys = configSection.getKeys(false);

		if (questionKeys.size() == 0)
		{
			plugin.log("No questions for exam called '" + examName + "'");
			return false;
		}

		if (questionKeys.size() < getExamNumberOfQuestions(examName))
		{
			plugin.log("Not enough questions for exam '" + examName + "'");
			return false;
		}

		this.plugin.logDebug("Got " + questionKeys.size() + " questions");

		List<String> selectedQuestions = new ArrayList<String>();

		for (int q = 0; q < getExamNumberOfQuestions(examName); q++)
		{
			selectedQuestions.add(String.valueOf(this.random.nextInt(questionKeys.size())));
		}

		while (!isDifferentStrings(selectedQuestions))
		{
			selectedQuestions.set(random.nextInt(selectedQuestions.size()), String.valueOf(random.nextInt(questionKeys.size())));
		}

		plugin.getStudentManager().setExamForStudent(playerName, examName, selectedQuestions);

		return true;
	}

	private boolean isDifferentStrings(List<String> strings)
	{
		for (int s1 = 0; s1 < strings.size(); s1++)
		{
			for (int s2 = 0; s2 < strings.size(); s2++)
			{
				if (s1 != s2)
				{
					String string1 = (String) strings.get(s1);
					String string2 = (String) strings.get(s2);

					if (string1.equals(string2))
					{
						return false;
					}
				}
			}
		}
		return true;
	}

	public void doExamQuestion(String playerName)
	{
		String question = plugin.getStudentManager().getExamQuestionForStudent(playerName);
		String examName = plugin.getStudentManager().getExamForStudent(playerName);
		List<String> options = plugin.getStudentManager().getExamQuestionOptionsForStudent(playerName);

		plugin.sendMessage(playerName, "------------- Exam question " + ChatColor.YELLOW + (plugin.getStudentManager().getExamProgressIndexForStudent(playerName) + 1) + "/" + getExamNumberOfQuestions(examName) + ChatColor.AQUA + " -------------");
		plugin.sendMessage(playerName, question);

		int n = 0;

		for (String option : options)
		{
			switch(n)
			{
				case 0 : plugin.sendMessage(playerName, ChatColor.YELLOW + "A - " + ChatColor.AQUA + option); break;
				case 1 : plugin.sendMessage(playerName, ChatColor.YELLOW + "B - " + ChatColor.AQUA + option); break;
				case 2 : plugin.sendMessage(playerName, ChatColor.YELLOW + "C - " + ChatColor.AQUA + option); break;
				case 3 : plugin.sendMessage(playerName, ChatColor.YELLOW + "D - " + ChatColor.AQUA + option); break;
			}
						
			n++;
		}

		plugin.sendMessage(playerName, ChatColor.AQUA + "Type " + ChatColor.WHITE + "/exams a, /exams b, /exams c or /exams d" + ChatColor.AQUA + " to answer.");
	}

	public boolean handleNewExamSign(SignChangeEvent event)
	{
		String[] lines = event.getLines();

		if (!examExists(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lines[2]))))
		{
			event.getPlayer().sendMessage(ChatColor.RED + "There is no exam called '" + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lines[2])) + "'");
			this.plugin.logDebug(event.getPlayer().getName() + " placed an exam sign for an invalid exam");
			return false;
		}

		// Getting color codes from the original sign
        List<String> signLineColors = new ArrayList<String>();
		for (String signLine : lines)
        {
            signLineColors.add(signLine.replace(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', signLine)), ""));
        }

        // Setting the lines
		String examName = getExactExamName(ChatColor.stripColor(lines[2]));

		event.setLine(0, signLineColors.get(0)+"Exam");
		event.setLine(1, signLineColors.get(1)+"In");
		event.setLine(2, signLineColors.get(2)+examName);

		event.getPlayer().sendMessage(ChatColor.AQUA + "You placed a sign for the " + ChatColor.GOLD + examName + ChatColor.AQUA + " exam!");

		return true;
	}

	public List<String> getExams()
	{
		List<String> exams = new ArrayList<String>();

		for (String examName : this.examsConfig.getKeys(false))
		{
			exams.add(examName);
		}

		return exams;
	}

	public boolean examExists(String examName)
	{
		for (String name : this.examsConfig.getKeys(false))
		{
			if (examName.equalsIgnoreCase(name))
			{
				return true;
			}
		}

		return false;
	}
	
	public String getExactExamName(String examName)
	{
		for (String name : this.examsConfig.getKeys(false))
		{
			if (examName.equalsIgnoreCase(name))
			{
				return name;
			}
		}

		return examName;
	}

	public boolean signupForExam(String playerName, String examName, Player player)
	{
		double price = getExamPrice(examName);
		OfflinePlayer offlinePlayer = (Player) player;

		if (this.plugin.examPricesEnabled)
		{
			if (price > 0.0D && !economy.has(offlinePlayer, price))
			{
				plugin.sendMessage(playerName, ChatColor.RED + "You need " + economy.format(getExamPrice(examName)) + " to take this exam");
				return false;
			}
		}

		if (plugin.getStudentManager().hasRecentExamAttempt(playerName))
		{
			if (!player.hasPermission("exams.nocooldown"))
			{
				plugin.sendMessage(playerName, ChatColor.RED + "You cannot take another exam so soon!");
				plugin.sendMessage(playerName, ChatColor.RED + "Try again in " + ChatColor.YELLOW + plugin.getStudentManager().getTimeUntilCanDoExam(plugin.getServer().getPlayer(playerName).getWorld(), playerName) + ChatColor.RED + " minutes");
				return false;
			}
		}

		
		String[] oldRanks = plugin.getPermissionsManager().getGroups(playerName);
		
		plugin.getStudentManager().setOriginalRanks(playerName, oldRanks);
		
		plugin.getPermissionsManager().removeGroups(playerName, oldRanks);

		plugin.getPermissionsManager().addGroup(playerName, "student");

		plugin.getStudentManager().signupForExam(playerName, examName);

		if (plugin.examPricesEnabled)
		{
			if(price > 0.0D)
			{
				economy.withdrawPlayer(offlinePlayer, price);
				plugin.sendMessage(playerName, ChatColor.AQUA + "You paid " + ChatColor.YELLOW + economy.format(getExamPrice(examName)) + ChatColor.AQUA + " for signing up to this exam");
			}
		}

		return true;
	}
}