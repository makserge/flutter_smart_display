/***************************************************************************
    begin........: September 2018
    copyright....: Sebastian Fedrau
    email........: sebastian.fedrau@gmail.com
 ***************************************************************************/

/***************************************************************************
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License v3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    General Public License v3 for more details.
 ***************************************************************************/
package de.dixieflatline.mpcw.client.mpd;

import android.os.Build;
import android.util.Pair;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import de.dixieflatline.mpcw.client.Song;

public class FindScanner
{
	private boolean firstItemFound;
	private String filename;
	private String artist;
	private String album;
	private int track;
	private String title;
	private final List<IFindScannerListener> listeners;
	
	public FindScanner()
	{
		listeners = new ArrayList<>();
	}

	public void reset()
	{
		firstItemFound = false;
		filename = null;
		artist = null;
		album = null;
		track = 0;
		title = null;
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public void feed(String line) throws InvalidFormatException
	{
		Pair<String, String> pair = ResponseParser.splitLineIntoPair(line);

		switch (pair.first) {
			case "file":
				if (firstItemFound) {
					raiseOnSongFound();
				}

				firstItemFound = true;
				filename = pair.second;
				artist = null;
				album = null;
				track = 0;
				title = null;
				break;
			case "Artist":
				artist = pair.second;
				break;
			case "Album":
				album = pair.second;
				break;
			case "Track":
				try {
					track = Integer.parseInt(pair.second);
				} catch (Exception ex) {
					track = 0;
				}
				break;
			case "Title":
				title = pair.second;
				break;
		}
	}
	
	@RequiresApi(api = Build.VERSION_CODES.N)
	public void flush()
	{
		raiseOnSongFound();
	}
	
	public void addListener(IFindScannerListener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(IFindScannerListener listener)
	{
		listeners.remove(listener);
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	private void raiseOnSongFound()
	{
		if(filename != null)
		{
			Song song = new Song(filename, artist, album, track, title);
			
			listeners.forEach((l) -> l.onSongFound(song));
		}
	}
}