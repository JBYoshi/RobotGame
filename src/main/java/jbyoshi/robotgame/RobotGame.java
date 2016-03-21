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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import jbyoshi.robotgame.api.Game;
import jbyoshi.robotgame.api.Point;
import jbyoshi.robotgame.graphics.*;
import jbyoshi.robotgame.gui.ScriptSelectionComponent;
import jbyoshi.robotgame.gui.ScriptStorage;
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

	private static ScriptStorage selectedScript;
	private static final DelegatingScript script = new DelegatingScript();

	private static final PlayerImpl red = new PlayerImpl("Red", new Color(235, 0, 0)); // Should not be exactly Color.RED as that's the damage flash color.
	private static final PlayerImpl orange = new PlayerImpl("Orange", new Color(255, 127, 0));
	private static final PlayerImpl yellow = new PlayerImpl("Yellow", Color.YELLOW);
	private static final PlayerImpl green = new PlayerImpl("Green", new Color(0, 140, 0));
	private static final PlayerImpl lightBlue = new PlayerImpl("Light Blue", Color.CYAN);
	private static final PlayerImpl darkBlue = new PlayerImpl("Blue", Color.BLUE);
	private static final PlayerImpl purple = new PlayerImpl("Purple", new Color(150, 0, 255));
	private static final PlayerImpl pink = new PlayerImpl("Pink", Color.MAGENTA);
	private static final PlayerImpl brown = new PlayerImpl("Brown", new Color(127, 64, 0));
	private static final PlayerImpl[] allPlayers = new PlayerImpl[] {red, orange, yellow, green, lightBlue, darkBlue,
			purple, pink, brown};

	private static final Updater updater = Updater.getUpdater("https://api.github.com/repos/JBYoshi/RobotGame");

	@SuppressWarnings("unused")
	public static void main(JFrame frame) {
		try {
			MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme() {
				private final FontUIResource plain10 = new FontUIResource(Font.MONOSPACED, Font.PLAIN, 10);
				private final FontUIResource plain12 = new FontUIResource(Font.MONOSPACED, Font.PLAIN, 12);
				private final FontUIResource bold12 = new FontUIResource(Font.MONOSPACED, Font.BOLD, 12);

				@Override
				public String getName() {
					return "Robot Game";
				}

				@Override
				public FontUIResource getControlTextFont() {
					return super.getControlTextFont().getStyle() == Font.BOLD ? bold12 : plain12;
				}

				@Override
				public FontUIResource getSystemTextFont() {
					return plain12;
				}

				@Override
				public FontUIResource getUserTextFont() {
					return plain12;
				}

				@Override
				public FontUIResource getMenuTextFont() {
					return super.getMenuTextFont().getStyle() == Font.BOLD ? bold12 : plain12;
				}

				@Override
				public FontUIResource getWindowTitleFont() {
					return bold12;
				}

				@Override
				public FontUIResource getSubTextFont() {
					return plain10;
				}
			});
			UIManager.setLookAndFeel(new MetalLookAndFeel());
			SwingUtilities.updateComponentTreeUI(frame);
		} catch (UnsupportedLookAndFeelException e) {
			throw new AssertionError(e);
		}

		frame.setTitle("Robot Game");
		frame.getContentPane().add(new JLabel("Starting up..."));
		frame.setSize(900, 700);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

		try {
			final Optional<Update> update = updater.checkForUpdates();
			if (update.isPresent()) {
				installUpdate(frame, update.get(), Thread.currentThread());
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		selectScript(frame);

		new Thread(() -> gameLoop(frame, updater), "Game Loop").start();
	}

	private static void gameLoop(final JFrame frame, Updater updater) {
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
                int i = -1;
                do {
					i++;
                    while (currentPlayers.contains(allPlayers[i])) i++;
                    random--;
                } while (random >= 0);
                currentPlayers.add(allPlayers[i]);
            }

            GameModel serverModel = new GameModel();
            serverModel.add(new SpawnerModel(currentPlayers.get(0), new Point(Game.WORLD_SIZE / 4, Game.WORLD_SIZE / 4)));
            serverModel.add(new SpawnerModel(currentPlayers.get(1), new Point(Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE / 4)));
            serverModel.add(new SpawnerModel(currentPlayers.get(2), new Point(Game.WORLD_SIZE / 4, Game.WORLD_SIZE * 3 / 4)));
            serverModel.add(new SpawnerModel(currentPlayers.get(3), new Point(Game.WORLD_SIZE * 3 / 4, Game.WORLD_SIZE * 3 / 4)));

            reloadScript();

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
		update.addActionListener(event -> reloadScript());
		buttonPanel.add(update);

		frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		frame.revalidate();
	}

	private static void selectScript(JFrame frame) {
		JDialog dialog = new JDialog(frame, "Select Script", true);
		dialog.getContentPane().add(new ScriptSelectionComponent(scriptStorage -> {
			selectedScript = scriptStorage;
			reloadScript();
			dialog.setVisible(false);
		}));
		dialog.pack();
		if (selectedScript == null) {
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
		}
		dialog.setLocation(frame.getX() + frame.getWidth() / 2 - dialog.getWidth() / 2,
				           frame.getY() + frame.getHeight() / 2 - dialog.getHeight() / 2);
		dialog.setVisible(true);
	}

	private static void reloadScript() {
		try {
			script.setScript(ScriptLoader.loadScript(selectedScript));
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
			System.exit(1);
        }
	}

	private static void openEditor() throws IOException {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
			Desktop.getDesktop().open(selectedScript.getRootDir());
		} else {
			throw new IOException("Your system doesn't seem to support opening files.");
		}
	}
}
