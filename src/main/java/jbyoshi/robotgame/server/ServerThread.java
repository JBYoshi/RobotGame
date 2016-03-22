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
package jbyoshi.robotgame.server;

import com.google.common.collect.ImmutableList;
import jbyoshi.robotgame.action.BoundAction;
import jbyoshi.robotgame.api.Game;
import jbyoshi.robotgame.api.Point;
import jbyoshi.robotgame.model.GameModel;
import jbyoshi.robotgame.model.SpawnerModel;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class ServerThread implements Runnable {
    private final List<? extends PlayerConnection> scripts;
    private final GameModel game;

    public ServerThread(List<? extends PlayerConnection> scripts) {
        this.scripts = scripts;
        
        this.game = new GameModel();
        game.add(new SpawnerModel(scripts.get(0).getPlayerObject(), new Point(Game.WORLD_SIZE / 4, Game.WORLD_SIZE / 4)));
        game.add(new SpawnerModel(scripts.get(1).getPlayerObject(), new Point(Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE / 4)));
        game.add(new SpawnerModel(scripts.get(2).getPlayerObject(), new Point(Game.WORLD_SIZE / 4, Game.WORLD_SIZE * 3 / 4)));
        game.add(new SpawnerModel(scripts.get(3).getPlayerObject(), new Point(Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE * 3 / 4)));
    }

    @Override
    public void run() {
        for (PlayerConnection conn : this.scripts) {
            conn.gameStarted(new GameModel(game));
        }
        while (game.isRunning()) {
            if (Thread.currentThread().isInterrupted()) {
                handleInterrupted();
                return;
            }

            long start = System.currentTimeMillis();
            game.preTick();
            scripts.forEach(PlayerConnection::startTick);
            final List<BoundAction> actions = new Vector<>();
            final List<PlayerConnection> pendingScripts = new Vector<>(scripts);
            CountDownLatch latch = new CountDownLatch(scripts.size());
            for (PlayerConnection conn : scripts) {
                try {
                    conn.startTick().whenComplete((result, exception) -> {
                        synchronized (actions) {
                            pendingScripts.remove(conn);
                            if (result != null) {
                                actions.addAll(result);
                            }
                            latch.countDown();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            try {
                latch.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                handleInterrupted();
                return;
            }
            List<BoundAction> safeActions;
            synchronized (actions) {
                safeActions = ImmutableList.copyOf(actions);
                for (PlayerConnection conn : pendingScripts) {
                    System.out.println("WARN: Script for " + conn.getPlayerObject().getName() + " is taking longer than usual");
                }
            }
            game.postTick(safeActions);
            scripts.forEach(data -> data.tickEnded(safeActions));

            long end = System.currentTimeMillis();
            if (end - start < 1000) {
                try {
                    Thread.sleep(1000 - end + start);
                } catch (InterruptedException e) {
                    handleInterrupted();
                    return;
                }
            }
        }
        scripts.forEach(PlayerConnection::gameEnded);
    }

    private void handleInterrupted() {
        // TODO will need different handling in multiplayer
        scripts.forEach(PlayerConnection::gameEnded);
        Thread.currentThread().interrupt();
    }
}
