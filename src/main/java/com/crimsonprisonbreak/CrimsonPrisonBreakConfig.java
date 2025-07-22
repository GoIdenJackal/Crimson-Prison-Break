package com.crimsonprisonbreak;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import java.awt.Color;

@ConfigGroup("crimsonprisonbreak")
public interface CrimsonPrisonBreakConfig extends Config
{
    @ConfigSection(
        name = "Display Settings",
        description = "Visual settings for the overlay",
        position = 0
    )
    String displaySettings = "displaySettings";


    @Alpha
    @ConfigItem(
        keyName   = "borderColour",
        name      = "Border Colour",
        description = "Overlay border colour.",
        section   = displaySettings,
        position  = 1
    )
    default Color borderColour() { return new Color(45,35,23,150); }

    @Alpha
    @ConfigItem(
        keyName   = "backgroundColour",
        name      = "Background Colour",
        description = "Overlay background colour.",
        section   = displaySettings,
        position  = 2
    )
    default Color backgroundColour() { return new Color(62,53,41,150); }


    @ConfigItem(
        keyName = "iconSize",
        name = "Icon Size",
        description = "Size of protection icon in pixels",
        section = displaySettings,
        position = 3
    )
    @Range(min = 8, max = 64)
    default int iconSize()
    {
        return 30;
    }

    @ConfigItem(
        keyName = "iconPadding",
        name = "Icon Padding",
        description = "Padding between icon and text",
        section = displaySettings,
        position = 4
    )
    @Range(min = 0, max = 50)
    default int iconPadding()
    {
        return 11;
    }

    @ConfigItem(
        keyName = "borderThickness",
        name = "Border Thickness",
        description = "Thickness of border around overlay",
        section = displaySettings,
        position = 5
    )
    @Range(min = 0, max = 10)
    default int borderThickness()
    {
        return 2;
    }

    @ConfigItem(
        keyName = "overlayTransparency",
        name = "Overlay Transparency",
        description = "0 is fully transparent, 255 is fully opaque",
        section = displaySettings,
        position = 6
    )
    @Range(min = 0, max = 255)
    default int overlayTransparency()
    {
        return 80;
    }

    @ConfigItem(
        keyName = "textYOffset",
        name = "Text Y Offset",
        description = "Vertical adjustment for text position",
        section = displaySettings,
        position = 7
    )
    @Range(min = 0, max = 50)
    default int textYOffset()
    {
        return 10;
    }

    @ConfigItem(
        keyName = "textSize",
        name = "Text Font Size",
        description = "Font size for overlay text",
        section = displaySettings,
        position = 8
    )
    @Range(min = 8, max = 32)
    default int textSize()
    {
        return 17;
    }
    @ConfigItem(
        keyName = "hideBeforePerfectedWeaponObtained",
        name = "Hide before perfected weapon?",
        description = "Hides the overlay until after a perfected weapon has been acquired.",
        section = displaySettings,
        position = 9
    )
    default boolean hideBeforePerfectedWeaponObtained()
    {
        return false;
    }
    @ConfigSection(
        name      = "Combat Style Colour Settings",
        description = "Colours used for the next‑hit text",
        position  = 10
    )
    String combatStyleColourSettings = "combatStyleColourSettings";

    @Alpha
    @ConfigItem(
        keyName   = "rangedColour",
        name      = "Ranged",
        description = "Colour for a pure Ranged next hit",
        section   = combatStyleColourSettings,
        position  = 0
    )
    default Color rangedColour() { return Color.GREEN; }

    @Alpha
    @ConfigItem(
        keyName   = "magicColour",
        name      = "Magic",
        description = "Colour for a pure Magic next hit",
        section   = combatStyleColourSettings,
        position  = 1
    )
    default Color magicColour() { return Color.BLUE; }

    @Alpha
    @ConfigItem(
        keyName   = "meleeColour",
        name      = "Melee",
        description = "Colour for a pure Melee next hit",
        section   = combatStyleColourSettings,
        position  = 2
    )
    default Color meleeColour() { return Color.RED; }

    @Alpha
    @ConfigItem(
        keyName   = "meleeRangedColour",
        name      = "Melee + Ranged",
        description = "Colour when both Melee and Ranged are viable",
        section   = combatStyleColourSettings,
        position  = 3
    )
    default Color meleeRangedColour() { return Color.ORANGE; }

    @Alpha
    @ConfigItem(
        keyName   = "magicRangedColour",
        name      = "Magic + Ranged",
        description = "Colour when both Magic and Ranged are viable",
        section   = combatStyleColourSettings,
        position  = 4
    )
    default Color magicRangedColour() { return Color.CYAN; }

    @Alpha
    @ConfigItem(
        keyName   = "meleeMagicColour",
        name      = "Melee + Magic",
        description = "Colour when both Melee and Magic are viable",
        section   = combatStyleColourSettings,
        position  = 5
    )
    default Color meleeMagicColour() { return Color.MAGENTA; }
}