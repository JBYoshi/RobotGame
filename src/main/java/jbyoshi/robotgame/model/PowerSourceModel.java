/*
 * Copyright (C) 2016 JBYoshi.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package jbyoshi.robotgame.model;

import jbyoshi.robotgame.api.Point;

public final class PowerSourceModel extends AttackableModel {
    private static final int MAX_HEALTH = 100;
    private static final int POWER_PER_HIT = 3;

    public PowerSourceModel(Point loc) {
        super(loc, MAX_HEALTH);
    }

    @Override
    public void wasAttacked(RobotModel attacker, int damage) {
        // Ignore given damage to preserve game balance.
        damage = POWER_PER_HIT;
        if (damage > health) damage = health;
        int newPower = attacker.power + damage;
        if (newPower > RobotModel.MAX_POWER) {
            int over = newPower - RobotModel.MAX_POWER;
            damage -= over;
            if (damage == 0) return;
            newPower = RobotModel.MAX_POWER;
        }
        super.wasAttacked(attacker, damage);
        attacker.power = newPower;
    }

    @Override
    void die() {
    }

    @Override
    void onTickStart() {
        if (health < maxHealth) health++;
    }
}
