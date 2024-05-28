package com.lycanitesmobs.core.pets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.VersionChecker;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.Variant;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;

public class PlayerFamiliars {
	public static PlayerFamiliars INSTANCE = new PlayerFamiliars();
	public Map<UUID, Map<UUID, PetEntry>> playerFamiliars = new HashMap<>();
	public Map<UUID, Long> playerFamiliarLoadedTimes = new HashMap<>();
	public List<String> familiarBlacklist = new ArrayList<>();
	public SSLContext sslContext;

	public PlayerFamiliars() {
		// Trust Self Signed SSL Certificates:
		TrustManager[] trustAllCerts = new TrustManager[]{
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
				}
		};
		try {
			this.sslContext = SSLContext.getInstance("SSL");
			this.sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class FamiliarLoader implements Runnable {
		PlayerEntity player;

		public FamiliarLoader(PlayerEntity player) {
			this.player = player;
		}

		@Override
		public void run() {
			this.getForPlayerUUID(player.getUUID());
			ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(this.player);
			if (extendedPlayer != null) {
				extendedPlayer.loadFamiliars();
			}
			PlayerFamiliars.INSTANCE.updatePlayerFamiliarLoadedTime(this.player);
		}

		private void getForPlayerUUID(UUID uuid) {
			String jsonString;
			try {
				URL url = new URL(LycanitesMobs.serviceAPI + "/familiars?minecraft_uuid=" + uuid.toString());
				HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
				urlConnection.setSSLSocketFactory(PlayerFamiliars.INSTANCE.sslContext.getSocketFactory());
				urlConnection.setRequestProperty("Authorization", "Bearer 7ed1f44cbc1aff693e604075f23d56402983a4a0"); // This is a public api so the key is safe to be exposed like this. :)
				String osName = System.getProperty("os.name");
				urlConnection.setRequestProperty("User-Agent", "Minecraft " + LycanitesMobs.versionMC + " (" + osName + ") LycanitesMobs " + LycanitesMobs.versionNumber);
				InputStream inputStream = urlConnection.getInputStream();
				try {
					jsonString = IOUtils.toString(inputStream, (Charset) null);
				} catch (Exception e) {
					throw e;
				} finally {
					inputStream.close();
				}
				LycanitesMobs.logInfo("", "Online familiars loaded successfully for " + uuid + ".");
			} catch (Throwable e) {
				LycanitesMobs.logInfo("", "Unable to access the online familiars service.");
				e.printStackTrace();
				return;
			}

			// Parse JSON File:
			PlayerFamiliars.INSTANCE.parseFamiliarJSON(jsonString);
		}
	}

	// Parses JSON to Familiars, returns false if the JSON is invalid.
	public void parseFamiliarJSON(String jsonString) {
		try {
			JsonParser jsonParser = new JsonParser();
			JsonObject json = jsonParser.parse(jsonString).getAsJsonObject();
			JsonArray jsonArray = json.getAsJsonArray("data");
			Iterator<JsonElement> jsonIterator = jsonArray.iterator();
			while (jsonIterator.hasNext()) {
				try {
					// Familiar UUIDs:
					JsonObject familiarJson = jsonIterator.next().getAsJsonObject();
					UUID minecraft_uuid = UUID.fromString(familiarJson.get("minecraft_uuid").getAsString());
					if (this.familiarBlacklist.contains(minecraft_uuid.toString())) {
						continue;
					}
					UUID familiar_uuid = UUID.fromString(familiarJson.get("familiar_uuid").getAsString());

					// Familiar Properties:
					String familiar_species = familiarJson.get("familiar_species").getAsString();
					CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(familiar_species);
					int familiar_subspecies = familiarJson.get("familiar_subspecies").getAsInt();
					int familiar_variant = familiarJson.get("familiar_variant").getAsInt();
					Variant creatureVariant = creatureInfo != null ? creatureInfo.getSubspecies(familiar_subspecies).getVariant(familiar_variant) : null;
					String familiar_name = familiarJson.get("familiar_name").getAsString();
					String familiar_color = familiarJson.get("familiar_color").getAsString();
					double familiar_size = familiarJson.get("familiar_size").getAsDouble();
					if(familiar_size <= 0) { // Default Reduced Size:
						familiar_size = 0.5D;
						if(creatureVariant != null && creatureVariant.scale != 1) {
							familiar_size *= 1 / creatureVariant.scale;
						}
					}

					// Create Familiar Pet Entry:
					PetEntryFamiliar familiarEntry = new PetEntryFamiliar(familiar_uuid, null, familiar_species.toLowerCase());
					familiarEntry.setEntitySubspecies(familiar_subspecies);
					familiarEntry.setEntityVariant(familiar_variant);
					familiarEntry.setEntitySize(familiar_size);
					if (!"".equals(familiar_name)) {
						familiarEntry.setEntityName(familiar_name);
					}
					familiarEntry.setColor(familiar_color);

					// Add Pet Entries or Update Existing Entries:
					if (!this.playerFamiliars.containsKey(minecraft_uuid)) {
						this.playerFamiliars.put(minecraft_uuid, new HashMap<>());
					}
					if (!this.playerFamiliars.get(minecraft_uuid).containsKey(familiar_uuid)) {
						this.playerFamiliars.get(minecraft_uuid).put(familiar_uuid, familiarEntry);
					}
					else {
						PetEntry existingEntry = this.playerFamiliars.get(minecraft_uuid).get(familiar_uuid);
						existingEntry.copy(familiarEntry);
					}
				}
				catch(Exception e) {}
			}
		}
		catch(Exception e) {
            /*LycanitesMobs.logWarning("", "A problem occurred when loading online player familiars:");
            e.printStackTrace();*/
		}
	}

	public Map<UUID, PetEntry> getFamiliarsForPlayer(PlayerEntity player) {
		long currentTime = System.currentTimeMillis() / 1000;
		long loadedTime = this.getPlayerFamiliarLoadedTime(player);
		if (loadedTime < 0 || currentTime - loadedTime > 30 * 60) {
			this.updatePlayerFamiliarLoadedTime(player);
			FamiliarLoader familiarLoader = new FamiliarLoader(player);
			Thread thread = new Thread(familiarLoader);
			thread.start();
		}

		Map<UUID, PetEntry> playerFamiliarEntries = new HashMap<>();
		if (this.playerFamiliars.containsKey(player.getUUID())) {
			playerFamiliarEntries = this.playerFamiliars.get(player.getUUID());
			for(PetEntry familiarEntry : playerFamiliarEntries.values()) {
				if(familiarEntry.host == null) {
					familiarEntry.host = player;
				}
			}
		}
		return playerFamiliarEntries;
	}

	public long getPlayerFamiliarLoadedTime(PlayerEntity player) {
		UUID uuid = player.getUUID();
		if (!this.playerFamiliarLoadedTimes.containsKey(uuid)) {
			return -1;
		}
		return this.playerFamiliarLoadedTimes.get(uuid);
	}

	public void updatePlayerFamiliarLoadedTime(PlayerEntity player) {
		this.playerFamiliarLoadedTimes.put(player.getUUID(), System.currentTimeMillis() / 1000);
	}
}
