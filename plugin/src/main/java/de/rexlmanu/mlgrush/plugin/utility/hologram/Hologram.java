package de.rexlmanu.mlgrush.plugin.utility.hologram;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

/**
 * Hologram api from https://www.spigotmc.org/resources/api-hologram-api.81553/
 */
public class Hologram {

    private static List<String> removeFirstElement(List<String> lines) {
        ArrayList<String> modifiedLines = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            modifiedLines.add(lines.get(i));
        }
        return modifiedLines;
    }

    private ArrayList<ArmorStand> holoEntities;

    public Hologram(List<String> lines, Location location) {
        this(lines.get(0), removeFirstElement(lines), location);
    }

    public Hologram(String header, List<String> lines, Location location) {
        holoEntities = new ArrayList<ArmorStand>();
        setupHologram(header, location);
        if (lines == null)
            return;
        for (String line : lines)
            addLine(line);
    }

    //Only used on creation, use setLine() to edit the Hologram
    private void setupHologram(String header, Location location) {
        ArmorStand headerStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        headerStand.setCustomName(getColoredText(header));
        headerStand.setCustomNameVisible(true);
        headerStand.setGravity(false);
        headerStand.setVisible(false);

        holoEntities.add(headerStand);
    }

    public void addLine(String line) {
        Location headerLocation = holoEntities.get(0).getLocation().clone();
        ArmorStand lineStand = (ArmorStand) headerLocation.getWorld().spawnEntity(headerLocation.add(0, -0.28 * holoEntities.size(), 0), EntityType.ARMOR_STAND);
        lineStand.setCustomName(getColoredText(line));
        lineStand.setCustomNameVisible(true);
        lineStand.setGravity(false);
        lineStand.setVisible(false);

        holoEntities.add(lineStand);
    }

    //Edits line with specified index to user input
    public void setLine(int line, String text) {
        if (line < 1 || line > holoEntities.size())
            return;
        ArmorStand lineStand = holoEntities.get(line - 1);
        lineStand.setCustomName(getColoredText(text));
        lineStand.setCustomNameVisible(true);
        lineStand.setGravity(false);
        lineStand.setVisible(false);
    }

    public void removeLine(int line) {
        if (line < 1 || line > holoEntities.size())
            return;
        ArmorStand lineStand = holoEntities.get(line - 1);
        lineStand.remove();

        holoEntities.remove(lineStand);
        update();
    }

    public String getLine(int line) {
        if (line < 1 || line > holoEntities.size())
            return null;
        return holoEntities.get(line - 1).getCustomName();
    }

    //Useful for checking and comparing lines
    public String getStrippedLine(int line) {
        if (line < 1 || line > holoEntities.size())
            return null;
        return getStrippedText(holoEntities.get(line - 1).getCustomName());
    }

    public void move(Location newLocation) {
        holoEntities.get(0).teleport(newLocation);
        update();
    }

    //Relocates armourstands to correct position
    public void update() {
        Location headerLocation = holoEntities.get(0).getLocation().clone();
        for (int x = 1; x < holoEntities.size(); x++) {
            ArmorStand lineStand = holoEntities.get(x);
            lineStand.teleport(headerLocation.add(0, -0.28, 0));
        }
    }

    public void delete() {
        for (Entity e : holoEntities)
            e.remove();
    }

    public static String getColoredText(String text) {
        return text.replace("\u00a7", "§").replace("&", "§");
    }

    //Removes text colour for easier usage
    public static String getStrippedText(String text) {
        String[] split = text.split("");
        for (int x = 0; x < split.length; x++)
            if (split[x].equals("§") && !split[x + 1].equals(" "))
                text = text.replace(split[x] + split[x + 1], "");
        return text;
    }

}