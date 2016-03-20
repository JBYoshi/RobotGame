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
package jbyoshi.robotgame.script;

import jbyoshi.robotgame.action.BoundAction;
import jbyoshi.robotgame.impl.GameView;
import jbyoshi.robotgame.impl.PlayerImpl;
import jbyoshi.robotgame.model.GameModel;
import jbyoshi.robotgame.server.PlayerConnection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class ScriptThread extends Thread implements PlayerConnection {
    private GameModel model;
    private final PlayerImpl player;
    private final Script script;
    private final LinkedBlockingQueue<CompletableFuture<Set<BoundAction>>> queue = new LinkedBlockingQueue<>();

    public ScriptThread(PlayerImpl player, Script script) {
        setName("Script thread - " + player);
        this.player = player;
        this.script = script;
    }

    @Override
    @Deprecated
    public final synchronized void start() {
        throw new UnsupportedOperationException("Use gameStarted() instead!");
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void run() {
        if (model == null) {
            throw new IllegalStateException("Don't call run() directly!");
        }
        while (!isInterrupted()) {
            CompletableFuture<Set<BoundAction>> onEnded = null;
            try {
                onEnded = queue.take();
                final Set<BoundAction> actions = tick();
                onEnded.complete(actions);
            } catch (InterruptedException e) {
                return;
            } catch (Throwable t) {
                if (onEnded != null) {
                    onEnded.completeExceptionally(t);
                }
            }
        }
    }

    private Set<BoundAction> tick() {
        model.preTick();
        try {
            GameView view = new GameView(model, player);
            script.tick(view);
            return view.popActions();
        } catch (Throwable e) {
            System.err.print("Error in script for " + player.getName() + ": ");
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    public CompletableFuture<Set<BoundAction>> startTick() {
        CompletableFuture<Set<BoundAction>> future = new CompletableFuture<>();
        synchronized (queue) {
            // Remove any older, still-pending futures.
            // If a take() call comes here, it gets the old one, and poll() here just returns null.
            while (queue.size() > 1) {
                queue.poll();
            }
            // And add the new one.
            // If a take() call comes here, it will wait until the add() has finished.
            queue.add(future);
        }
        return future;
    }

    @Override
    public void tickEnded(List<BoundAction> allActions) {
        model.postTick(allActions);
    }

    @Override
    public void gameEnded() {
        interrupt();
    }

    @Override
    public PlayerImpl getPlayerObject() {
        return this.player;
    }

    @Override
    public final synchronized void gameStarted(GameModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Already started!");
        }
        if (model == null) {
            throw new NullPointerException("model == null");
        }
        this.model = model;
        finishSetup(model);
        super.start();
    }

    protected void finishSetup(GameModel game) {
    }

    protected final GameModel getGame() {
        return model;
    }
}
