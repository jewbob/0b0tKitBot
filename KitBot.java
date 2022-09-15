package fb.fbware.modules.player;

import fb.fbware.util.ChatUtil;
import fb.fbware.modules.Module;
import fb.fbware.setting.BooleanSetting;
import fb.fbware.setting.StringSetting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KitBot extends Module {

    //Credit: FB#7334
	//thank you to Konas for parseChatMessage
	

    public KitBot() {
        super("KitBot","Automatically teleports to people when they type a password in chat.", Category.PLAYER, this);
    }

    public StringSetting<String> password = register(new StringSetting<>("Password", "FBware"));
    public BooleanSetting<Boolean> kill = register(new BooleanSetting<>("AutoSuicide", true));
    public BooleanSetting<Boolean> debug = register(new BooleanSetting<>("Announce", true));

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.getMessage().getUnformattedText().toLowerCase().contains(password.getValue())) {

            if (debug.getValue()) {
                ChatUtil.sendWarnMessage("Registered a kit request.");
            }

            Optional<Map.Entry<String, String>> parsedMessage = parseChatMessage(event.getMessage().getUnformattedText());

            if (parsedMessage.isPresent()) {

                if (debug.getValue()) {
                    ChatUtil.sendWarnMessage("Attempting to teleport to: " + parsedMessage.get().getKey() + ".");
                }

                mc.player.sendChatMessage("/tpa " + parsedMessage.get().getKey());
            }
        }

        if (event.getMessage().getUnformattedText().contains("teleporting to:")) {

            if (debug.getValue()) {
                ChatUtil.sendWarnMessage("Teleport success!");
            }

            if (kill.getValue()) {

                    if (debug.getValue()) {
                        ChatUtil.sendWarnMessage("Attempting to /kill back to stash.");
                    }

                    mc.player.sendChatMessage("/kill");
                }
        }
    }


		//on 0b0t redstone is shit so when you respawn you will not always get a kit if you step on the pressureplate
		//simple fix: if your inventory is empty, jump onto the pressure plate until you get a kit
    @Override
    public void onUpdate() {
        if (mc.player.inventory.isEmpty() && !mc.player.isDead && mc.player.onGround) {
                if (debug.getValue()) {
                    ChatUtil.sendWarnMessage("Inventory empty, attempting to jump to trigger pressure plate.");
                }
                mc.player.jump();
        }
    }

    public static Optional<Map.Entry<String, String>> parseChatMessage(String messageRaw) {

        Matcher matcher = Pattern.compile("^<(" + "[a-zA-Z0-9_]{3,16}" + ")> (.+)$").matcher(messageRaw);

        String senderName = null;
        String message = null;

        while (matcher.find()) {
            senderName = matcher.group(1);
            message = matcher.group(2);
        }

        if (senderName == null || senderName.isEmpty()) {
            return Optional.empty();
        }

        if (message == null || message.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new AbstractMap.SimpleEntry<>(senderName, message));
    }

}