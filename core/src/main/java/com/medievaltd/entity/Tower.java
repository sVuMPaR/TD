package com.medievaltd.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.medievaltd.model.DamageType;
import com.medievaltd.model.TempleUpgradeChoice;
import com.medievaltd.model.TowerType;
import com.medievaltd.research.ResearchState;
import com.medievaltd.system.Combat;
import com.medievaltd.util.Assets;
import com.medievaltd.util.FloatingText;
import com.medievaltd.util.Sfx;

import java.util.ArrayList;
import java.util.List;

public class Tower {
    private final TowerType type;
    private final Vector2 position;
    private int level = 1;
    private float fireCooldown;
    private TempleUpgradeChoice templeChoice;
    private final ResearchState research;

    private final List<Soldier> soldiers = new ArrayList<>();
    private static final int MAX_SOLDIERS = 3;
    private final Vector2 tmp = new Vector2();
    private final Vector2 tmp2 = new Vector2();
    private final Vector2 dest = new Vector2();
    private static final float PLAY_MIN_X = 28f, PLAY_MAX_X = 1252f;
    private static final float PLAY_MIN_Y = 88f, PLAY_MAX_Y = 680f;

    public Tower(TowerType type, float x, float y, ResearchState research) {
        this.research = research;
        this.type = type;
        this.position = new Vector2(x, y);
        if (type == TowerType.BARRACKS) {
            spawnInitialSoldiers();
        }
    }

    public void update(float delta, List<Enemy> enemies, List<Projectile> projectiles,
                       Vector2[] path, Assets assets, float damageBonus, float speedBonus,
                       List<FloatingText> floatingTexts, Sfx sfx) {
        if (type == TowerType.TEMPLE) return;
        if (type == TowerType.BARRACKS) {
            updateBarracks(delta, enemies, path, damageBonus, floatingTexts, sfx);
            return;
        }

        fireCooldown -= delta;
        if (fireCooldown > 0) return;

        Enemy target = findTarget(enemies, path);
        if (target == null) return;

        float effectiveFireRate = type.fireRateAtLevel(level) * (1f - speedBonus);
        fireCooldown = Math.max(0.2f, effectiveFireRate);

        float accuracy = Combat.BASE_HIT_CHANCE * (research != null ? research.getAccuracyMultiplier() : 1f);
        if (!Combat.rollHit(accuracy, target.getDodgeChance())) {
            floatingTexts.add(new FloatingText("Промах", position.x, position.y + 36));
            if (sfx != null) sfx.miss();
            return;
        }
        if (sfx != null) sfx.shoot(type);

        int damage = (int) (type.damageAtLevel(level) * (1f + damageBonus));

        Projectile proj = switch (type) {
            case ARCHER -> Projectile.arrow(new Vector2(position), target, damage, assets);
            case ARTILLERY -> Projectile.cannonball(new Vector2(position), target, damage, assets);
            case BALLISTA -> Projectile.bolt(new Vector2(position), target, damage, assets);
            case MAGIC -> Projectile.magic(new Vector2(position), target, damage, DamageType.MAGIC, assets);
            case FIRE_MAGIC -> Projectile.magic(new Vector2(position), target, damage, DamageType.FIRE, assets);
            case ICE_MAGIC -> Projectile.magic(new Vector2(position), target, damage, DamageType.ICE, assets, 0.4f);
            case LIGHTNING_MAGIC -> Projectile.magic(new Vector2(position), target, damage, DamageType.LIGHTNING, assets);
            case ICE_TOWER -> Projectile.magic(new Vector2(position), target, damage, DamageType.ICE, assets, 0.3f);
            default -> null;
        };
        if (proj != null) projectiles.add(proj);
    }

    private void spawnInitialSoldiers() {
        int count = soldierCountForLevel(level);
        for (int i = 0; i < count; i++) {
            soldiers.add(createSoldier(i, count));
        }
    }

    private Soldier createSoldier(int index, int total) {
        Soldier s = new Soldier();
        applySoldierStats(s);
        float spread = (index - (total - 1) / 2f) * 0.85f;
        s.homeOffset.set(MathUtils.cos(spread) * 22f, MathUtils.sin(spread) * 22f);
        s.position.set(position).add(s.homeOffset);
        clampToPlayable(s.position);
        s.homeOffset.set(s.position).sub(position);
        return s;
    }

    private void applySoldierStats(Soldier s) {
        s.maxHp = 40 + level * 30;
        s.hp = s.maxHp;
        s.damage = 8 + level * 5;
        s.alive = true;
        s.respawnTimer = 0;
    }

    private int soldierCountForLevel(int lvl) {
        return lvl >= 3 ? MAX_SOLDIERS : 2;
    }

    private void updateBarracks(float delta, List<Enemy> enemies, Vector2[] path,
                                float damageBonus, List<FloatingText> floatingTexts, Sfx sfx) {
        float leash = getRange();
        int dmg = Math.round((8 + level * 5) * (1f + damageBonus));

        for (Soldier soldier : soldiers) {
            if (!soldier.alive) {
                soldier.respawnTimer -= delta;
                if (soldier.respawnTimer <= 0) {
                    applySoldierStats(soldier);
                    soldier.position.set(position).add(soldier.homeOffset);
                    clampToLeash(soldier.position, leash);
                    soldier.attackCooldown = 0.3f;
                }
                continue;
            }

            Enemy target = findSoldierTarget(soldier, enemies, path, leash);
            dest.set(position).add(soldier.homeOffset);
            clampToPlayable(dest);
            clampToLeash(dest, leash);
            if (target != null) {
                target.getPosition(path, dest);
                clampToLeash(dest, leash);
            }

            tmp2.set(dest).sub(soldier.position);
            float dist = tmp2.len();
            float move = 80f * delta;
            if (dist > 2f) {
                soldier.position.mulAdd(tmp2.scl(1f / dist), Math.min(move, dist));
            }
            clampToLeash(soldier.position, leash);

            if (target != null) {
                Vector2 ePos = target.getPosition(path, tmp);
                float melee = soldier.position.dst(ePos);
                if (melee <= 24f) {
                    target.block(0.35f);
                    soldier.attackCooldown -= delta;
                    soldier.hurtCooldown -= delta;
                    if (soldier.attackCooldown <= 0) {
                        soldier.attackCooldown = 0.85f;
                        float accuracy = Combat.BASE_HIT_CHANCE * (research != null ? research.getAccuracyMultiplier() : 1f);
                        if (Combat.rollHit(accuracy, target.getDodgeChance())) {
                            target.takeDamage(dmg, DamageType.PHYSICAL);
                            if (sfx != null) sfx.hit();
                        } else {
                            floatingTexts.add(new FloatingText("Промах", soldier.position.x, soldier.position.y + 22));
                            if (sfx != null) sfx.miss();
                        }
                    }
                    if (soldier.hurtCooldown <= 0) {
                        soldier.hurtCooldown = 1.15f;
                        int enemyHit = Math.max(5, target.getType().damageToBase / 2);
                        soldier.hp -= enemyHit;
                        if (soldier.hp <= 0) {
                            soldier.hp = 0;
                            soldier.alive = false;
                            soldier.respawnTimer = 4f;
                        }
                    }
                }
            } else {
                soldier.attackCooldown = Math.max(0, soldier.attackCooldown - delta);
            }
        }
    }

    private Enemy findSoldierTarget(Soldier soldier, List<Enemy> enemies, Vector2[] path, float leash) {
        Enemy best = null;
        float bestProgress = -1;
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive() || enemy.hasReachedEnd()) continue;
            Vector2 ePos = enemy.getPosition(path, tmp);
            if (position.dst(ePos) > leash) continue;
            if (enemy.getPathProgress() > bestProgress) {
                bestProgress = enemy.getPathProgress();
                best = enemy;
            }
        }
        return best;
    }

    private Enemy findTarget(List<Enemy> enemies, Vector2[] path) {
        Enemy best = null;
        float bestProgress = -1;
        float range = getRange();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            Vector2 enemyPos = enemy.getPosition(path, tmp);
            if (position.dst(enemyPos) <= range && enemy.getPathProgress() > bestProgress) {
                bestProgress = enemy.getPathProgress();
                best = enemy;
            }
        }
        return best;
    }

    private void clampToPlayable(Vector2 p) {
        p.x = MathUtils.clamp(p.x, PLAY_MIN_X, PLAY_MAX_X);
        p.y = MathUtils.clamp(p.y, PLAY_MIN_Y, PLAY_MAX_Y);
    }

    private void clampToLeash(Vector2 p, float leash) {
        tmp.set(p).sub(position);
        float len = tmp.len();
        if (len > leash && len > 0.001f) {
            tmp.scl(leash / len);
            p.set(position).add(tmp);
        }
        clampToPlayable(p);
    }

    public int currentDamage() {
        return type.damageAtLevel(level);
    }

    public float currentFireRate() {
        return type.fireRateAtLevel(level);
    }

    public int soldierMaxHp() {
        return 40 + level * 30;
    }

    public int soldierDamage() {
        return 8 + level * 5;
    }

    public int aliveSoldierCount() {
        int n = 0;
        for (Soldier s : soldiers) if (s.alive) n++;
        return n;
    }

    public int nextUpgradeCost() {
        if (level >= 3) return -1;
        return type.upgradeCost(level);
    }

    public boolean upgrade() {
        if (level >= 3) return false;
        level++;
        if (type == TowerType.BARRACKS) {
            for (Soldier s : soldiers) {
                boolean wasAlive = s.alive;
                float respawn = s.respawnTimer;
                applySoldierStats(s);
                if (!wasAlive) {
                    s.alive = false;
                    s.respawnTimer = respawn;
                }
            }
            if (soldiers.size() < soldierCountForLevel(level)) {
                soldiers.add(createSoldier(soldiers.size(), soldierCountForLevel(level)));
            }
        }
        return true;
    }

    public void setTempleChoice(TempleUpgradeChoice choice) {
        this.templeChoice = choice;
    }

    public TempleUpgradeChoice getTempleChoice() {
        return templeChoice;
    }

    public float getTempleDamageBonus() {
        if (type != TowerType.TEMPLE) return 0;
        if (templeChoice == TempleUpgradeChoice.ATTACK_DAMAGE) {
            return 0.2f * level;
        }
        return 0;
    }

    public float getTempleSpeedBonus() {
        if (type != TowerType.TEMPLE) return 0;
        if (templeChoice == TempleUpgradeChoice.ATTACK_SPEED) {
            return 0.15f * level;
        }
        return 0;
    }

    public void render(SpriteBatch batch, Assets assets) {
        TextureRegion tex = assets.getTowerTexture(type);
        float size = tex.getRegionWidth();
        batch.draw(tex, position.x - size / 2f, position.y - size / 2f, size, size);

        if (level > 1) {
            batch.setColor(1f, 0.85f, 0.2f, 0.8f);
            for (int i = 0; i < level - 1; i++) {
                batch.draw(assets.white, position.x - 8 + i * 8, position.y + size / 2 + 2, 6, 6);
            }
            batch.setColor(1, 1, 1, 1);
        }

        if (type == TowerType.BARRACKS) {
            renderSoldiers(batch, assets);
        }
    }

    private void renderSoldiers(SpriteBatch batch, Assets assets) {
        for (Soldier soldier : soldiers) {
            if (!soldier.alive) continue;
            float size = 22f + level * 2f;
            batch.draw(assets.soldierIcon, soldier.position.x - size / 2f, soldier.position.y - size / 2f, size, size);
            float barW = 22f;
            float pct = soldier.hp / (float) soldier.maxHp;
            batch.setColor(0.15f, 0.15f, 0.15f, 0.85f);
            batch.draw(assets.white, soldier.position.x - barW / 2f, soldier.position.y + size / 2f + 2, barW, 3);
            batch.setColor(0.25f, 0.75f, 0.25f, 1f);
            batch.draw(assets.white, soldier.position.x - barW / 2f, soldier.position.y + size / 2f + 2, barW * pct, 3);
            batch.setColor(1, 1, 1, 1);
        }
    }

    public TowerType getType() { return type; }
    public Vector2 getPosition() { return position; }
    public int getLevel() { return level; }
    public float getRange() {
        return type.rangeAtLevel(level) * (research != null ? research.getRangeMultiplier() : 1f);
    }
    public List<Soldier> getSoldiers() { return soldiers; }

    public static class Soldier {
        public boolean alive = true;
        public int hp = 70;
        public int maxHp = 70;
        public int damage = 13;
        public float respawnTimer = 0;
        public float attackCooldown = 0;
        public float hurtCooldown = 0;
        public final Vector2 position = new Vector2();
        public final Vector2 homeOffset = new Vector2();
    }
}
