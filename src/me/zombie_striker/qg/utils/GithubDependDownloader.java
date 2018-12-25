package me.zombie_striker.qg.utils;

import java.io.*;
import java.net.*;

import com.google.gson.*;
import org.bukkit.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GithubDependDownloader {

	/*
	 * public void update(Player p){ String version = Main.version; String
	 * parsedVersion = version.replace(".", "");
	 * 
	 * try { URL api = new
	 * URL("https://api.github.com/repos/greeves12/COD/releases/latest");
	 * URLConnection con = api.openConnection(); con.setConnectTimeout(15000);
	 * con.setReadTimeout(15000);
	 * 
	 * String tagName = null;
	 * 
	 * try{ JsonObject json = new JsonParser().parse(new
	 * InputStreamReader(con.getInputStream())).getAsJsonObject(); tagName =
	 * json.get("tag_name").getAsString();
	 * 
	 * String finalTagName = tagName.replace(".", ""); int latestVersion =
	 * Integer.parseInt(finalTagName.substring(1, finalTagName.length()));
	 * 
	 * if(latestVersion > Integer.parseInt(parsedVersion)) {
	 * 
	 * p.sendMessage("§8*** [COD] §bThere is a new version available §a" + tagName +
	 * "§8***"); p.sendMessage("§8*** §dDownload the new build from here §8***");
	 * p.sendMessage("§8*** §6§6https://github.com/greeves12/COD/releases §8***");
	 * p.sendMessage("§bOnly Admins can see this message!"); }
	 * 
	 * }catch(JsonIOException e){ e.printStackTrace(); } } catch (IOException e) {
	 * e.printStackTrace(); } }
	 */

	public static boolean autoUpdate(final Plugin main, final File output, String author, String githubProject,
			String jarname) {
		try {

			String tagname = null;
			URL api = new URL("https://api.github.com/repos/" + author + "/" + githubProject + "/releases/latest");
			URLConnection con = api.openConnection();
			con.setConnectTimeout(15000);
			con.setReadTimeout(15000);

			JsonObject json = new JsonParser().parse(new InputStreamReader(con.getInputStream())).getAsJsonObject();
			tagname = json.get("tag_name").getAsString();

			final URL download = new URL("https://github.com/" + author + "/" + githubProject + "/releases/download/"
					+ tagname + "/" + jarname);

			Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Found a new version " + ChatColor.WHITE + tagname
					+ ChatColor.LIGHT_PURPLE + " downloading now!!");

			new BukkitRunnable() {

				@Override
				public void run() {
					try {

						InputStream in = download.openStream();

						File pluginFile = output;

						// File temp = new File("plugins/update");
						// if (!temp.exists()) {
						// temp.mkdir();
						// }

						// Path path = new File("plugins/update" + File.separator + "COD.jar").toPath();
						pluginFile.setWritable(true, false);
						pluginFile.delete();
						// Files.copy(in, pluginFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
						copy(in, new FileOutputStream(pluginFile));

						new BukkitRunnable() {
							@Override
							public void run() {
								Bukkit.reload();
							}
						}.runTaskLater(main, 4);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.runTaskLaterAsynchronously(main, 0);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static long copy(InputStream in, OutputStream out) throws IOException {
		long bytes = 0;
		byte[] buf = new byte[0x1000];
		while (true) {
			int r = in.read(buf);
			if (r == -1)
				break;
			out.write(buf, 0, r);
			bytes += r;
			// debug("Another 4K, current: " + r);
		}
		out.flush();
		out.close();
		in.close();
		return bytes;
	}

}
