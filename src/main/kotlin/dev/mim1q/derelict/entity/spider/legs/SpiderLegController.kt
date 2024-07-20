package dev.mim1q.derelict.entity.spider.legs

import dev.mim1q.derelict.util.TWO_PI
import dev.mim1q.derelict.util.extensions.getLocallyOffsetPos
import dev.mim1q.derelict.util.extensions.radians
import dev.mim1q.derelict.util.wrapRadians
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper.HALF_PI
import net.minecraft.util.math.MathHelper.PI
import net.minecraft.util.math.Vec3d
import kotlin.math.sin

class SpiderLegController(
    private val entity: LivingEntity,
    private val upperLimbLength: Float,
    private val lowerLimbLength: Float,
    vararg offsetsAndTargets: Pair<() -> Vec3d, () -> Vec3d>
) {
    init {
        assert(offsetsAndTargets.size == 8)
    }

    private val legs = offsetsAndTargets.mapIndexed { index, it ->
        SingleLegController(it.first, it.second, index >= 4)
    }.toTypedArray()

    fun tick() {
        legs.forEachIndexed { index, leg ->
            val offset = if ((index % 2 == 0) xor (index >= 4)) 5 else 0
            val isTimeToMove = entity.age % 10 == (offset + index % 2)

            val legYaw = wrapRadians(leg.ikSolver.getYaw(1f) - entity.bodyYaw.radians() + (if (leg.right) PI else 0f))
            val legUpperRoll = leg.ikSolver.getUpperRoll(1f)
            val shouldMoveAnyway = ((legYaw) !in -HALF_PI..HALF_PI) || legUpperRoll < 10f.radians() || entity.hurtTime == entity.maxHurtTime - 5

            leg.step(isTimeToMove || (shouldMoveAnyway && leg.targetChangeTicks == 0))

            // FOR DEBUGGING PURPOSES

//            val particlePos = entity.getLocallyOffsetPos(leg.offset)
//
//            entity.world.addParticle(
//                ParticleTypes.CRIT,
//                particlePos.x, particlePos.y, particlePos.z,
//                0.0, 0.0, 0.0
//            )
//            entity.world.addParticle(
//                ParticleTypes.ENCHANTED_HIT,
//                leg.currentTargetPos.x, leg.currentTargetPos.y, leg.currentTargetPos.z,
//                0.0, 0.0, 0.0
//            )
        }
    }

    fun getIk(index: Int) = legs[index].ikSolver

    private inner class SingleLegController(
        val offset: () -> Vec3d,
        val target: () -> Vec3d,
        val right: Boolean,
    ) {
        val ikSolver = SpiderLegIKSolver(upperLimbLength, lowerLimbLength)

        var prevTargetPos: Vec3d = entity.pos
        var currentTargetPos: Vec3d = entity.pos
        var nextTargetPos: Vec3d? = entity.pos

        var targetChangeTicks: Int = 0

        val closerTarget: Vec3d
            get() {
                val calculatedTarget = target()
                return calculatedTarget.horizontalLength().let { length ->
                    Vec3d((calculatedTarget.x / length) * (length - 0.1), calculatedTarget.y, (calculatedTarget.z / length) * (length - 0.1))
                }
            }

        fun step(shouldMove: Boolean) {
            if (shouldMove) {
                prevTargetPos = currentTargetPos
                val velocity = Vec3d(entity.x - entity.prevX, 0.0, entity.z - entity.prevZ).normalize()
                val posY = findLegY(entity.getLocallyOffsetPos(closerTarget.add(velocity)))
                nextTargetPos = entity.getLocallyOffsetPos(target().add(velocity)).withAxis(Direction.Axis.Y, posY)
            }

            val transitionTime = 6.0

            if (nextTargetPos != null) {
                if (currentTargetPos.squaredDistanceTo(nextTargetPos) > 0.1) {
                    currentTargetPos = prevTargetPos.lerp(nextTargetPos, targetChangeTicks / transitionTime).add(
                        0.0,
                        0.5 * (sin((targetChangeTicks - (transitionTime / 4.0)) * TWO_PI / transitionTime) + 1.0),
                        0.0
                    )
                }

                targetChangeTicks++
                if (targetChangeTicks > transitionTime) {
                    nextTargetPos = null
                }
            } else {
                targetChangeTicks = 0
            }

            ikSolver.convertToLocalAndSolve(entity, offset(), currentTargetPos, true, 5.0)
        }

        private fun findLegY(base: Vec3d): Double {
            for (i in 1 downTo -3) {
                val pos = BlockPos.ofFloored(base).up(i)
                val block = entity.world.getBlockState(pos)
                val shape = block.getCollisionShape(entity.world, pos)
                if (!shape.isEmpty) {
                    return pos.y + shape.getMax(Direction.Axis.Y)
                }
            }
            return base.y
        }
    }
}