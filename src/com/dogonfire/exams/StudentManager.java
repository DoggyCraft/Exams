package com.dogonfire.exams;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class StudentManager
{
	static private FileConfiguration	studentsConfig		= null;
	static private File					studentsConfigFile	= null;
	static private StudentManager	 	instance;

	public StudentManager()
	{
		instance = this;
	}

	public void load()
	{
		if (studentsConfigFile == null)
		{
			studentsConfigFile = new File(Exams.instance().getDataFolder(), "students.yml");
		}

		studentsConfig = YamlConfiguration.loadConfiguration(studentsConfigFile);

		Exams.log("Loaded " + studentsConfig.getKeys(false).size() + " students.");
	}

	public void save()
	{
		if ((studentsConfig == null) || (studentsConfigFile == null))
		{
			return;
		}

		try
		{
			studentsConfig.save(studentsConfigFile);
		}
		catch (Exception ex)
		{
			Exams.log("Could not save config to " + studentsConfigFile + ": " + ex.getMessage());
		}
	}

	public static void setLastExamTime(String playerName)
	{
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();

		studentsConfig.set(playerName + ".LastExamTime", formatter.format(thisDate));

		instance.save();
	}
	
	public static void resetExamTime(String playerName)
	{
		studentsConfig.set(playerName + ".LastExamTime", null);
		
		save();
	}

	public static boolean hasRecentExamAttempt(String playerName)
	{
		String lastExamString = studentsConfig.getString(playerName + ".LastExamTime");

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date lastExamDate = null;
		Date thisDate = new Date();
		try
		{
			lastExamDate = formatter.parse(lastExamString);
		}
		catch (Exception ex)
		{
			lastExamDate = new Date();
			lastExamDate.setTime(0L);
		}

		long diff = thisDate.getTime() - lastExamDate.getTime();
		long diffMinutes = diff / 60000L;

		return diffMinutes < Exams.instance().minExamTime;
	}

	public static boolean hasOutdatedExamAttempt(String playerName)
	{
		String lastExamString = studentsConfig.getString(playerName + ".LastExamTime");

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date lastExamDate = null;
		Date thisDate = new Date();
		try
		{
			lastExamDate = formatter.parse(lastExamString);
		}
		catch (Exception ex)
		{
			lastExamDate = new Date();
			lastExamDate.setTime(0L);
		}

		long diff = thisDate.getTime() - lastExamDate.getTime();
		long diffMinutes = diff / 60000L;

		return diffMinutes > Exams.instance().autoCleanTime;
	}

	public static int getTimeUntilCanDoExam(World world, String studentName)
	{
		String lastExamString = studentsConfig.getString(studentName + ".LastExamTime");

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date lastExamDate = null;
		Date thisDate = new Date();

		try
		{
			lastExamDate = formatter.parse(lastExamString);
		}
		catch (Exception ex)
		{
			lastExamDate = new Date();
			lastExamDate.setTime(0L);
		}

		long diff = thisDate.getTime() - lastExamDate.getTime();
		long diffMinutes = diff / 60000L;

		return (int) (Exams.instance().minExamTime - diffMinutes);
	}

	public static void answer(String playerName, String answer)
	{
		String correctAnswer = studentsConfig.getString(playerName + ".ExamCorrectOption");

		if (answer.equalsIgnoreCase(correctAnswer))
		{
			int correctAnswers = studentsConfig.getInt(playerName + ".ExamCorrectAnswers");

			correctAnswers++;

			studentsConfig.set(playerName + ".ExamCorrectAnswers", correctAnswers);

			save();
		}

		this.setLastExamTime(playerName);
	}
	
	public static void setOriginalRanks(String playerName, String[] oldRanks)
	{
		List<String> ranks = Arrays.asList(oldRanks);
		studentsConfig.set(playerName + ".OriginalRanks", ranks);
		instance.save();
	}
	
	public static List<String> getOriginalRanksList(String playerName)
	{
		return studentsConfig.getStringList(playerName + ".OriginalRanks");
	}
	
	public static String[] getOriginalRanks(String playerName)
	{
		List<String> originalRanksList = getOriginalRanksList(playerName);
		String[] originalRanks = originalRanksList.toArray(new String[0]);
		return originalRanks;
	}
	
	public static String getLastExamTime(String playerName)
	{
		return studentsConfig.getString(playerName + ".LastExamTime");
	}
	
	public static void setPassedExam(String playerName, String exam)
	{
		List<String> passedExams = getPassedExams(playerName);
		
		passedExams.add(exam);
		
		studentsConfig.set(playerName + ".PassedExams", passedExams);		
		
		instance.save();
	}

	public static List<String> getPassedExams(String playerName)
	{
		return studentsConfig.getStringList(playerName + ".PassedExams");		
	}

	public static boolean signupForExam(String playerName, String examName)
	{
		studentsConfig.set(playerName + ".Exam", examName);
		studentsConfig.set(playerName + ".ExamCorrectAnswers", 0);
		studentsConfig.set(playerName + ".ExamProgressIndex", -1);

		instance.save();

		return true;
	}

	public static boolean isDoingExam(String playerName)
	{
		return studentsConfig.getInt(playerName + ".ExamProgressIndex") > -1;
	}

	public static int nextExamQuestionIndex(String playerName)
	{
		int questionIndex = studentsConfig.getInt(playerName + ".ExamProgressIndex");

		questionIndex++;

		studentsConfig.set(playerName + ".ExamProgressIndex", questionIndex);

		instance.save();

		return questionIndex;
	}

	public static int getExamProgressIndexForStudent(String playerName)
	{
		return studentsConfig.getInt(playerName + ".ExamProgressIndex");
	}

	public static int getExamQuestionIndexForStudent(String playerName)
	{
		List<String> questions = studentsConfig.getStringList(playerName + ".ExamQuestionIndices");
		int examProgressIndex = Integer.parseInt(studentsConfig.getString(playerName + ".ExamProgressIndex"));

		return Integer.parseInt(questions.get(examProgressIndex));
	}

	public static void setExamQuestionForStudent(String playerName, String question, List<String> options, String correctOption)
	{
		studentsConfig.set(playerName + ".ExamQuestion", question);
		studentsConfig.set(playerName + ".ExamQuestionOptions", options);
		studentsConfig.set(playerName + ".ExamCorrectOption", correctOption);

		instance.save();
	}

	public static String getExamQuestionForStudent(String playerName)
	{
		return studentsConfig.getString(playerName + ".ExamQuestion");
	}

	public static List<String> getExamQuestionOptionsForStudent(String playerName)
	{
		return studentsConfig.getStringList(playerName + ".ExamQuestionOptions");
	}

	public static void setExamForStudent(String playerName, String examName, List<String> questions)
	{
		studentsConfig.set(playerName + ".Exam", examName);
		studentsConfig.set(playerName + ".ExamProgressIndex", -1);
		studentsConfig.set(playerName + ".ExamCorrectAnswers", 0);
		studentsConfig.set(playerName + ".ExamQuestionIndices", questions);

		Exams.logDebug("Setting question indices of size " + questions.size());

		instance.save();
	}

	public static int getCorrectAnswersForStudent(String playerName)
	{
		return studentsConfig.getInt(playerName + ".ExamCorrectAnswers");
	}

	public static String getExamForStudent(String playerName)
	{
		return studentsConfig.getString(playerName + ".Exam");
	}

	public static Set<String> getStudents()
	{
		Set<String> allStudents = studentsConfig.getKeys(false);

		return allStudents;
	}

	public static void removeStudent(String studentName)
	{
		studentsConfig.set(studentName + ".Exam", null);
		studentsConfig.set(studentName + ".ExamProgressIndex", null);
		studentsConfig.set(studentName + ".ExamQuestionIndices", null);
		studentsConfig.set(studentName + ".ExamQuestion", null);
		studentsConfig.set(studentName + ".ExamQuestionOptions", null);
		studentsConfig.set(studentName + ".ExamCorrectOption", null);
		studentsConfig.set(studentName + ".ExamCorrectAnswers", null);
		studentsConfig.set(studentName + ".OriginalRanks", null);

		Exams.logDebug(studentName + " was removed as student");

		instance.save();
	}

	public static void deleteStudent(String studentName)
	{
		studentsConfig.set(studentName, null);

		instance.save();
	}
}
