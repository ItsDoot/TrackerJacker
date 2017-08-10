package io.github.xdotdash.trackerjacker.runnable;

import com.earth2me.essentials.Essentials;
import io.github.xdotdash.trackerjacker.DB;
import io.github.xdotdash.trackerjacker.TrackerJacker;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrackerRunnable implements Runnable {

    private final DataSource ds;
    private final Logger logger;
    private final boolean recalculator;

    private LocalDate date = LocalDate.now();

    public TrackerRunnable(DataSource ds, Logger logger, boolean recalculator) {
        this.ds = ds;
        this.logger = logger;
        this.recalculator = recalculator;
    }

    @Override
    public void run() {
        if (recalculator) {
            LocalDate curDate = LocalDate.now();

            if (curDate.isAfter(date)) { // It's a new day
                logger.info("A new day is upon us; recalculating online times.");
                recalculate();
                logger.info("Online times have been recalculated.");
                date = curDate;
            }
        }

        try {
            Connection conn = ds.getConnection();

            for (String uuid : TrackerJacker.ONLINE_UUIDS) {
                if (TrackerJacker.usingEssentials && isAfk(uuid)) {
                    continue;
                }

                String select = "SELECT day0 FROM " + DB.PLAYERS + " WHERE uuid='" + uuid + "';";

                ResultSet rs = conn.prepareStatement(select).executeQuery();

                if (rs.next()) {
                    int day0 = rs.getInt("day0");

                    String update = "UPDATE " + DB.PLAYERS + " SET day0 = " + (day0 + 1) + " WHERE uuid='" + uuid + "';";
                    conn.prepareStatement(update).execute();
                } else {
                    String insert = "INSERT INTO " + DB.PLAYERS + " VALUES ('" + uuid + "', 1, 0, 0, 0, 0, 0, 0);";
                    conn.prepareStatement(insert).execute();
                }
            }

            conn.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "An error occurred while attempting to update online times", e);
        }
    }

    private void recalculate() {
        try {
            Connection conn = ds.getConnection();

            String selectAll = "SELECT uuid, day0, day1, day2, day3, day4, day5 FROM " + DB.PLAYERS + ";";

            ResultSet rs = conn.prepareStatement(selectAll).executeQuery();

            while (rs.next()) {
                String uuid = rs.getString("uuid");
                int day0 = rs.getInt("day0");
                int day1 = rs.getInt("day1");
                int day2 = rs.getInt("day2");
                int day3 = rs.getInt("day3");
                int day4 = rs.getInt("day4");
                int day5 = rs.getInt("day5");

                String update = "UPDATE " + DB.PLAYERS + " SET "
                        + "day0 = " + 0
                        + ", day1 = " + day0
                        + ", day2 = " + day1
                        + ", day3 = " + day2
                        + ", day4 = " + day3
                        + ", day5 = " + day4
                        + ", day6 = " + day5
                        + " WHERE uuid='" + uuid + "';";

                conn.prepareStatement(update).execute();
            }

            conn.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "An error occurred while attempting to recalculate online times", e);
        }
    }

    private boolean isAfk(String uuid) {
        return Essentials.getPlugin(Essentials.class).getUser(UUID.fromString(uuid)).isAfk();
    }
}