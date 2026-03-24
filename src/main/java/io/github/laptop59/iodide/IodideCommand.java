package io.github.laptop59.iodide;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class IodideCommand {
    public static LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("iodide")
                .requires(ctx -> ctx.getSender().isOp())
                .then(
                        Commands.literal("reload")
                                .executes(IodideCommand::reload)
                )
                .then(
                        Commands.literal("generate")
                                .executes(IodideCommand::generate)
                )
                .then(
                        Commands.literal("force_reset_template")
                                .executes(IodideCommand::forceReplaceTemplate)
                )
                .build();
    }

    public static int forceReplaceTemplate(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        Iodide.PACKER.forceResetTemplate().whenComplete((_v, e) -> {
            Component message;
            if (e == null) {
                message = Component.text("Successfully reset the Iodide template!").color(NamedTextColor.GREEN);
            } else {
                message = Component.text("Failed to reset the Iodide template. Check the console for more details!").color(NamedTextColor.RED);
                Iodide.logger().log(Level.SEVERE, message.toString(), e);
            }
            Bukkit.getScheduler().runTask(Iodide.INSTANCE, () -> sender.sendMessage(message));
        });
        return 1;
    }

    public static int reload(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        Iodide.PACKER.reload().whenComplete((_v, e) -> {
            Component message;
            if (e == null) {
                message = Component.text("Successfully reloaded Iodide!").color(NamedTextColor.GREEN);
            } else {
                message = Component.text("Failed to reload Iodide. Check the console for more details!").color(NamedTextColor.RED);
                Iodide.logger().log(Level.SEVERE, message.toString(), e);
            }
            Bukkit.getScheduler().runTask(Iodide.INSTANCE, () -> sender.sendMessage(message));
        });
        return 1;
    }

    public static int generate(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        Iodide.PACKER.regenerateZip();
        return 1;
    }
}
