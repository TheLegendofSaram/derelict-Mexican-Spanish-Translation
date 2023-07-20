package com.github.mim1q.derelict.util

import com.github.mim1q.derelict.Derelict
import com.github.mim1q.derelict.client.render.block.entry
import com.github.mim1q.derelict.init.ModBlocksAndItems
import com.github.mim1q.derelict.init.client.ModRender
import com.github.mim1q.derelict.interfaces.AbstractBlockAccessor
import com.github.mim1q.derelict.item.CrosshairTipItem
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.model.*
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.model.EntityModelLoader
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import kotlin.math.sin


object RenderUtil {
  private val HONEYCOMB_TEXTURE = Identifier("textures/item/honeycomb.png")
  private val MARKER_TEXTURE = Derelict.id("textures/block/marker.png")
  private var MARKER_MODEL: Model? = null

  fun renderWaxedIndicator(x: Double, y: Double) {
    val size = Derelict.CLIENT_CONFIG.waxedIndicatorScale() * 16
    val alpha = Derelict.CLIENT_CONFIG.waxedIndicatorOpacity()
    val tessellator = Tessellator.getInstance()
    val bufferBuilder = tessellator.buffer
    RenderSystem.setShader(GameRenderer::getPositionTexColorShader)
    RenderSystem.setShaderTexture(0, HONEYCOMB_TEXTURE)
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha)
    RenderSystem.enableBlend()
    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)
    val z = 300.0
    bufferBuilder.vertex(x, y + size, z).texture(0f, 1f).color(255, 255, 255, 255).next()
    bufferBuilder.vertex(x + size, y + size, z).texture(1f, 1f).color(255, 255, 255, 255).next()
    bufferBuilder.vertex(x + size, y, z).texture(1f, 0f).color(255, 255, 255, 255).next()
    bufferBuilder.vertex(x, y, z).texture(0f, 0f).color(255, 255, 255, 255).next()
    tessellator.draw()
    RenderSystem.disableBlend()
  }

  fun renderCrosshairTip(item: CrosshairTipItem) {
    val target = MinecraftClient.getInstance().crosshairTarget
    if (target?.type == HitResult.Type.BLOCK) {
      val block = MinecraftClient.getInstance().world?.getBlockState((target as BlockHitResult).blockPos)?.block
      if (item.shouldShowTip(block)) {
        renderCrosshairTipTexture(item.getTipTexture())
      }
    }
  }

  private fun renderCrosshairTipTexture(texture: Identifier) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader)
    RenderSystem.setShaderTexture(0, texture)
    val x = MinecraftClient.getInstance().window.scaledWidth / 2 + 6
    val y = MinecraftClient.getInstance().window.scaledHeight / 2 - 8
    DrawableHelper.drawTexture(MatrixStack(), x, y, 0F, 0F, 16, 16, 16, 16)
  }

  fun setupMarkerModel(loader: EntityModelLoader) {
    if (MARKER_MODEL == null) MARKER_MODEL = MarkerModel(loader)
  }

  fun drawMarkers(ctx: WorldRenderContext) {
    val range = Derelict.CLIENT_CONFIG.waxableAndAgeableHightlightRange()
    if (range <= 0) return
    val player = MinecraftClient.getInstance().player ?: return
    val stack = player.mainHandStack
    val waxing = Derelict.CLIENT_CONFIG.waxableHighlights()
      && (stack.isOf(ModBlocksAndItems.WAXING_STAFF) || stack.isOf(Items.HONEYCOMB))
    val aging = Derelict.CLIENT_CONFIG.ageableHighlights() && stack.isOf(ModBlocksAndItems.AGING_STAFF)
    if (!aging && !waxing) return
    val opacity = sin((ctx.world().time + ctx.tickDelta()) * 0.1F) * 0.2F + 0.5F
    BlockPos.iterateOutwards(player.blockPos, range, range, range).forEach {
      val block = ctx.world().getBlockState(it).block as AbstractBlockAccessor
      if (
        (aging && block.isAgeable)
        || (waxing && block.isWaxable)
      ) renderBlockMarker(ctx, it, 0.55F, 0.4F, 0.05F, opacity)
    }
  }

  private fun renderBlockMarker(ctx: WorldRenderContext, pos: BlockPos, red: Float, green: Float, blue: Float, alpha: Float) {
    val matrices = ctx.matrixStack()
    val vertices = ctx.consumers() ?: return

    matrices.entry {
      val camera = ctx.camera()
      val consumer = vertices.getBuffer(RenderLayer.getEntityTranslucentEmissive(MARKER_TEXTURE))
      translate(-camera.pos.x + 0.5 + pos.x, -camera.pos.y - 0.5 + pos.y, -camera.pos.z + 0.5 + pos.z)
      MARKER_MODEL?.render(matrices, consumer, 0xF000F0, OverlayTexture.DEFAULT_UV, red, green, blue, alpha)
    }
  }

  fun getMarkerTexturedModelData(): TexturedModelData {
    val modelData = ModelData()
    val modelPartData = modelData.root
    modelPartData.addChild(
      "bb_main",
      ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, -16.0f, -8.0f, 16.0f, 16.0f, 16.0f, Dilation(0.5f)),
      ModelTransform.pivot(0.0f, 24.0f, 0.0f)
    )
    return TexturedModelData.of(modelData, 64, 64)
  }

  private class MarkerModel(loader: EntityModelLoader) : Model(RenderLayer::getEntityTranslucent) {
    private val main = loader.getModelPart(ModRender.MARKER_LAYER)

    override fun render(
      matrices: MatrixStack,
      vertices: VertexConsumer,
      light: Int,
      overlay: Int,
      red: Float,
      green: Float,
      blue: Float,
      alpha: Float
    ) = main.render(matrices, vertices, light, overlay, red, green, blue, alpha)
  }
}