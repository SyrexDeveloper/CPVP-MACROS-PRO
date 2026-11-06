package dev.priyme.legitcrystalmacro.gui;

import java.util.ArrayList;
import java.util.List;

public interface GuiGraphicsContext {
   default String getClipboard() {
      return "";
   }

   default void setClipboard(String text) {
   }

   void drawTextHeading(String var1, int var2, int var3, int var4);

   int getTextWidthHeading(String var1);

   void setOpacity(float var1);

   float getOpacity();

   int getFps();

   boolean isDebugVisible();

   void fill(int var1, int var2, int var3, int var4, int var5);

   void drawText(String var1, int var2, int var3, int var4, boolean var5);

   void drawTextBold(String var1, int var2, int var3, int var4, boolean var5);

   void drawTextMedium(String var1, int var2, int var3, int var4, boolean var5);

   void drawTextSmall(String var1, int var2, int var3, int var4, boolean var5);

   void drawTextSmallBold(String var1, int var2, int var3, int var4, boolean var5);

   int getTextWidth(String var1);

   int getTextWidthBold(String var1);

   int getTextWidthMedium(String var1);

   int getTextWidthSmall(String var1);

   int getTextWidthSmallBold(String var1);

   List<String> wrapText(String var1, int var2);

   default List<String> wrapTextSmall(String text, int width) {
      ArrayList<String> lines = new ArrayList<>();
      StringBuilder line = new StringBuilder();

      for (String word : text.split(" ")) {
         String candidate = line.isEmpty() ? word : line + " " + word;
         if (!line.isEmpty() && this.getTextWidthSmall(candidate) > width) {
            lines.add(line.toString());
            line.setLength(0);
         }

         if (!line.isEmpty()) {
            line.append(' ');
         }

         line.append(word);
      }

      if (!line.isEmpty()) {
         lines.add(line.toString());
      }

      return List.copyOf(lines);
   }

   default String trimToWidth(String text, int maxWidth) {
      if (text != null && maxWidth > 0) {
         if (this.getTextWidth(text) <= maxWidth) {
            return text;
         } else {
            int low = 0;
            int high = text.length();
            int best = 0;

            while (low <= high) {
               int mid = low + high >>> 1;
               if (this.getTextWidth(text.substring(0, mid)) <= maxWidth) {
                  best = mid;
                  low = mid + 1;
               } else {
                  high = mid - 1;
               }
            }

            return text.substring(0, best);
         }
      } else {
         return "";
      }
   }

   default String trimToWidthSmall(String text, int maxWidth) {
      if (text != null && maxWidth > 0) {
         if (this.getTextWidthSmall(text) <= maxWidth) {
            return text;
         } else {
            int low = 0;
            int high = text.length();
            int best = 0;

            while (low <= high) {
               int mid = low + high >>> 1;
               if (this.getTextWidthSmall(text.substring(0, mid)) <= maxWidth) {
                  best = mid;
                  low = mid + 1;
               } else {
                  high = mid - 1;
               }
            }

            return text.substring(0, best);
         }
      } else {
         return "";
      }
   }

   void enableScissor(int var1, int var2, int var3, int var4);

   void disableScissor();

   void pushMatrix();

   void scaleMatrix(float var1, float var2);

   void translateMatrix(float var1, float var2);

   void popMatrix();

   default void nextLayer() {
   }

   float getScaleFactor();

   void drawNineSlice(NineSliceSprite var1, int var2, int var3, int var4, int var5, int var6);

   void drawIcon(IconId var1, int var2, int var3, int var4, int var5);

   void drawIcon(String var1, int var2, int var3, int var4);

   void drawLogo(int var1, int var2, int var3);

   boolean drawPlayerAvatar(int var1, int var2, int var3);

   String getSessionPlayerName();

   int getFillCalls();

   int getTextureCalls();

   int getDrawTextCalls();

   int getMatrixPushCalls();

   void resetMetrics();
}
