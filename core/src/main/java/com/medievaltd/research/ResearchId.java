package com.medievaltd.research;

public enum ResearchId {
    // Tier 1 — basic, cheapest
    GOLD_BONUS,          // больше золота с монстра
    DAMAGE_BOOST,        // +урон всех башен
    RANGE_BOOST,         // +дальность всех башен
    SPEED_BOOST,         // +скорость атаки всех башен

    // Tier 2 — elemental pen, requires Tier 1
    FIRE_PEN,            // снижение огневого сопротивления врагов
    ICE_PEN,             // снижение ледяного сопротивления
    LIGHTNING_PEN,       // снижение сопротивления молнии
    MAGIC_PEN,           // снижение магического сопротивления
    PHYSICAL_PEN,        // снижение физического сопротивления

    // Tier 3 — situational, requires Tier 2
    BONUS_VS_BOSSES,     // доп. урон по боссам и мини-боссам
    BONUS_VS_SLOWED,     // доп. урон по замедлённым целям
    BONUS_VS_BLOCKED,    // доп. урон по заблокированным целям

    // Tier 4 — endgame upgrades
    DAMAGE_BOOST_2,      // ещё +урон (требует DAMAGE_BOOST + BONUS_VS_BOSSES)
    GOLD_BONUS_2,        // ещё +золото (требует GOLD_BONUS + BONUS_VS_BOSSES)
    ALL_PEN,             // комбо-снижение всех сопротивлений (требует 3 пробивания)
}
