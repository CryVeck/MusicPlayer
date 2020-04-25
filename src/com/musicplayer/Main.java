package com.musicplayer;

import java.util.ArrayList;

import com.musicplayer.gui.GUI;
import com.musicplayer.gui.SwingGUI;
import com.musicplayer.player.Player;
import com.musicplayer.player.VLCJListPlayer;
import com.musicplayer.scanner.FolderScanner;
import com.musicplayer.scanner.Scanner;

/**
 * Actual "Player" glue: creates all the necessary objects and binds them
 * together
 * 
 * @author Louis Hermier
 *
 */
public class Main {
	private static Player player;
	private static final String DEFAULT_ART = "default.png";
	private static Scanner scanner;
	private static ArrayList<String> songs;
	private static ArrayList<String> covers;
	private static ArrayList<String> images;
	private static String albumArt;
	private static GUI gui;

	public static void main(String[] args) throws InterruptedException {
		if (args.length < 1) {
			System.err.println("This program requires a path argument to play!");
			System.exit(-1);
		} else if (args.length > 1) {
			System.out.println("Warning: Only the first argument is used.");
		}
		// get the scanner, gui and player.
		// works with interfaces
		// Dropping in a new Scanner, gui or player should only induce changes here
		scanner = new FolderScanner(args[0]);
		gui = new SwingGUI(scanner.getAlbumName());
		player = new VLCJListPlayer();

		// get the songs and images from the scanner
		songs = scanner.getAllMusicFiles();
		covers = scanner.getCoverArt();
		images = scanner.getAllImageFiles();

		// check if a cover was found, otherwise get some alternatives
		if (covers.size() > 0) {
			System.out.println("Cover art found, using " + covers.get(0));
			albumArt = covers.get(0);
		} else {
			System.out.println("No Cover art found.");
			if (images.size() > 0) {
				System.out.println("Found images, using " + images.get(0));
				albumArt = images.get(0);
			} else {
				System.out.println("No other images found. Defaulting to " + DEFAULT_ART);
				albumArt = DEFAULT_ART;
			}
		}
		// pass the name of the chosen image file to be the art to the gui
		gui.setAlbumArt(albumArt);
		// bind the GUI buttons to the player-exposed functions
		// uses lambdas.
		gui.setNextAction(() -> player.next());
		gui.setPauseAction(() -> {
			if (player.isPaused()) { // if the player is paused
				player.play(); // then play
			} else {
				player.pause(); // else, pause playback
			}
		});
		gui.setPreviousAction(() -> player.previous());
		gui.setVUpAction(() -> player.volumeUp());
		gui.setVDownAction(() -> player.volumeDown());
		gui.setTrackLabel(scanner.getAlbumName());
		player.setUpdateMediaAction(() -> {
			String withArtist = player.nowPlayingArtist() + " - " + player.nowPlayingTitle(); // test label to check for
																								// width
			if (!gui.isStringTooWide(withArtist)) { // if it is narrow enough to fit on the screen
				gui.setTrackLabel(withArtist); // display title and artist (artist - title)
			} else {
				gui.setTrackLabel(player.nowPlayingTitle()); // else, only display the title
			}

			gui.setWindowName(player.nowPlayingAlbum()); // since we extracted metadata from the song, get album name
															// from here
			// allows / , trailing ., ....
		});
		// add all the songs we found to the playback
		player.addMultiple(songs);
		// show the gui
		gui.showWindow();
		// finally; start playing the music
		player.play();
		Thread.sleep(1000);
	}
}
