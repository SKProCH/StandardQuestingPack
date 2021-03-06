package bq_standard.client.gui.editors.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterNumber;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.gui_args.GArgsNBT;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetGUIs;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import bq_standard.tasks.TaskScoreboard;
import bq_standard.tasks.TaskScoreboard.ScoreOperation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

public class GuiEditTaskScoreboard extends GuiScreenCanvas implements IVolatileScreen
{
    private final IQuest quest;
    private final TaskScoreboard task;
    
    public GuiEditTaskScoreboard(GuiScreen parent, IQuest quest, TaskScoreboard task)
    {
        super(parent);
        this.quest = quest;
        this.task = task;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        Keyboard.enableRepeatEvents(true);
        
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
        
        cvBackground.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(16, 16, 16, -32), 0), QuestTranslation.translate("bq_standard.title.edit_scoreboard")).setAlignment(1).setColor(PresetColor.TEXT_HEADER.getColor()));
        
        cvBackground.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.MID_CENTER, -100, -28, 50, 12, 0), QuestTranslation.translate("betterquesting.gui.name")).setColor(PresetColor.TEXT_MAIN.getColor()));
        cvBackground.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.MID_CENTER, -100, -12, 50, 12, 0), "ID").setColor(PresetColor.TEXT_MAIN.getColor())); // TODO: Localise this?
        
        cvBackground.addPanel(new PanelTextField<>(new GuiTransform(GuiAlign.MID_CENTER, -50, -32, 150, 16, 0), task.scoreDisp, FieldFilterString.INSTANCE).setCallback(value -> task.scoreDisp = value));
        cvBackground.addPanel(new PanelTextField<>(new GuiTransform(GuiAlign.MID_CENTER, -50, -16, 150, 16, 0), task.scoreName, FieldFilterString.INSTANCE).setCallback(value -> task.scoreName = value));
        
        cvBackground.addPanel(new PanelButtonStorage<ScoreOperation>(new GuiTransform(GuiAlign.MID_CENTER, -100, 0, 50, 16, 0), -1, task.operation.GetText(), task.operation)
        {
            @Override
            public void onButtonClick()
            {
                ScoreOperation[] v = ScoreOperation.values();
                ScoreOperation n = v[(getStoredValue().ordinal() + 1)%v.length];
                this.setStoredValue(n);
                this.setText(n.GetText());
                task.operation = n;
            }
        });
        
        cvBackground.addPanel(new PanelTextField<>(new GuiTransform(GuiAlign.MID_CENTER, -50, 0, 150, 16, 0), "" + task.target, FieldFilterNumber.INT).setCallback(value -> task.target = value));
        
        final GuiScreen screenRef = this;
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, -100, 16, 200, 16, 0), -1, QuestTranslation.translate("betterquesting.btn.advanced"))
        {
            @Override
            public void onButtonClick()
            {
                mc.displayGuiScreen(QuestingAPI.getAPI(ApiReference.THEME_REG).getGui(PresetGUIs.EDIT_NBT, new GArgsNBT<>(screenRef, task.writeToNBT(new NBTTagCompound()), task::readFromNBT, null)));
            }
        });
        
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), -1, QuestTranslation.translate("gui.back"))
        {
            @Override
            public void onButtonClick()
            {
                sendChanges();
                mc.displayGuiScreen(parent);
            }
        });
    }
    
    private static final ResourceLocation QUEST_EDIT = new ResourceLocation("betterquesting:quest_edit"); // TODO: Really need to make the native packet types accessible in the API
    private void sendChanges()
    {
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("config", quest.writeToNBT(new NBTTagCompound()));
		base.setTag("progress", quest.writeProgressToNBT(new NBTTagCompound(), null));
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal()); // Action: Update data
		tags.setInteger("questID", QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
		tags.setTag("data",base);
		QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(QUEST_EDIT, tags));
    }
}
