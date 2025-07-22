package com.crimsonprisonbreak;

import com.google.inject.Provides;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Item;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "Crimson Prisonbreak", 
    description = "Attack style tracker/helper to assist with learning corrupted gauntlet. Includes cycles for both 5:1 (single perfected) and multiple perfected weapons.", 
    tags = {
        "Attack", 
        "Corrupted Gauntlet", 
        "Gauntlet", 
        "5:1", 
        "CG", 
        "Red Prison", 
        "Crimson Prison", 
        "Ironman", 
        "Iron" 
    })
public class CrimsonPrisonBreakPlugin extends Plugin {

    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private CrimsonPrisonBreakOverlay overlay;

    int totalOffPrayHits = 0;
    public boolean isPluginActive;

    private NPC hunllefNPC;
    private boolean isPendingHunllefOverheadLoad;
    private AttackStyle hunllefCurrentOverhead;

    private String equippedWeaponName = "";
    private String currentCycleType = "5:1";
    private AttackStyle initialPerfectedWeaponStyle = null;
    private List<AttackStyle> ownedPerfectedWeapons;
    AttackStyle lastUsedStyle = null;
    private String nextRecommendedStyle = "";

    @Provides
    CrimsonPrisonBreakConfig provideConfig(ConfigManager cm) {
        return cm.getConfig(CrimsonPrisonBreakConfig.class);
    }

    @Override
    protected void startUp() {
        ownedPerfectedWeapons = new ArrayList<>();
        hunllefCurrentOverhead = AttackStyle.MELEE; // Default hunllef prot to Melee.
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        totalOffPrayHits = 0;
        isPluginActive = true;
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        if (!isPlayerInGauntletInstance() && isPluginActive) {
            return;
        }

        if (isPendingHunllefOverheadLoad) {
            isPendingHunllefOverheadLoad = false;

            var overheadResult = hunllefNPC.getOverheadSpriteIds();
            if (overheadResult == null) {
                isPendingHunllefOverheadLoad = true;
                return;
            }

            int spriteID = overheadResult[0];
            if (spriteID == 0) { // Sprite ID for Melee = 0
                hunllefCurrentOverhead = AttackStyle.MELEE;
            } else if (spriteID == 1) { // Sprite ID for Ranged = 1
                hunllefCurrentOverhead = AttackStyle.RANGED;
            } else if (spriteID == 2) { // Sprite ID for Magic = 2
                hunllefCurrentOverhead = AttackStyle.MAGIC;
            }
        }

        Player player = client.getLocalPlayer();
        equippedWeaponName = getEquippedWeaponName(player).toUpperCase();

        if (equippedWeaponName.contains("PERFECTED")) {
            AttackStyle detectedStyle = null;

            if (equippedWeaponName.contains("STAFF")) {
                detectedStyle = AttackStyle.MAGIC;
            } else if (equippedWeaponName.contains("BOW")) {
                detectedStyle = AttackStyle.RANGED;
            } else if (equippedWeaponName.contains("HALBERD")) {
                detectedStyle = AttackStyle.MELEE;
            }

            if (detectedStyle != null) {
                if (initialPerfectedWeaponStyle == null) {
                    initialPerfectedWeaponStyle = detectedStyle;
                    ownedPerfectedWeapons.add(detectedStyle);
                    if (hunllefCurrentOverhead != initialPerfectedWeaponStyle) {
                        nextRecommendedStyle = initialPerfectedWeaponStyle.toString();
                    }
                } else if (!ownedPerfectedWeapons.contains(detectedStyle)) {
                    ownedPerfectedWeapons.add(detectedStyle);
                    nextRecommendedStyle = getOffPrayerPerfectedWeaponStyles();
                    currentCycleType = "Multiple Perfected Weapons";
                }
            }
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (!isPlayerInGauntletInstance() || event.getNpc().getName() == null) {
            return;
        }

        if (event.getNpc().getName().toUpperCase().contains("HUNLLEF")) // Catch both "Corrupted Hunllef" and
                                                                        // "Crystalline Hunllef"
        {
            hunllefNPC = event.getNpc();
            isPendingHunllefOverheadLoad = true;
            totalOffPrayHits = 0;
            ownedPerfectedWeapons.clear();
            initialPerfectedWeaponStyle = null;
            isPluginActive = true;
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged ev) {
        Actor animationActorTarget = client.getLocalPlayer().getInteracting();

        // Return if we're not the animation actor, or not in a gauntlet instance, or
        // there's no animation 'target'.
        if (ev.getActor() != client.getLocalPlayer() ||
                !isPluginActive ||
                !isPlayerInGauntletInstance() ||
                animationActorTarget == null ||
                animationActorTarget.getName() == null) {
            return;
        }

        // Return if hunllef isn't the animation 'target'.
        String animationActorTargetName = client.getLocalPlayer().getInteracting().getName();
        if (animationActorTargetName != null && !animationActorTargetName.toUpperCase().contains("HUNLLEF")) {
            return;
        }

        int animationID = ev.getActor().getAnimation();
        if (animationID == 400 || /* Sceptre */
                animationID == 401 || /* Sceptre */
                animationID == 422 || /* Unarmed */
                animationID == 423 || /* Unarmed */
                animationID == 428 || /* Halberd */
                animationID == 440 /* Halberd */
        ) {
            lastUsedStyle = AttackStyle.MELEE;
        } else if (animationID == 426 /* Crystal Bow Animation. */) {
            lastUsedStyle = AttackStyle.RANGED;
        } else if (animationID == 1167 /* Crystal Staff Animation */) {
            lastUsedStyle = AttackStyle.MAGIC;
        } else {
            return; // Not an attack animation we're tracking.
        }

        processAttackCycle();
    }

    private String getEquippedWeaponName(Player player) {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment != null) {
            Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
            if (weapon != null) {
                return client.getItemDefinition(weapon.getId()).getName();
            }
        }
        return null;
    }

    // Returns all perfected weapon styles that Hunllef is not currently protecting
    // against.
    public String getOffPrayerPerfectedWeaponStyles() {
        StringJoiner offPrayNonIdeal = new StringJoiner(", ");
        for (AttackStyle style : ownedPerfectedWeapons) {
            if (style != hunllefCurrentOverhead) {
                offPrayNonIdeal.add(style.toString());
            }
        }
        return offPrayNonIdeal.toString();
    }

    private void processAttackCycle() {

        if (currentCycleType == "Multiple Perfected Weapons") {
            if (lastUsedStyle == hunllefCurrentOverhead) {
                return;
            }

            totalOffPrayHits++;
            
            if (totalOffPrayHits % 6 == 0) { // This hit will swap prayer.
                hunllefCurrentOverhead = lastUsedStyle;
            }

            nextRecommendedStyle = getOffPrayerPerfectedWeaponStyles();
            return;
        }

        // Handle non ideal cycle logic if Hunllef starts protected against the ideal
        // style.
        if (hunllefCurrentOverhead == initialPerfectedWeaponStyle) {
            nextRecommendedStyle = getOffPrayerStyles();

            if (lastUsedStyle != hunllefCurrentOverhead) {
                totalOffPrayHits++;
            }

            if (totalOffPrayHits % 6 == 0) {
                hunllefCurrentOverhead = lastUsedStyle;
                nextRecommendedStyle = initialPerfectedWeaponStyle.toString();
            }

            return; // Skip the normal 5:1 cycle during pre-cycle
        }

        if (totalOffPrayHits % 6 < 5) {
            // Still in 5x ideal attack phase
            if (lastUsedStyle != hunllefCurrentOverhead) {
                totalOffPrayHits++;
                if (totalOffPrayHits % 6 == 5) { /* Next attack after this one will proc protection change. */
                    // 6th attack should be off-prayer with the non-perfected attack style.
                    nextRecommendedStyle = getOffPrayerStyles()
                            .replace(initialPerfectedWeaponStyle.toString(), "")
                            .replace(", ", "")
                            .trim();
                } else {
                    // Use perfected attack style.
                    nextRecommendedStyle = initialPerfectedWeaponStyle.toString();
                }
            }
        } else if (totalOffPrayHits % 6 == 5) { // This next attack will swap hunllef's prayer.
            // Attack was on prayer.
            if (lastUsedStyle == hunllefCurrentOverhead) {
                return;
            }            

            totalOffPrayHits++;
            // Attacked correctly with non-perfected weapon.
            if (lastUsedStyle != initialPerfectedWeaponStyle) {
                hunllefCurrentOverhead = lastUsedStyle;
                nextRecommendedStyle = initialPerfectedWeaponStyle.toString();

            // Attacked incorrectly with perfected weapon.
            } else if (lastUsedStyle == initialPerfectedWeaponStyle) {
                hunllefCurrentOverhead = initialPerfectedWeaponStyle;
                nextRecommendedStyle = getOffPrayerStyles();
            }
        }
    }

    public String getOffPrayerStyles() {
        StringJoiner offPrayerStyles = new StringJoiner(", ");
        for (AttackStyle style : AttackStyle.values()) {
            if (style != hunllefCurrentOverhead) {
                offPrayerStyles.add(style.toString());
            }
        }
        return offPrayerStyles.toString();
    }

    // Getters for the overlay to display information
    public AttackStyle getHunllefCurrentOverhead() {
        return hunllefCurrentOverhead;
    }

    public String getNextRecommendedStyle() {
        return nextRecommendedStyle;
    }

    boolean isPlayerInGauntletInstance() {
        if (client == null) {
            return false;
        }
        int[] mapRegions = client.getMapRegions();
        if (mapRegions == null)
            return false;

        for (int region : mapRegions) {
            if (region == 7768 /* CG Boss Area */ || region == 7512 /* Regular Gauntlet Boss Area */) {
                return true;
            }
        }
        return false;
    }

    public enum AttackStyle {
        MAGIC,
        RANGED,
        MELEE
    }
}