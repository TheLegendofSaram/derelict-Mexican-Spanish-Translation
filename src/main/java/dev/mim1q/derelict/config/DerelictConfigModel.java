package dev.mim1q.derelict.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.Config;

@SuppressWarnings("unused")
@Config(name = "derelict", wrapperName = "DerelictConfig")
public class DerelictConfigModel {
    @Comment("""
        Which type of flickering lights to use:
          FAST: Best performance, doesn't have an effect on the emitted light level
          FANCY: Affects emitted light, worse performance. Best option to use with custom texture packs
          FABULOUS: Affects emitted light, looks the best, slightly worse performance than fancy. Best option for vanilla textures

        Note: - When switching from FAST to FANCY/FABULOUS, all existing flickering light blocks will have to be replaced in order for the change to take place.
              - Consider using FAST if you're hosting a server with a lot of players, or if any of the players might be sensitive to flickering lights."""
    )
    public FlickeringLightsSetting flickeringLights = FlickeringLightsSetting.FABULOUS;

    public enum FlickeringLightsSetting {
        FAST, FANCY, FABULOUS
    }

    @Comment("""
        The chance for a Spiderling to spawn when an entity dies.
        Entities that this applies to are defined in the `derelict:entity_types/spawns_spiderlings_on_death` tag.
        Value is a percentage, 0.0 - 100.0"""
    )
    public float spiderlingSpawnChance = 10f;

    @Comment("""
        The ID of the advancement that is necessary to break Arachne's Egg to summon the boss. Not having this
        advancement will display a message with the advancement's DESCRIPTION in-game.
        
        If left empty, no advancement is required."""
    )
    public String arachneEggAdvancement = "";

    @Comment("""
        The time it takes for an Arachne's Egg to respawn, in seconds.
        Set to -1 to disable respawning (only recommended if you have a proper reason to only allow one fight per\s
        nest)."""
    )
    public int arachneEggRespawnTime = 3600;
}