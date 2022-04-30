package util;
import com.intellij.ui.JBColor;
import java.awt.Color;
public enum Colors{
    BACKGROUND_COLOR(new JBColor(0xFDFEFF, 0x292B2D)),
    UN_HIGHLIGHTED_COLOR(new JBColor(0xC6C8CA,0x585A5C)),
    NEUTRAL_COLOR(new JBColor(0x626466, 0x949698)),
    HIGHLIGHTED_COLOR(new JBColor(0x4285F4, 0x589DEF)),
    HIGHLIGHTED_BACKGROUND_COLOR(new JBColor(0xFFFF00, 0xFFFF00)),
    UPSTREAM_COLOR(new JBColor(0xFBBC05, 0xBE9117)),
    DOWNSTREAM_COLOR(new JBColor(0x34A853, 0x538863)),
    DEEP_BLUE(new JBColor(0x0000FF, 0x0000FF)),
    BLUE(new JBColor(0x0088FF, 0x0088FF)),
    LIGHT_BLUE(new JBColor(0x00FFFF, 0x00FFFF)),
    CYAN(new JBColor(0x00FF88, 0x00FF88)),
    GREEN(new JBColor(0x00FF00, 0x00FF00)),
    LIGHT_GREEN(new JBColor(0x88FF00, 0x88FF00)),
    YELLOW(new JBColor(0xFFFF00, 0xFFFF00)),
    LIGHT_ORANGE(new JBColor(0xFFAA00,0xFFAA00)),
    ORANGE(new JBColor(0xFF6600, 0xFF6600)),
    RED(new JBColor(0xFF0000, 0xFF0000));
    public  Color color;
    Colors(Color color) {
        this.color = color;
    }

}
