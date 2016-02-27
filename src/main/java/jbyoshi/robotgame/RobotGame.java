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
package jbyoshi.robotgame;

import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.*;

import jbyoshi.robotgame.api.Game;
import jbyoshi.robotgame.api.Point;
import jbyoshi.robotgame.graphics.*;
import jbyoshi.robotgame.impl.PlayerImpl;
import jbyoshi.robotgame.model.*;
import jbyoshi.robotgame.script.*;
import jbyoshi.robotgame.server.ServerThread;
import jbyoshi.robotgame.util.updater.Update;
import jbyoshi.robotgame.util.updater.Updater;

public final class RobotGame {
	public static void main(String[] args) {
		relaunch(new JFrame());
	}

	private static final File scriptFile = new File("RobotGameScript.java");
	private static DelegatingScript script;

	@SuppressWarnings("unused")
	public static void main(JFrame frame) {
		frame.setTitle("Robot Game");
		frame.getContentPane().add(new JLabel("Starting up..."));
		frame.setSize(900, 700);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

		Updater updater =  Updater.getUpdater("https://api.github.com/repos/JBYoshi/RobotGame");

		PlayerImpl red = new PlayerImpl("Red", new Color(235, 0, 0)); // Should not be exactly Color.RED as that's the damage flash color.
		PlayerImpl orange = new PlayerImpl("Orange", new Color(255, 127, 0));
		PlayerImpl yellow = new PlayerImpl("Yellow", Color.YELLOW);
		PlayerImpl green = new PlayerImpl("Green", new Color(0, 140, 0));
		PlayerImpl lightBlue = new PlayerImpl("Light Blue", Color.CYAN);
		PlayerImpl darkBlue = new PlayerImpl("Blue", Color.BLUE);
		PlayerImpl purple = new PlayerImpl("Purple", new Color(150, 0, 255));
		PlayerImpl pink = new PlayerImpl("Pink", Color.MAGENTA);
		PlayerImpl brown = new PlayerImpl("Brown", new Color(127, 64, 0));
		PlayerImpl[] allPlayers = new PlayerImpl[] {red, orange, yellow, green, lightBlue, darkBlue, purple, pink, brown};

		Thread gameThread = new Thread(() -> {
			boolean first = true;
			while (!Thread.currentThread().isInterrupted()) {
				try {
					final Optional<Update> update = updater.checkForUpdates();
					if (update.isPresent()) {
						installUpdate(frame, update.get(), Thread.currentThread());
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				int numPlayersPerGame = 4;
				List<PlayerImpl> currentPlayers = new ArrayList<>(numPlayersPerGame);
				for (int playerNum = 0; playerNum < numPlayersPerGame; playerNum++) {
					int random = (int) (Math.random() * (allPlayers.length - playerNum));
					int i = 0;
					do {
						while (currentPlayers.contains(allPlayers[i])) i++;
						random--;
					} while (random >= 0);
					assert !currentPlayers.contains(allPlayers[i]);
					currentPlayers.add(allPlayers[i]);
				}

				GameModel serverModel = new GameModel();
				serverModel.add(new SpawnerModel(currentPlayers.get(0), new Point(Game.WORLD_SIZE / 4, Game.WORLD_SIZE / 4)));
				serverModel.add(new SpawnerModel(currentPlayers.get(1), new Point(Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE / 4)));
				serverModel.add(new SpawnerModel(currentPlayers.get(2), new Point(Game.WORLD_SIZE / 4, Game.WORLD_SIZE * 3 / 4)));
				serverModel.add(new SpawnerModel(currentPlayers.get(3), new Point(Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE * 3 / 4)));

				reloadScript(frame);

				final List<ScriptThread> scriptThreads = currentPlayers.stream().map(new Function<PlayerImpl, ScriptThread>() {
					private boolean firstScript = true;

					@Override
					public ScriptThread apply(PlayerImpl player) {
						if (firstScript) {
							firstScript = false;
							return new DrawScriptThread(player, script, draw -> setupGuiIngame(draw, frame));
						}
						return new ScriptThread(player, script);
					}
				}).collect(Collectors.toList());
				new ServerThread(scriptThreads).run();

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					return;
				}
			}
		} , "Game Loop");
		gameThread.start();
	}

	private static void setupGuiIngame(JComponent draw, JFrame frame) {
		frame.getContentPane().removeAll();
		frame.getContentPane().add(draw);
		JPanel buttonPanel = new JPanel(new GridLayout(1, 0));

		JButton edit = new JButton("Edit Script");
		edit.addActionListener(event -> {
            try {
                openEditor();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
		buttonPanel.add(edit);

		JButton update = new JButton("Reload Script");
		update.addActionListener(event -> reloadScript(frame));
		buttonPanel.add(update);

		frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		frame.revalidate();
	}

	private static void reloadScript(JFrame frame) {
		try {
			if (!scriptFile.exists()) {
				try (FileWriter writer = new FileWriter(scriptFile)) {
					writer.write("public class RobotGameScript {\n\tpublic static void tick(Game game) {\n\t\t// Your code goes here\n\t}\n}".replace("\n", System.lineSeparator()));
					writer.flush();
				}
				if (JOptionPane.showConfirmDialog(frame, new String[]{
						"To use this game, you will need to write some code for it.",
						"This code should be put in the file in your current working directory named RobotGameScript.java.",
						"Be aware that it may not support some newer Java language features, such as lambdas.",
						"Would you like to open the script file for editing?"
				}, "Script Not Found", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
					openEditor();
				}
			}
			if (script == null) {
				script = new DelegatingScript(ScriptLoader.loadScript(scriptFile));
			} else {
				script.setScript(ScriptLoader.loadScript(scriptFile));
			}
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@SuppressWarnings("unused")
	private static void installUpdate(JFrame frame, Update update, Thread gameThread) {
		new Thread(() -> {
			gameThread.interrupt();
			try {
				gameThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Load update installation classes ahead of time.
			new ResourceClassLoader();
			new BootstrapClassLoader();

			frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			try {
				update.install(frame);
				// From this point on, no new code can be loaded!
			} catch (Throwable e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, new String[] {
						"Something went wrong during the update. Your game is",
						"probably corrupted. You can redownload it at:",
						"https://github.com/JBYoshi/RobotGame/releases/latest"}, "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			relaunch(frame);
		}, "Updater").start();
	}

	private static void relaunch(JFrame frame) {
		try {
			ClassLoader loader = new ResourceClassLoader(new BootstrapClassLoader());
			loader.loadClass(RobotGame.class.getName())
					.getMethod("main", JFrame.class).invoke(null, frame);
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) e = e.getCause();
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			writer.write("Something went wrong, and the game crashed.\n");
			writer.write("Please post the following text at https://github.com/JBYoshi/RobotGame/issues:\n");
			e.printStackTrace(new PrintWriter(writer));
			JOptionPane.showMessageDialog(frame, writer.toString().split("\n"), "Crashed!",
					JOptionPane.ERROR_MESSAGE);
        }
	}

	private static void openEditor() throws IOException {
		if (Desktop.isDesktopSupported()) {
			if (Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
				Desktop.getDesktop().edit(scriptFile);
				return;
			}
			if (Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
				Desktop.getDesktop().open(scriptFile);
				return;
			}
		}
		throw new IOException("Your system doesn't seem to support opening files.");
	}
}
