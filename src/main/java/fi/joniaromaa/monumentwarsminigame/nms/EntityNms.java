package fi.joniaromaa.monumentwarsminigame.nms;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;

public class EntityNms
{
	public static <T extends Entity> T spawnEntity(org.bukkit.Location location, Class<T> entityClass)
	{
		WorldServer world = ((CraftWorld)location.getWorld()).getHandle();
		
		try
		{
			T entity = (T)entityClass.getConstructor(World.class).newInstance(world);
			entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

			world.addEntity(entity, SpawnReason.CUSTOM);
			
			return entity;
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
