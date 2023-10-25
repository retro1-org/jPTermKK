package jpterm;

import java.awt.event.KeyEvent;

/**
 * This code was copied from the pterm and pterm221
 * programs, which are copyright Tom Hunter and Paul Koning.
 * 
 * @author Ken Kachnowich
 * Created on May 8, 2005
 * Version $Id$
 * License: See file license.txt for license details
 *
 */
public class PlatoSpecific {

    /* Note that we have valid entries (entry != -1) here only for those keys
    ** where we do not need to do anything unusual for Shift.  For example,
    ** "space" is not decoded here because Shift-Space gets a different code.
    ** However, that doesn't apply to control keystrokes; those are shown
    ** here even though shift affects them in the PLATO/Portal key conventions.
    */
	private static final int[] asciiToPlato =
	{
	 /*                                                                         */
	 /* 000- */ -1,    022,    030,    033,    031,    027,    064,    013,
	 /*                                                                         */
	 /* 010- */-1,     -1,     -1,     -1,    035,    024,     -1,     -1,
	 /*                                                                         */
	 /* 020- */020,    034,     -1,    032,    062,     -1,     -1,     -1,
	 /*                                                                         */
	 /* 030- */012,    021,     -1,     -1,     -1,     -1,     -1,     -1,
	 /*          space   !       "       #       $       %       &       '      */
	 /* 040- */ -1,      0176,   0177, 074044,   0044,   0045, 074016,   0047,
	 /*          (       )       *       +       ,       -       .       /      */
	 /* 050- */  0051,   0173,   0050,   0016,   0137,   0017,   0136,   0135,
	 /*          0       1       2       3       4       5       6       7      */
	 /* 060- */  0000,   0001,   0002,   0003,   0004,   0005,   0006,   0007,
	 /*          8       9       :       ;       <       =       >       ?      */
	 /* 070- */  0010,   0011,   0174,   0134,   0040,   0133,   0041,   0175,
	 /*          @       A       B       C       D       E       F       G      */
	 /* 100- */ 074005,  0141,   0142,   0143,   0144,   0145,   0146,   0147,
	 /*          H       I       J       K       L       M       N       O      */
	 /* 110- */  0150,   0151,   0152,   0153,   0154,   0155,   0156,   0157,
	 /*          P       Q       R       S       T       U       V       W      */
	 /* 120- */  0160,   0161,   0162,   0163,   0164,   0165,   0166,   0167,
	 /*          X       Y       Z       [       \       ]       ^       _      */
	 /* 130- */  0170,   0171,   0172,   0042, 074135,   0043, 074130,   0046,
	 /*          `       a       b       c       d       e       f       g      */
	 /* 140- */ 074121,  0101,   0102,   0103,   0104,   0105,   0106,   0107,
	 /*          h       i       j       k       l       m       n       o      */
	 /* 150- */  0110,   0111,   0112,   0113,   0114,   0115,   0116,   0117,
	 /*          p       q       r       s       t       u       v       w      */
	 /* 160- */  0120,   0121,   0122,   0123,   0124,   0125,   0126,   0127,
	 /*          x       y       z       {       |       }       ~              */
	 /* 170- */  0130,   0131,   0132, 074042, 074151, 074043, 074116,     -1
	};

	private static final int[] altKeyToPlato =
	{
	 /*                                                                         */
	 /* 000- */ -1,     -1,     -1,     -1,     -1,     -1,     -1,     -1,
	 /*                                                                         */
	 /* 010- */ -1,     -1,     -1,     -1,     -1,     -1,     -1,     -1,
	 /*                                                                         */
	 /* 020- */ -1,     -1,     -1,     -1,     -1,     -1,     -1,     -1,
	 /*                                                                         */
	 /* 030- */ -1,     -1,     -1,     -1,     -1,     -1,     -1,     -1,
	 /*          space   !       "       #       $       %       &       '      */
	 /* 040- */ -1,     -1,     -1,     -1,     -1,     -1,     -1,     -1,
	 /*          (       )       *       +       ,       -       .       /      */
	 /* 050- */ -1,     -1,     -1,     -1,     -1,     -1,     -1,     -1,
	 /*          0       1       2       3       4       5       6       7      */
	 /* 060- */ -1,     -1,     -1,     -1,     -1,     -1,     -1,     -1,
	 /*          8       9       :       ;       <       =       >       ?      */
	 /* 070- */ -1,     -1,     -1,     -1,     -1,      0015,  -1,     -1,
	 /*          @       A       B       C       D       E       F       G      */
	 /* 100- */ -1,      0062,   0070,   0073,   0071,   0067,   0064,  -1,
	 /*          H       I       J       K       L       M       N       O      */
	 /* 110- */  0065,  -1,     -1,     -1,      0075,   0064,   0066,  -1,
	 /*          P       Q       R       S       T       U       V       W      */
	 /* 120- */ -1,      0074,   0063,   0072,   0062,  -1,     -1,     -1,
	 /*          X       Y       Z       [       \       ]       ^       _      */
	 /* 130- */ -1,     -1,     -1,     -1,     -1,     -1,     -1,     -1,
	 /*          `       a       b       c       d       e       f       g      */
	 /* 140- */ -1,      0022,   0030,   0033,   0031,   0027,   0064,  -1,
	 /*          h       i       j       k       l       m       n       o      */
	 /* 150- */  0025,  -1,     -1,     -1,      0035,   0024,   0026,  -1,
	 /*          p       q       r       s       t       u       v       w      */
	 /* 160- */ -1,      0034,   0023,   0032,   0062,  -1,     -1,     -1,
	 /*          x       y       z       {       |       }       ~              */
	 /* 170- */ -1,     -1,     -1,     -1,     -1,     -1,     -1,     -1
	};

	/** Used for debug display */
	public final static char[][] romChar = { { ':', 'a', 'b', 'c', 'd', 'e', 'f',
			'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
			't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', '+', '-', '*', '/', '(', ')', '$', '=', ' ',
			',', '.', '/', '[', ']', '%', ' ', ' ', '\'', '\"', '!', ';',
			'<', '>', '-', '?', ' ', 'U' },
			{':', 'A', 'B', 'C', 'D', 'E', 'F',
			 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
			 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '~', ' ', ' ', ' ', ' ', ' ',
			 ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			 ',', '.', '/', '[', ']', '%', ' ', ' ', '\'', '\"', '!', ';',
			 '<', '>', '-', '?', ' ', 'U' } };
	
	
    /* data for plato font, set 0. */
    public static final int[] plato_m0 = {
        0x0000, 0x0000, 0x0330, 0x0330, 0x0000, 0x0000, 0x0000, 0x0000, // :
        0x0060, 0x0290, 0x0290, 0x0290, 0x0290, 0x01e0, 0x0010, 0x0000, // a
        0x1ff0, 0x0120, 0x0210, 0x0210, 0x0210, 0x0120, 0x00c0, 0x0000, // b
        0x00c0, 0x0120, 0x0210, 0x0210, 0x0210, 0x0210, 0x0120, 0x0000, // c
        0x00c0, 0x0120, 0x0210, 0x0210, 0x0210, 0x0120, 0x1ff0, 0x0000, // d
        0x00c0, 0x01a0, 0x0290, 0x0290, 0x0290, 0x0290, 0x0190, 0x0000, // e
        0x0000, 0x0000, 0x0210, 0x0ff0, 0x1210, 0x1000, 0x0800, 0x0000, // f
        0x01a8, 0x0254, 0x0254, 0x0254, 0x0254, 0x0194, 0x0208, 0x0000, // g
        0x1000, 0x1ff0, 0x0100, 0x0200, 0x0200, 0x0200, 0x01f0, 0x0000, // h
        0x0000, 0x0000, 0x0210, 0x13f0, 0x0010, 0x0000, 0x0000, 0x0000, // i
        0x0000, 0x0002, 0x0202, 0x13fc, 0x0000, 0x0000, 0x0000, 0x0000, // j
        0x1010, 0x1ff0, 0x0080, 0x0140, 0x0220, 0x0210, 0x0010, 0x0000, // k
        0x0000, 0x0000, 0x1010, 0x1ff0, 0x0010, 0x0000, 0x0000, 0x0000, // l
        0x03f0, 0x0200, 0x0200, 0x01f0, 0x0200, 0x0200, 0x01f0, 0x0000, // m
        0x0200, 0x03f0, 0x0100, 0x0200, 0x0200, 0x0200, 0x01f0, 0x0000, // n
        0x00c0, 0x0120, 0x0210, 0x0210, 0x0210, 0x0120, 0x00c0, 0x0000, // o
        0x03fe, 0x0120, 0x0210, 0x0210, 0x0210, 0x0120, 0x00c0, 0x0000, // p
        0x00c0, 0x0120, 0x0210, 0x0210, 0x0210, 0x0120, 0x03fe, 0x0000, // q
        0x0200, 0x03f0, 0x0100, 0x0200, 0x0200, 0x0200, 0x0100, 0x0000, // r
        0x0120, 0x0290, 0x0290, 0x0290, 0x0290, 0x0290, 0x0060, 0x0000, // s
        0x0200, 0x0200, 0x1fe0, 0x0210, 0x0210, 0x0210, 0x0000, 0x0000, // t
        0x03e0, 0x0010, 0x0010, 0x0010, 0x0010, 0x03e0, 0x0010, 0x0000, // u
        0x0200, 0x0300, 0x00c0, 0x0030, 0x00c0, 0x0300, 0x0200, 0x0000, // v
        0x03e0, 0x0010, 0x0020, 0x01c0, 0x0020, 0x0010, 0x03e0, 0x0000, // w
        0x0200, 0x0210, 0x0120, 0x00c0, 0x00c0, 0x0120, 0x0210, 0x0000, // x
        0x0382, 0x0044, 0x0028, 0x0010, 0x0020, 0x0040, 0x0380, 0x0000, // y
        0x0310, 0x0230, 0x0250, 0x0290, 0x0310, 0x0230, 0x0000, 0x0000, // z
        0x0010, 0x07e0, 0x0850, 0x0990, 0x0a10, 0x07e0, 0x0800, 0x0000, // 0
        0x0000, 0x0000, 0x0410, 0x0ff0, 0x0010, 0x0000, 0x0000, 0x0000, // 1
        0x0000, 0x0430, 0x0850, 0x0890, 0x0910, 0x0610, 0x0000, 0x0000, // 2
        0x0000, 0x0420, 0x0810, 0x0910, 0x0910, 0x06e0, 0x0000, 0x0000, // 3
        0x0000, 0x0080, 0x0180, 0x0280, 0x0480, 0x0ff0, 0x0080, 0x0000, // 4
        0x0000, 0x0f10, 0x0910, 0x0910, 0x0920, 0x08c0, 0x0000, 0x0000, // 5
        0x0000, 0x03e0, 0x0510, 0x0910, 0x0910, 0x00e0, 0x0000, 0x0000, // 6
        0x0000, 0x0800, 0x0830, 0x08c0, 0x0b00, 0x0c00, 0x0000, 0x0000, // 7
        0x0000, 0x06e0, 0x0910, 0x0910, 0x0910, 0x06e0, 0x0000, 0x0000, // 8
        0x0000, 0x0700, 0x0890, 0x0890, 0x08a0, 0x07c0, 0x0000, 0x0000, // 9
        0x0000, 0x0080, 0x0080, 0x03e0, 0x0080, 0x0080, 0x0000, 0x0000, // +
        0x0000, 0x0080, 0x0080, 0x0080, 0x0080, 0x0080, 0x0000, 0x0000, // -
        0x0000, 0x0240, 0x0180, 0x0660, 0x0180, 0x0240, 0x0000, 0x0000, // *
        0x0010, 0x0020, 0x0040, 0x0080, 0x0100, 0x0200, 0x0400, 0x0000, // /
        0x0000, 0x0000, 0x0000, 0x0000, 0x07e0, 0x0810, 0x1008, 0x0000, // (
        0x1008, 0x0810, 0x07e0, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, // )
        0x0640, 0x0920, 0x0920, 0x1ff0, 0x0920, 0x0920, 0x04c0, 0x0000, // $
        0x0000, 0x0140, 0x0140, 0x0140, 0x0140, 0x0140, 0x0000, 0x0000, // =
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, // space
        0x0000, 0x0000, 0x0034, 0x0038, 0x0000, 0x0000, 0x0000, 0x0000, // ,
        0x0000, 0x0000, 0x0030, 0x0030, 0x0000, 0x0000, 0x0000, 0x0000, // .
        0x0000, 0x0080, 0x0080, 0x02a0, 0x0080, 0x0080, 0x0000, 0x0000, // divide
        0x0000, 0x0000, 0x0000, 0x0000, 0x1ff8, 0x1008, 0x1008, 0x0000, // [
        0x1008, 0x1008, 0x1ff8, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, // ]
        0x0c20, 0x1240, 0x0c80, 0x0100, 0x0260, 0x0490, 0x0860, 0x0000, // %
        0x0000, 0x0000, 0x0240, 0x0180, 0x0180, 0x0240, 0x0000, 0x0000, // multiply
        0x0080, 0x0140, 0x0220, 0x0770, 0x0140, 0x0140, 0x0140, 0x0000, // assign
        0x0000, 0x0000, 0x0000, 0x1c00, 0x0000, 0x0000, 0x0000, 0x0000, // '
        0x0000, 0x0000, 0x1c00, 0x0000, 0x1c00, 0x0000, 0x0000, 0x0000, // "
        0x0000, 0x0000, 0x0000, 0x1f90, 0x0000, 0x0000, 0x0000, 0x0000, // !
        0x0000, 0x0000, 0x0334, 0x0338, 0x0000, 0x0000, 0x0000, 0x0000, // ;
        0x0000, 0x0080, 0x0140, 0x0220, 0x0410, 0x0000, 0x0000, 0x0000, // <
        0x0000, 0x0000, 0x0410, 0x0220, 0x0140, 0x0080, 0x0000, 0x0000, // >
        0x0004, 0x0004, 0x0004, 0x0004, 0x0004, 0x0004, 0x0004, 0x0004, // _
        0x0000, 0x0c00, 0x1000, 0x10d0, 0x1100, 0x0e00, 0x0000, 0x0000, // ?
        0x1c1c, 0x1224, 0x0948, 0x0490, 0x0220, 0x0140, 0x0080, 0x0000, // arrow
        0x0000, 0x0000, 0x0000, 0x000a, 0x0006, 0x0000, 0x0000, 0x0000, // cedilla
    };

    /* data for plato font, set 1. */
    public static final int[] plato_m1 = {
        0x0500, 0x0500, 0x1fc0, 0x0500, 0x1fc0, 0x0500, 0x0500, 0x0000, // #
        0x07f0, 0x0900, 0x1100, 0x1100, 0x1100, 0x0900, 0x07f0, 0x0000, // A
        0x1ff0, 0x1210, 0x1210, 0x1210, 0x1210, 0x0e10, 0x01e0, 0x0000, // B
        0x07c0, 0x0820, 0x1010, 0x1010, 0x1010, 0x1010, 0x0820, 0x0000, // C
        0x1ff0, 0x1010, 0x1010, 0x1010, 0x1010, 0x0820, 0x07c0, 0x0000, // D
        0x1ff0, 0x1110, 0x1110, 0x1110, 0x1010, 0x1010, 0x1010, 0x0000, // E
        0x1ff0, 0x1100, 0x1100, 0x1100, 0x1000, 0x1000, 0x1000, 0x0000, // F
        0x07c0, 0x0820, 0x1010, 0x1010, 0x1090, 0x1090, 0x08e0, 0x0000, // G
        0x1ff0, 0x0100, 0x0100, 0x0100, 0x0100, 0x0100, 0x1ff0, 0x0000, // H
        0x0000, 0x1010, 0x1010, 0x1ff0, 0x1010, 0x1010, 0x0000, 0x0000, // I
        0x0020, 0x0010, 0x1010, 0x1010, 0x1fe0, 0x1000, 0x1000, 0x0000, // J
        0x1ff0, 0x0080, 0x0100, 0x0280, 0x0440, 0x0820, 0x1010, 0x0000, // K
        0x1ff0, 0x0010, 0x0010, 0x0010, 0x0010, 0x0010, 0x0010, 0x0000, // L
        0x1ff0, 0x0800, 0x0400, 0x0200, 0x0400, 0x0800, 0x1ff0, 0x0000, // M
        0x1ff0, 0x0800, 0x0600, 0x0100, 0x00c0, 0x0020, 0x1ff0, 0x0000, // N
        0x07c0, 0x0820, 0x1010, 0x1010, 0x1010, 0x0820, 0x07c0, 0x0000, // O
        0x1ff0, 0x1100, 0x1100, 0x1100, 0x1100, 0x1100, 0x0e00, 0x0000, // P
        0x07c0, 0x0820, 0x1010, 0x1018, 0x1014, 0x0824, 0x07c0, 0x0000, // Q
        0x1ff0, 0x1100, 0x1100, 0x1180, 0x1140, 0x1120, 0x0e10, 0x0000, // R
        0x0e20, 0x1110, 0x1110, 0x1110, 0x1110, 0x1110, 0x08e0, 0x0000, // S
        0x1000, 0x1000, 0x1000, 0x1ff0, 0x1000, 0x1000, 0x1000, 0x0000, // T
        0x1fe0, 0x0010, 0x0010, 0x0010, 0x0010, 0x0010, 0x1fe0, 0x0000, // U
        0x1800, 0x0700, 0x00c0, 0x0030, 0x00c0, 0x0700, 0x1800, 0x0000, // V
        0x1fe0, 0x0010, 0x0020, 0x03c0, 0x0020, 0x0010, 0x1fe0, 0x0000, // W
        0x1830, 0x0440, 0x0280, 0x0100, 0x0280, 0x0440, 0x1830, 0x0000, // X
        0x1800, 0x0400, 0x0200, 0x01f0, 0x0200, 0x0400, 0x1800, 0x0000, // Y
        0x1830, 0x1050, 0x1090, 0x1110, 0x1210, 0x1410, 0x1830, 0x0000, // Z
        0x0000, 0x1000, 0x2000, 0x2000, 0x1000, 0x1000, 0x2000, 0x0000, // ~
        0x0000, 0x0000, 0x1000, 0x0000, 0x1000, 0x0000, 0x0000, 0x0000, // dieresis
        0x0000, 0x1000, 0x2000, 0x4000, 0x2000, 0x1000, 0x0000, 0x0000, // circumflex
        0x0000, 0x0000, 0x0000, 0x1000, 0x2000, 0x4000, 0x0000, 0x0000, // acute
        0x0000, 0x4000, 0x2000, 0x1000, 0x0000, 0x0000, 0x0000, 0x0000, // grave
        0x0000, 0x0100, 0x0300, 0x07f0, 0x0300, 0x0100, 0x0000, 0x0000, // uparrow
        0x0080, 0x0080, 0x0080, 0x0080, 0x03e0, 0x01c0, 0x0080, 0x0000, // rightarrow
        0x0000, 0x0040, 0x0060, 0x07f0, 0x0060, 0x0040, 0x0000, 0x0000, // downarrow
        0x0080, 0x01c0, 0x03e0, 0x0080, 0x0080, 0x0080, 0x0080, 0x0000, // leftarrow
        0x0000, 0x0080, 0x0100, 0x0100, 0x0080, 0x0080, 0x0100, 0x0000, // low tilde
        0x1010, 0x1830, 0x1450, 0x1290, 0x1110, 0x1010, 0x1010, 0x0000, // Sigma
        0x0030, 0x00d0, 0x0310, 0x0c10, 0x0310, 0x00d0, 0x0030, 0x0000, // Delta
        0x0000, 0x0380, 0x0040, 0x0040, 0x0040, 0x0380, 0x0000, 0x0000, // union
        0x0000, 0x01c0, 0x0200, 0x0200, 0x0200, 0x01c0, 0x0000, 0x0000, // intersect
        0x0000, 0x0000, 0x0000, 0x0080, 0x0f78, 0x1004, 0x1004, 0x0000, // {
        0x1004, 0x1004, 0x0f78, 0x0080, 0x0000, 0x0000, 0x0000, 0x0000, // }
        0x00e0, 0x0d10, 0x1310, 0x0c90, 0x0060, 0x0060, 0x0190, 0x0000, // &
        0x0150, 0x0160, 0x0140, 0x01c0, 0x0140, 0x0340, 0x0540, 0x0000, // not equal
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, // space
        0x0000, 0x0000, 0x0000, 0x1ff0, 0x0000, 0x0000, 0x0000, 0x0000, // |
        0x0000, 0x0c00, 0x1200, 0x1200, 0x0c00, 0x0000, 0x0000, 0x0000, // degree
        0x0000, 0x02a0, 0x02a0, 0x02a0, 0x02a0, 0x02a0, 0x0000, 0x0000, // equiv
        0x01e0, 0x0210, 0x0210, 0x01a0, 0x0060, 0x0090, 0x0310, 0x0000, // alpha
        0x0002, 0x03fc, 0x0510, 0x0910, 0x0910, 0x0690, 0x0060, 0x0000, // beta
        0x0000, 0x0ce0, 0x1310, 0x1110, 0x0890, 0x0460, 0x0000, 0x0000, // delta
        0x0000, 0x1030, 0x0cc0, 0x0300, 0x00c0, 0x0030, 0x0000, 0x0000, // lambda
        0x0002, 0x0002, 0x03fc, 0x0010, 0x0010, 0x03e0, 0x0010, 0x0000, // mu
        0x0100, 0x0200, 0x03f0, 0x0200, 0x03f0, 0x0200, 0x0400, 0x0000, // pi
        0x0006, 0x0038, 0x00e0, 0x0110, 0x0210, 0x0220, 0x01c0, 0x0000, // rho
        0x00e0, 0x0110, 0x0210, 0x0310, 0x02e0, 0x0200, 0x0200, 0x0000, // sigma
        0x01e0, 0x0210, 0x0010, 0x00e0, 0x0010, 0x0210, 0x01e0, 0x0000, // omega
        0x0220, 0x0220, 0x0520, 0x0520, 0x08a0, 0x08a0, 0x0000, 0x0000, // less/equal
        0x0000, 0x08a0, 0x08a0, 0x0520, 0x0520, 0x0220, 0x0220, 0x0000, // greater/equal
        0x07c0, 0x0920, 0x1110, 0x1110, 0x1110, 0x0920, 0x07c0, 0x0000, // theta
        0x01e0, 0x0210, 0x04c8, 0x0528, 0x05e8, 0x0220, 0x01c0, 0x0000, // @
        0x0400, 0x0200, 0x0100, 0x0080, 0x0040, 0x0020, 0x0010, 0x0000, /* \ */
        0x01e0, 0x0210, 0x0210, 0x01e0, 0x0290, 0x0290, 0x01a0, 0x0000, // oe
    };
    
	
	private static boolean debug = false;
	
	private static void doDebug(String s) {
		if(debug) System.out.println(s);
	}
	
	/**
	 * Process a key press. Return a Plato key code or -1 to skip the key.
	 * @param e
	 * @return a valid plato key code or -1
	 */
    public static int platoKeypress(KeyEvent e) {
        int key= e.getKeyCode();
        int shift= 0;
        int pc= -1;

        if (e.isShiftDown()) {
        	//doDebug("Shift down");
            shift= 040;
        }
        
        //doDebug("Key: " + key);
        
        if (key == KeyEvent.VK_SHIFT
            || key == KeyEvent.VK_CONTROL
            || key == KeyEvent.VK_ALT
            || key == KeyEvent.VK_META) {
        	//doDebug("ShiftCntrlAlt");
            return pc;
        }

        // ALT leftarrow is assignment arrow
        if (e.isAltDown() && key == KeyEvent.VK_LEFT) {
            pc= 015 | shift; // assignment arrow
            //doDebug("Alt Left");
            return pc; // NOTE return
        }

       if (e.isAltDown() && isalpha(key)) {
       	   int k2 = Character.toLowerCase((char)key);
           //doDebug("< 0200 "+key+" " + k2);
           key = k2;
        }

        
        // check ascii to plato mapping array
        if (key < asciiToPlato.length) {
			//doDebug("<asciiToPlato");
            if (e.isAltDown()) {
                pc= altKeyToPlato[key];

                if (pc >= 0) {
					//doDebug("Alt down");
                    return pc | shift; // NOTE return
                } else {
                    return -1;
                }
            } else if (e.isControlDown() && key >= 040) { //e.isActionKey()) { // !isalpha(key)) {
                // control but not a letter --
                // translate to what a PLATO keyboard
                // would have on the shifted position for that key
                if (isalpha(key)) {
					//doDebug(">= 040 and isalpha");
                    pc= asciiToPlato[key & 037];
                } else {
					//doDebug("not alpha");
                    pc= asciiToPlato[key];
                    shift= 040;
                }

                if (pc >= 0) {
					//doDebug("> 0");
                    pc |= shift;
                    return pc; // NOTE return
                }
            }
        }

        switch (key) {
            case KeyEvent.VK_SPACE :
                pc= 0100; // space
                break;
            case KeyEvent.VK_BACK_SPACE :
                pc= 023; // erase
                break;
            case KeyEvent.VK_ENTER :
                pc= 026; // next
                break;
            case KeyEvent.VK_HOME :
            case KeyEvent.VK_F8 :
                pc= 030; // back
                break;
            case KeyEvent.VK_PAUSE :
            case KeyEvent.VK_F10 :
                pc= 032; // stop
                break;
            case KeyEvent.VK_TAB :
                pc= 014; // tab
                break;
            case KeyEvent.VK_ESCAPE:
            	pc = 015;
            	break;   
            case KeyEvent.VK_PLUS :
                if (e.isControlDown()) {
                    pc= 056; // Sigma
                } else {
                    pc= 016; // +
                }
                break;
            case KeyEvent.VK_SUBTRACT : //XK_KP_Subtract :
                if (e.isControlDown()) {
                    pc= 057; // Delta
                } else {
                    pc= 017; // -
                }
                break;
            case KeyEvent.VK_MULTIPLY : //XK_KP_Multiply :
            case KeyEvent.VK_DELETE :
                pc= 012; // multiply sign
                break;
            case KeyEvent.VK_DIVIDE : //XK_KP_Divide :
            case KeyEvent.VK_INSERT :
                pc= 013; // divide sign
                break;
            case KeyEvent.VK_LEFT :
            case KeyEvent.VK_KP_LEFT :
                pc= 0101; // left arrow (a)
                break;
            case KeyEvent.VK_RIGHT :
            case KeyEvent.VK_KP_RIGHT :
                pc= 0104; // right arrow (d)
                break;
            case KeyEvent.VK_UP :
            case KeyEvent.VK_KP_UP :
                pc= 0127; // up arrow (w)
                break;
            case KeyEvent.VK_DOWN :
            case KeyEvent.VK_KP_DOWN :
                pc= 0130; // down arrow (x)
                break;
            case KeyEvent.VK_PAGE_UP :
                pc= 020; // super
                break;
            case KeyEvent.VK_PAGE_DOWN :
                pc= 021; // sub
                break;
            case KeyEvent.VK_F3 :
                pc= 034; // square
                break;
            case KeyEvent.VK_F2 :
                pc= 022; // ans
                break;
            case KeyEvent.VK_F1 :
            case KeyEvent.VK_F11 :
                pc= 033; // copy
                break;
            case KeyEvent.VK_F9 :
                pc= 031; // data
                break;
            case KeyEvent.VK_F5 :
                pc= 027; // edit
                break;
            case KeyEvent.VK_F4 :
                pc= 024; // micro/font
                break;
            case KeyEvent.VK_F6 :
                pc= 025; // help
                break;
            case KeyEvent.VK_F7 :
                pc= 035; // lab
                break;
            default :
                return -1;
        }
		//doDebug("past switch:"+key +"-"+(pc|shift));
        pc |= shift;
        return pc;
    }

    /**
     * A key was typed. Convert it to a valid Plato key code or return -1 
     * to skip it.
     * @param e
     * @return valid Plato key code or -1
     */
	public static int platoKeyType(KeyEvent e) {
		int pc = -1;
		int shift = 0;
		int key  = e.getKeyChar();
		
		if (e.isControlDown() || e.isAltDown()) {
			return -1;
		}
		if (e.isShiftDown()){
			shift = 040;
		}
			
		if (key < asciiToPlato.length){
			pc = asciiToPlato[key];
		}
		//doDebug("typed:" +key+"-"+ pc);
		return pc;
	}
	
	
    private static boolean isalpha(int key) {
        return (key >= KeyEvent.VK_A && key <= KeyEvent.VK_Z);
    }
}
