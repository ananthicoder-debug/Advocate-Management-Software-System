package com.ams.util;

import java.awt.*;

/**
 * Global Theme / Colour Palette for the AMS
 * Bright, professional legal-themed UI
 */
public class AMSTheme {

    // ── Primary Palette ───────────────────────────────────────────────────────
    public static final Color PRIMARY_DARK   = new Color(0x0A2540);   // deep navy
    public static final Color PRIMARY        = new Color(0x1A4B8C);   // royal blue
    public static final Color PRIMARY_LIGHT  = new Color(0x2D6CCF);   // bright blue
    public static final Color ACCENT         = new Color(0xF0A500);   // gold / amber
    public static final Color ACCENT_LIGHT   = new Color(0xFFD166);   // light gold

    // ── Background / Surface ──────────────────────────────────────────────────
    public static final Color BG_MAIN        = new Color(0xF4F7FC);   // soft white-blue
    public static final Color BG_CARD        = new Color(0xFFFFFF);   // pure white card
    public static final Color BG_SIDEBAR     = new Color(0x0A2540);   // dark navy sidebar
    public static final Color BG_HEADER      = new Color(0x1A4B8C);   // blue header
    public static final Color BG_INPUT       = new Color(0xF0F4FF);   // input field bg

    // ── Text ──────────────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY   = new Color(0x1A1A2E);   // near black
    public static final Color TEXT_SECONDARY = new Color(0x4A5568);   // gray
    public static final Color TEXT_LIGHT     = new Color(0xFFFFFF);   // white
    public static final Color TEXT_MUTED     = new Color(0x8898AA);   // muted

    // ── Status ────────────────────────────────────────────────────────────────
    public static final Color SUCCESS        = new Color(0x27AE60);   // green
    public static final Color WARNING        = new Color(0xF39C12);   // orange
    public static final Color DANGER         = new Color(0xE74C3C);   // red
    public static final Color INFO           = new Color(0x3498DB);   // sky blue
    public static final Color PENDING        = new Color(0x9B59B6);   // purple

    // ── Card Gradients (role colours) ─────────────────────────────────────────
    public static final Color CARD_BLUE_1    = new Color(0x1A4B8C);
    public static final Color CARD_BLUE_2    = new Color(0x2D6CCF);
    public static final Color CARD_GREEN_1   = new Color(0x0E6655);
    public static final Color CARD_GREEN_2   = new Color(0x1ABC9C);
    public static final Color CARD_GOLD_1    = new Color(0xB7770D);
    public static final Color CARD_GOLD_2    = new Color(0xF0A500);
    public static final Color CARD_RED_1     = new Color(0x922B21);
    public static final Color CARD_RED_2     = new Color(0xE74C3C);
    public static final Color CARD_PURPLE_1  = new Color(0x6C3483);
    public static final Color CARD_PURPLE_2  = new Color(0x9B59B6);
    public static final Color CARD_TEAL_1    = new Color(0x0D7377);
    public static final Color CARD_TEAL_2    = new Color(0x14A085);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final Font  FONT_TITLE     = new Font("Segoe UI", Font.BOLD,   28);
    public static final Font  FONT_SUBTITLE  = new Font("Segoe UI", Font.BOLD,   18);
    public static final Font  FONT_HEADING   = new Font("Segoe UI", Font.BOLD,   16);
    public static final Font  FONT_BODY      = new Font("Segoe UI", Font.PLAIN,  14);
    public static final Font  FONT_SMALL     = new Font("Segoe UI", Font.PLAIN,  12);
    public static final Font  FONT_BOLD      = new Font("Segoe UI", Font.BOLD,   14);
    public static final Font  FONT_CARD_TITLE= new Font("Segoe UI", Font.BOLD,   15);
    public static final Font  FONT_CARD_VALUE= new Font("Segoe UI", Font.BOLD,   32);
    public static final Font  FONT_NAV       = new Font("Segoe UI", Font.PLAIN,  13);

    // ── Dimensions ────────────────────────────────────────────────────────────
    public static final int   SIDEBAR_W      = 220;
    public static final int   TOPBAR_H       = 65;
    public static final int   CARD_RADIUS    = 16;
    public static final int   BTN_RADIUS     = 10;
    public static final int   FIELD_H        = 42;
}
