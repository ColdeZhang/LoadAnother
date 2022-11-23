package deercloud.loadanother;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.util.Objects;

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

    public boolean setDestination(Player player,Chunk to) {
        if (player != creator) {
            return false;
        }
        if (to.getWorld().getEnvironment() == World.Environment.NORMAL) {
            m_world_chunk = to;
        } else if (to.getWorld().getEnvironment() == World.Environment.NETHER) {
            m_nether_chunk = to;
        }
        m_world_chunk.getWorld().setChunkForceLoaded(m_world_chunk.getX(), m_world_chunk.getZ(), true);
        m_nether_chunk.getWorld().setChunkForceLoaded(m_nether_chunk.getX(), m_nether_chunk.getZ(), true);
        hash = String.valueOf(m_world_chunk.getX()) + String.valueOf(m_world_chunk.getZ()) + String.valueOf(m_nether_chunk.getX()) + String.valueOf(m_nether_chunk.getZ());
        return true;
    }

    public String getHash() {
        return hash;
    }

    public boolean isCreatorExist() {
        Entity[] entities_world = m_world_chunk.getEntities();
        Entity[] entities_nether = m_nether_chunk.getEntities();
        Entity[] entities = new Entity[entities_world.length + entities_nether.length];
        System.arraycopy(entities_world, 0, entities, 0, entities_world.length);
        System.arraycopy(entities_nether, 0, entities, entities_world.length, entities_nether.length);
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (player == creator) {
                    return true;
                }
            }
        }
        return false;
    }

    public void unload() {
        m_logger.warn(creator, "| 总共清理了" + clearChunkEntitiesByName(m_world_chunk, getHash()) + clearChunkEntitiesByName(m_nether_chunk, getHash()) + "个实体");
        m_world_chunk.getWorld().setChunkForceLoaded(m_world_chunk.getX(), m_world_chunk.getZ(), false);
        m_nether_chunk.getWorld().setChunkForceLoaded(m_nether_chunk.getX(), m_nether_chunk.getZ(), false);
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
    public int clearChunkEntitiesByName(Chunk chunk, String with_name) {
        int number = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Monster) {
                String name_custom = entity.getCustomName();
                if (Objects.equals(name_custom, with_name)) {
                    ((Monster) entity).setRemoveWhenFarAway(true);
                    entity.remove();
                    ((Monster) entity).setHealth(0);
                }
            }
            number++;
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

    MyLogger m_logger = LoadAnother.getInstance().getMyLogger();

}
