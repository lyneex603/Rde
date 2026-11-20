package hexa.staffchat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StaffChatPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private static final String PERM = "hexa.staffchat";
    private final Set<UUID> toggled = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("sc").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission(PERM)) {
            sender.sendMessage(Component.text("Нет прав.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(Component.text("Использование: /sc <сообщение>", NamedTextColor.RED));
                return true;
            }
            if (toggled.remove(p.getUniqueId())) {
                p.sendMessage(Component.text("Чат стаффа: выключен. Ты пишешь в общий чат.", NamedTextColor.YELLOW));
            } else {
                toggled.add(p.getUniqueId());
                p.sendMessage(Component.text("Чат стаффа: включён. Все сообщения идут только админам.", NamedTextColor.GREEN));
            }
            return true;
        }

        send(sender.getName(), String.join(" ", args));
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        if (!toggled.contains(p.getUniqueId())) return;
        if (!p.hasPermission(PERM)) {
            toggled.remove(p.getUniqueId());
            return;
        }
        e.setCancelled(true);
        String msg = PlainTextComponentSerializer.plainText().serialize(e.message());
        send(p.getName(), msg);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        toggled.remove(e.getPlayer().getUniqueId());
    }

    private void send(String name, String msg) {
        Component line = Component.text("[Стафф] ", NamedTextColor.RED)
                .append(Component.text(name, NamedTextColor.YELLOW))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(msg, NamedTextColor.WHITE));
        Bukkit.broadcast(line, PERM);
    }
}
