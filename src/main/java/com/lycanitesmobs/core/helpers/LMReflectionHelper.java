package com.lycanitesmobs.core.helpers;

import com.lycanitesmobs.LycanitesMobs;
import cpw.mods.modlauncher.api.INameMappingService;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class LMReflectionHelper {
	/**
	 * Sets the value of a private final field.
	 * @param classToAccess
	 * @param instance
	 * @param value
	 * @param fieldName
	 */
    public static <T, E> void setPrivateFinalValue(Class <? super T > classToAccess, T instance, E value, String fieldName) throws NoSuchFieldException, IllegalAccessException {
//		fieldName = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, fieldName);
		Field field = ObfuscationReflectionHelper.findField(classToAccess, fieldName);
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		field.set(instance, value);
    }

	/**
	 * Fixes the max health value (why would it be set so low?!) by scanning for matching fields and correcting them.
	 * This is a workaround for remapName not working.
	 */
	public static void fixMaxHealth() {
		try {
			Field[] classFields = RangedAttribute.class.getDeclaredFields();
			for (Field field : classFields) {
				if (field.getType().equals(double.class)) {
					field.setAccessible(true);
					if ((double)field.get(Attributes.MAX_HEALTH) == 1024.0D) {
						field.setDouble(Attributes.MAX_HEALTH, Double.MAX_VALUE);
					}
				}
			}
		}
		catch (Exception e) {
			LycanitesMobs.logError("Unable to fix the Maximum Mob health limit, all bosses will be capped at 1024 health, if you are using the latest version of Lycanites Mobs please report this as a bug with the following stack trace:");
			e.printStackTrace();
		}
	}
}
