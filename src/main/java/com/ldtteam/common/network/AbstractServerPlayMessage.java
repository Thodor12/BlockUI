package com.ldtteam.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client (sender) -> Server (receiver) message
 */
public abstract class AbstractServerPlayMessage extends AbstractUnsidedPlayMessage implements IServerboundDistributor
{
    /**
     * This constructor should be called from message call site, ie. the code where you instantiate the message to send it to server
     *
     * @param type message type
     */
    public AbstractServerPlayMessage(final PlayMessageType<?> type)
    {
        super(type);
    }

    /**
     * In this constructor you deserialize received network payload. Formerly known as <code>#fromBytes(RegistryFriendlyByteBuf)</code>
     *
     * @param buf received network payload
     * @param type message type
     * @apiNote you can keep this protected to reduce visibility
     */
    protected AbstractServerPlayMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(type);
    }

    /**
     * Executes message action on main thread.
     *
     * @param context network context
     * @param player  server player which is receiving this packet
     */
    protected abstract void onExecute(final IPayloadContext context, final ServerPlayer player);
}
