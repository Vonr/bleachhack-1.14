/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack;

import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;

import bleach.hack.command.CommandManager;
import bleach.hack.command.CommandSuggestor;
import bleach.hack.gui.title.BleachTitleScreen;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.util.FriendManager;
import bleach.hack.util.io.BleachFileHelper;
import bleach.hack.util.io.BleachFileMang;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Util;

import java.lang.reflect.Method;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BleachHack implements ModInitializer {

	private static BleachHack instance = null;
	public static Logger logger;

	public static final String VERSION = "1.0.1";
	public static final int INTVERSION = 32;

	// TODO: custom eventbus
	public static final EventBus eventBus = Util.make(new EventBus("bleachhack"), bus -> {
		try {
			Method m = ClassUtils.getClass("com.google.common.eventbus.Dispatcher").getDeclaredMethod("immediate");
			m.setAccessible(true);
			FieldUtils.writeDeclaredField(bus, "dispatcher", m.invoke(null), true);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	});

	public static FriendManager friendMang;

	//private BleachFileMang bleachFileManager;

	public static BleachHack getInstance() {
		return instance;
	}

	public BleachHack() {
		if (instance != null) {
			throw new RuntimeException("A BleachHack instance already exists.");
		}
	}

	@Override
	public void onInitialize() {
		long initStartTime = System.currentTimeMillis();

		if (instance != null) {
			throw new RuntimeException("BleachHack has already been initialized.");
		}

		instance = this;
		logger = LogManager.getFormatterLogger("BleachHack");

		//TODO base-rewrite
		//this.eventBus = new EventBus();
		//this.bleachFileManager = new BleachFileMang();
		BleachFileMang.init();
		ModuleManager.loadModules(this.getClass().getClassLoader().getResourceAsStream("bleachhack.modules.json"));
		BleachFileHelper.readModules();

		ClickGui.clickGui.initWindows();
		BleachFileHelper.readClickGui();
		BleachFileHelper.readFriends();
		BleachFileHelper.readUI();

		CommandManager.readPrefix();
		CommandManager.loadCommands(this.getClass().getClassLoader().getResourceAsStream("bleachhack.commands.json"));
		CommandSuggestor.start();

		JsonElement mainMenu = BleachFileHelper.readMiscSetting("customTitleScreen");
		if (mainMenu != null && !mainMenu.getAsBoolean()) {
			BleachTitleScreen.customTitleScreen = false;
		}

		logger.log(Level.INFO, "Loaded BleachHack in %d ms.", System.currentTimeMillis() - initStartTime);
	}
}
