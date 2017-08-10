package io.github.xdotdash.trackerjacker.command;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainAbortAction;
import co.aikar.taskchain.TaskChainFactory;
import io.github.xdotdash.trackerjacker.DB;
import io.github.xdotdash.trackerjacker.util.StaffTime;
import io.github.xdotdash.trackerjacker.util.Tasks;
import io.github.xdotdash.trackerjacker.util.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public class TrackCommand implements CommandExecutor {

    private final DataSource ds;

    public TrackCommand(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /track <player>");
            return true;
        }

        TaskChainFactory factory = Tasks.factory;

        String player = args[0];

        Tasks.newChain()
                .asyncFirst(() -> {
                    Player exact = Bukkit.getPlayerExact(player);

                    if (player.length() == 36) {
                        return UUID.fromString(player);
                    } else if (exact != null) {
                        return exact.getUniqueId();
                    } else try {
                        return UUIDFetcher.fromName(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .abortIfNull(new NoUuidAbortAction(sender))
                .async((uuid) -> {
                    try {
                        Connection conn = ds.getConnection();

                        ResultSet rs = conn.prepareStatement(selectSql(uuid.toString())).executeQuery();

                        if (!rs.next()) {
                            return null;
                        }

                        return new StaffTime(uuid, rs.getInt("day0"), rs.getInt("day1"), rs.getInt("day2"),
                                rs.getInt("day3"), rs.getInt("day4"), rs.getInt("day5"), rs.getInt("day6"));
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .abortIfNull(new NotTrackedAbortAction(sender))
                .syncLast((time) -> {
                    int weekTotal = time.getDay0() + time.getDay1() + time.getDay2() + time.getDay3() + time.getDay4()
                            + time.getDay5() + time.getDay6();

                    sender.sendMessage(DARK_GRAY + "== " + GOLD + player + DARK_GRAY + " ===============");
                    sender.sendMessage(GRAY + " Today: " + GOLD + displayTime(time.getDay0()));
                    sender.sendMessage(GRAY + " Yesterday: " + GOLD + displayTime(time.getDay1()));
                    sender.sendMessage(GRAY + " 2 Days Ago: " + GOLD + displayTime(time.getDay2()));
                    sender.sendMessage(GRAY + " 3 Days Ago: " + GOLD + displayTime(time.getDay3()));
                    sender.sendMessage(GRAY + " 4 Days Ago: " + GOLD + displayTime(time.getDay4()));
                    sender.sendMessage(GRAY + " 5 Days Ago: " + GOLD + displayTime(time.getDay5()));
                    sender.sendMessage(GRAY + " 6 Days Ago: " + GOLD + displayTime(time.getDay6()));
                    sender.sendMessage(GRAY + " Week Total: " + GOLD + displayTime(weekTotal));
                    sender.sendMessage(DARK_GRAY + "===" + chained('=', player.length()) + "================");
                })
                .execute();

        return true;
    }

    private String selectSql(String uuid) {
        //language=MySQL
        return "SELECT * FROM " + DB.PLAYERS + " WHERE uuid='" + uuid + "';";
    }

    private String displayTime(int minutes) {
        StringBuilder time = new StringBuilder();

        double days = minutes / 24 / 60;
        double hours = minutes / 60 % 24;
        double mins = minutes % 60;

        if (days >= 1)
            time.append(Math.round(days)).append(days == 1 ? " day " : " days ");
        if (hours >= 1)
            time.append(Math.round(hours)).append(hours == 1 ? " hour " : " hours ");
        if (mins >= 1)
            time.append(Math.round(mins)).append(mins == 1 ? " min" : " mins");

        String str = time.toString();

        return str.isEmpty() ? "0" : time.toString();
    }

    private String chained(char c, int amount) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < amount; i++) {
            sb.append(c);
        }

        return sb.toString();
    }

    private static class NoUuidAbortAction implements TaskChainAbortAction {

        private final CommandSender sender;

        NoUuidAbortAction(CommandSender sender) {
            this.sender = sender;
        }

        @Override
        public void onAbort(TaskChain chain, Object arg1) {
            sender.sendMessage(ChatColor.DARK_RED + "That player has not logged in since the tracker has been enabled.");
            sender.sendMessage(ChatColor.DARK_RED + "Names are also case-sensitive.");
        }
    }

    private static class NotTrackedAbortAction implements TaskChainAbortAction {

        private final CommandSender sender;

        NotTrackedAbortAction(CommandSender sender) {
            this.sender = sender;
        }

        @Override
        public void onAbort(TaskChain chain, Object arg1) {
            sender.sendMessage(ChatColor.RED + "That player is not tracked.");
        }
    }
}