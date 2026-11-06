package dev.priyme.legitcrystalmacro.util;

import java.util.HashSet;
import java.util.Set;

public final class FriendManager {
   private static final Set<String> friends = new HashSet<>();

   private FriendManager() {
   }

   public static boolean isFriend(String name) {
      return name == null ? false : friends.contains(name.toLowerCase());
   }

   public static void addFriend(String name) {
      if (name != null) {
         friends.add(name.toLowerCase());
      }
   }

   public static void removeFriend(String name) {
      if (name != null) {
         friends.remove(name.toLowerCase());
      }
   }
}
