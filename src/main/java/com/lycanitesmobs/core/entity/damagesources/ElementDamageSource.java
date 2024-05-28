package com.lycanitesmobs.core.entity.damagesources;

import com.lycanitesmobs.core.info.ElementInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSource;

public class ElementDamageSource extends EntityDamageSource {
    private ElementInfo element;

    public static ElementDamageSource causeElementDamage(Entity entity, ElementInfo element) {
        return new ElementDamageSource(entity, element);
    }

    public ElementDamageSource(Entity entity, ElementInfo element) {
		super("mob", entity);
        this.element = element;
	}

    public ElementInfo getElement() {
        return this.element;
    }
}
