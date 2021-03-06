package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import bq_standard.ScoreboardBQ;
import bq_standard.client.gui.editors.tasks.GuiEditTaskScoreboard;
import bq_standard.client.gui.tasks.PanelTaskScoreboard;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskScoreboard;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.scoreboard.*;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskScoreboard implements ITaskTickable
{
	private final List<UUID> completeUsers = new ArrayList<>();
	public String scoreName = "Score";
	public String scoreDisp = "Score";
	public String type = "dummy";
	public int target = 1;
	public float conversion = 1F;
	public String suffix = "";
	public ScoreOperation operation = ScoreOperation.MORE_OR_EQUAL;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskScoreboard.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.scoreboard";
	}
	
	@Override
	public boolean isComplete(UUID uuid)
	{
		return completeUsers.contains(uuid);
	}
	
	@Override
	public void setComplete(UUID uuid)
	{
		if(!completeUsers.contains(uuid))
		{
			completeUsers.add(uuid);
		}
	}

	@Override
	public void resetUser(UUID uuid)
	{
		completeUsers.remove(uuid);
	}

	@Override
	public void resetAll()
	{
		completeUsers.clear();
	}
	
	@Override
	public void tickTask(@Nonnull DBEntry<IQuest> quest, @Nonnull EntityPlayer player)
	{
		if(player.ticksExisted%20 == 0) // Auto-detect once per second
		{
			detect(player, quest.getValue());
		}
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
	    UUID playerID = QuestingAPI.getQuestingUUID(player);
		if(isComplete(playerID)) return;
		
        QuestCache qc = (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
        
		Scoreboard board = player.getWorldScoreboard();
		ScoreObjective scoreObj = board.getObjective(scoreName);
		
		if(scoreObj == null)
		{
			try
			{
		        IScoreObjectiveCriteria criteria = (IScoreObjectiveCriteria)IScoreObjectiveCriteria.field_96643_a.get(type);
		        criteria = criteria != null? criteria : new ScoreDummyCriteria(scoreName);
				scoreObj = board.addScoreObjective(scoreName, criteria);
				scoreObj.setDisplayName(scoreDisp);
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to create score '" + scoreName + "' for task!", e);
				return;
			}
		}

		Score score = board.func_96529_a(player.getCommandSenderName(), scoreObj);
		int points = score.getScorePoints();
		ScoreboardBQ.setScore(player, scoreName, points);
		
		if(operation.checkValues(points, target))
		{
			setComplete(playerID);
			if(qc != null) qc.markQuestDirty(QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json)
	{
		json.setString("scoreName", scoreName);
		json.setString("scoreDisp", scoreDisp);
		json.setString("type", type);
		json.setInteger("target", target);
		json.setFloat("unitConversion", conversion);
		json.setString("unitSuffix", suffix);
		json.setString("operation", operation.name());
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json)
	{
		scoreName = json.getString("scoreName");
		scoreName = scoreName.replaceAll(" ", "_");
		scoreDisp = json.getString("scoreDisp");
		type = json.hasKey("type", 8) ? json.getString("type") : "dummy";
		target = json.getInteger("target");
		conversion = json.getFloat("unitConversion");
		suffix = json.getString("unitSuffix");
		try
        {
            operation = ScoreOperation.valueOf(json.hasKey("operation", 8) ? json.getString("operation") : "MORE_OR_EQUAL");
        } catch(Exception e)
        {
            operation = ScoreOperation.MORE_OR_EQUAL;
        }
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound json, List<UUID> users)
	{
		NBTTagList jArray = new NBTTagList();
		for(UUID uuid : completeUsers)
		{
			jArray.appendTag(new NBTTagString(uuid.toString()));
		}
		json.setTag("completeUsers", jArray);
		
		return json;
	}
 
	@Override
	public void readProgressFromNBT(NBTTagCompound json, boolean merge)
	{
		completeUsers.clear();
		NBTTagList cList = json.getTagList("completeUsers", 8);
		for(int i = 0; i < cList.tagCount(); i++)
		{
			try
			{
				completeUsers.add(UUID.fromString(cList.getStringTagAt(i)));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load UUID for task", e);
			}
		}
	}
	
	public enum ScoreOperation
	{
		EQUAL("="),
		LESS_THAN("<"),
		MORE_THAN(">"),
		LESS_OR_EQUAL("<="),
		MORE_OR_EQUAL(">="),
		NOT("=/=");
		
		private final String text;
		
		ScoreOperation(String text)
		{
			this.text = text;
		}
		
		public String GetText()
		{
			return text;
		}
		
		public boolean checkValues(int n1, int n2)
		{
			switch(this)
			{
				case EQUAL:
					return n1 == n2;
				case LESS_THAN:
					return n1 < n2;
				case MORE_THAN:
					return n1 > n2;
				case LESS_OR_EQUAL:
					return n1 <= n2;
				case MORE_OR_EQUAL:
					return n1 >= n2;
				case NOT:
					return n1 != n2;
			}
			
			return false;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
	{
	    return new PanelTaskScoreboard(rect, quest, this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
	{
	    return new GuiEditTaskScoreboard(parent, quest, this);
	}
}
