package deercloud.loadanother;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class BindChunk {

    public BindChunk(Player player, Chunk from, Chunk to) {
        this.m_last_player = player;
        if (from.getWorld().getEnvironment() == World.Environment.NORMAL) {
            m_world_chunk = from;
            m_nether_chunk = to;
        } else if (from.getWorld().getEnvironment() == World.Environment.NETHER) {
            m_world_chunk = to;
            m_nether_chunk = from;
        }
        m_nether_chunk_hash = m_nether_chunk.hashCode();
        m_world_chunk_hash = m_world_chunk.hashCode();
        hash = m_nether_chunk_hash + m_world_chunk_hash;
        m_state = State.FORCE;
    }

    public Integer getHashCode() {
        return hash;
    }

    public void updateLastPlayer(Player player) {
        m_last_player = player;
    }

    public Player getContainingPlayer() {
        Entity[] entities_world = m_world_chunk.getEntities();
        Entity[] entities_nether = m_nether_chunk.getEntities();
        Entity[] entities = new Entity[entities_world.length + entities_nether.length];
        System.arraycopy(entities_world, 0, entities, 0, entities_world.length);
        System.arraycopy(entities_nether, 0, entities, entities_world.length, entities_nether.length);
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                return (Player) entity;
            }
        }
        return null;
    }

    public boolean isContain(Chunk chunk) {
        return chunk.hashCode() == m_nether_chunk_hash || chunk.hashCode() == m_world_chunk_hash;
    }

    public Player getLastPlayer() {
        return m_last_player;
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

    private Player m_last_player = null;
    private Chunk m_world_chunk = null;
    private Integer m_world_chunk_hash = null;
    private Chunk m_nether_chunk = null;
    private Integer m_nether_chunk_hash = null;
    private State m_state = null;
    int hash = 0;
    enum State {
        FORCE,  // 一般强加载
        DELAY,  // 延迟强加载
        UNLOAD  // 可被卸载
    }

}
