package draylar.maybedata.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import draylar.maybedata.data.MaybeLootTable;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

@Mixin(LootManager.class)
public class LootManagerMixin {


    @Inject(method = "method_51189", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/JsonDataLoader;load(Lnet/minecraft/resource/ResourceManager;Ljava/lang/String;Lcom/google/gson/Gson;Ljava/util/Map;)V"), cancellable = true)
    private static void deserializeCondition(ResourceManager resourceManager, LootDataType<?> lootDataType, Map<Identifier, JsonElement> map, CallbackInfo ci) {
        ResourceFinder resourceFinder = ResourceFinder.json(lootDataType.getId());

        for (Map.Entry<Identifier, Resource> identifierResourceEntry : resourceFinder.findResources(resourceManager).entrySet()) {
            try {
                Reader reader = identifierResourceEntry.getValue().getReader();
                // Yes, this is unsafe. But I couldn't find a better way to do this.
                MaybeLootTable table = lootDataType.getGson().fromJson(reader, MaybeLootTable.class);
                if (table.getMaybeCondition() != null) {
                    if (!table.getMaybeCondition().verify()) {
                        ci.cancel();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
