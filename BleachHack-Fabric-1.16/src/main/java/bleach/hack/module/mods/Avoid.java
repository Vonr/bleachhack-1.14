/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import bleach.hack.eventbus.BleachSubscribe;
import bleach.hack.event.events.EventBlockShape;
import bleach.hack.event.events.EventClientMove;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.ModuleCategory;
import bleach.hack.setting.base.SettingToggle;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.shape.VoxelShapes;
import bleach.hack.module.Module;

public class Avoid extends Module {

	public Avoid() {
		super("Avoid", KEY_UNBOUND, ModuleCategory.WORLD, "Adds collision boxes to certain blocks/areas",
				new SettingToggle("Cactus", true).withDesc("Adds a bigger collision box to cactuses"),
				new SettingToggle("Fire", true).withDesc("Adds a collision box to fire"),
				new SettingToggle("Lava", true).withDesc("Adds a collision box to lava"),
				new SettingToggle("Unloaded", true).withDesc("Adds walls to unloaded chunks"));
	}

	@BleachSubscribe
	public void onBlockShape(EventBlockShape event) {
		if ((getSetting(0).asToggle().state && event.getState().getBlock() instanceof CactusBlock)
				|| (getSetting(1).asToggle().state && event.getState().getBlock() instanceof FireBlock)
				|| (getSetting(2).asToggle().state && event.getState().getFluidState().getFluid() instanceof LavaFluid)) {
			event.setShape(VoxelShapes.fullCube());
		}
	}

	@BleachSubscribe
	public void onClientMove(EventClientMove event) {
		int x = (int) (mc.player.getX() + event.getVec().x) >> 4;
		int z = (int) (mc.player.getZ() + event.getVec().z) >> 4;
		if (getSetting(3).asToggle().state && !mc.world.getChunkManager().isChunkLoaded(x, z)) {
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void onSendPacket(EventSendPacket event) {
		if (getSetting(3).asToggle().state) {
			if (event.getPacket() instanceof VehicleMoveC2SPacket) {
				VehicleMoveC2SPacket packet = (VehicleMoveC2SPacket) event.getPacket();
				if (!mc.world.getChunkManager().isChunkLoaded((int) packet.getX() >> 4, (int) packet.getZ() >> 4)) {
					mc.player.getVehicle().updatePosition(mc.player.getVehicle().prevX, mc.player.getVehicle().prevY, mc.player.getVehicle().prevZ);
					event.setCancelled(true);
				}
			} else if (event.getPacket() instanceof PlayerMoveC2SPacket) {
				PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) event.getPacket();
				if (!mc.world.getChunkManager().isChunkLoaded((int) packet.getX(mc.player.getX()) >> 4, (int) packet.getZ(mc.player.getZ()) >> 4)) {
					event.setCancelled(true);
				}
			}
		}
	}
}
