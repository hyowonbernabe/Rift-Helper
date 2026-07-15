package view;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.Icon;
import java.awt.Color;

/** Vector icons from the FontAwesome pack (via Ikonli), recolorable and crisp at any size. */
public final class Icons {
    public enum G {
        LOBBY, RIFT, ARAM, ARENA, LOOT, SETTINGS, INFO,
        ACCEPT, DECLINE, SWAP, REROLL, BAN, LOCK, BRAVERY,
        PLUS, MINUS, SAVE, IMPORT, EXPORT, RESET, TRAY, PIN, UPDATE, TEST, CROWD, LANE, BELL,
        TOP, JUNGLE, MID, BOT, SUPPORT
    }

    private Icons() {
    }

    public static Icon of(G glyph, int size, Color color) {
        return FontIcon.of(map(glyph), size, color);
    }

    private static FontAwesomeSolid map(G glyph) {
        return switch (glyph) {
            case LOBBY -> FontAwesomeSolid.USERS;
            case RIFT -> FontAwesomeSolid.MAP_MARKED_ALT;
            case ARAM -> FontAwesomeSolid.SNOWFLAKE;
            case ARENA -> FontAwesomeSolid.TROPHY;
            case LOOT -> FontAwesomeSolid.BOX_OPEN;
            case SETTINGS -> FontAwesomeSolid.COG;
            case INFO -> FontAwesomeSolid.INFO_CIRCLE;
            case ACCEPT -> FontAwesomeSolid.CHECK;
            case DECLINE -> FontAwesomeSolid.TIMES;
            case SWAP -> FontAwesomeSolid.EXCHANGE_ALT;
            case REROLL -> FontAwesomeSolid.RANDOM;
            case BAN -> FontAwesomeSolid.BAN;
            case LOCK -> FontAwesomeSolid.LOCK;
            case BRAVERY -> FontAwesomeSolid.DICE;
            case PLUS -> FontAwesomeSolid.PLUS;
            case MINUS -> FontAwesomeSolid.MINUS;
            case SAVE -> FontAwesomeSolid.SAVE;
            case IMPORT -> FontAwesomeSolid.FILE_IMPORT;
            case EXPORT -> FontAwesomeSolid.FILE_EXPORT;
            case RESET -> FontAwesomeSolid.UNDO;
            case TRAY -> FontAwesomeSolid.WINDOW_MINIMIZE;
            case PIN -> FontAwesomeSolid.THUMBTACK;
            case UPDATE -> FontAwesomeSolid.SYNC;
            case TEST -> FontAwesomeSolid.FLASK;
            case CROWD -> FontAwesomeSolid.HEART;
            case LANE -> FontAwesomeSolid.ROAD;
            case BELL -> FontAwesomeSolid.BELL;
            case TOP -> FontAwesomeSolid.CHESS_ROOK;
            case JUNGLE -> FontAwesomeSolid.TREE;
            case MID -> FontAwesomeSolid.HAT_WIZARD;
            case BOT -> FontAwesomeSolid.CROSSHAIRS;
            case SUPPORT -> FontAwesomeSolid.HANDS_HELPING;
        };
    }
}
