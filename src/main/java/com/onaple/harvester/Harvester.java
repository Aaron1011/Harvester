package com.onaple.harvester;

import javax.inject.Inject;

import com.onaple.harvester.command.ReloadCommand;
import com.onaple.harvester.data.dao.RespawningBlockDao;
import com.onaple.harvester.data.handlers.ConfigurationHandler;
import com.onaple.harvester.exception.PluginNotFoundException;
import com.onaple.harvester.utils.SpawnUtil;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Plugin(id = "harvester", name = "Harvester", version = "0.0.1")
public class Harvester {

	@Inject
	@ConfigDir(sharedRoot=true)
	private Path configDir;

	private static Logger logger;
	@Inject
	private void setLogger(Logger logger) {
		Harvester.logger = logger;
	}
	public static Logger getLogger() {
		return logger;
	}

	private static Harvester harvester;

	public static Harvester getHarvester() {
		return harvester;
	}

	public PluginContainer getInstance() throws PluginNotFoundException {


			return Sponge.getPluginManager().getPlugin("harvester").orElseThrow(() ->  new PluginNotFoundException("harvester"));

	}

	@Listener
	public void onServerStart(GameInitializationEvent event) {
		harvester = this;
		RespawningBlockDao.createTableIfNotExist();
		try {
			logger.info("Number of Block in configuration : " + loadHarvestable());
		} catch (IOException e) {
			logger.error("IOException : ".concat(e.getMessage()));
		} catch (ObjectMappingException e) {
			logger.error("ObjectMappingException : ".concat(e.getMessage()));
		}
		try {
			logger.info("Number of drops in configuration : " + loadDrops());

		} catch (IOException e) {
			logger.error("IOException : ".concat(e.getMessage()));
		} catch (ObjectMappingException e) {
			logger.error("ObjectMappingException : ".concat(e.getMessage()));
		}
		Sponge.getEventManager().registerListeners(this, new HarvestListener());
        Task.builder().execute(() -> SpawnUtil.checkBlockRespawn())
                .delay(5, TimeUnit.SECONDS).interval(30, TimeUnit.SECONDS)
                .name("Task respawning mined resources.").submit(this);

		CommandSpec reload = CommandSpec.builder()
				.description(Text.of("Reaload Harvester configuration from files."))
				.permission("harvester.command.reload")
				.executor(new ReloadCommand()).build();
		Sponge.getCommandManager().register(this, reload, "reload-harvester");



        logger.info("HARVESTER initialized.");
	}

	/**
	 * Load Harvester configuration. If the config file does'nt exist, it load the default file
	 * @return the number of harvestable block imported
	 * @throws IOException error when copying default config in config/harvester/ folder
	 * @throws ObjectMappingException error when the configuration file have an syntax error
	 */
	public int loadHarvestable() throws IOException, ObjectMappingException {
		initDefaultConfig("harvestables.conf");
		return ConfigurationHandler.readHarvestablesConfiguration(ConfigurationHandler.loadConfiguration(configDir+"/harvester/harvestables.conf"));
	}
	/**
	 * Load drops configuration. If the config file does'nt exist, it load the default file
	 * @return the number of drops imported
	 * @throws IOException error when copying default config in config/harvester/ folder
	 * @throws ObjectMappingException error when the configuration file have an syntax error
	 */
	public int loadDrops() throws IOException, ObjectMappingException {
		initDefaultConfig("drops.conf");
		return ConfigurationHandler.readHarvestDropsConfiguration(ConfigurationHandler.loadConfiguration(configDir+"/harvester/drops.conf"));

	}

	/**
	 * Load the default configuration from resources if no files found.
	 * @param path
	 */
	public void initDefaultConfig(String path){
		if (Files.notExists(Paths.get(configDir +"/harvester/" + path))) {
			try {
				PluginContainer pluginInstance = getInstance();
				Optional<Asset> defaultConfigFile = pluginInstance.getAsset(path);
				getLogger().info("No config file set for " + path + " default config will be loaded");
				if (defaultConfigFile.isPresent()) {
					try {
						defaultConfigFile.get().copyToDirectory(Paths.get(configDir + "/harvester/"));
					} catch (IOException e) {
						getLogger().error("Error while setting default configuration : " + e.getMessage());
					}
				} else {
					logger.warn("default config not found");
				}

			}
			catch (PluginNotFoundException e){
				getLogger().error(e.toString());
			}
		}
	}/*

	@Listener
	public void onPlayerItemDrop(DropItemEvent.Dispense event, @First Player player) {
	    if (!player.gameMode().get().equals(GameModes.CREATIVE)) {
            event.setCancelled(true);
        }
	}*/
}
