package deercloud.loadanother;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class Cache {

    public Cache(LoadAnother plugin) {
        this.m_plugin = plugin;
        reload();
    }

    public void reload() {
        if (!isCacheFileExist()) {
            createCacheFile();
        }
        m_cache_file = loadCacheFile();
    }

    // 创建缓存文件 cache.yml
    public void createCacheFile() {
        m_plugin.saveResource(".cache/cache.yml", false);
    }

    // 加载缓存文件 cache.yml
    public YamlConfiguration loadCacheFile() {
        return YamlConfiguration.loadConfiguration(new File(m_plugin.getDataFolder(), ".cache/cache.yml"));
    }

    // 检查是否存在缓存文件 cache.yml
    public boolean isCacheFileExist() {
        return m_plugin.getResource(".cache/cache.yml") != null;
    }

    public void saveCacheFile() {
        try {
            m_cache_file.save(new File(m_plugin.getDataFolder(), ".cache/cache.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveBindChunk(BindChunk bindChunk) {
        Chunk m_world = bindChunk.getWorldChunk();
        Chunk m_nether = bindChunk.getNetherChunk();

        // chunk信息添加到yaml列表
        List<String> chunk_info = m_cache_file.getStringList("chunk_info");
        chunk_info.add(m_world.getX() + "," + m_world.getZ() + "," + m_nether.getX() + "," + m_nether.getZ());
        m_cache_file.set("chunk_info", chunk_info);

        saveCacheFile();
    }

    public int getCacheSize() {
        return m_cache_file.getStringList("chunk_info").size();
    }

    public Chunk getWorldChunk(int index) {
        String[] chunk_info = m_cache_file.getStringList("chunk_info").get(index).split(",");
        return new Location(m_plugin.getServer().getWorld("world"), Integer.parseInt(chunk_info[0]), 0, Integer.parseInt(chunk_info[1])).getChunk();
    }

    public Chunk getNetherChunk(int index) {
        String[] chunk_info = m_cache_file.getStringList("chunk_info").get(index).split(",");
        return new Location(m_plugin.getServer().getWorld("world_nether"), Integer.parseInt(chunk_info[2]), 0, Integer.parseInt(chunk_info[3])).getChunk();
    }

    public void removeBindChunk(BindChunk bindChunk) {
        Chunk m_world = bindChunk.getWorldChunk();
        Chunk m_nether = bindChunk.getNetherChunk();

        // chunk信息添加到yaml列表
        List<String> chunk_info = m_cache_file.getStringList("chunk_info");
        chunk_info.remove(m_world.getX() + "," + m_world.getZ() + "," + m_nether.getX() + "," + m_nether.getZ());
        m_cache_file.set("chunk_info", chunk_info);

        saveCacheFile();
    }
    public void removeBindChunk(int index) {
        // chunk信息添加到yaml列表
        List<String> chunk_info = m_cache_file.getStringList("chunk_info");
        chunk_info.remove(index);
        m_cache_file.set("chunk_info", chunk_info);

        saveCacheFile();
    }

    LoadAnother m_plugin;

    YamlConfiguration m_cache_file = null;

}
