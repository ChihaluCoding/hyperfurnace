package chihalu.hyperfurnace.network.payload;

import chihalu.hyperfurnace.HyperFurnace;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetFurnaceSpeedPayload(int syncId, int speedLevel) implements CustomPayload {
    public static final Id<SetFurnaceSpeedPayload> ID = new Id<>(Identifier.of(HyperFurnace.MOD_ID, "set_furnace_speed"));
    public static final PacketCodec<RegistryByteBuf, SetFurnaceSpeedPayload> CODEC = PacketCodec.of(SetFurnaceSpeedPayload::write, SetFurnaceSpeedPayload::read);

    private static void write(SetFurnaceSpeedPayload payload, RegistryByteBuf buf) {
        buf.writeVarInt(payload.syncId());
        buf.writeVarInt(payload.speedLevel());
    }

    private static SetFurnaceSpeedPayload read(RegistryByteBuf buf) {
        return new SetFurnaceSpeedPayload(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
