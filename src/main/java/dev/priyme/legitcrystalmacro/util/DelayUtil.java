package dev.priyme.legitcrystalmacro.util;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import java.util.concurrent.ThreadLocalRandom;

public final class DelayUtil {
   private DelayUtil() {
   }

   public static int withRandomness(int baseDelay, MacroConfig config) {
      baseDelay = Math.max(0, baseDelay);
      if (config != null && config.randomizeDelays && baseDelay != 0) {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         if (config.humanizerGaussian) {
            double stdDev = config.randomizationRange / 2.0;
            int jitter = (int)Math.round(random.nextGaussian() * stdDev);
            jitter = Math.max(-config.randomizationRange, Math.min(config.randomizationRange, jitter));
            return Math.max(0, baseDelay + jitter);
         } else {
            int jitter = random.nextInt(-config.randomizationRange, config.randomizationRange + 1);
            return Math.max(0, baseDelay + jitter);
         }
      } else {
         return baseDelay;
      }
   }
}
