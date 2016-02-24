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
package jbyoshi.robotgame.graphics;

import jbyoshi.robotgame.action.BoundAction;
import jbyoshi.robotgame.impl.PlayerImpl;
import jbyoshi.robotgame.model.GameModel;
import jbyoshi.robotgame.script.Script;
import jbyoshi.robotgame.script.ScriptThread;

import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

public final class DrawScriptThread extends ScriptThread {
    private GameDraw draw;
    private final Consumer<? super GameComponent> drawFuture;

    public DrawScriptThread(PlayerImpl player, Script script, Consumer<? super JComponent> drawFuture) {
        super(player, script);
        this.drawFuture = drawFuture;
    }

    @Override
    protected void finishSetup(GameModel game) {
        draw = new GameDraw(game);
        drawFuture.accept(draw.component);
    }

    @Override
    public void tickEnded(List<BoundAction> allActions) {
        super.tickEnded(allActions);
        draw.tick(allActions);
    }
}
