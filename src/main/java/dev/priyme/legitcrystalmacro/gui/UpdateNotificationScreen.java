package dev.priyme.legitcrystalmacro.gui;

import dev.priyme.legitcrystalmacro.util.KeyInput;
import dev.priyme.legitcrystalmacro.util.UpdateTracker;

public class UpdateNotificationScreen extends CompatScreen {
   private final Object parent;

   public UpdateNotificationScreen(Object parent) {
      this.parent = parent;
   }

   @Override
   public void closeScreen() {
      KeyInput.returnToScreen(this.parent);
   }

   private static boolean in(double mx, double my, int x, int y, int w, int h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }

   @Override
   public void renderContent(GuiGraphicsContext context, int mouseX, int mouseY, float delta) {
      int sw = this.getScreenWidth();
      int sh = this.getScreenHeight();
      context.fill(0, 0, sw, sh, -1442315254);
      int cardW = 340;
      int cardH = 200;
      int cardX = (sw - cardW) / 2;
      int cardY = (sh - cardH) / 2;
      GuiCompat.drawDropShadow(context, cardX, cardY, cardW, cardH, 12, 16);
      GuiCompat.drawRoundedRectWithBorder(context, cardX, cardY, cardW, cardH, 12, 1, Theme.ACCENT, Theme.CARD_BG);
      GuiCompat.drawRoundedRect(context, cardX + 2, cardY + 2, cardW - 4, 3, 2, Theme.ACCENT);
      context.drawLogo(cardX + 16, cardY + 16, 24);
      context.drawTextHeading("CPvP Macros", cardX + 46, cardY + 12, Theme.TEXT_PRIMARY);
      int closeX = cardX + cardW - 24;
      int closeY = cardY + 14;
      boolean closeHover = in(mouseX, mouseY, closeX - 4, closeY - 2, 20, 20);
      context.drawTextBold("✕", closeX, closeY, closeHover ? Theme.ACCENT : Theme.TEXT_MUTED, false);
      GuiCompat.drawRoundedRect(context, cardX + 150, cardY + 17, 114, 16, 4, Theme.ACCENT_MUTED);
      context.drawTextSmallBold("UPDATE AVAILABLE", cardX + 156, cardY + 17, Theme.ACCENT, false);
      context.drawTextMedium("A new version is available!", cardX + 18, cardY + 54, Theme.TEXT_PRIMARY, false);
      GuiCompat.drawRoundedRect(context, cardX + 18, cardY + 74, 145, 28, 6, Theme.BUTTON_BG);
      context.drawTextSmall("Installed:", cardX + 26, cardY + 76, Theme.TEXT_MUTED, false);
      context.drawTextBold("v3.6", cardX + 26, cardY + 88, Theme.TEXT_SECONDARY, false);
      GuiCompat.drawRoundedRect(context, cardX + 175, cardY + 74, 147, 28, 6, 858302277);
      context.drawTextSmall("Latest Version:", cardX + 183, cardY + 76, -7807608, false);
      context.drawTextBold("v" + UpdateTracker.getLatestVersion(), cardX + 183, cardY + 88, -12255420, false);
      context.drawTextSmall("Click the button below to open Modrinth and download", cardX + 18, cardY + 112, Theme.TEXT_MUTED, false);
      context.drawTextSmall("the latest update directly in your browser.", cardX + 18, cardY + 124, Theme.TEXT_MUTED, false);
      int btn1X = cardX + 18;
      int btn1Y = cardY + 150;
      int btn1W = 180;
      int btn1H = 30;
      boolean btn1Hover = in(mouseX, mouseY, btn1X, btn1Y, btn1W, btn1H);
      GuiCompat.drawRoundedRectWithBorder(
         context, btn1X, btn1Y, btn1W, btn1H, 8, 1, btn1Hover ? -1 : Theme.ACCENT, btn1Hover ? Theme.ACCENT_HOVER : Theme.ACCENT
      );
      context.drawTextBold("⬇  Download on Modrinth", btn1X + 12, btn1Y + 9, -1, false);
      int btn2X = cardX + 208;
      int btn2Y = cardY + 150;
      int btn2W = 114;
      int btn2H = 30;
      boolean btn2Hover = in(mouseX, mouseY, btn2X, btn2Y, btn2W, btn2H);
      GuiCompat.drawRoundedRectWithBorder(
         context, btn2X, btn2Y, btn2W, btn2H, 8, 1, btn2Hover ? Theme.ACCENT : 1157627903, btn2Hover ? Theme.BUTTON_HOVER_BG : Theme.BUTTON_BG
      );
      context.drawTextMedium("Later", btn2X + 42, btn2Y + 9, btn2Hover ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY, false);
   }

   @Override
   public boolean onMouseClicked(int mouseX, int mouseY, int button) {
      if (button != 0) {
         return false;
      } else {
         int sw = this.getScreenWidth();
         int sh = this.getScreenHeight();
         int cardW = 340;
         int cardH = 200;
         int cardX = (sw - cardW) / 2;
         int cardY = (sh - cardH) / 2;
         int closeX = cardX + cardW - 24;
         int closeY = cardY + 14;
         if (in(mouseX, mouseY, closeX - 4, closeY - 2, 20, 20)) {
            this.closeScreen();
            return true;
         } else {
            int btn1X = cardX + 18;
            int btn1Y = cardY + 150;
            int btn1W = 180;
            int btn1H = 30;
            if (in(mouseX, mouseY, btn1X, btn1Y, btn1W, btn1H)) {
               UpdateTracker.openModrinth();
               return true;
            } else {
               int btn2X = cardX + 208;
               int btn2Y = cardY + 150;
               int btn2W = 114;
               int btn2H = 30;
               if (in(mouseX, mouseY, btn2X, btn2Y, btn2W, btn2H)) {
                  this.closeScreen();
                  return true;
               } else {
                  return in(mouseX, mouseY, cardX, cardY, cardW, cardH);
               }
            }
         }
      }
   }

   @Override
   public boolean onMouseReleased(int mouseX, int mouseY, int button) {
      return false;
   }

   @Override
   public boolean onMouseDragged(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      return false;
   }

   @Override
   public boolean onMouseScrolled(int mouseX, int mouseY, double amountX, double amountY) {
      return false;
   }

   @Override
   public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode != 256 && keyCode != 257) {
         return false;
      } else {
         this.closeScreen();
         return true;
      }
   }

   @Override
   public boolean onCharTyped(char chr) {
      return false;
   }
}
