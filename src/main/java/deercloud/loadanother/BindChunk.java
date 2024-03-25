package deercloud.loadanother;

import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public class BindChunk {

    public BindChunk(Player player, Chunk from) {
        this.creator = player;
        if (from.getWorld().getEnvironment() == World.Environment.NORMAL) {
            m_world_chunk = from;
        } else if (from.getWorld().getEnvironment() == World.Environment.NETHER) {
            m_nether_chunk = from;
        }
        m_state = State.FORCE;
    }

    public BindChunk(Chunk world, Chunk nether) {
        creator = null;
        m_world_chunk = world;
        m_nether_chunk = nether;
        hash = String.valueOf(m_world_chunk.getX()) + m_world_chunk.getZ() + m_nether_chunk.getX() + m_nether_chunk.getZ();
    }

    public boolean setDestination(Player player, Chunk to) {
        if (player != creator) {
            return false;
        }
        if (to.getWorld().getEnvironment() == World.Environment.NORMAL) {
            m_world_chunk = to;
        } else if (to.getWorld().getEnvironment() == World.Environment.NETHER) {
            m_nether_chunk = to;
        }
        setForceLoaded(true);
        hash = String.valueOf(m_world_chunk.getX()) + m_world_chunk.getZ() + m_nether_chunk.getX() + m_nether_chunk.getZ();
        m_cache.saveBindChunk(this);
        return true;
    }

    public String getHash() {
        return hash;
    }

    public boolean isCreatorExist() {
        int R = LoadAnother.instance.config.getRadius() - 1;
        World player_world = creator.getWorld();
        int player_x = creator.getLocation().getBlockX();
        int player_z = creator.getLocation().getBlockZ();
        if (player_world.getEnvironment() == World.Environment.NORMAL) {
            return player_x >= m_world_chunk.getX() * 16 - R && player_x <= m_world_chunk.getX() * 16 + R && player_z >= m_world_chunk.getZ() * 16 - R && player_z <= m_world_chunk.getZ() * 16 + R;
        } else if (player_world.getEnvironment() == World.Environment.NETHER) {
            return player_x >= m_nether_chunk.getX() * 16 - R && player_x <= m_nether_chunk.getX() * 16 + R && player_z >= m_nether_chunk.getZ() * 16 - R && player_z <= m_nether_chunk.getZ() * 16 + R;
        }
        return false;
    }

    public void unload() {
        LoadAnother.globalScheduler.run(LoadAnother.instance, (instance) -> {
            m_world_chunk.load();
            m_nether_chunk.load();
            XLogger.debug("| 总共清理了" + (clearChunkEntities(m_world_chunk) + clearChunkEntities(m_nether_chunk)) + "个实体");

            setForceLoaded(false);
            m_cache.removeBindChunk(this);
        });
    }

    public boolean isContain(Chunk chunk) {
        if (chunk.getWorld().getEnvironment() == World.Environment.NORMAL) {
            return chunk.getX() == m_world_chunk.getX() && chunk.getZ() == m_world_chunk.getZ();
        } else if (chunk.getWorld().getEnvironment() == World.Environment.NETHER) {
            return chunk.getX() == m_nether_chunk.getX() && chunk.getZ() == m_nether_chunk.getZ();
        }
        return false;
    }

    // 清空一个区块中的敌对生物实体
    public int clearChunkEntities(Chunk chunk) {
        int number = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Monster) {
                Monster monster = (Monster) entity;
                Component name = monster.customName();
                if (name != null) {
                    continue;
                }
                monster.setRemoveWhenFarAway(true);
                monster.remove();
                number++;
            }
        }
        return number;
    }


    public Player getCreator() {
        return creator;
    }

    public State getState() {
        return m_state;
    }

    public void setState(State state) {
        m_state = state;
    }

    public Chunk getWorldChunk() {
        return m_world_chunk;
    }

    public Chunk getNetherChunk() {
        return m_nether_chunk;
    }

    private void setForceLoaded(boolean forceLoaded) {
        LoadAnother.globalScheduler.run(LoadAnother.instance, (instance) -> {
            int R = LoadAnother.instance.config.getRadius() - 1;
            for (int x = -R; x <= R; x++) {
                for (int z = -R; z <= R; z++) {
                    m_world_chunk.getWorld().getChunkAt(m_world_chunk.getX() + x, m_world_chunk.getZ() + z).setForceLoaded(forceLoaded);
                    m_nether_chunk.getWorld().getChunkAt(m_nether_chunk.getX() + x, m_nether_chunk.getZ() + z).setForceLoaded(forceLoaded);
                }
            }
        });
    }

    private final Player creator;
    private Chunk m_world_chunk = null;
    private Chunk m_nether_chunk = null;
    private State m_state;
    String hash = null;

    enum State {
        FORCE,  // 一般强加载
        DELAY,  // 延迟强加载
        UNLOAD  // 可被卸载
    }

    Cache m_cache = LoadAnother.instance.getCache();

}
