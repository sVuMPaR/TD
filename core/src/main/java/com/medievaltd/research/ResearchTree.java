package com.medievaltd.research;

import java.util.*;

public class ResearchTree {
    private final Map<ResearchId, ResearchNode> nodes = new LinkedHashMap<>();

    public ResearchTree() {
        // Tier 1 — Y=580 (baseCost: level 1 affordable after 1-2 map clears)
        add(ResearchId.GOLD_BONUS,
            "+Золото", "+золота с монстра", "макс: +15%",
            50, List.of(), 180, 580);
        add(ResearchId.DAMAGE_BOOST,
            "+Урон", "+урон всех башен", "макс: +10%",
            60, List.of(), 380, 580);
        add(ResearchId.SPEED_BOOST,
            "+Скорость", "+скорость атаки", "макс: +8%",
            60, List.of(), 580, 580);
        add(ResearchId.RANGE_BOOST,
            "+Дальность", "+дальность башен", "макс: +10%",
            50, List.of(), 780, 580);

        // Tier 2 — Y=420 (need several clears to unlock + level)
        add(ResearchId.PHYSICAL_PEN,
            "Физ. пробой", "-физ. сопротивления", "макс: -15%",
            80, List.of(ResearchId.DAMAGE_BOOST), 120, 420);
        add(ResearchId.FIRE_PEN,
            "Огн. пробой", "-огн. сопротивления", "макс: -15%",
            80, List.of(ResearchId.DAMAGE_BOOST), 320, 420);
        add(ResearchId.ICE_PEN,
            "Лед. пробой", "-лед. сопротивления", "макс: -15%",
            80, List.of(ResearchId.SPEED_BOOST), 520, 420);
        add(ResearchId.LIGHTNING_PEN,
            "Молн. пробой", "-сопр. молнии", "макс: -15%",
            80, List.of(ResearchId.SPEED_BOOST), 720, 420);
        add(ResearchId.MAGIC_PEN,
            "Маг. пробой", "-магич. сопротивления", "макс: -15%",
            80, List.of(ResearchId.RANGE_BOOST), 920, 420);

        // Tier 3 — Y=260 (serious investment needed)
        add(ResearchId.BONUS_VS_BOSSES,
            "Охота на боссов", "+урон по боссам", "макс: +20%",
            120, List.of(ResearchId.PHYSICAL_PEN, ResearchId.FIRE_PEN), 220, 260);
        add(ResearchId.BONUS_VS_SLOWED,
            "Добивание", "+урон по замедлённым", "макс: +15%",
            100, List.of(ResearchId.ICE_PEN), 520, 260);
        add(ResearchId.BONUS_VS_BLOCKED,
            "Добивание-2", "+урон по блокированным", "макс: +15%",
            100, List.of(ResearchId.LIGHTNING_PEN), 720, 260);
        add(ResearchId.ALL_PEN,
            "Комбо-пробой", "-всех сопротивлений", "макс: -10%",
            140, List.of(ResearchId.FIRE_PEN, ResearchId.ICE_PEN, ResearchId.MAGIC_PEN), 920, 260);

        // Tier 4 — Y=110 (endgame, many sessions to max)
        add(ResearchId.DAMAGE_BOOST_2,
            "+Урон II", "+урон всех башен", "макс: +15%",
            160, List.of(ResearchId.DAMAGE_BOOST, ResearchId.BONUS_VS_BOSSES), 320, 110);
        add(ResearchId.GOLD_BONUS_2,
            "+Золото II", "+золото с монстра", "макс: +20%",
            130, List.of(ResearchId.GOLD_BONUS, ResearchId.BONUS_VS_BOSSES), 620, 110);
    }

    private void add(ResearchId id, String name, String desc, String maxEffect,
                     int baseCost, List<ResearchId> requires, float x, float y) {
        nodes.put(id, new ResearchNode(id, name, desc, maxEffect, baseCost, requires, x, y));
    }

    public Collection<ResearchNode> all() {
        return nodes.values();
    }

    public ResearchNode get(ResearchId id) {
        return nodes.get(id);
    }
}
