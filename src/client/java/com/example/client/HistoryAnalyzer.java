package com.example.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.inventory.ChestMenu;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HistoryAnalyzer {

    private static final Pattern HITS   = Pattern.compile("Попадания\\D*(\\d+)");
    private static final Pattern MISSES = Pattern.compile("Промахи\\D*(\\d+)");
    private static final Pattern REACH  = Pattern.compile("Reach\\D*([0-9]+[.,]?[0-9]*)");
    private static final Pattern COMBO  = Pattern.compile("комбо\\D*(\\d+)");
    private static final Pattern DAMAGE = Pattern.compile("Урон\\D*([0-9]+[.,]?[0-9]*)");
    private static final Pattern WIN    = Pattern.compile("Победа");

    private static final int COLS = 9; // ширина
    private static final int ROWS = 6; // высота, сверху вниз

    // данные каждого матча — по ключу, чтобы не дублировать
    private static final Set<String> seenMatches = new LinkedHashSet<>();
    private static final List<int[]>   matchStats = new ArrayList<>(); // {hits, misses, win}
    private static final List<double[]> matchExtra = new ArrayList<>(); // {reach, dmg, combo}

    private static String currentNick = "";
    private static boolean started = false;

    public static void reset() {
        seenMatches.clear();
        matchStats.clear();
        matchExtra.clear();
        currentNick = "";
        started = false;
    }

    public static String signature() {
        return ""; // больше не используется, оставлено для совместимости
    }

    public static boolean isReady() {
        return true; // больше не используется — копим непрерывно
    }

    // вызывается каждый тик, пока открыта история: тихо копит уникальные матчи
    public static void collect(String guiTitle) {
        String nick = guiTitle.replace("История:", "").trim();

        // сменился игрок или это новое открытие — начинаем копить заново
        if (!started || !nick.equals(currentNick)) {
            reset();
            currentNick = nick;
            started = true;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.containerMenu instanceof ChestMenu menu)) return;
        Container container = menu.getContainer();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int i = row * COLS + col;
                if (i >= container.getContainerSize()) break;

                ItemStack stack = container.getItem(i);
                if (stack.isEmpty()) continue;

                StringBuilder sb = new StringBuilder();
                Component customName = stack.get(DataComponents.CUSTOM_NAME);
                if (customName != null) sb.append(customName.getString()).append("\n");
                ItemLore lore = stack.get(DataComponents.LORE);
                if (lore != null) {
                    sb.append(String.join("\n",
                            lore.lines().stream().map(Component::getString).toList()));
                }

                String text = sb.toString();
                if (text.isEmpty()) continue;
                text = text.replaceAll("§.", "").replace('\u00A0', ' ');

                if (text.contains("Загрузка")) continue; // слот ещё не заполнен

                Matcher mH = HITS.matcher(text);
                Matcher mMiss = MISSES.matcher(text);
                if (!mH.find() || !mMiss.find()) continue; // не матч

                String key = customName != null
                        ? customName.getString()
                        : text; // уникальность матча — по имени, а без имени — по тексту

                if (!seenMatches.add(key)) continue; // этот матч уже учтён

                int hits = Integer.parseInt(mH.group(1));
                int misses = Integer.parseInt(mMiss.group(1));
                double dmg = 0, reach = 0, combo = 0;

                Matcher mReach = REACH.matcher(text);
                Matcher mCombo = COMBO.matcher(text);
                Matcher mDmg = DAMAGE.matcher(text);

                if (mDmg.find()) dmg = Double.parseDouble(mDmg.group(1).replace(',', '.'));
                if (mReach.find()) reach = Double.parseDouble(mReach.group(1).replace(',', '.'));
                if (mCombo.find()) combo = Integer.parseInt(mCombo.group(1));

                int win = WIN.matcher(text).find() ? 1 : 0;

                matchStats.add(new int[]{hits, misses, win});
                matchExtra.add(new double[]{reach, dmg, combo});
            }
        }
    }

    // вызывается при закрытии меню — печатает ОДИН итоговый вердикт по всем матчам
    public static void finish() {
        if (!started) return; // история даже не открывалась — молчим

        String nick = currentNick;
        int matches = matchStats.size();

        send("Анализ игрока: §e" + nick);
        if (matches == 0) {
            send("§7Матчи не найдены — нечего анализировать. Смотрите history_debug.txt");
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.containerMenu instanceof ChestMenu menu) {
                dumpContainer(nick, menu.getContainer());
            }
            reset();
            return;
        }

        int totalHits = 0, totalMisses = 0, wins = 0, perfectFights = 0;
        double reachSum = 0, reachMax = 0, damageSum = 0, maxCombo = 0;
        int reachCount = 0;

        for (int k = 0; k < matches; k++) {
            int[] st = matchStats.get(k);
            double[] ex = matchExtra.get(k);
            totalHits += st[0];
            totalMisses += st[1];
            wins += st[2];
            if (st[0] > 0 && st[1] == 0 && ex[1] > 0) perfectFights++;
            if (ex[0] > 0) {
                reachSum += ex[0];
                reachMax = Math.max(reachMax, ex[0]);
                reachCount++;
            }
            damageSum += ex[1];
            maxCombo = Math.max(maxCombo, ex[2]);
        }

        double accuracy = (totalHits + totalMisses) > 0
                ? totalHits * 100.0 / (totalHits + totalMisses) : 0.0;
        double avgReach = reachCount > 0 ? reachSum / reachCount : 0.0;
        double winRate = wins * 100.0 / matches;

        send(String.format("Матчей: §f%d §7| Побед: §f%d§7 (%.0f%%)", matches, wins, winRate));
        send(String.format("Попаданий: §f%d §7| Промахов: §f%d §7| Общая точность: §b%.1f%%",
                totalHits, totalMisses, accuracy));
        send(String.format("Ср. Reach: §f%.1f §7| Макс. Reach: §f%.1f §7| Макс. комбо: §f%d",
                avgReach, reachMax, (int) maxCombo));

        // ---- вердикт ----
        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (avgReach >= 4.0) {
            score += 5;
            reasons.add(String.format("аномальный Reach (ср. %.1f) — типично для Reach/KillAura", avgReach));
        } else if (avgReach >= 3.2) {
            score += 3;
            reasons.add(String.format("завышенный Reach (ср. %.1f > ванильных ~3.0)", avgReach));
        }

        if (matches >= 5 && (totalHits + totalMisses) >= 100 && accuracy >= 85) {
            score += 3;
            reasons.add(String.format("точность %.0f%% на %d матчах — слишком стабильно", accuracy, matches));
        }

        if (perfectFights >= 3) {
            score += 2;
            reasons.add(perfectFights + " боёв с нулём промахов при активной агрессии");
        }

        if (matches >= 5 && winRate >= 90 && damageSum > 0) {
            score += 1;
            reasons.add(String.format("винрейт %.0f%% при наличии урона - редкое доминирование", winRate));
        }

        if (totalHits == 0 && totalMisses == 0) {
            reasons.add("ударов не зафиксировано - данные пустые, проверьте вручную");
        }

        String verdict;
        ChatFormatting color;
        if (score >= 5)      { verdict = "ЧИТЕР";          color = ChatFormatting.RED; }
        else if (score >= 2) { verdict = "ПОДОЗРИТЕЛЬНЫЙ"; color = ChatFormatting.GOLD; }
        else                 { verdict = "ЧИСТЫЙ";         color = ChatFormatting.GREEN; }

        send("Вердикт: " + color + verdict + " §7(скор: " + score + ")");
        for (String r : reasons) send("  §8• §7" + r);

        reset();
    }

    // полный дамп контейнера — в файл
    private static void dumpContainer(String nick, Container container) {
        try {
            StringBuilder sb = new StringBuilder("=== История: " + nick + " ===\n");
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack st = container.getItem(i);
                if (st.isEmpty()) continue;
                sb.append("Слот ").append(i).append(": ").append(st.getItem()).append("\n");
                sb.append("   ИМЯ: ").append(st.getHoverName().getString()).append("\n");
                ItemLore lore2 = st.get(DataComponents.LORE);
                if (lore2 != null) {
                    for (Component line : lore2.lines()) {
                        sb.append("   ЛОР: ").append(line.getString()).append("\n");
                    }
                }
            }
            java.nio.file.Files.writeString(java.nio.file.Path.of("history_debug.txt"), sb.toString());
        } catch (Exception ignored) {}
    }

    private static void send(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null && mc.gui.getChat() != null) {
            mc.gui.getChat().addMessage(Component.literal("§b[АнтиЧит] §f" + text));
        }
    }
}
