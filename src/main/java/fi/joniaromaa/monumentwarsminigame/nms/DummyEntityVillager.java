package fi.joniaromaa.monumentwarsminigame.nms;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.potion.PotionEffectType;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.MobEffect;
import net.minecraft.server.v1_8_R3.World;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DummyEntityVillager extends EntityVillager
{
	static
	{
		try
		{
			Field d = EntityTypes.class.getDeclaredField("d");
			Field f = EntityTypes.class.getDeclaredField("f");
			
			d.setAccessible(true);
			f.setAccessible(true);
			
			String villagerName = (String)((Map)d.get(null)).get(EntityVillager.class);
	        ((Map)d.get(null)).put(DummyEntityVillager.class, villagerName);

			int villagerId = (int)((Map)f.get(null)).get(EntityVillager.class);
	        ((Map)f.get(null)).put(DummyEntityVillager.class, villagerId);
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException e)
		{
			e.printStackTrace();
		}
	}
	
	@Getter @Setter private Function<org.bukkit.entity.HumanEntity, Boolean> rightClickCallable;
	
	@SuppressWarnings("deprecation")
	public DummyEntityVillager(World world)
	{
		super(world);
		
		this.k(true); //No AI
		
		this.ageLocked = true;
		this.setAge(0);
		
		this.noDamageTicks = Integer.MAX_VALUE;
		this.addEffect(new MobEffect(PotionEffectType.DAMAGE_RESISTANCE.getId(), Integer.MAX_VALUE, Byte.MAX_VALUE, false, false));
	}
	
	@Override
    protected String z() //Living sound
    {
    	return null;
    }

	@Override
	public boolean a(EntityHuman entityHuman)
	{
		try
		{
			if (this.rightClickCallable != null && this.rightClickCallable.apply(entityHuman.getBukkitEntity()))
			{
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			return false; //Oops...
		}
		
		return super.a(entityHuman);
	}
	
	@Override
	protected void a(BlockPosition blockposition, Block block) //Step sound
	{
		
	}
}
