package jukebot.commands;

import jukebot.JukeBot;
import jukebot.audioutilities.GuildMusicManager;
import jukebot.utils.Bot;
import jukebot.utils.Command;
import jukebot.utils.Helpers;
import jukebot.utils.Permissions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Volume implements Command {

    private final Permissions permissions = new Permissions();

    public void execute(GuildMessageReceivedEvent e, String query) {

        final GuildMusicManager musicManager = JukeBot.getGuildMusicManager(e.getGuild().getAudioManager());

        if (!musicManager.isPlaying()) {
            e.getChannel().sendMessage(new EmbedBuilder()
                    .setColor(Bot.EmbedColour)
                    .setTitle("No playback activity")
                    .setDescription("There's nothing playing.")
                    .build()
            ).queue();
            return;
        }

        if (query.length() == 0) {
            e.getChannel().sendMessage(new EmbedBuilder()
                    .setColor(Bot.EmbedColour)
                    .setTitle("Volume")
                    .setDescription("\uD83D\uDD08 " + musicManager.player.getVolume() + "%")
                    .build()
            ).queue();
        } else {
            if (!permissions.isElevatedUser(e.getMember(), false)) {
                e.getChannel().sendMessage(new EmbedBuilder()
                        .setColor(Bot.EmbedColour)
                        .setTitle("Permission Error")
                        .setDescription("You need to have the DJ role.")
                        .build()
                ).queue();
                return;
            }

            final int newVolume = Helpers.ParseNumber(query, 100);

            musicManager.player.setVolume(newVolume);

            e.getChannel().sendMessage(new EmbedBuilder()
                    .setColor(Bot.EmbedColour)
                    .setTitle("Volume")
                    .setDescription("\uD83D\uDD08 " + musicManager.player.getVolume() + "%")
                    .build()
            ).queue();
        }

    }
}
