package dev.priyme.legitcrystalmacro.util;

public final class FallDistanceTracker {
   private static Object player;
   private static double previousY;
   private static double peakY;
   private static double distance;

   private FallDistanceTracker() {
   }

   public static void reset() {
      player = null;
      distance = 0.0;
   }

   public static void update(Object currentPlayer, double y, double velocityY, boolean groundedOrFlying) {
      if (Double.isFinite(y) && Double.isFinite(velocityY)) {
         if (currentPlayer != player || groundedOrFlying || Math.abs(y - previousY) > Math.abs(velocityY) * 2.0 + 1.0) {
            player = currentPlayer;
            peakY = y;
            distance = 0.0;
         } else if (!(y >= previousY) && !(velocityY >= 0.0)) {
            distance = Math.max(0.0, peakY - y);
         } else {
            peakY = y;
            distance = 0.0;
         }

         previousY = y;
      } else {
         reset();
      }
   }

   public static double distance(double nativeDistance) {
      return Math.max(Double.isFinite(nativeDistance) ? nativeDistance : 0.0, distance);
   }
}
