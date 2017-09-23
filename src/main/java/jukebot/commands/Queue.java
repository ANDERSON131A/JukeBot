package jukebot.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import jukebot.DatabaseHandler;
import jukebot.JukeBot;
import jukebot.audioutilities.GuildMusicManager;
import jukebot.utils.Bot;
import jukebot.utils.Command;
import jukebot.utils.Time;
import jukebot.utils.Parsers;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.List;

public class Queue implements Command {

    private final DatabaseHandler db = new DatabaseHandler();

    public void execute(MessageReceivedEvent e, String query) {

        final GuildMusicManager musicManager = JukeBot.getGuildMusicManager(e.getGuild());
        final List<AudioTrack> currentQueue = musicManager.handler.getQueue();

        if (currentQueue.size() == 0) {
            e.getTextChannel().sendMessage(new EmbedBuilder()
                    .setColor(Bot.EmbedColour)
                    .setTitle("No songs queued")
                    .setDescription("Use `" + db.getPrefix(e.getGuild().getIdLong()) + "now` to view the current track.")
                    .build()
            ).queue();
            return;
        }

        final String queueDuration = Time.format(currentQueue.stream().map(AudioTrack::getDuration).reduce(0L, (a, b) -> a + b));
        final StringBuilder queue = new StringBuilder();

        final int maxPages = (int) Math.ceil((double) currentQueue.size() / 10);
        int page = Parsers.Number(query, 1);

        if (page < 1)
            page = 1;

        if (page > maxPages)
            page = maxPages;

        int begin = (page - 1) * 10;
        int end = (begin + 10) > currentQueue.size() ? currentQueue.size() : (begin + 10);

        for (int i = begin; i < end; i++)
            queue.append("`").append(i + 1).append(".` **[").append(currentQueue.get(i).getInfo().title).append("](").append(currentQueue.get(i).getInfo().uri).append(")** <@").append(currentQueue.get(i).getUserData()).append(">\n");

        e.getChannel().sendMessage(new EmbedBuilder()
                .setColor(Bot.EmbedColour)
                .setTitle("Queue (" + currentQueue.size() + " songs, " + queueDuration + ")")
                .setDescription(queue.toString().trim())
                .addField("Playback", "**Repeat:** " + (musicManager.handler.repeat ? "On" : "Off") + " **/ Shuffle:** " + (musicManager.handler.shuffle ? "On" : "Off"), true)
                .setFooter("Viewing page " + (page) + "/" + (maxPages), null)
                .build()
        ).queue();

    }
}