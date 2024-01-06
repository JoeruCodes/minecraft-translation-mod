package net.joeruPOGsan.translationmod.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public interface ChatCallback {
    Event<ChatCallback> EVENT = EventFactory.createArrayBacked(ChatCallback.class,
            (listeners) -> (player, message) -> {
        for (ChatCallback listener : listeners) {
            ActionResult result = listener.onChatMessage(player, message);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
            });
    ActionResult onChatMessage(ServerPlayerEntity player, Text message);
}
