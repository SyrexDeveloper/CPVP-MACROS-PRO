package dev.priyme.legitcrystalmacro.gui;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public final class ModuleFilters {
   private ModuleFilters() {
   }

   public static String group(ClickGuiScreen.Module module) {
      return module.subcategory != null && !module.subcategory.isBlank() && !module.subcategory.equals("All") ? module.subcategory : "Other";
   }

   public static List<String> pills(List<ClickGuiScreen.Module> modules) {
      LinkedHashSet<String> groups = new LinkedHashSet<>();

      for (ClickGuiScreen.Module m : modules) {
         groups.add(group(m));
      }

      List<String> result = new ArrayList<>();
      result.add("All");
      if (groups.size() > 5) {
         for (String group : groups) {
            if (!group.equals("Other") && result.size() < 5) {
               result.add(group);
            }
         }

         result.add("Other");
      } else {
         result.addAll(groups);
      }

      return List.copyOf(result);
   }

   public static boolean matches(ClickGuiScreen.Module m, List<String> pills, String selected) {
      String group = group(m);
      return selected.equals("All") || selected.equals(pills.contains(group) ? group : "Other");
   }

   public static boolean search(ClickGuiScreen.Module m, String query) {
      return m.name.toLowerCase(Locale.ROOT).contains(query) || m.description.toLowerCase(Locale.ROOT).contains(query);
   }
}
