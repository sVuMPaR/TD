package com.medievaltd.research;

import java.util.*;

public class ResearchTree {
    private final Map<ResearchId, ResearchNode> nodes = new LinkedHashMap<>();

    public ResearchTree() {
        // Tier 1 — row Y=600
        add(new ResearchNode(ResearchId.GOLD_BONUS,
            "+Золото", "+15% золота с монстра",
            80, List.of(), 180, 580));
        add(new ResearchNode(ResearchId.DAMAGE_BOOST,
            "+Урон", "+10% урона всех башен",
            100, List.of(), 380, 580));
        add(new ResearchNode(ResearchId.SPEED_BOOST,
            "+Скорость", "+8% скорости атаки",
            100, List.of(), 580, 580));
        add(new ResearchNode(ResearchId.RANGE_BOOST,
            "+Дальность", "+10% дальности башен",
            80, List.of(), 780, 580));

        // Tier 2 — row Y=430 (elemental pen, requires Tier 1 mix)
        add(new ResearchNode(ResearchId.PHYSICAL_PEN,
            "Физ. пробой", "-15% физ. сопротивления врагов",
            120, List.of(ResearchId.DAMAGE_BOOST), 180, 420));
        add(new ResearchNode(ResearchId.FIRE_PEN,
            "Огн. пробой", "-15% огн. сопротивления",
            120, List.of(ResearchId.DAMAGE_BOOST), 380, 420));
        add(new ResearchNode(ResearchId.ICE_PEN,
            "Лед. пробой", "-15% лед. сопротивления",
            120, List.of(ResearchId.SPEED_BOOST), 580, 420));
        add(new ResearchNode(ResearchId.LIGHTNING_PEN,
            "Молн. пробой", "-15% сопр. молнии",
            120, List.of(ResearchId.SPEED_BOOST), 780, 420));
        add(new ResearchNode(ResearchId.MAGIC_PEN,
            "Маг. пробой", "-15% магич. сопротивления",
            120, List.of(ResearchId.RANGE_BOOST), 980, 420));

        // Tier 3 — row Y=270
        add(new ResearchNode(ResearchId.BONUS_VS_BOSSES,
            "Охота на боссов", "+20% урона по боссам и мини-боссам",
            180, List.of(ResearchId.PHYSICAL_PEN, ResearchId.FIRE_PEN), 280, 260));
        add(new ResearchNode(ResearchId.BONUS_VS_SLOWED,
            "Добивание", "+15% урона по замедлённым целям",
            150, List.of(ResearchId.ICE_PEN), 580, 260));
        add(new ResearchNode(ResearchId.BONUS_VS_BLOCKED,
            "Добивание-2", "+15% урона по заблокированным целям",
            150, List.of(ResearchId.LIGHTNING_PEN), 780, 260));
        add(new ResearchNode(ResearchId.ALL_PEN,
            "Комбо-пробой", "-10% всех сопротивлений",
            200, List.of(ResearchId.FIRE_PEN, ResearchId.ICE_PEN, ResearchId.MAGIC_PEN), 980, 260));

        // Tier 4 — row Y=110
        add(new ResearchNode(ResearchId.DAMAGE_BOOST_2,
            "+Урон II", "+15% урона всех башен",
            250, List.of(ResearchId.DAMAGE_BOOST, ResearchId.BONUS_VS_BOSSES), 380, 110));
        add(new ResearchNode(ResearchId.GOLD_BONUS_2,
            "+Золото II", "+20% золота с монстра",
            200, List.of(ResearchId.GOLD_BONUS, ResearchId.BONUS_VS_BOSSES), 580, 110));
    }

    private void add(ResearchNode node) {
        nodes.put(node.id, node);
    }

    public Collection<ResearchNode> all() {
        return nodes.values();
    }

    public ResearchNode get(ResearchId id) {
        return nodes.get(id);
    }
}
