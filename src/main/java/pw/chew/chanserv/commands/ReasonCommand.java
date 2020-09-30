package pw.chew.chanserv.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import pw.chew.chanserv.util.AuditLogManager;
import pw.chew.chanserv.util.MemberHelper;
import pw.chew.chanserv.util.MessageHelper;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReasonCommand extends Command {

    public ReasonCommand() {
        this.name = "reason";
        this.guildOnly = true;
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

        event.getMessage().delete().queue();

        int caseId = Integer.parseInt(event.getArgs().split(" ")[0]);
        String reason = event.getArgs().replace(caseId + " ", "");

        if (caseId < 95) {
            event.reply("This log happened prior to audit-log 2.0. The reason will not be able to change! Please try again.");
            return;
        }

        List<String> cases = AuditLogManager.getEntries();
        Message message = event.getGuild().getTextChannelById("210174983278690304").retrieveMessageById(cases.get(caseId)).complete();

        MessageEmbed embed = message.getEmbeds().get(0);

        List<MessageEmbed.Field> fields = new ArrayList<>(embed.getFields());

        for (MessageEmbed.Field find : fields) {
            if (!find.getValue().equals("[Unknown]")) {
                continue;
            }
            fields.remove(find);
            fields.add(new MessageEmbed.Field(find.getName(), event.getAuthor().getAsMention(), true));
        }

        for (MessageEmbed.Field find : fields) {
            if (!find.getName().equals("Reason")) {
                continue;
            }
            fields.remove(find);
            fields.add(new MessageEmbed.Field(find.getName(), reason, true));
        }

        int color = 0;

        switch (embed.getTitle().split(" | ")[0]) {
            case "Ban" -> color = 0xFF0000;
            case "Kick" -> color = 0xFAD765;
            case "User Mode Updated" -> color = 0x2FFA76;
        }

        EmbedBuilder newEmbed = new EmbedBuilder();
        newEmbed.setTitle(embed.getTitle());
        for (MessageEmbed.Field field : fields) {
            newEmbed.addField(field);
        }
        if (color > 0)
            newEmbed.setColor(color);

        message.editMessage(newEmbed.build()).queue(msg -> MessageHelper.sendTemporaryMessage(event.getTextChannel(), "Reason for case " + caseId + " set to: " + reason, 5));
    }
}
