package pw.chew.chanserv.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import pw.chew.chanserv.util.AuditLogManager;
import pw.chew.chanserv.util.MemberHelper;
import pw.chew.chanserv.util.Roles;

import java.awt.Color;

public class VoiceCommand extends Command {
    public VoiceCommand() {
        this.name = "voice";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (MemberHelper.getRank(event.getMember()).getPriority() < 2) {
            event.reply(
                new EmbedBuilder()
                    .setTitle("**Permission Error**")
                    .setDescription("You do not have the proper user modes to do this! You must have +h (half-op) or higher.")
                    .setColor(Color.RED)
                    .build()
            );
            return;
        }

        Member user = event.getGuild().getMemberById(event.getArgs().replace("<@!", "").replace(">", ""));
        if (user == null) {
            event.reply("Member could not be found. How? did they leave when you pinged? wtf. if you see this, something went bad"); // or you're just browsing github
            return;
        }
        event.getGuild().addRoleToMember(user, Roles.Rank.VOICED.getRole(event.getGuild())).queue(
            e -> {
                event.reply(new EmbedBuilder()
                    .setTitle("**User Mode Changed Successfully**")
                    .setDescription(user.getAsMention() + " has been voiced by " + event.getAuthor().getAsMention())
                    .setColor(Color.GREEN)
                    .build());
                AuditLogManager.logEntry(AuditLogManager.LogType.MODE_CHANGE, user.getUser(), event.getMember(), event.getGuild(), "+v");
            }
        );
    }
}