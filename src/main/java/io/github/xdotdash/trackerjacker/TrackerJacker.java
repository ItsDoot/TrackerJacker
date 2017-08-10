package io.github.xdotdash.trackerjacker;

import co.aikar.taskchain.BukkitTaskChainFactory;
import com.google.common.collect.Lists;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.github.xdotdash.trackerjacker.command.TrackCommand;
import io.github.xdotdash.trackerjacker.listener.TrackerListener;
import io.github.xdotdash.trackerjacker.runnable.TrackerRunnable;
import io.github.xdotdash.trackerjacker.util.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class TrackerJacker extends JavaPlugin {

    public static boolean usingEssentials;

    public static final List<String> ONLINE_UUIDS = Lists.newCopyOnWriteArrayList();

    private ComboPooledDataSource cpds;

    @Override
    public void onEnable() {
        Bukkit.getOnlinePlayers().stream()
                .filter((player) -> player.hasPermission("tracker.track"))
                .map(Entity::getUniqueId)
                .map(UUID::toString)
                .forEach(ONLINE_UUIDS::add);

        Tasks.factory = BukkitTaskChainFactory.create(this);

        usingEssentials = Bukkit.getPluginManager().isPluginEnabled("Essentials");

        initConfig();

        try {
            initDB();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (PropertyVetoException e) {
            getLogger().log(Level.SEVERE, "An error occurred while creating the connection pool", e);
            getLogger().severe("Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        Bukkit.getPluginManager().registerEvents(new TrackerListener(), this);

        getCommand("track").setExecutor(new TrackCommand(cpds));
        getCommand("track").setPermission("tracker.check");
        getCommand("track").setTabCompleter((sender, command, label, args) -> Bukkit.getOnlinePlayers().stream()
                .filter((player) -> ONLINE_UUIDS.contains(player.getUniqueId().toString()))
                .map(HumanEntity::getName)
                .collect(Collectors.toList()));

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new TrackerRunnable(cpds, getLogger(), getConfig().getBoolean("recalculator")), 0, 20 * 60);
    }

    private void initConfig() {
        getConfig().addDefault("mysql.hostname", "127.0.0.1");
        getConfig().addDefault("mysql.port", 3306);
        getConfig().addDefault("mysql.username", "admin");
        getConfig().addDefault("mysql.password", "password");
        getConfig().addDefault("mysql.database", "minecraft");
        getConfig().addDefault("recalculator", false);
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();
    }

    private void initDB() throws SQLException, PropertyVetoException {
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass("com.mysql.jdbc.Driver");

        String hostname = getConfig().getString("mysql.hostname");
        Integer port = getConfig().getInt("mysql.port");
        String username = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");
        String database = getConfig().getString("mysql.database");

        cpds.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database);
        cpds.setUser(username);
        cpds.setPassword(password);

        cpds.setInitialPoolSize(5);
        cpds.setMinPoolSize(5);
        cpds.setMaxPoolSize(20);
        cpds.setAcquireIncrement(5);

        Connection conn = cpds.getConnection();

        String create = "CREATE TABLE IF NOT EXISTS " + DB.PLAYERS + " (uuid VARCHAR(36), day0 INT, day1 INT, day2 INT, day3 INT, day4 INT, day5 INT, day6 INT);";

        conn.prepareStatement(create).execute();

        conn.close();
    }

    @Override
    public void onDisable() {
        cpds.close();
    }
}