package cd4017be.thermokin.render;

import java.util.Map.Entry;

import cd4017be.thermokin.item.ItemPart;
import cd4017be.thermokin.module.Part;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;


/**
 * @author CD4017BE
 *
 */
public class PartMeshDefinition implements ItemMeshDefinition {

	private final ItemPart item;

	public PartMeshDefinition(ItemPart item) {
		this.item = item;
		ResourceLocation loc = item.getRegistryName();
		ModelResourceLocation[] locs = new ModelResourceLocation[item.names.size()];
		int i = 0;
		for (Entry<Integer, String> e : item.names.entrySet()) {
			ModelResourceLocation mloc = new ModelResourceLocation(loc, e.getValue());
			locs[i++] = mloc;
			Part p = Part.getPart(item.get(e.getKey()));
			if (p != Part.NULL_MAIN)
				ModularModel.register(p, mloc, BlockRenderLayer.CUTOUT);
		}
		ModelBakery.registerItemVariants(item, locs);
		
	}

	@Override
	public ModelResourceLocation getModelLocation(ItemStack stack) {
		String name = item.names.get(stack.getMetadata());
		return new ModelResourceLocation(item.getRegistryName(), name == null ? "inventory" : name);
	}

}
