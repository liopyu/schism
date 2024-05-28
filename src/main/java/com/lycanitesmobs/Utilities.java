package com.lycanitesmobs;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.time.temporal.ChronoUnit;
import java.util.*;

public class Utilities {
	// ==================================================
  	//                      Raytrace
  	// ==================================================
	// ========== Raytrace All ==========
    public static RayTraceResult raytrace(World world, double x, double y, double z, double tx, double ty, double tz, float borderSize, Entity entity, HashSet<Entity> excluded) {
		Vector3d startVec = new Vector3d(x, y, z);
        Vector3d lookVec = new Vector3d(tx - x, ty - y, tz - z);
        Vector3d endVec = new Vector3d(tx, ty, tz);
		float minX = (float)(x < tx ? x : tx);
		float minY = (float)(y < ty ? y : ty);
		float minZ = (float)(z < tz ? z : tz);
		float maxX = (float)(x > tx ? x : tx);
		float maxY = (float)(y > ty ? y : ty);
		float maxZ = (float)(z > tz ? z : tz);

		// Get Block Collision:
        RayTraceResult collision = world.clip(new RayTraceContext(startVec, endVec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
		startVec = new Vector3d(x, y, z);

		// Get Entity Collision:
		if(excluded != null) {
			AxisAlignedBB bb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).expandTowards(borderSize, borderSize, borderSize);
			List<Entity> allHitEntities = world.getEntities(null, bb);
			Entity closestHitEntity = null;
			double closestEntDistance = Float.POSITIVE_INFINITY;
			for(Entity hitEntity : allHitEntities) {
				if(hitEntity.isPickable() && !excluded.contains(hitEntity)) {
					double entDistance = startVec.distanceTo(hitEntity.position());
					if(entDistance < closestEntDistance) {
						closestEntDistance = entDistance;
						closestHitEntity = hitEntity;
					}
				}
			}
			if(closestHitEntity != null) {
				collision = new EntityRayTraceResult(closestHitEntity);
			}
		}
		
		return collision;
    }
	
	
	// ==================================================
  	//                      Seasonal
  	// ==================================================
	public static boolean isValentines() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) == calendar.FEBRUARY && calendar.get(Calendar.DAY_OF_MONTH) >= 7 && calendar.get(Calendar.DAY_OF_MONTH) <= 14;
	}

	protected static Calendar easterCalendar;
	public static boolean isEaster() {
		Calendar calendar = Calendar.getInstance();

		if(easterCalendar == null) {
			int Y = calendar.get(Calendar.YEAR);
			int a = Y % 19;
			int b = Y / 100;
			int c = Y % 100;
			int d = b / 4;
			int e = b % 4;
			int f = (b + 8) / 25;
			int g = (b - f + 1) / 3;
			int h = (19 * a + b - d - g + 15) % 30;
			int i = c / 4;
			int k = c % 4;
			int L = (32 + 2 * e + 2 * i - h - k) % 7;
			int m = (a + 11 * h + 22 * L) / 451;
			int easterMonth = (h + L - 7 * m + 114) / 31;
			int easterDay = ((h + L - 7 * m + 114) % 31) + 1;
			easterCalendar = new GregorianCalendar(Y, easterMonth, easterDay);
		}

		long daysUntilEaster = ChronoUnit.DAYS.between(calendar.toInstant(), easterCalendar.toInstant());
		return daysUntilEaster <= 7 && daysUntilEaster >= 0;
	}

	public static boolean isMidsummer() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) == calendar.JULY && calendar.get(Calendar.DAY_OF_MONTH) >= 10 && calendar.get(Calendar.DAY_OF_MONTH) <= 20;
	}

    public static boolean isHalloween() {
    	Calendar calendar = Calendar.getInstance();
		if(		(calendar.get(Calendar.DAY_OF_MONTH) >= 25 && calendar.get(Calendar.MONTH) == calendar.OCTOBER)
			||	(calendar.get(Calendar.DAY_OF_MONTH) == 1 && calendar.get(Calendar.MONTH) == calendar.NOVEMBER)
		)
			return true;
		return false;
    }

    public static boolean isYuletide() {
    	Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) == calendar.DECEMBER && calendar.get(Calendar.DAY_OF_MONTH) >= 10 && calendar.get(Calendar.DAY_OF_MONTH) <= 25;
    }

    public static boolean isYuletidePeak() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == calendar.DECEMBER && calendar.get(Calendar.DAY_OF_MONTH) == 25;
    }

    public static boolean isNewYear() {
    	Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) == calendar.JANUARY && calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

	public static int daysBetween(Date d1, Date d2){
		return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	}
}
