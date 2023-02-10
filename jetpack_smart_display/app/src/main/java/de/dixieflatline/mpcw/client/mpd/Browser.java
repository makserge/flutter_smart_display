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

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.dixieflatline.mpcw.client.CommunicationException;
import de.dixieflatline.mpcw.client.ETag;
import de.dixieflatline.mpcw.client.Filter;
import de.dixieflatline.mpcw.client.IBrowser;
import de.dixieflatline.mpcw.client.ISearchResult;
import de.dixieflatline.mpcw.client.IteratorSearchResult;
import de.dixieflatline.mpcw.client.ProtocolException;
import de.dixieflatline.mpcw.client.Song;
import de.dixieflatline.mpcw.client.Tag;
import de.dixieflatline.mpcw.client.TagIterator;

public class Browser implements IBrowser
{
	private final Channel channel;

	public Browser(Channel channel)
	{
		this.channel = channel;
	}

	@Override
	public ISearchResult<Tag> findTags(ETag tag) throws CommunicationException, ProtocolException
	{
		IResponse response = channel.send("list " + TagMapper.map(tag));

		return toSearchResult(tag, response);
	}

	@Override
	public ISearchResult<Tag> findTags(ETag tag, Filter[] filter) throws CommunicationException, ProtocolException
	{
		String command = String.format("list %s %s", TagMapper.map(tag), filterToString(filter));
		IResponse response = channel.send(command.trim());

		return toSearchResult(tag, filter, response);
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	@Override
	public ISearchResult<Song> findSongs(Filter[] filter) throws CommunicationException, ProtocolException
	{
		String command = "find " + filterToString(filter);
		IResponse response = channel.send(command.trim());

		return toSearchResult(filter, response);
	}

	private static String filterToString(Filter[] filter)
	{
		StringBuilder builder = new StringBuilder();

		for(Filter f : filter)
		{
			String value = f.getValue()
			                .replaceAll("\"", "\\\\\"");

			builder.append(TagMapper.map(f.getTag()));
			builder.append(" \"");
			builder.append(value);
			builder.append("\" ");
		}

		return builder.toString()
		              .trim();
	}

	private static IteratorSearchResult<Tag> toSearchResult(ETag tag, IResponse response) {
		return toSearchResult(tag, null, response);
	}

	private static IteratorSearchResult<Tag> toSearchResult(ETag tag, Filter[] filter, IResponse response) {
		Iterator<Tag> iterator = new TagIterator(tag, response);

		return filter == null ? new IteratorSearchResult<>(iterator)
		                      : new IteratorSearchResult<>(iterator, filter);
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	private static IteratorSearchResult<Song> toSearchResult(Filter[] filter, IResponse response) throws ProtocolException
	{
		FindScanner scanner = new FindScanner();
		List<Song> songs = new ArrayList<>();

		scanner.addListener(songs::add);

		for(String line : response)
		{
			if(!line.equals("OK"))
			{
				try
				{
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						scanner.feed(line);
					}
				}
				catch(InvalidFormatException ex)
				{
					throw new ProtocolException(ex);
				}
			}
		}

		scanner.flush();

		return new IteratorSearchResult<>(songs.iterator(), filter);
	}
}