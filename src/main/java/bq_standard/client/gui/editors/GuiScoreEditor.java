package bq_standard.client.gui.editors;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.controls.GuiNumberField;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.utils.JsonHelper;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import bq_standard.tasks.TaskScoreboard;
import bq_standard.tasks.TaskScoreboard.ScoreOperation;
import com.google.gson.JsonObject;

public class GuiScoreEditor extends GuiScreenThemed implements IVolatileScreen
{
	TaskScoreboard task;
	GuiTextField txtField;
	GuiNumberField numField;
	ScoreOperation operation = ScoreOperation.MORE_OR_EQUAL;
	JsonObject data;
	
	public GuiScoreEditor(GuiScreen parent, TaskScoreboard task)
	{
		super(parent, "bq_standard.title.edit_hunt");
		this.data = task.writeToJson(new JsonObject(), EnumSaveType.CONFIG);
		operation = ScoreOperation.valueOf(JsonHelper.GetString(data, "operation", "MORE_OR_EQUAL").toUpperCase());
		operation = operation != null? operation : ScoreOperation.MORE_OR_EQUAL;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		txtField = new GuiTextField(mc.fontRenderer, guiLeft + sizeX/2 - 99, guiTop + sizeY/2 - 19, 198, 18);
		txtField.setText(JsonHelper.GetString(data, "scoreName", "Score"));
		numField = new GuiNumberField(mc.fontRenderer, guiLeft + sizeX/2 + 1, guiTop + sizeY/2 + 1, 98, 18);
		numField.setText("" + JsonHelper.GetNumber(data, "target", 1).intValue());
		this.buttonList.add(new GuiButtonThemed(buttonList.size(), guiLeft + sizeX/2 - 100, guiTop + sizeY/2, 100, 20, operation.GetText()));
		this.buttonList.add(new GuiButtonThemed(buttonList.size(), guiLeft + sizeX/2 - 100, guiTop + sizeY/2 + 20, 200, 20, I18n.format("betterquesting.btn.advanced")));
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.name"), guiLeft + sizeX/2 - 100, guiTop + sizeY/2 - 32, getTextColor());
		numField.drawTextBox();
		txtField.drawTextBox();
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 0)
		{
			task.readFromJson(data, EnumSaveType.CONFIG);
		} else if(button.id == 1)
		{
			int i = operation.ordinal();
			operation = ScoreOperation.values()[(i + 1)%ScoreOperation.values().length];
			button.displayString = operation.GetText();
			data.addProperty("operation", operation.name());
		} else if(button.id == 2)
		{
			mc.displayGuiScreen(new GuiJsonObject(this, data, null));
		}
	}
	
    /**
     * Called when the mouse is clicked.
     */
	@Override
    protected void mouseClicked(int mx, int my, int click)
    {
		super.mouseClicked(mx, my, click);
		
		numField.mouseClicked(mx, my, click);
		txtField.mouseClicked(mx, my, click);
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int keyCode)
    {
        super.keyTyped(character, keyCode);
        
        numField.textboxKeyTyped(character, keyCode);
		data.addProperty("target", numField.getNumber().intValue());
		
		txtField.textboxKeyTyped(character, keyCode);
		data.addProperty("scoreName", txtField.getText());
    }
}
