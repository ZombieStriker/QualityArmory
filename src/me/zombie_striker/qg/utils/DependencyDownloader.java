/*
 *  Copyright (C) 2017 Zombie_Striker
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */

package me.zombie_striker.qg.utils;

import org.bukkit.configuration.file.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.*;
import org.json.simple.parser.*;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.*;

/**
 * Downloads dependencies for your plugins <br>
 * <br>
 * <b>You must have a option in your config to disable the downloader!</b>
 * 
 * @author Zombie_Striker
 * 
 *         --Based on the Updater by
 * @author ArsenArsen
 */
public class DependencyDownloader {

	private static final String HOST = "https://api.curseforge.com";
	private static final String QUERY = "/servermods/files?projectIds=";
	private static final String AGENT = "Mozilla/5.0 Dependency Downloader by Zombie_Striker";
	private static final File WORKING_DIR = new File("plugins" + File.separator
			+ "DependencyDownloader" + File.separator);
	private static final File CONFIG_FILE = new File(WORKING_DIR, "global.yml");
	private static final char[] HEX_CHAR_ARRAY = "0123456789abcdef"
			.toCharArray();

	private int id = -1;

	private Plugin p;

	private boolean debug = false;
	private String downloadURL = null;
	private String futuremd5;
	private String downloadName;
	private List<Channel> allowedChannels = Arrays.asList(Channel.ALPHA,
			Channel.BETA, Channel.RELEASE);
	private FileConfiguration global;

	public boolean downloaderActive = false;

	/**
	 * Downloads the dependency with the id
	 * 
	 * @param p
	 *            The plugin
	 * @param id
	 *            Plugin ID
	 */
	public DependencyDownloader(Plugin p, int id) {
		setID(id);

		this.p = p;
		if (!CONFIG_FILE.exists()) {
			try {
				CONFIG_FILE.getParentFile().mkdirs();
				CONFIG_FILE.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		global = YamlConfiguration.loadConfiguration(CONFIG_FILE);
		global.options().header(
				"DependencyDownloader by Zombie_Striker\nGlobal config");
		if (!global.isSet("download")) {
			global.set("download", true);
			try {
				global.save(CONFIG_FILE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		downloaderActive = global.getBoolean("download");
		downloadDependency();
	}

	/**
	 * Downloads the dependency with the id
	 * 
	 * @param p
	 *            The plugin
	 * @param id
	 *            Plugin ID
	 */
	public DependencyDownloader(Plugin p, ProjectID id) {
		this(p, id.getID());
	}

	/**
	 * Gets the plugin ID
	 * 
	 * @return the plugin ID
	 */
	public int getID() {
		return id;
	}

	/**
	 * Sets the plugin ID
	 * 
	 * @param id
	 *            The plugin ID
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * Attempts a update
	 */
	public void downloadDependency() {
		if (!downloaderActive) {
			debug("Disabled!");
			return;
		}
		new BukkitRunnable() {

			@Override
			public void run() {
				debug("Downloading STARTED!");
				download();
			}
		}.runTaskAsynchronously(p);
	}

	private void download() {
		String target = HOST + QUERY + id;
		debug(target);
		try {
			URL url = new URL(target);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.addRequestProperty("User-Agent", AGENT);
			connection.connect();
			debug("Connecting!");
			BufferedReader responseReader = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			StringBuilder responseBuffer = new StringBuilder();
			String line;
			while ((line = responseReader.readLine()) != null) {
				responseBuffer.append(line);
			}
			responseReader.close();
			String response = responseBuffer.toString();
			int counter = 1;
			if (connection.getResponseCode() == 200) {

				try {
					debug("RESCODE 200");
					while (true) {
						debug("Counter: " + counter);
						JSONParser parser = new JSONParser();
						JSONArray json = (JSONArray) parser.parse(response);
						if (json.size() - counter < 0) {
							debug("No version available!");
							break;
							// :Should never happen, but keep it here just
							// in case
						}
						JSONObject latest = (JSONObject) json.get(json.size()
								- counter);
						futuremd5 = (String) latest.get("md5");
						String channel = (String) latest.get("releaseType");
						// String name = (String) latest.get("name");
						if (allowedChannels.contains(Channel
								.matchChannel(channel.toUpperCase()))
						/* && !hasTag(name) */) {
							downloadURL = ((String) latest.get("downloadUrl"))
									.replace(" ", "%20");
							downloadName = (String) latest.get("fileName");
							break;
						} else
							counter++;
					}
				} catch (ParseException e) {
					p.getLogger().log(Level.SEVERE,
							"Could not parse API Response for " + target, e);
				}
			}
		} catch (IOException e) {
			p.getLogger().log(
					Level.SEVERE,
					"Could not check for the dependencies for the plugin "
							+ p.getName(), e);
		}

		try {
			File downloadTo = null;
			try {
				downloadTo = new File(p.getDataFolder().getParentFile()
						.getAbsolutePath(), downloadName);
			} catch (Exception e) {
				downloadTo = new File(p.getDataFolder().getParentFile()
						.getAbsolutePath(), "PluginConstructorAPI v1.0.0.jar");
			}
			downloadTo.getParentFile().mkdirs();
			downloadTo.delete();
			debug("Started download!");

			downloadIsSeperateBecauseGotoGotRemoved(downloadTo);

			debug("Ended download!");
			if (!fileHash(downloadTo).equalsIgnoreCase(futuremd5))
				return;
			if (downloadTo.getName().endsWith(".jar")) {
				return;
			} else
				unzip(downloadTo);

		} catch (IOException e) {
			p.getLogger().log(Level.SEVERE,
					"Couldn't download " + downloadName, e);
			return;
		}
	}

	/**
	 * God damn it Gosling, <a
	 * href="http://stackoverflow.com/a/4547764/3809164">reference here.</a>
	 */
	private void downloadIsSeperateBecauseGotoGotRemoved(File downloadTo)
			throws IOException {
		URL url=null;
		try{url= new URL(downloadURL);}catch(Exception e){
			url = new URL("https://dev.bukkit.org/projects/pluginconstructorapi/files/2473165/download");
		}
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.addRequestProperty("User-Agent", AGENT);
		connection.connect();
		if (connection.getResponseCode() >= 300
				&& connection.getResponseCode() < 400) {
			downloadURL = connection.getHeaderField("Location");
			downloadIsSeperateBecauseGotoGotRemoved(downloadTo);
		} else {
			debug(connection.getResponseCode() + " "
					+ connection.getResponseMessage() + " when requesting "
					+ downloadURL);
			copy(connection.getInputStream(), new FileOutputStream(downloadTo));
		}
	}

	private long copy(InputStream in, OutputStream out) throws IOException {
		long bytes = 0;
		byte[] buf = new byte[0x1000];
		while (true) {
			int r = in.read(buf);
			if (r == -1)
				break;
			out.write(buf, 0, r);
			bytes += r;
			debug("Another 4K, current: " + r);
		}
		out.flush();
		out.close();
		in.close();
		return bytes;
	}

	private void unzip(File download) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(download);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			ZipEntry entry;
			File downloadedFile = new File(p.getDataFolder().getParentFile()
					.getAbsoluteFile(), downloadName);
			while ((entry = entries.nextElement()) != null) {
				File target = new File(downloadedFile, entry.getName());
				File inPlugins = new File(p.getDataFolder().getParentFile(),
						downloadName);
				if (!inPlugins.exists()) {
					target = inPlugins;
				}
				if (!entry.isDirectory()) {
					target.getParentFile().mkdirs();
					InputStream zipStream = zipFile.getInputStream(entry);
					OutputStream fileStream = new FileOutputStream(target);
					copy(zipStream, fileStream);
				}
			}
			return;
		} catch (IOException e) {
			if (e instanceof ZipException) {
				p.getLogger().log(Level.SEVERE,
						"Could not unzip downloaded file!", e);
				return;
			} else {
				p.getLogger().log(
						Level.SEVERE,
						"An IOException occured while trying to unzip %s!"
								.replace("%s", p.getName()), e);
				return;
			}
		} finally {
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	private void debug(String message) {
		if (debug)
			p.getLogger().info(
					message + ' ' + new Throwable().getStackTrace()[1]);
	}

	/**
	 * Sets allowed channels, AKA release types
	 * 
	 * @param channels
	 *            The allowed channels
	 */
	public void setChannels(Channel... channels) {
		allowedChannels.clear();
		allowedChannels.addAll(Arrays.asList(channels));
	}

	public enum Channel {
		/**
		 * Normal release
		 */
		RELEASE("release"),

		/**
		 * Beta release
		 */
		BETA("beta"),

		/**
		 * Alpha release
		 */
		ALPHA("alpha");

		private String channel;

		Channel(String channel) {
			this.channel = channel;
		}

		/**
		 * Gets the channel value
		 * 
		 * @return the channel value
		 */
		public String getChannel() {
			return channel;
		}

		/**
		 * Returns channel whose channel value matches the given string
		 * 
		 * @param channel
		 *            The channel value
		 * @return The Channel constant
		 */
		public static Channel matchChannel(String channel) {
			for (Channel c : values()) {
				if (c.channel.equalsIgnoreCase(channel)) {
					return c;
				}
			}
			return null;
		}
	}

	/**
	 * Calculates files MD5 hash
	 * 
	 * @param file
	 *            The file to digest
	 * @return The MD5 hex or null, if the operation failed
	 */
	public String fileHash(File file) {
		FileInputStream is;
		try {
			is = new FileInputStream(file);
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] bytes = new byte[2048];
			int numBytes;
			while ((numBytes = is.read(bytes)) != -1) {
				md.update(bytes, 0, numBytes);
			}
			byte[] digest = md.digest();
			char[] hexChars = new char[digest.length * 2];
			for (int j = 0; j < digest.length; j++) {
				int v = digest[j] & 0xFF;
				hexChars[j * 2] = HEX_CHAR_ARRAY[v >>> 4];
				hexChars[j * 2 + 1] = HEX_CHAR_ARRAY[v & 0x0F];
			}
			is.close();
			return new String(hexChars);
		} catch (IOException | NoSuchAlgorithmException e) {
			p.getLogger().log(Level.SEVERE,
					"Could not digest " + file.getPath(), e);
			return null;
		}
	}

	public enum ProjectID {
		PROTOCOLLIB(45564), WORLDGUARD(31054), WORLDEDIT(31043), VAULT(33184), HOLOGRAPHICDISPLAYS(
				75097), CITIZENS(31073), FACTIONS(31292), DYNMAP(31620);
		private int id;

		ProjectID(int i) {
			id = i;
		}

		public int getID() {
			return id;
		}
	}

}