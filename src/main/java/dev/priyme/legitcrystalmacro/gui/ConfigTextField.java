package dev.priyme.legitcrystalmacro.gui;

final class ConfigTextField {
   String value = "";
   boolean focused;
   private int cursor;
   private int anchor;
   private int start;
   private final int limit;

   ConfigTextField(int limit) {
      this.limit = limit;
   }

   void set(String text) {
      this.value = text;
      this.cursor = this.anchor = text.length();
      this.start = 0;
   }

   void selectAll() {
      this.anchor = 0;
      this.cursor = this.value.length();
   }

   private void insert(String text) {
      String cleaned = text.replaceAll("[\\p{Cntrl}]", "");
      int lo = Math.min(this.cursor, this.anchor);
      int hi = Math.max(this.cursor, this.anchor);
      int available = Math.max(0, this.limit - (this.value.length() - hi + lo));
      int end = Math.min(cleaned.length(), available);
      if (end > 0 && end < cleaned.length() && Character.isHighSurrogate(cleaned.charAt(end - 1))) {
         end--;
      }

      this.value = this.value.substring(0, lo) + cleaned.substring(0, end) + this.value.substring(hi);
      this.cursor = this.anchor = lo + end;
   }

   boolean character(char chr) {
      if (this.focused && chr >= ' ' && chr != 127) {
         this.insert(Character.toString(chr));
         return true;
      } else {
         return false;
      }
   }

   boolean key(int key, int mods, GuiGraphicsContext context) {
      if (!this.focused) {
         return false;
      } else {
         boolean control = (mods & 2) != 0;
         boolean shift = (mods & 1) != 0;
         int lo = Math.min(this.cursor, this.anchor);
         int hi = Math.max(this.cursor, this.anchor);
         if (control && key == 65) {
            this.selectAll();
            return true;
         } else if (!control || key != 67 && key != 88) {
            if (control && key == 86) {
               this.insert(context.getClipboard());
               return true;
            } else if (key != 259 && key != 261) {
               if (key == 268) {
                  this.cursor = 0;
               } else if (key == 269) {
                  this.cursor = this.value.length();
               } else if (key == 263) {
                  this.cursor = !shift && lo != hi ? lo : this.value.offsetByCodePoints(this.cursor, this.cursor > 0 ? -1 : 0);
               } else {
                  if (key != 262) {
                     return false;
                  }

                  this.cursor = !shift && lo != hi ? hi : this.value.offsetByCodePoints(this.cursor, this.cursor < this.value.length() ? 1 : 0);
               }

               if (!shift) {
                  this.anchor = this.cursor;
               }

               return true;
            } else {
               if (lo == hi) {
                  if (key == 259 && lo > 0) {
                     this.anchor = this.value.offsetByCodePoints(lo, -1);
                  }

                  if (key == 261 && hi < this.value.length()) {
                     this.anchor = this.value.offsetByCodePoints(hi, 1);
                  }
               }

               this.insert("");
               return true;
            }
         } else {
            context.setClipboard(this.value.substring(lo, hi));
            if (key == 88) {
               this.insert("");
            }

            return true;
         }
      }
   }

   void draw(GuiGraphicsContext c, int x, int y, int w, String placeholder) {
      c.drawNineSlice(NineSliceSprite.SEARCH, x, y, w, 26, Theme.SEARCH_BG);
      if (this.focused) {
         c.drawNineSlice(NineSliceSprite.BORDER_SEARCH, x, y, w, 26, Theme.ACCENT);
      }

      this.start = Math.min(this.start, this.cursor);

      while (this.start < this.cursor && c.getTextWidthSmall(this.value.substring(this.start, this.cursor)) > w - 20) {
         this.start = this.value.offsetByCodePoints(this.start, 1);
      }

      c.enableScissor(x + 7, y + 3, w - 14, 21);
      if (this.focused && this.cursor != this.anchor) {
         int lo = Math.max(this.start, Math.min(this.cursor, this.anchor));
         int hi = Math.max(this.start, Math.max(this.cursor, this.anchor));
         int left = c.getTextWidthSmall(this.value.substring(this.start, lo));
         c.fill(x + 8 + left, y + 5, c.getTextWidthSmall(this.value.substring(lo, hi)), 16, Theme.ACCENT_MUTED);
      }

      c.drawTextSmall(
         this.value.isEmpty() ? placeholder : this.value.substring(this.start),
         x + 8,
         y + 8,
         this.value.isEmpty() ? Theme.TEXT_MUTED : Theme.TEXT_PRIMARY,
         false
      );
      if (this.focused && System.nanoTime() / 500000000L % 2L == 0L) {
         c.fill(x + 8 + c.getTextWidthSmall(this.value.substring(this.start, this.cursor)), y + 6, 1, 14, Theme.TEXT_PRIMARY);
      }

      c.disableScissor();
   }
}
