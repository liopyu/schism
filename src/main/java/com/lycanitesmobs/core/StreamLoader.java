package com.lycanitesmobs.core;

import com.lycanitesmobs.LycanitesMobs;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class StreamLoader {
	public static StreamLoader CLIENT;
	public static StreamLoader SERVER;
	public static StreamLoader COMMON;

	/**
	 * Creates and initialises all file loaders for the provided mod domain.
	 * @param domain The mod domain get load from, ex: "lycanitesmobs"
	 */
	public static void initAll(String domain) {
		CLIENT = new StreamLoader("assets", domain);
		SERVER = new StreamLoader("data", domain);
		COMMON = new StreamLoader("common", domain);
		LycanitesMobs.logDebug("", "All FileLoaders initialised successfully.");
	}

	private String rootPath;
	private String domain;

	/**
	 * Constrcutor
	 * @param type The type of directory to load from, ex: "assets", "data", "common"
	 * @param domain The mod domain get load from, ex: "lycanitesmobs"
	 */
	public StreamLoader(String type, String domain) {
		this.domain = domain;
		this.rootPath = type + "/" + domain;
	}

	public File getJarFile() {
		try {
			Path path = new File("mods").toPath();
			Iterator<Path> iterator = Files.walk(path).iterator();
			while (iterator.hasNext()) {
				Path filePath = iterator.next();
				if (filePath.getFileName().toString().toLowerCase().contains(this.domain)) {
					return filePath.toFile();
				}
			}
			throw new RuntimeException("Unable to locate mod jar file.");
		}
		catch(Exception e) {
			LycanitesMobs.logError("Error locating mod jar file.");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates an input stream from this Stream Loader for the provided sub path.
	 * @param subPath The sub directory or file path to load, ex: "creatures/grue.json", "globalspawning.json"
	 * @return
	 */
	public InputStream getStream(String subPath) {
		return this.getClass().getResourceAsStream("/" + this.rootPath + "/" + subPath);
	}

	/**
	 * Creates a list of input streams from this Stream Loader for the provided sub directory.
	 * @param dir The directory to stream files from, ex: "creatures", "textures/blocks"
	 * @return
	 */
	public List<InputStream> getStreams(String dir) {
		List<InputStream> inputStreams = new ArrayList<>();
		try {
			final JarFile jar = new JarFile(this.getJarFile());
			final Enumeration<JarEntry> entries = jar.entries();
			while(entries.hasMoreElements()) {
				final String name = entries.nextElement().getName();
				if (name.startsWith(this.rootPath + "/" + dir + "/") && !name.endsWith("/")) {
					InputStream inputStream = this.getClass().getResourceAsStream("/" + name);
					if(inputStream == null) {
						throw new RuntimeException("Error streaming file from mod jar: /" + name);
					}
					inputStreams.add(inputStream);
				}
			}
			jar.close();
		}
		catch (Exception e) {
			LycanitesMobs.logError("Error reading files within jar file for dir: " + dir);
			throw new RuntimeException(e);
		}
		return inputStreams;
	}
}
