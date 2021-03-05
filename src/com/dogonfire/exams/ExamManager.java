package com.dogonfire.exams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

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

import net.milkbowl.vault.economy.Economy;

public class ExamManager
{
	static private FileConfiguration	examsConfig			= null;
	static private File					examsConfigFile		= null;
	static private Random				random				= new Random();
	static private Economy				economy				= null;
	static private ExamManager			instance;

	public ExamManager()
	{
		instance = this;
		
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);

		if (economyProvider != null)
		{
			economy = ((Economy) economyProvider.getProvider());
			Exams.instance().examPricesEnabled = true;
			Exams.log("Vault economy found, exam prices enabled.");
		}
		else
		{
			Exams.log("Vault economy not found, exam prices disabled.");
			Exams.instance().examPricesEnabled = false;
		}
	}

	public void load()
	{
		if (examsConfigFile == null)
		{
			examsConfigFile = new File(Exams.instance().getDataFolder(), "exams.yml");
		}

		examsConfig = YamlConfiguration.loadConfiguration(examsConfigFile);
		
		// Fill out exams.yml with 2 example exams...
		if(!examsConfigFile.exists())
		{
			// Citizen test exam
			String testExam = "Citizen";

			List<String> questions = new ArrayList<String>();
			
			questions.add("Is it ok to grief?");
			questions.add("Is it ok to spam?");
			questions.add("Can I become admin?");
			questions.add("Does admins give out free stuff?");
			questions.add("Is this a RPG server?");
			questions.add("Are you allowed to insult people?");

			examsConfig.set(testExam + ".Command", "/lp user $PlayerName group add Citizen");
			examsConfig.set(testExam + ".StartTime", 600);
			examsConfig.set(testExam + ".EndTime", 13000);
			examsConfig.set(testExam + ".NumberOfQuestions", 3);


			List<Map<?,?>> questionList = new ArrayList<Map<?,?>>();
			
			for (String question : questions)
			{
				Map<String,Object> questionMap = new LinkedHashMap<String, Object>();
				questionMap.put("Question", question);
				
				List<String> options = new ArrayList<String>();
				options.add("Yes");
				options.add("No");
				options.add("Maybe");
				options.add("I dont know");

				questionMap.put("Options", options);
				questionMap.put("CorrectOption", "B");

				questionList.add(questionMap);
			}

			examsConfig.set(testExam + ".Questions", questionList);

			// Wizard test exam
			testExam = "Wizard";

			questions = new ArrayList<String>();
			questions.add("What does a speed potion consist of?");
			questions.add("How do you spawn 4 pigs?");
			questions.add("Where do you become wizard?");
			questions.add("How do you cast a fireball spell?");
			questions.add("In which world are wizards enabled?");
			questions.add("How do you slay a dragon?");

			examsConfig.set(testExam + ".RequiredRank", "Citizen");
			List<String> commands = new ArrayList<String>();
			commands.add("/give $PlayerName minecraft:poppy 1");
			commands.add("/lp user $PlayerName group remove Citizen");
			commands.add("/lp user $PlayerName group add Wizard");
			examsConfig.set(testExam + ".Commands", commands);
			examsConfig.set(testExam + ".StartTime", 500);
			examsConfig.set(testExam + ".EndTime", 12500);
			examsConfig.set(testExam + ".Price", 100);
			examsConfig.set(testExam + ".NumberOfQuestions", 4);


			questionList = new ArrayList<Map<?,?>>();

			for (String question : questions)
			{
				Map<String,Object> questionMap = new LinkedHashMap<String, Object>();
				questionMap.put("Question", question);

				List<String> options = new ArrayList<String>();
				options.add("Cobweb and spidereyes");
				options.add("Light and darkness");
				options.add("No idea");
				options.add("Blue monday");

				questionMap.put("Options", options);
				questionMap.put("CorrectOption", "A");

				questionList.add(questionMap);
			}

			examsConfig.set(testExam + ".Questions", questionList);
			
			save();
			
			Exams.log("Couldn't load exams.yml, generated an example file");
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

		Set<String> exams = examsConfig.getKeys(false);
		if (exams.size() > 0)
		{
			Exams.log("Loaded " + exams.size() + " exams.");

			// Compatibility checks
			boolean oldRanks = false;
			boolean oldQuestions = false;
			for (String examName : exams) {
				if (!oldRanks) {
					// If using rank names for giving ranks, instead of commands
					if (examsConfig.getString(examName + ".RankName") != null) {
						Exams.log(Level.WARNING, "It seems like you have exams, where there is still the 'RankName' property. Please move said rank name into command(s), using the group commands from your Permission Provider, as Exams doesn't support giving out ranks anymore.");
						oldRanks = true;
					}
				}

				if (!oldQuestions) {
					// If using the old questions system on the exam
					if (examsConfig.getConfigurationSection(examName + ".Questions") != null) {
						Exams.log(Level.WARNING, "It seems like you have exams using the old questions format - please backup your exams.yml file and delete it in order to generate a file with the new format, and change your exams to follow the new standard. The exams will NOT work.");
						oldQuestions = true;
					}
				}

				if (oldRanks && oldQuestions) {
					break;
				}
			}
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
	            Exams.logDebug("Oops, that was a directory... skipping");
	        } 
			else 
			{
				plugin.logDebug("That wasn't a directory, trying to save it.");
				try
				{
					this.getExamConfig(exam.getName()).save(examsConfigFile);
				}
				catch (Exception ex)
				{
					Exams.log("Could not save config to " + examsConfigFile + ": " + ex.getMessage());
				}
	        }
		}*/
		
		if ((examsConfig == null) || (examsConfigFile == null))
		{
			return;
		}

		try
		{
			examsConfig.save(examsConfigFile);
		}
		catch (Exception ex)
		{
			Exams.log("Could not save config to " + examsConfigFile + ": " + ex.getMessage());
		}
	}

	public static boolean isExamOpen(World world, String examName)
	{
		long time = world.getFullTime() % 24000L;

		long startTime = examsConfig.getLong(examName + ".StartTime");
		long endTime = examsConfig.getLong(examName + ".EndTime");

		if (startTime == endTime)
		{
			return true;
		}

		Exams.logDebug("Time is " + time);
		Exams.logDebug("Startime is " + startTime);
		Exams.logDebug("Endtime is " + endTime);

		return (time >= startTime) && (time <= endTime);
	}

	public static boolean handleNewExamPrerequisites(Player player, String examName)
	{
		// Check for required RANK
		String requiredRank = ExamManager.getRequiredRankForExam(examName);
		if (requiredRank!=null && !PermissionsManager.inGroup(player.getName(), requiredRank))
		{
			Exams.sendInfo(player, ChatColor.RED + "Only players with the " + ChatColor.YELLOW + requiredRank + ChatColor.RED + " rank, can take this exam!");
			return false;			
		}
		
		// Check for required PERMISSION
		String requiredPermission = ExamManager.getRequiredPermissionForExam(examName);
		if (requiredPermission!=null && !PermissionsManager.hasPermission(player, requiredPermission))
		{
			Exams.sendInfo(player, ChatColor.RED + "Only players with the " + ChatColor.YELLOW + requiredPermission + ChatColor.RED + " permission, can take this exam!");
			return false;
		}

		// Check for required EXAM
		String requiredExam = ExamManager.getUnpassedRequiredExamForExam(player.getName(), examName);
		
		if (requiredExam!=null)
		{
			Exams.sendInfo(player, ChatColor.RED + "You must pass the " + ChatColor.YELLOW + requiredExam + ChatColor.RED + " exam before taking this exam!");
			return false;	 		
		}

		// Sign the player up for the EXAM
		if (ExamManager.signupForExam(player.getName(), examName, player))
		{
			Exams.sendMessage(player.getName(), ChatColor.AQUA + "Click the sign again to start this exam!");
			Exams.sendToAll(ChatColor.AQUA + player.getName() + " signed up for the " + ChatColor.YELLOW + examName + ChatColor.AQUA + " exam!");
		}
		else
		{
			return false;
		}

		return true;		
	}
	
	public static boolean isWallSign(Block sign) {
		Material block = sign.getType();
		Exams.logDebug("Material: " + block.toString());
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
	
	public static String getExamFromSign(Block clickedBlock)
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
	
	public static String getRequiredRankForExam(String examName)
	{
		return examsConfig.getString(examName + ".RequiredRank");
	}
	
	public static String getRequiredPermissionForExam(String examName)
	{
		return examsConfig.getString(examName + ".RequiredPermission");
	}
	
	public static String getUnpassedRequiredExamForExam(String playerName, String examName)
	{
		String requiredExamName = "";
		
		requiredExamName = examsConfig.getString(examName + ".RequiredExam");

		for(String passedExam : StudentManager.getPassedExams(playerName))
		{
			if(passedExam.equals(requiredExamName))
			{
				return null;
			}
		}

		return requiredExamName;
	}

	public static boolean isExamSign(Block clickedBlock)
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

	public static boolean isExamSign(Block clickedBlock, String[] lines)
	{
		if (!isWallSign(clickedBlock))
		{
			Exams.logDebug("Not an exam sign");
			return false;
		}

		clickedBlock.getState();

		if (!ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lines[0])).equalsIgnoreCase("Exam"))
		{
			Exams.logDebug("Not written exam on first line: " + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lines[0])));
			return false;
		}

		return true;
	}

	public static void calculateExamResult(String playerName)
	{
		int correctAnswers = StudentManager.getCorrectAnswersForStudent(playerName);
		String examName = StudentManager.getExamForStudent(playerName);

		int score = 100 * correctAnswers / getExamNumberOfQuestions(examName);

		Exams.sendMessage(playerName, ChatColor.YELLOW + "");
		Exams.sendMessage(playerName, ChatColor.YELLOW + "");
		Exams.sendMessage(playerName, ChatColor.YELLOW + "");
		Exams.sendMessage(playerName, ChatColor.YELLOW + "");
		Exams.sendMessage(playerName, ChatColor.YELLOW + "------------- Exam done -------------");
		Exams.sendMessage(playerName, ChatColor.YELLOW + "");
		Exams.sendMessage(playerName, ChatColor.AQUA + " Exam score:  " + ChatColor.YELLOW + score + ChatColor.AQUA + " points");
		Exams.sendMessage(playerName, ChatColor.AQUA + " Points needed: " + ChatColor.YELLOW + Exams.instance().requiredExamScore + ChatColor.AQUA + " points");

		StudentManager.setLastExamTime(playerName);
		
		if (score >= Exams.instance().requiredExamScore)
		{
			String command = getExamCommand(examName);
			
			if(command != null)
			{
				Exams.logDebug("Reading single command");
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("$PlayerName", playerName));
			}
			else
			{
				Exams.logDebug("Reading multiple commands");

				List<String> commands = getExamCommands(examName);

				for(String c : commands)
				{
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), c.replace("$PlayerName", playerName));
				}
			}
			
			StudentManager.setPassedExam(playerName, examName);

			Exams.sendMessage(playerName, ChatColor.GREEN + "Congratulations, you passed the exam!");
			Exams.sendToAll(ChatColor.GREEN + playerName + " just PASSED the " + ChatColor.YELLOW + StudentManager.getExamForStudent(playerName) + ChatColor.GREEN + " exam!");
		}
		else
		{
			Exams.sendMessage(playerName, ChatColor.RED + "Sorry, you did not pass the exam...");
			Exams.sendToAll(ChatColor.RED + playerName + " just FAILED the " + ChatColor.YELLOW + StudentManager.getExamForStudent(playerName) + ChatColor.RED + " exam...");
			Exams.log(playerName + " failed the " + examName + " exam with " + score + " points");

			String command = getExamFailCommand(examName);
			
			if(command!=null)
			{
				Exams.logDebug("Reading single fail command");
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("$PlayerName", playerName));
			}
		}
	}

	public static double getExamPrice(String examName)
	{
		return examsConfig.getDouble(examName + ".Price");
	}

	public static int getExamNumberOfQuestions(String examName)
	{
		int number = examsConfig.getInt(examName + ".NumberOfQuestions");

		if (number == 0)
		{
			Exams.log("Found no NumberOfQuestions for exam '" + examName + "'. Setting NumberOfQuestions to 1.");
			number = 1;
		}

		return number;
	}

	public static String getExamStartTime(String examName)
	{
		int time = examsConfig.getInt(examName + ".StartTime") % 24000;

		int hours = 6 + time / 1000;

		return hours + ":00";
	}

	public static int cleanStudentData()
	{
		int n = 0;

		for (String studentName : StudentManager.getStudents())
		{
			if (StudentManager.hasOutdatedExamAttempt(studentName))
			{
				StudentManager.deleteStudent(studentName);

				n++;
			}
		}
		return n;
	}

	public static String getExamCommand(String examName)
	{
		return examsConfig.getString(examName + ".Command");
	}

	public static String getExamFailCommand(String examName)
	{
		return examsConfig.getString(examName + ".CommandOnFail");
	}

	public static List<String> getExamCommands(String examName)
	{
		return examsConfig.getStringList(examName + ".Commands");
	}

	public static boolean nextExamQuestion(String playerName)
	{
		String examName = StudentManager.getExamForStudent(playerName);

//		Exams.logDebug("getExamNumberOfQuestions is " + getExamNumberOfQuestions(examName));
		int nextQuestionIndex = StudentManager.nextExamQuestionIndex(playerName);
//		Exams.logDebug("StudentManager.nextExamQuestion(playerName) is " + nextQuestionIndex);
		
		if (nextQuestionIndex >= getExamNumberOfQuestions(examName))
		{
			Exams.log("getExamNumberOfQuestions: No more questions");
			return false;
		}

		int examQuestionIndex = StudentManager.getExamQuestionIndexForStudent(playerName);

		String question = getExamQuestionText(examName, examQuestionIndex);

		if (question == null)
		{
			Exams.log("nextExamQuestion: No question found for exam " + examName);
			return false;
		}

		String correctOption = getExamQuestionCorrectOptionText(examName, examQuestionIndex);
		List<String> options = getExamQuestionOptionText(examName, examQuestionIndex);

		if (options==null || options.size() == 0)
		{
			Exams.log("nextExamQuestion: No options found for question '" + question + "' (" + examQuestionIndex + ")");
			return false;
		}

		Exams.log("nextExamQuestion: Question is '" + question + "'");
		Exams.log("nextExamQuestion: ExamQuestionIndex is " + examQuestionIndex);

		StudentManager.setExamQuestionForStudent(playerName, question, options, correctOption);

		return true;
	}

	public static String getExamQuestionText(String examName, int examQuestionIndex)
	{
//		ConfigurationSection configSection = examsConfig.getConfigurationSection(examName + ".Questions");
//		assert configSection != null;
//		Set<String> questions = configSection.getKeys(false);

		List<Map<?,?>> questions = examsConfig.getMapList(examName + ".Questions");

		if(examQuestionIndex >= questions.size())
		{
			Exams.log("ERROR: Could not find question text with index " + examQuestionIndex + ". There are only " + questions.size() + " questions!");
			return null;
		}
		
//		return (String) questions.toArray()[examQuestionIndex];
		return (String) questions.get(examQuestionIndex).get("Question");
	}

	public static String getExamQuestionCorrectOptionText(String examName, int examQuestionIndex)
	{
//		ConfigurationSection configSection = examsConfig.getConfigurationSection(examName + ".Questions");
//		assert configSection != null;
//		Set<String> questions = configSection.getKeys(false);

		List<Map<?,?>> questions = examsConfig.getMapList(examName + ".Questions");

		if(examQuestionIndex >= questions.size())
		{
			Exams.log("ERROR: Could not find question correct option with index " + examQuestionIndex + ". There are only " + questions.size() + " questions!");
			return null;
		}

//		String question = (String) questions.toArray()[examQuestionIndex];

		Map<?,?> question = questions.get(examQuestionIndex);

//		Exams.logDebug(question.toString());

		String correctOption = (String) question.get("CorrectOption");

//		Exams.logDebug(correctOption);

		return correctOption;

//		return examsConfig.getString(examName + ".Questions." + question + ".CorrectOption");
	}

	public static List<String> getExamQuestionOptionText(String examName, int examQuestionIndex)
	{
//		ConfigurationSection configSection = examsConfig.getConfigurationSection(examName + ".Questions");
//		assert configSection != null;
//
//		Exams.logDebug(examsConfig.getStringList(examName + ".Questions").toString());

		List<Map<?,?>> questions = examsConfig.getMapList(examName + ".Questions");
//		Exams.logDebug(questions.toString());

		if(examQuestionIndex >= questions.size())
		{
			Exams.log("ERROR: Could not find question option with index " + examQuestionIndex + ". There are only " + questions.size() + " questions!");
			return null;
		}
		
		Map<?,?> question = questions.get(examQuestionIndex);

//		Exams.logDebug(question.toString());

		List<String> options = (List<String>) question.get("Options");

//		Exams.logDebug(options.toString());

		return options;
	}

	public static boolean generateExam(String playerName, String examName)
	{
//		ConfigurationSection configSection = examsConfig.getConfigurationSection(examName + ".Questions");
//		assert configSection != null;
//		Set<String> questionKeys = configSection.getKeys(false);

		List<Map<?,?>> questions = examsConfig.getMapList(examName + ".Questions");

//		Exams.logDebug(questions.toString());

		if (questions.size() == 0)
		{
			Exams.log("No questions for exam called '" + examName + "'");
			return false;
		}

		if (questions.size() < getExamNumberOfQuestions(examName))
		{
			Exams.log("Not enough questions for exam '" + examName + "'");
			return false;
		}

		Exams.logDebug("Got " + questions.size() + " questions");

		List<String> selectedQuestions = new ArrayList<String>();

		for (int q = 0; q < getExamNumberOfQuestions(examName); q++)
		{
			selectedQuestions.add(String.valueOf(random.nextInt(questions.size())));
		}

		while (!isDifferentStrings(selectedQuestions))
		{
			selectedQuestions.set(random.nextInt(selectedQuestions.size()), String.valueOf(random.nextInt(questions.size())));
		}

		StudentManager.setExamForStudent(playerName, examName, selectedQuestions);

		return true;
	}

	private static boolean isDifferentStrings(List<String> strings)
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

	public static void doExamQuestion(String playerName)
	{
		String question = StudentManager.getExamQuestionForStudent(playerName);
		String examName = StudentManager.getExamForStudent(playerName);
		List<String> options = StudentManager.getExamQuestionOptionsForStudent(playerName);

		Exams.sendMessage(playerName, "------------- Exam question " + ChatColor.YELLOW + (StudentManager.getExamProgressIndexForStudent(playerName) + 1) + "/" + getExamNumberOfQuestions(examName) + ChatColor.AQUA + " -------------");
		Exams.sendMessage(playerName, question);

		int n = 0;

		for (String option : options)
		{
			switch(n)
			{
				case 0 : Exams.sendMessage(playerName, ChatColor.YELLOW + "A - " + ChatColor.AQUA + option); break;
				case 1 : Exams.sendMessage(playerName, ChatColor.YELLOW + "B - " + ChatColor.AQUA + option); break;
				case 2 : Exams.sendMessage(playerName, ChatColor.YELLOW + "C - " + ChatColor.AQUA + option); break;
				case 3 : Exams.sendMessage(playerName, ChatColor.YELLOW + "D - " + ChatColor.AQUA + option); break;
			}
						
			n++;
		}

		Exams.sendMessage(playerName, ChatColor.AQUA + "Type " +
				ChatColor.WHITE + "/exams a" + ChatColor.AQUA + ", " +
				ChatColor.WHITE + "/exams b" + ChatColor.AQUA + ", " +
				ChatColor.WHITE + "/exams c" + ChatColor.AQUA + " or " +
				ChatColor.WHITE + "/exams d" + ChatColor.AQUA + " to answer.");
	}

	public static boolean handleNewExamSign(SignChangeEvent event)
	{
		String[] lines = event.getLines();

		if (!examExists(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lines[2]))))
		{
			event.getPlayer().sendMessage(ChatColor.RED + "There is no exam called '" + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lines[2])) + "'");
			Exams.logDebug(event.getPlayer().getName() + " placed an exam sign for an invalid exam");
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

	public static List<String> getExams()
	{
		return new ArrayList<String>(examsConfig.getKeys(false));
	}

	public static boolean examExists(String examName)
	{
		for (String name : examsConfig.getKeys(false))
		{
			if (examName.equalsIgnoreCase(name))
			{
				return true;
			}
		}

		return false;
	}
	
	public static String getExactExamName(String examName)
	{
		for (String name : examsConfig.getKeys(false))
		{
			if (examName.equalsIgnoreCase(name))
			{
				return name;
			}
		}

		return examName;
	}

	public static boolean signupForExam(String playerName, String examName, Player player)
	{
		double price = getExamPrice(examName);
		OfflinePlayer offlinePlayer = (Player) player;

		if (Exams.instance().examPricesEnabled)
		{
			if (price > 0.0D && !economy.has(offlinePlayer, price))
			{
				Exams.sendMessage(playerName, ChatColor.RED + "You need " + economy.format(getExamPrice(examName)) + " to take this exam");
				return false;
			}
		}

		if (StudentManager.hasRecentExamAttempt(playerName))
		{
			if (!player.hasPermission("exams.nocooldown"))
			{
				Exams.sendMessage(playerName, ChatColor.RED + "You cannot take another exam so soon!");
				Exams.sendMessage(playerName, ChatColor.RED + "Try again in " + ChatColor.YELLOW + StudentManager.getTimeUntilCanDoExam(playerName) + ChatColor.RED + " minutes");
				return false;
			}
		}

		StudentManager.signupForExam(playerName, examName);

		if (Exams.instance().examPricesEnabled)
		{
			if(price > 0.0D)
			{
				economy.withdrawPlayer(offlinePlayer, price);
				Exams.sendMessage(playerName, ChatColor.AQUA + "You paid " + ChatColor.YELLOW + economy.format(getExamPrice(examName)) + ChatColor.AQUA + " for signing up to this exam");
			}
		}

		return true;
	}
}