package pw.chew.chanserv.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import pw.chew.chanserv.util.AuditLogManager;
import pw.chew.chanserv.util.Roles;
import pw.chew.chewbotcca.util.ResponseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReasonCommand extends SlashCommand {

    public ReasonCommand() {
        this.name = "reason";
        this.help = "Set a reason for a audit log entry (Requires half-op+)";
        this.enabledRoles = Roles.Rank.getRoleIdsHigherThan(2);
        this.defaultEnabled = false;

        this.options = Arrays.asList(
            new OptionData(OptionType.INTEGER, "case", "The case to update.").setRequired(true),
            new OptionData(OptionType.STRING, "reason", "The reason to set the log to.").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        int caseId = (int) event.getOption("case").getAsLong();
        String reason = ResponseHelper.guaranteeStringOption(event, "reason", "");

        if (caseId < 95) {
            event.reply("This log happened prior to audit-log 2.0. The reason will not be able to change! Please try again.").setEphemeral(true).queue();
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
            fields.set(fields.indexOf(find), new MessageEmbed.Field(find.getName(), event.getUser().getAsMention(), true));
        }

        for (MessageEmbed.Field find : fields) {
            if (!find.getName().equals("Reason")) {
                continue;
            }
            fields.set(fields.indexOf(find), new MessageEmbed.Field(find.getName(), reason, true));
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

        message.editMessageEmbeds(newEmbed.build()).queue(msg -> event.reply("Reason for case " + caseId + " set to: " + reason).setEphemeral(true).queue());
    }
}
