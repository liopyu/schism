package com.lycanitesmobs.core;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileLoader {
	public static FileLoader CLIENT;
	public static FileLoader SERVER;
	public static FileLoader COMMON;

	/**
	 * Creates and initialises all file loaders for the provided mod domain.
	 * @param domain The mod domain to load from, ex: "lycanitesmobs"
	 */
	public static void initAll(String domain) {
		CLIENT = new FileLoader("assets", domain);
		SERVER = new FileLoader("data", domain);
		COMMON = new FileLoader("common", domain);
		LycanitesMobs.logDebug("", "All FileLoaders initialised successfully.");
	}

	private FileSystem fileSystem;
	private String rootPath;
	private String domain;
	public boolean ready = false;

	/**
	 * Constrcutor
	 * @param type The type of directory to load from, ex: "assets", "data", "common"
	 * @param domain The mod domain to load from, ex: "lycanitesmobs"
	 */
	public FileLoader(String type, String domain) {
		this.domain = domain;
		this.rootPath = "/" + type + "/" + domain;
		try {
			URI rootUri = this.getClass().getResource(this.rootPath + "/" + ".root").toURI();

			// Get File System:
			if ("file".equals(rootUri.getScheme())) {
				Path rootPathInstance = Paths.get(rootUri);
				this.rootPath = rootPathInstance.toString().replace(rootPathInstance.getFileName().toString(), "");
				this.fileSystem = rootPathInstance.getFileSystem();
				this.ready = true;
			}

			// Create Custom File Provider as Modjar Is Not Ready In Time:
			else if ("modjar".equals(rootUri.getScheme())) {

				//LycanitesMobs.logInfo("", "Creating file system for " + this.domain + " Jar Path: " + this.getModJarPath());
				//this.fileSystem = FileSystems.newFileSystem(this.getModJarPath(), null);
				//LycanitesMobs.logInfo("", "Created file system for " + this.domain + " Source: " + this.fileSystem);

				/*try {
					this.fileSystem = FileSystems.getFileSystem(rootUri);
				} catch (FileSystemNotFoundException e) {
					this.fileSystem = FileSystems.newFileSystem(rootUri, Collections.emptyMap());
				}*/
			}

			// Unknown File Scheme:
			else {
				LycanitesMobs.logError("Unsupported file scheme: " + rootUri.getScheme());
				throw new RuntimeException("Unsupported file scheme: " + rootUri.getScheme());
			}
		}
		catch(Exception e) {
			LycanitesMobs.logError("An exception occurred when creating a FileLoader for root path: " + this.rootPath);
			throw new RuntimeException(e);
		}

		/*/ Validate:
		Path testPath = this.getPath(".root");
		LycanitesMobs.logInfo("", "Validating file system: " + this.fileSystem + " Test path: " + testPath);
		File testFile = testPath.toFile();
		if(!testFile.exists()) {
			throw new RuntimeException("Unable to validate FileLoader for root path: " + this.rootPath);
		}
		LycanitesMobs.logInfo("", "File system validated, good to go!");*/
	}

	public File getJarFile() {
		LycanitesMobs.logWarning("", "Test Path: " + LycanitesMobs.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		return new File(LycanitesMobs.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	}

	public Path getModJarPath() throws IOException {
		Path path = new File("mods").toPath();
		Iterator<Path> iterator = Files.walk(path).iterator();
		while(iterator.hasNext()) {
			Path filePath = iterator.next();
			if(filePath.getFileName().toString().toLowerCase().contains(this.domain)) {
				return Paths.get(filePath.toFile().getCanonicalPath());
			}
		}
		throw new RuntimeException("Unable to find Lycanites Mobs jar file by name, make sure that it at least has 'lycanitesmobs' in the jar file name if it is renamed.");
		//return Paths.get(this.getJarFile().getCanonicalPath());
	}

	/**
	 * Creates a Path from this File Loader for the provided sub path.
	 * @param subPath The sub directory or file path to load, ex: "creatures", "globalspawning.json", "textures/blocks"
	 * @return
	 */
	public Path getPath(String subPath) {
		return this.fileSystem.getPath(this.rootPath + "/" + subPath);
	}

	/**
	 * Returns a list of ResourceLocations for every file in the provided Path instance.
	 * @param subPath The sub directory or file path to load, ex: "creatures", "textures/blocks"
	 * @param fileType The file extension to use. Ex: "png"
	 * @return A list of ResourceLocations found.
	 */
	public List<ResourceLocation> getPathResourceLocations(String subPath, String fileType) {
		Path path = this.getPath(subPath);
		List<ResourceLocation> resourceLocations = new ArrayList<>();
		try {
			Iterator<Path> iterator = Files.walk(path).iterator();
			while(iterator.hasNext()) {
				Path filePath = iterator.next();
				if (fileType == null || fileType.equals(FilenameUtils.getExtension(filePath.toString()))) {
					Path relativePath = path.relativize(filePath);
					String resourceLocationPath = FilenameUtils.removeExtension(relativePath.toString()).replaceAll("\\\\", "/");
					ResourceLocation resourceLocation = new ResourceLocation(this.domain, resourceLocationPath);
					resourceLocations.add(resourceLocation);
				}
			}
		}
		catch (Exception e) {
			LycanitesMobs.logWarning("", "There was a problem getting ResourceLocations for: " + path + ", " + fileType + ", " + " \n" + e.toString());
		}

		return resourceLocations;
	}
}
