package me.zombie_striker.qg.miscitems;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import me.zombie_striker.qg.ArmoryBaseObject;

public interface ThrowableItems extends ArmoryBaseObject {

	HashMap<Entity, ThrowableHolder> throwItems = new HashMap<>();
	
	class ThrowableHolder {
		private Entity holder;
		private UUID owner;

		private BukkitTask timer;

		public ThrowableHolder(UUID owner, Entity holder) {
			this.holder = holder;
			this.owner = owner;
		}

		public void setHolder(Entity e) {
			this.holder = e;
		}

		public Entity getHolder() {
			return holder;
		}

		public void setTimer(BukkitTask bt) {
			this.timer = bt;
		}

		public BukkitTask getTask() {
			return timer;
		}

		public UUID getOwner() {
			return owner;
		}
	}
}
