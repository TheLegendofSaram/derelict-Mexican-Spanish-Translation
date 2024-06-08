package dev.mim1q.derelict.init

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.block.EmbersBlock
import dev.mim1q.derelict.block.SmolderingLeavesBlock
import dev.mim1q.derelict.block.cobweb.FancyCobwebBlock
import dev.mim1q.derelict.block.cobweb.FancyCobwebWithSpiderNestBlock
import dev.mim1q.derelict.block.cobweb.FancyCornerCobwebBlock
import dev.mim1q.derelict.block.flickering.*
import dev.mim1q.derelict.entity.SpiderlingEntity
import dev.mim1q.derelict.featureset.CoverBoardsSet
import dev.mim1q.derelict.featureset.GrassSet
import dev.mim1q.derelict.featureset.MetalSet
import dev.mim1q.derelict.featureset.WoodSet
import dev.mim1q.derelict.interfaces.AbstractBlockAccessor
import dev.mim1q.derelict.item.StaffItem
import dev.mim1q.derelict.item.weapon.Wildfire
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.mixin.`object`.builder.AbstractBlockSettingsAccessor
import net.minecraft.block.*
import net.minecraft.block.AbstractBlock.Offsetter
import net.minecraft.entity.SpawnReason
import net.minecraft.item.*
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import java.util.*

@Suppress("UNUSED")
object ModBlocksAndItems {
    private val lastAgeable = mutableSetOf<Block>()
    private val lastWaxable = mutableSetOf<Block>()

    val AGING_STAFF = registerItem("aging_staff", StaffItem.Aging(defaultItemSettings().maxCount(1)))
    val WAXING_STAFF = registerItem("waxing_staff", StaffItem.Waxing(defaultItemSettings().maxCount(1)))

    val BURNED_WOOD = WoodSet(Derelict.id("burned"), defaultItemSettings()).register()
    val BURNED_LEAVES = register("burned_leaves", LeavesBlock(FabricBlockSettings.copyOf(Blocks.OAK_LEAVES)))
    val SMOLDERING_LEAVES =
        register("smoldering_leaves", SmolderingLeavesBlock(FabricBlockSettings.copyOf(Blocks.OAK_LEAVES)))
    val SMOKING_EMBERS = register(
        "smoking_embers",
        EmbersBlock.Smoking(FabricBlockSettings.copyOf(Blocks.FIRE).luminance(1).breakInstantly())
    )
    val SMOLDERING_EMBERS = register(
        "smoldering_embers", EmbersBlock.Smoldering(
            FabricBlockSettings.copyOf(Blocks.FIRE).luminance(4).emissiveLighting { _, _, _ -> true }.breakInstantly()
        )
    )
    val DRIED_GRASS = GrassSet(Derelict.id("dried"), defaultItemSettings())
    val BURNED_GRASS = GrassSet(Derelict.id("burned"), defaultItemSettings())

    val OAK_COVER_BOARDS = CoverBoardsSet(Derelict.id("oak"), defaultItemSettings())
    val SPRUCE_COVER_BOARDS = CoverBoardsSet(Derelict.id("spruce"), defaultItemSettings())
    val BIRCH_COVER_BOARDS = CoverBoardsSet(Derelict.id("birch"), defaultItemSettings())
    val JUNGLE_COVER_BOARDS = CoverBoardsSet(Derelict.id("jungle"), defaultItemSettings())
    val ACACIA_COVER_BOARDS = CoverBoardsSet(Derelict.id("acacia"), defaultItemSettings())
    val DARK_OAK_COVER_BOARDS = CoverBoardsSet(Derelict.id("dark_oak"), defaultItemSettings())
    val MANGROVE_COVER_BOARDS = CoverBoardsSet(Derelict.id("mangrove"), defaultItemSettings())
    val CHERRY_COVER_BOARDS = CoverBoardsSet(Derelict.id("cherry"), defaultItemSettings())
    val BAMBOO_COVER_BOARD = CoverBoardsSet(Derelict.id("bamboo"), defaultItemSettings())
    val WARPED_COVER_BOARDS = CoverBoardsSet(Derelict.id("warped"), defaultItemSettings())
    val CRIMSON_COVER_BOARDS = CoverBoardsSet(Derelict.id("crimson"), defaultItemSettings())
    val BURNED_COVER_BOARDS = CoverBoardsSet(Derelict.id("burned"), defaultItemSettings())

    val FLICKERING_REDSTONE_LAMP = register("flickering_redstone_lamp", FlickeringSolidBlock(FabricBlockSettings.copyOf(Blocks.REDSTONE_LAMP)))
    val FLICKERING_SEA_LANTERN = register("flickering_sea_lantern", FlickeringSolidBlock(FabricBlockSettings.copyOf(Blocks.SEA_LANTERN)))
    val BROKEN_SEA_LANTERN = register("broken_sea_lantern", Block(FabricBlockSettings.copyOf(Blocks.SEA_LANTERN).luminance { 0 }))
    val FLICKERING_JACK_O_LANTERN = register("flickering_jack_o_lantern", FlickeringCarvedPumpkinBlock(FabricBlockSettings.copyOf(Blocks.JACK_O_LANTERN)))
    val FLICKERING_LANTERN = register("flickering_lantern", FlickeringLanternBlock(FabricBlockSettings.copyOf(Blocks.LANTERN)))
    val FLICKERING_SOUL_LANTERN = register("flickering_soul_lantern", FlickeringLanternBlock(FabricBlockSettings.copyOf(Blocks.SOUL_LANTERN)))
    val BROKEN_LANTERN = register("broken_lantern", LanternBlock(FabricBlockSettings.copyOf(Blocks.LANTERN).luminance { 0 }))
    val FLICKERING_OCHRE_FROGLIGHT = register("flickering_ochre_froglight", FlickeringSolidBlock(FabricBlockSettings.copyOf(Blocks.OCHRE_FROGLIGHT)))
    val BROKEN_OCHRE_FROGLIGHT = register("broken_ochre_froglight", Block(FabricBlockSettings.copyOf(Blocks.OCHRE_FROGLIGHT).luminance { 0 }))
    val FLICKERING_VERDANT_FROGLIGHT = register("flickering_verdant_froglight", FlickeringSolidBlock(FabricBlockSettings.copyOf(Blocks.VERDANT_FROGLIGHT)))
    val BROKEN_VERDANT_FROGLIGHT = register("broken_verdant_froglight", Block(FabricBlockSettings.copyOf(Blocks.VERDANT_FROGLIGHT).luminance { 0 }))
    val FLICKERING_PEARLESCENT_FROGLIGHT = register("flickering_pearlescent_froglight", FlickeringSolidBlock(FabricBlockSettings.copyOf(Blocks.PEARLESCENT_FROGLIGHT)))
    val BROKEN_PEARLESCENT_FROGLIGHT = register("broken_pearlescent_froglight", Block(FabricBlockSettings.copyOf(Blocks.PEARLESCENT_FROGLIGHT).luminance { 0 }))
    val FLICKERING_GLOWSTONE = register("flickering_glowstone", FlickeringSolidBlock(FabricBlockSettings.copyOf(Blocks.GLOWSTONE)))
    val BROKEN_GLOWSTONE = register("broken_glowstone", Block(FabricBlockSettings.copyOf(Blocks.GLOWSTONE).luminance { 0 }))
    val FLICKERING_SHROOMLIGHT = register("flickering_shroomlight", FlickeringSolidBlock(FabricBlockSettings.copyOf(Blocks.SHROOMLIGHT)))
    val BROKEN_SHROOMLIGHT = register("broken_shroomlight", Block(FabricBlockSettings.copyOf(Blocks.SHROOMLIGHT).luminance { 0 }))
    val FLICKERING_END_ROD = register("flickering_end_rod", FlickeringEndRodBlock(FabricBlockSettings.copyOf(Blocks.END_ROD)))
    val BROKEN_END_ROD = register("broken_end_rod", BrokenEndRodBlock(FabricBlockSettings.copyOf(Blocks.END_ROD).luminance { 0 }))

    val FANCY_COBWEB = register("fancy_cobweb", FancyCobwebBlock(FabricBlockSettings.copyOf(Blocks.COBWEB)))
    val FANCY_COBWEB_WITH_SPIDER_NEST = register("fancy_cobweb_with_spider_nest", FancyCobwebWithSpiderNestBlock(FabricBlockSettings.copyOf(Blocks.COBWEB)))
    val CORNER_COBWEB = register("corner_cobweb", FancyCornerCobwebBlock(FabricBlockSettings.copyOf(Blocks.COBWEB)))
    val FANCY_CORNER_COBWEB = register("fancy_corner_cobweb", FancyCornerCobwebBlock(FabricBlockSettings.copyOf(Blocks.COBWEB)))

    val SPIDERLING_IN_A_BUCKET = registerItem("spiderling_in_a_bucket", object : Item(defaultItemSettings().maxCount(1)) {
        override fun useOnBlock(context: ItemUsageContext): ActionResult {
            if (context.world.isClient) return ActionResult.SUCCESS

            val entity = ModEntities.SPIDERLING.spawnFromItemStack(
                context.world as ServerWorld,
                context.stack, context.player,
                context.blockPos.add(context.side.vector),
                SpawnReason.BUCKET,
                true,
                false
            )
            if (entity is SpiderlingEntity) {
                entity.copyDataFromNbt(context.stack.getOrCreateNbt())
                entity.isFromBucket = true
                if (context.side == Direction.DOWN) {
                    entity.anchorPosition = context.blockPos
                    entity.setVelocity(0.0, -0.01, 0.0)
                }
                entity.setPersistent()
                context.world.spawnEntity(entity)
                context.world.playSoundAtBlockCenter(context.blockPos, SoundEvents.ENTITY_SPIDER_STEP, SoundCategory.NEUTRAL, 1.0f, 1.0f, false)
                context.player?.setStackInHand(context.hand, ItemStack(Items.BUCKET))
            }
            return ActionResult.SUCCESS
        }
    })

    val NOCTISTEEL = MetalSet.ThreeLevelOxidizable(
        Derelict.id("noctisteel"),
        defaultItemSettings(),
        FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(2.0f, 1200f).nonOpaque()
    ).register()

    val BLOCK_AGING_MAP = mapOf(
        Blocks.REDSTONE_LAMP to FLICKERING_REDSTONE_LAMP,
        Blocks.SEA_LANTERN to FLICKERING_SEA_LANTERN,
        FLICKERING_SEA_LANTERN to BROKEN_SEA_LANTERN,
        Blocks.JACK_O_LANTERN to FLICKERING_JACK_O_LANTERN,
        Blocks.LANTERN to FLICKERING_LANTERN,
        FLICKERING_LANTERN to BROKEN_LANTERN,
        Blocks.SOUL_LANTERN to FLICKERING_SOUL_LANTERN,
        FLICKERING_SOUL_LANTERN to BROKEN_LANTERN,
        Blocks.OCHRE_FROGLIGHT to FLICKERING_OCHRE_FROGLIGHT,
        FLICKERING_OCHRE_FROGLIGHT to BROKEN_OCHRE_FROGLIGHT,
        Blocks.VERDANT_FROGLIGHT to FLICKERING_VERDANT_FROGLIGHT,
        FLICKERING_VERDANT_FROGLIGHT to BROKEN_VERDANT_FROGLIGHT,
        Blocks.PEARLESCENT_FROGLIGHT to FLICKERING_PEARLESCENT_FROGLIGHT,
        FLICKERING_PEARLESCENT_FROGLIGHT to BROKEN_PEARLESCENT_FROGLIGHT,
        Blocks.GLOWSTONE to FLICKERING_GLOWSTONE,
        FLICKERING_GLOWSTONE to BROKEN_GLOWSTONE,
        Blocks.SHROOMLIGHT to FLICKERING_SHROOMLIGHT,
        FLICKERING_SHROOMLIGHT to BROKEN_SHROOMLIGHT,
        Blocks.END_ROD to FLICKERING_END_ROD,
        FLICKERING_END_ROD to BROKEN_END_ROD
    )

    val WILDFIRE = registerItem("wildfire", Wildfire(defaultItemSettings().maxCount(1)))

    fun init() {}

    fun setupWaxableAndAgeable() {
        lastWaxable.forEach { (it as AbstractBlockAccessor).isWaxable = false }
        lastAgeable.forEach { (it as AbstractBlockAccessor).isAgeable = false }
        BLOCK_AGING_MAP.forEach { (from, _) ->
            (from as AbstractBlockAccessor).isAgeable = true
            lastAgeable.add(from)
        }
        HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().forEach { (from, _) ->
            (from as AbstractBlockAccessor).isWaxable = true
            lastWaxable.add(from)
        }
        Oxidizable.OXIDATION_LEVEL_INCREASES.get().forEach { (from, _) ->
            (from as AbstractBlockAccessor).isAgeable = true
            lastAgeable.add(from)
        }
        HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().forEach { (from, to) ->
            if (Oxidizable.OXIDATION_LEVEL_INCREASES.get().containsKey(to))
                (from as AbstractBlockAccessor).isAgeable = true
        }
    }

    internal fun <T : Block> register(name: String, block: T, category: ItemCategory = ItemCategory.GENERAL): T {
        registerBlock(name, block)
        registerItem(name, BlockItem(block, defaultItemSettings()), category)
        return block
    }

    internal fun <T : Block> registerBlock(name: String, block: T): T = Registry.register(
        Registries.BLOCK, Derelict.id(name), block
    )

    internal fun <T : Item> registerItem(name: String, item: T, category: ItemCategory = ItemCategory.GENERAL): T {
        category.add(item)
        return Registry.register(Registries.ITEM, Derelict.id(name), item)
    }

    private fun defaultItemSettings() = FabricItemSettings()

    enum class ItemCategory {
        GENERAL,
        METAL_BLOCKS,
        METAL_DECORATION,
        WAXED_METAL_BLOCKS,
        WAXED_METAL_DECORATION;

        val items: MutableList<Item> = mutableListOf()
        fun add(item: Item) = items.add(item)
    }
}

object NoZFightingOffsetter : Offsetter {
    override fun evaluate(state: BlockState, world: BlockView, pos: BlockPos): Vec3d {
        val x = pos.x % 3
        val y = pos.y % 3
        val z = pos.z % 3
        return (Vec3d(
            (z * 0.001) + (y * 0.0015),
            (x * 0.001) + (z * 0.0015),
            (y * 0.001) + (x * 0.0015)
        ))
    }
}

@Suppress("UnstableApiUsage")
fun <T : AbstractBlock.Settings> T.noZFighting(): T {
    (this as AbstractBlockSettingsAccessor).offsetter = Optional.of(NoZFightingOffsetter)
    this.dynamicBounds = true
    return this
}