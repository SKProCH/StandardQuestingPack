package bq_standard.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketHandler;
import bq_standard.core.BQ_Standard;
import bq_standard.network.StandardPacketType;
import bq_standard.rewards.loot.LootRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

public class PktHandlerLootDatabase implements IPacketHandler
{
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
	    if(sender == null || sender.mcServer == null) return;
	    
		if(!sender.mcServer.getConfigurationManager().func_152596_g(sender.getGameProfile()))
		{
			BQ_Standard.logger.log(Level.WARN, "Player " + sender.getCommandSenderName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit loot chests without OP permissions!");
			sender.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "You need to be OP to edit loot!"));
			return; // Player is not operator. Do nothing
		}
		
		BQ_Standard.logger.log(Level.INFO, "Player " + sender.getCommandSenderName() + " edited loot chests");
		
		LootRegistry.INSTANCE.readFromNBT(data.getCompoundTag("Database"));
		LootRegistry.INSTANCE.updateClients();
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		LootRegistry.INSTANCE.readFromNBT(data.getCompoundTag("Database"));
	}

	@Override
	public ResourceLocation getRegistryName()
	{
		return StandardPacketType.LOOT_SYNC.GetLocation();
	}
}
