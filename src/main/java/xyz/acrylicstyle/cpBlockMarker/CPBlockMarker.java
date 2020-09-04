package xyz.acrylicstyle.cpBlockMarker;

import net.minecraft.server.v1_16_R2.Entity;
import net.minecraft.server.v1_16_R2.EntityFallingBlock;
import net.minecraft.server.v1_16_R2.IBlockData;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R2.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_16_R2.PlayerConnection;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import util.Collection;
import util.CollectionList;
import util.CollectionSet;
import xyz.acrylicstyle.cpBlockMarker.commands.CPBMTabCompleter;
import xyz.acrylicstyle.paper.event.server.PluginSendMessageEvent;
import xyz.acrylicstyle.tomeito_api.TomeitoAPI;
import xyz.acrylicstyle.tomeito_api.utils.Log;

import java.util.ConcurrentModificationException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class CPBlockMarker extends JavaPlugin implements Listener {
    public final CollectionSet<UUID> inspecting = new CollectionSet<>();
    public static final Logger log = Log.as("CoreProtectBlockMarker");
    public CollectionSet<UUID> integration = new CollectionSet<>();
    public final Collection<UUID, CollectionList<EntityData>> entities = new Collection<>();
    public static CPBlockMarker instance;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        TomeitoAPI.registerTabCompleter("cpbm", new CPBMTabCompleter());
        TomeitoAPI.getInstance().registerCommands(this.getClassLoader(), "cpbm", "xyz.acrylicstyle.cpBlockMarker.commands");
    }

    @Override
    public void onDisable() {
        Log.info("Clearing entities...");
        entities.keysList().forEach(this::clearEntities);
        Log.info("Done! Disabling plugin.");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (inspecting.contains(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    public void onPluginSendMessage(PluginSendMessageEvent e) {
        if (!integration.contains(e.getPlayer().getUniqueId())) return;
        if (e.getMessage() == null) return;
        if (!e.getMessage().contains("§")) return;
        if (e.getMessage().contains("- Inspector now enabled.")) {
            log.info("Enabling marker for " + e.getPlayer().getName());
            inspecting.add(e.getPlayer().getUniqueId());
        } else if (e.getMessage().contains("- Inspector now disabled.")) {
            log.info("Disabling marker for " + e.getPlayer().getName());
            clearEntities(e.getPlayer().getUniqueId());
            inspecting.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.VERY_LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!inspecting.contains(e.getPlayer().getUniqueId())) return;
        if (!entities.containsKey(e.getPlayer().getUniqueId())) entities.add(e.getPlayer().getUniqueId(), new CollectionList<>());
        if (e.getClickedBlock() == null) return;
        e.setCancelled(true);
        Location block = e.getClickedBlock().getLocation();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (block.getWorld().getTileEntity(block) == null) {
                Location relative = e.getClickedBlock().getRelative(e.getBlockFace()).getLocation();
                render(e.getPlayer(), relative);
            } else {
                render(e.getPlayer(), block);
            }
        } else {
            render(e.getPlayer(), block);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        inspecting.remove(e.getPlayer().getUniqueId());
        entities.remove(e.getPlayer().getUniqueId());
    }

    public void clearEntities(@NotNull UUID uuid) {
        if (!entities.containsKey(uuid)) entities.add(uuid, new CollectionList<>());
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) doClear(uuid, player);
        entities.get(uuid).clear();
    }

    public void clearEntitiesKeepCache(@NotNull UUID uuid) {
        if (!entities.containsKey(uuid)) entities.add(uuid, new CollectionList<>());
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        doClear(uuid, player);
    }

    private void doClear(@NotNull UUID uuid, Player player) {
        AtomicReference<Integer[]> arr = new AtomicReference<>();
        arr.set(entities.get(uuid).clone().map(EntityData::getEntity).map(Entity::getId).toArray(new Integer[0]));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(ArrayUtils.toPrimitive(arr.get())));
        entities.get(uuid).forEach(ed -> player.sendBlockChange(
                new Location(
                        ed.getEntity().getWorld().getWorld(),
                        ed.getX()-0.5,
                        ed.getY(),
                        ed.getZ()-0.5
                ),
                ed.getLocation().getBlock().getBlockData()
        ));
    }

    public static final IBlockData GLASS = ((CraftBlockData) Material.GLASS.createBlockData()).getState();

    public static IBlockData getIBlockData(Block block) {
        if (block.getType().isAir()) return GLASS;
        return ((CraftBlockData) block.getBlockData()).getState();
    }

    public void render(@NotNull Player player, @NotNull Location location) {
        Block block = location.getBlock();
        EntityFallingBlock entity = new EntityFallingBlock(((CraftWorld) location.getWorld()).getHandle(), block.getX()+0.5, block.getY(), block.getZ()+0.5, getIBlockData(location.getBlock()));
        EntityData ed = new EntityData(entity, block.getLocation());
        try {
            if (entities.get(player.getUniqueId()).filter(fb -> fb.equals(ed)).size() > 0) return;
        } catch (ConcurrentModificationException ignored) {}
        entity.setLocation(block.getX()+0.5, block.getY(), block.getZ()+0.5, 0, 0);
        entity.setNoGravity(true);
        entity.glowing = true;
        entity.setFlag(6, true);
        entities.get(player.getUniqueId()).add(ed);
        reRender(player);
    }

    public void reRender(@NotNull Player player) {
        clearEntitiesKeepCache(player.getUniqueId());
        PlayerConnection pc = ((CraftPlayer) player).getHandle().playerConnection;
        Bukkit.getScheduler().runTaskLater(this, () -> entities.get(player.getUniqueId()).forEach(data -> {
            pc.sendPacket(new PacketPlayOutSpawnEntity(data.getEntity(), net.minecraft.server.v1_16_R2.Block.getCombinedId(getIBlockData(data.getLocation().getBlock()))));
            pc.sendPacket(new PacketPlayOutEntityMetadata(data.getEntity().getId(), data.getEntity().getDataWatcher(), true));
        }), 1);
    }
}
