package com.ylinor.harvester;

import com.flowpowered.math.vector.Vector3i;
import com.ylinor.harvester.data.beans.HarvestableBean;
import com.ylinor.harvester.data.beans.RespawningBlockBean;
import com.ylinor.harvester.data.dao.RespawningBlockDao;
import com.ylinor.harvester.data.handlers.ConfigurationHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.property.item.HarvestingProperty;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class HarvestListener {
    /**
     * Handle actions occurring when blocks are destroyed
     * @param event Resource destruction event
     */
    @Listener
    public void onBlockBreakEvent(ChangeBlockEvent.Break event) {
        final Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent() && player.get().gameMode().get() != GameModes.CREATIVE) {
            for (Transaction<BlockSnapshot> transaction: event.getTransactions()) {
                BlockType destroyedBlockType = transaction.getOriginal().getState().getType();
                Optional<HarvestableBean> optionalHarvestable = identifyHarvestable(destroyedBlockType);
                if (optionalHarvestable.isPresent()) {
                    HarvestableBean harvestable = optionalHarvestable.get();
                    if (isBlockBreakable(harvestable, destroyedBlockType, player.get().getItemInHand(HandTypes.MAIN_HAND))) {
                        registerRespawningBlock(harvestable, transaction.getOriginal().getPosition());
                        return;
                    }
                }
            }
            event.setCancelled(true);
        }
    }

    /**
     * Return harvestable if present in configuration
     * @param blockType Type of block to look for
     * @return Optional of harvestable
     */
    private Optional<HarvestableBean> identifyHarvestable(BlockType blockType) {
        String blockTypeName = blockType.getName().trim();
        List<HarvestableBean> harvestables = ConfigurationHandler.getHarvestableList();
        for (HarvestableBean harvestable: harvestables){
            if (harvestable.getType().trim().equals(blockTypeName)) {
                return Optional.of(harvestable);
            }
        }
        return Optional.empty();
    }

    /**
     * Check if a block type is breakable with given tool
     * @param harvestable Data of the breakable block
     * @param optionalTool Tool in player's hand
     * @return Block is present in config file
     */
    private boolean isBlockBreakable(HarvestableBean harvestable, BlockType blockType, Optional<ItemStack> optionalTool) {
        if (optionalTool.isPresent()) {
            ItemStack tool = optionalTool.get();
            Optional<HarvestingProperty> optionalHarvestingProperty = tool.getProperty(HarvestingProperty.class);
            if (optionalHarvestingProperty.isPresent()) {
                HarvestingProperty harvestingProperty = optionalHarvestingProperty.get();
                return harvestingProperty.getValue().contains(blockType);
            }
        }
        return false;
    }

    /**
     * Register a mined block in database so it can be respawn later
     * @param harvestable Block to respawn later
     */
    private void registerRespawningBlock(HarvestableBean harvestable, Vector3i position) {
        Random random = new Random();
        int respawnMin = harvestable.getRespawnMin()*60, respawnMax = harvestable.getRespawnMax()*60;
        int respawnDelay = random.nextInt((respawnMax - respawnMin)+1) + respawnMin;
        Timestamp respawnDate = new Timestamp(Calendar.getInstance().getTime().getTime());
        respawnDate.setTime(respawnDate.getTime()/1000 + respawnDelay);
        RespawningBlockBean respawningBlock = new RespawningBlockBean(position.getX(), position.getY(), position.getZ(),
                harvestable.getType(), (int)respawnDate.getTime());
        RespawningBlockDao.addRespawningBlock(respawningBlock);
    }

    /**
     * Check if resources need to be respawn and do it if necessary
     */
    static public void checkBlockRespawn() {
        World world = Sponge.getServer().getWorld("world").get();
        List<RespawningBlockBean> respawningBlocks = RespawningBlockDao.getRespawningBlocks();
        if (respawningBlocks.size() > 0) {
            Harvester.getLogger().info("Respawning resources : " + respawningBlocks.size() + " resources.");
        }
        for (RespawningBlockBean block: respawningBlocks) {
            Location<World> location = new Location<World>(world, block.getX(), block.getY(), block.getZ());
            Optional<BlockType> replacingType = Sponge.getRegistry().getType(BlockType.class, block.getBlockType());
            if (replacingType.isPresent()) {
                location.setBlockType(replacingType.get(), Cause.source(Sponge.getPluginManager().getPlugin("harvester").get()).build());
}
        }
        RespawningBlockDao.removeRespawningBlocks(respawningBlocks);
    }
}
