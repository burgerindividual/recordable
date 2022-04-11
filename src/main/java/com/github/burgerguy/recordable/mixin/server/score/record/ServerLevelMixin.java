package com.github.burgerguy.recordable.mixin.server.score.record;

import com.github.burgerguy.recordable.server.score.broadcast.ScoreBroadcasterRegistry;
import com.github.burgerguy.recordable.server.score.record.ScoreRecorderRegistry;
import com.github.burgerguy.recordable.server.score.ServerScoreRegistriesContainer;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements ServerScoreRegistriesContainer {

    private final ScoreRecorderRegistry recorderRegistry = new ScoreRecorderRegistry();
    private final ScoreBroadcasterRegistry broadcasterRegistry = new ScoreBroadcasterRegistry();

    protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l) {
        super(writableLevelData, resourceKey, holder, supplier, bl, bl2, l);
    }

    @Override
    public ScoreRecorderRegistry getScoreRecorderRegistry() {
        return recorderRegistry;
    }

    @Override
    public ScoreBroadcasterRegistry getScoreBroadcasterRegistry() {
        return broadcasterRegistry;
    }

    @Inject(method = "playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", at = @At("TAIL"))
    private void captureSound(Player player, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, CallbackInfo ci) {
        // we probably don't need to deal with dimensions, because dimensions are stored in their own ServerLevel
        recorderRegistry.captureSound(
                sound,
                x,
                y,
                z,
                volume,
                pitch
        );
    }

    @Inject(method = "playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V", at = @At("TAIL"))
    private void captureSound(Player player, Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch, CallbackInfo ci) {
        recorderRegistry.captureSound(
                sound,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                volume,
                pitch
        );
    }

    public void playLocalSound(BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch, boolean distanceDelay) {
        this.playLocalSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, sound, category, volume, pitch, distanceDelay);
    }

    @Override
    public void playLocalSound(double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, boolean distanceDelay) {
        super.playLocalSound(x, y, z, sound, category, volume, pitch, distanceDelay);
        recorderRegistry.captureSound(
                sound,
                x,
                y,
                z,
                volume,
                pitch
        );
    }

    @Inject(method = "levelEvent", at = @At("TAIL"))
    private void captureEventAndReplicateSound(Player player, int type, BlockPos pos, int data, CallbackInfo ci) {
        Random random = this.random;
        switch(type) {
            case 1000:
                this.playLocalSound(pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1001:
                this.playLocalSound(pos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.2F, false);
                break;
            case 1002:
                this.playLocalSound(pos, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.2F, false);
                break;
            case 1003:
                this.playLocalSound(pos, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
                break;
            case 1004:
                this.playLocalSound(pos, SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
                break;
            case 1005:
                this.playLocalSound(pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1006:
                this.playLocalSound(pos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1007:
                this.playLocalSound(pos, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1008:
                this.playLocalSound(pos, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1009:
                if (data == 0) {
                    this.playLocalSound(pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
                } else if (data == 1) {
                    this.playLocalSound(pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.7F, 1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F, false);
                }
                break;
            case 1010:
//                if (Item.byId(data) instanceof RecordItem) {
//                    this.playStreamingMusic(((RecordItem)Item.byId(data)).getSound(), pos);
//                } else {
//                    this.playStreamingMusic((SoundEvent)null, pos);
//                }
                // TODO: implement other records playing
                break;
            case 1011:
                this.playLocalSound(pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1012:
                this.playLocalSound(pos, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1013:
                this.playLocalSound(pos, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1014:
                this.playLocalSound(pos, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1015:
                this.playLocalSound(pos, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1016:
                this.playLocalSound(pos, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1017:
                this.playLocalSound(pos, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1018:
                this.playLocalSound(pos, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1019:
                this.playLocalSound(pos, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1020:
                this.playLocalSound(pos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1021:
                this.playLocalSound(pos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1022:
                this.playLocalSound(pos, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1024:
                this.playLocalSound(pos, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1025:
                this.playLocalSound(pos, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 0.05F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1026:
                this.playLocalSound(pos, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1027:
                this.playLocalSound(pos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1029:
                this.playLocalSound(pos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1030:
                this.playLocalSound(pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1031:
                this.playLocalSound(pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1032:
                // don't record portal through sound
                break;
            case 1033:
                this.playLocalSound(pos, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1034:
                this.playLocalSound(pos, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1035:
                this.playLocalSound(pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1036:
                this.playLocalSound(pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1037:
                this.playLocalSound(pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1039:
                this.playLocalSound(pos, SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.3F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1040:
                this.playLocalSound(pos, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1041:
                this.playLocalSound(pos, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1042:
                this.playLocalSound(pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1043:
                this.playLocalSound(pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1044:
                this.playLocalSound(pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1045:
                this.playLocalSound(pos, SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.BLOCKS, 2.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1046:
                this.playLocalSound(pos, SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON, SoundSource.BLOCKS, 2.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1047:
                this.playLocalSound(pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 2.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1048:
                this.playLocalSound(pos, SoundEvents.SKELETON_CONVERTED_TO_STRAY, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1500:
                boolean success = data > 0;
                this.playLocalSound(pos, success ? SoundEvents.COMPOSTER_FILL_SUCCESS : SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1501:
                this.playLocalSound(pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
                break;
            case 1502:
                this.playLocalSound(pos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
                break;
            case 1503:
                this.playLocalSound(pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 1505:
                this.playLocalSound(pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
            case 2001:
                BlockState blockState = Block.stateById(data);
                if (!blockState.isAir()) {
                    SoundType soundType = blockState.getSoundType();
                    this.playLocalSound(pos, soundType.getBreakSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
                }
                break;
            case 2007:
                this.playLocalSound(pos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 2006:
                if (data == 1) {
                    this.playLocalSound(pos, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                }
                break;
            case 3000:
                this.playLocalSound(pos, SoundEvents.END_GATEWAY_SPAWN, SoundSource.BLOCKS, 10.0F, (1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F) * 0.7F, false);
                break;
            case 3001:
                this.playLocalSound(pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 64.0F, 0.8F + random.nextFloat() * 0.3F, false);
                break;
            case 3003:
                this.playLocalSound(pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;
        }
    }

}
