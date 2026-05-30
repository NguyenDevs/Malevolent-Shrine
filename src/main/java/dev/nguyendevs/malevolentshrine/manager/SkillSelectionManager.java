package dev.nguyendevs.malevolentshrine.manager;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class SkillSelectionManager {

    private final SkillToggleManager toggleManager;
    private final MessageManager messageManager;
    private final Map<UUID, String> selectedSkills = new HashMap<>();
    private final List<String> availableSkills = Arrays.asList("domain-expansion", "cleave", "dismantle", "rct", "divine-flame");

    public SkillSelectionManager(SkillToggleManager toggleManager, MessageManager messageManager) {
        this.toggleManager = toggleManager;
        this.messageManager = messageManager;
    }

    public String getSelectedSkill(UUID playerId) {
        return selectedSkills.getOrDefault(playerId, "domain-expansion");
    }

    public void cycleSkill(Player player, boolean forward) {
        UUID playerId = player.getUniqueId();

        List<String> enabledSkills = new ArrayList<>();
        for (String skill : availableSkills) {
            if (toggleManager.isSkillEnabled(playerId, skill)
                    && player.hasPermission("malevolentshrine.use." + skill)) {
                enabledSkills.add(skill);
            }
        }

        if (enabledSkills.isEmpty()) return;

        String current = selectedSkills.getOrDefault(playerId, enabledSkills.get(0));
        int index = enabledSkills.indexOf(current);

        if (index == -1) {
            index = 0;
        } else {
            if (forward) {
                index++;
                if (index >= enabledSkills.size()) index = 0;
            } else {
                index--;
                if (index < 0) index = enabledSkills.size() - 1;
            }
        }

        String newSkill = enabledSkills.get(index);
        selectedSkills.put(playerId, newSkill);

        sendSelectionMessage(player, newSkill);
    }

    private void sendSelectionMessage(Player player, String skill) {
        String displayName = getDisplayName(skill);
        String title = messageManager.getRawMessage("skill-select-title").replace("{skill}", displayName);
        String subtitle = messageManager.getRawMessage("skill-select-subtitle");
        player.sendTitle(title, subtitle, 10, 40, 10);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }

    private String getDisplayName(String skill) {
        switch (skill) {
            case "domain-expansion": return "Domain Expansion";
            case "cleave": return "Cleave";
            case "dismantle": return "Dismantle";
            case "rct": return "Reverse Cursed Technique";
            case "divine-flame": return "Divine Flame";
            default: return skill;
        }
    }
}
