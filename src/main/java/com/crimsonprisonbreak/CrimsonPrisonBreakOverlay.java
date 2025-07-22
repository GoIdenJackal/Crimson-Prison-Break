package com.crimsonprisonbreak;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.api.SpriteID;

@Singleton
class CrimsonPrisonBreakOverlay extends Overlay {
    private final CrimsonPrisonBreakPlugin plugin;
    private final SpriteManager spriteManager;
    private final CrimsonPrisonBreakConfig config;

    @Inject
    CrimsonPrisonBreakOverlay(
            CrimsonPrisonBreakPlugin plugin,
            Client client,
            SpriteManager spriteManager,
            CrimsonPrisonBreakConfig config) {
        this.plugin = plugin;
        this.spriteManager = spriteManager;
        this.config = config;

        setPosition(OverlayPosition.TOP_RIGHT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setMovable(true);
    }

    @Override
    public Dimension render(Graphics2D g) {

        if (!plugin.isPlayerInGauntletInstance() || !plugin.isPluginActive) {
            return null;
        }

        if (config.hideBeforePerfectedWeaponObtained()
                && plugin.getNextRecommendedStyle().toUpperCase().length() <= 0) {
            return null;
        }

        int iconSize = config.iconSize();
        int iconPadding = config.iconPadding();
        int borderThickness = config.borderThickness();
        int textYOffset = config.textYOffset();
        int textSize = config.textSize();

        int d = 20, x = 0, y = 0;
        int textX = x + d + 10;
        int textY = y + (d / 2) + textYOffset;

        // Font Size
        g.setFont(g.getFont().deriveFont((float) textSize));

        // Roboto default font, fallback to SansSerif
        Font robotoFont;
        try {
            robotoFont = new Font("Roboto", Font.PLAIN, textSize);
        } catch (Exception e) {
            robotoFont = new Font("SansSerif", Font.PLAIN, textSize);
        }
        g.setFont(robotoFont);

        BufferedImage protectionIcon = null;
        String currentHunllefProtection = plugin.getHunllefCurrentOverhead().name().toUpperCase();

        textY += 10;
        if (currentHunllefProtection.contains("RANGED")) {
            protectionIcon = spriteManager.getSprite(SpriteID.PRAYER_PROTECT_FROM_MISSILES, 0);
        } else if (currentHunllefProtection.contains("MAGIC")) {
            protectionIcon = spriteManager.getSprite(SpriteID.PRAYER_PROTECT_FROM_MAGIC, 0);
        } else if (currentHunllefProtection.contains("MELEE")) {
            protectionIcon = spriteManager.getSprite(SpriteID.PRAYER_PROTECT_FROM_MELEE, 0);
        }

        // Overlay dimensions
        int maxWidth = g.getFontMetrics().stringWidth("Hunllef Overhead: " + currentHunllefProtection);
        int totalHeight = textY - (y + (d / 2)) + (textSize + 5) * 2 + 20;
        if (protectionIcon != null) {
            totalHeight += iconSize - textSize;
        }

        // Draw background for the entire overlay
        Color backGroundColour = config.backgroundColour();
        g.setColor(backGroundColour);
        g.fillRect(x - borderThickness, y - borderThickness,
                textX + maxWidth + iconSize + iconPadding + 10 + borderThickness * 2,
                totalHeight + borderThickness * 2);

        // Draw border around the entire overlay
        Color borderColour = config.borderColour();
        if (borderThickness > 0) {
            g.setColor(borderColour);
            g.setStroke(new BasicStroke(borderThickness));
            g.drawRect(x - borderThickness, y - borderThickness,
                    textX + maxWidth + iconSize + iconPadding + 10 + borderThickness * 2,
                    totalHeight + borderThickness * 2);
        }

        if (protectionIcon != null) {
            // Scale icon to configured size
            Image scaledIcon = protectionIcon.getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
            g.drawImage(scaledIcon, textX, textY - iconSize / 2, null);
            g.setColor(Color.WHITE);
            g.drawString("Hunllef Overhead", textX + iconSize + iconPadding, textY + 5);
            textY += iconSize + 10;
        } else {
            // Fallback text
            g.setColor(Color.WHITE);
            g.drawString("Hunllef Overhead: " + currentHunllefProtection, textX, textY);
            textY += textSize + 5;
        }

        g.setColor(Color.WHITE);

        String nextHit = "";
        if (plugin.getNextRecommendedStyle().toUpperCase().length() > 0) {
            g.drawString("Overhead Change In " + (6 - (plugin.totalOffPrayHits % 6)) + " hits.", textX, textY);
            nextHit = plugin.getNextRecommendedStyle().toUpperCase();
            textY += textSize + 5;
            Color hitColour;

            if (nextHit.equals("RANGED")) {
                hitColour = config.rangedColour();
            } else if (nextHit.equals("MAGIC")) {
                hitColour = config.magicColour();
            } else if (nextHit.equals("MELEE")) {
                hitColour = config.meleeColour();
            } else if (nextHit.contains("MELEE") && nextHit.contains("RANGED")) {
                hitColour = config.meleeRangedColour();
            } else if (nextHit.contains("MAGIC") && nextHit.contains("RANGED")) {
                hitColour = config.magicRangedColour();
            } else if (nextHit.contains("MELEE") && nextHit.contains("MAGIC")) {
                hitColour = config.meleeMagicColour();
            } else {
                hitColour = Color.WHITE; // fallback
            }

            g.setColor(hitColour);
            g.drawString("Next Hit: " + nextHit, textX, textY);
        } else {
            g.drawString("Equip a perfected weapon.", textX, textY);
        }

        return new Dimension(textX + maxWidth + iconSize + iconPadding + 10 + borderThickness * 2,
                totalHeight + borderThickness * 2);
    }
}