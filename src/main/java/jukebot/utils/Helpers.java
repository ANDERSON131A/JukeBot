package jukebot.utils;


import com.patreon.resources.Pledge;
import jukebot.Database;
import jukebot.JukeBot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Helpers {
    private static ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "JukeBot-Helper"));
    private static ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "JukeBot-Timer"));
    public static ScheduledExecutorService monitorThread = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "JukeBot-Pledge-Monitor"));

    public static String padRight(String character, String text, int length) {
        if (text.length() == length)
            return text;

        StringBuilder textBuilder = new StringBuilder(text);

        while (textBuilder.length() < length)
            textBuilder.append(character);

        return textBuilder.toString();
    }

    public static String padNumber(int number, int length) {
        if (String.valueOf(number).length() == length)
            return String.valueOf(number);

        StringBuilder textBuilder = new StringBuilder(String.valueOf(number));

        while (textBuilder.length() < length)
            textBuilder.insert(0, "0");

        return textBuilder.toString();
    }

    public static int parseNumber(String num, int def) {
        if (num == null)
            return def;

        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static String fTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;
        long days = (milliseconds / (1000 * 60 * 60 * 24));

        final StringBuilder timeString = new StringBuilder();

        if (days > 0)
            timeString.append(padNumber((int) days, 2)).append(":");

        if (hours > 0 || days > 0)
            timeString.append(padNumber((int) hours, 2)).append(":");

        timeString.append(padNumber((int) minutes, 2)).append(":");
        timeString.append(padNumber((int) seconds, 2));

        return timeString.toString();
    }

    public static void schedule(Runnable task, int delay, TimeUnit unit) {
        timer.schedule(task, delay, unit);
    }

    public static Properties readConfig() throws IOException {
        try (FileReader fr = new FileReader("config.properties")) {
            final Properties p = new Properties();
            p.load(fr);
            return p;
        }
    }

    public static String readFile(String path) {
        try (final FileReader file = new FileReader(path);
             final BufferedReader reader = new BufferedReader(file)
        ) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return null;
        }
    }

    public static void getPatreonPledges(Consumer<List<Pledge>> callback) {
        executor.execute(() -> {
            try {
                callback.accept(JukeBot.patreonApi.fetchAllPledges("750822"));
            } catch (IOException e) {
                callback.accept(null);
                e.printStackTrace();
            }
        });
    }

    public static void monitorPledges() {
        JukeBot.LOG.info("Checking pledges...");
        getPatreonPledges(pledges -> {
            if (pledges == null)
                return;

            Database.getDonorIds().forEach(id -> {
                if (pledges.stream().noneMatch(p -> p.getPatron().getSocialConnections().getDiscord() != null
                        && Long.parseLong(p.getPatron().getSocialConnections().getDiscord().getUser_id()) == id)) {
                    Database.setTier(id, 0);
                    JukeBot.LOG.info("Removed " + id + " from donors");
                }
            });
        });
    }

}
