package bq_standard.rewards;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.rewards.IReward;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import bq_standard.NBTReplaceUtil;
import bq_standard.client.gui.rewards.GuiRewardItem;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.factory.FactoryRewardItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RewardItem implements IReward
{
	public ArrayList<BigItemStack> items = new ArrayList<BigItemStack>();
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryRewardItem.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.reward.item";
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, IQuest quest)
	{
		return true;
	}

	@Override
	public void claimReward(EntityPlayer player, IQuest quest)
	{
		for(BigItemStack r : items)
		{
			BigItemStack stack = r.copy();
			
			for(ItemStack s : stack.getCombinedStacks())
			{
				if(s.getTagCompound() != null)
				{
					s.setTagCompound(NBTReplaceUtil.replaceStrings(s.getTagCompound(), "VAR_NAME", player.getCommandSenderName()));
					s.setTagCompound(NBTReplaceUtil.replaceStrings(s.getTagCompound(), "VAR_UUID", player.getUniqueID().toString()));
				}
				
				if(!player.inventory.addItemStackToInventory(s))
				{
					player.dropPlayerItemWithRandomChoice(s, false);
				}
			}
		}
	}

	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		items = new ArrayList<BigItemStack>();
		for(JsonElement entry : JsonHelper.GetArray(json, "rewards"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			try
			{
				BigItemStack item = JsonHelper.JsonToItemStack(entry.getAsJsonObject());
				
				if(item != null)
				{
					items.add(item);
				} else
				{
					continue;
				}
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load reward item data", e);
			}
		}
	}

	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		JsonArray rJson = new JsonArray();
		for(BigItemStack stack : items)
		{
			rJson.add(JsonHelper.ItemStackToJson(stack, new JsonObject()));
		}
		json.add("rewards", rJson);
		return json;
	}

	@Override
	public IGuiEmbedded getRewardGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiRewardItem(this, posX, posY, sizeX, sizeY);
	}
	
	@Override
	public GuiScreen getRewardEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}
}
