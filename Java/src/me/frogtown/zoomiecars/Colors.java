package me.frogtown.zoomiecars;

public class Colors{
    public static char GetColor(int r, int g, int b){
        return (char)(16 + (36 * r) + (6 * g) + b);
    }

    public static char skyBlue     = GetColor(2, 3, 5);
    public static char grassGreen  = GetColor(2, 5, 2);
    public static char black       = GetColor(0, 0, 0);
    public static char white       = GetColor(5, 5, 5);
    public static char salmon      = GetColor(5, 2, 2);
    public static char offGray     = GetColor(1, 1, 2);
    public static char asphaltGray = GetColor(3, 3, 3);
    public static char green       = GetColor(2, 5, 2);
    public static char softPink    = GetColor(4, 2, 2);
    public static char pink        = GetColor(5, 2, 2);

    public static char noReplacement = 0;
}