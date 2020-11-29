#!/usr/bin/env python3
# -------------------------------------------
# Copyright (C) <2020> <Simon Slater>
#
# This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
# -------------------------------------------


# Please change this to the layout you want
# Make the @ character in the same place for the upper and lower case for extra functionality.
#    Circle around the board 1 more time to input your email address.
#    In caps lock mode writing an @ will enter @gmail.com for you
# Make the ! character in the same place for the upper and lower case for extra functionality.
#    Circle around the board 1 more time to write three exclamation marks !!!
new_layout_lower      = "nmpq ecx? rsb! ouvz hlj@ ifw, tyk. adg'"
new_layout_upper      = "NMPQ ECX* RSB! OUVZ HLJ@ IFW_ TYK- ADG\""
new_layout_lower = "ybpq arx? nmf! ouvw elk@ ihj, tcz. sdg'"
new_layout_upper = "YBPQ ARX* NMF! OUVW ELK@ IHJ_ TCZ- SDG\""

# WARNING: Don't change this original string!
# It's not used in the code, but is your reference for what the new layout will look like compared to the old one.
original_layout_lower = "ybpq arx? nmf! ouvw elk@ ihj, tcz. sdg'"
original_layout_upper = "YBPQ ARX* NMF! OUVW ELK@ IHJ_ TCZ- SDG\""

# Put your email address here for it to be added when you go all the way around the board for the @ sign.
your_email_address = "flideravi@gmail.com"

# Setup over.
# Please run the script and it will output the keyboard_actions.xml file for you :).

# -------------------
#    Program start
# -------------------

# The movements assigned to the original layout.
# We use this to make the new layout 
movement_lower = [
    # y
    "        <movementSequence>INSIDE_CIRCLE;TOP;RIGHT;INSIDE_CIRCLE;</movementSequence>",
    # b
    "        <movementSequence>INSIDE_CIRCLE;TOP;RIGHT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",
    # p
    "        <movementSequence>INSIDE_CIRCLE;TOP;RIGHT;BOTTOM;LEFT;INSIDE_CIRCLE;</movementSequence>",
    # q
    "        <movementSequence>INSIDE_CIRCLE;TOP;RIGHT;BOTTOM;LEFT;TOP;INSIDE_CIRCLE;</movementSequence>",

    # a
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;TOP;INSIDE_CIRCLE;</movementSequence>",
    # r
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;TOP;LEFT;INSIDE_CIRCLE;</movementSequence>",
    # x
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;TOP;LEFT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",
    # ?
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;TOP;LEFT;BOTTOM;RIGHT;INSIDE_CIRCLE;</movementSequence>",
            #<inputCapsLockString>*</inputCapsLockString>

    # n
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",
    # m
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;BOTTOM;LEFT;INSIDE_CIRCLE;</movementSequence>",
    # f
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;BOTTOM;LEFT;TOP;INSIDE_CIRCLE;</movementSequence>",
    # !
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;BOTTOM;LEFT;TOP;RIGHT;INSIDE_CIRCLE;</movementSequence>",

    # o
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;RIGHT;INSIDE_CIRCLE;</movementSequence>",
    # u
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;RIGHT;TOP;INSIDE_CIRCLE;</movementSequence>",
    # v
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;RIGHT;TOP;LEFT;INSIDE_CIRCLE;</movementSequence>",
    # w
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",

    # e
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;LEFT;INSIDE_CIRCLE;</movementSequence>",
    # l
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;LEFT;TOP;INSIDE_CIRCLE;</movementSequence>",
    # k
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;LEFT;TOP;RIGHT;INSIDE_CIRCLE;</movementSequence>",
    # @
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",

    # i
    "        <movementSequence>INSIDE_CIRCLE;LEFT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",
    # h
    "        <movementSequence>INSIDE_CIRCLE;LEFT;BOTTOM;RIGHT;INSIDE_CIRCLE;</movementSequence>",
    # j
    "        <movementSequence>INSIDE_CIRCLE;LEFT;BOTTOM;RIGHT;TOP;INSIDE_CIRCLE;</movementSequence>",
    # ,
    "        <movementSequence>INSIDE_CIRCLE;LEFT;BOTTOM;RIGHT;TOP;LEFT;INSIDE_CIRCLE;</movementSequence>",
            #<inputCapsLockString>_</inputCapsLockString>

    # t
    "        <movementSequence>INSIDE_CIRCLE;LEFT;TOP;INSIDE_CIRCLE;</movementSequence>",
    # c
    "        <movementSequence>INSIDE_CIRCLE;LEFT;TOP;RIGHT;INSIDE_CIRCLE;</movementSequence>",
    # z
    "        <movementSequence>INSIDE_CIRCLE;LEFT;TOP;RIGHT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",
    # .
    "        <movementSequence>INSIDE_CIRCLE;LEFT;TOP;RIGHT;BOTTOM;LEFT;INSIDE_CIRCLE;</movementSequence>",
            #<inputCapsLockString>-</inputCapsLockString>

    # s
    "        <movementSequence>INSIDE_CIRCLE;TOP;LEFT;INSIDE_CIRCLE;</movementSequence>",
    # d
    "        <movementSequence>INSIDE_CIRCLE;TOP;LEFT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",
    # g
    "        <movementSequence>INSIDE_CIRCLE;TOP;LEFT;BOTTOM;RIGHT;INSIDE_CIRCLE;</movementSequence>",
    # '
    "        <movementSequence>INSIDE_CIRCLE;TOP;LEFT;BOTTOM;RIGHT;TOP;INSIDE_CIRCLE;</movementSequence>"
            #<inputCapsLockString>"</inputCapsLockString>
]


# The movement for going all the way around the board to capitalize.
movement_upper = [
    # Y
    "        <movementSequence>INSIDE_CIRCLE;TOP;RIGHT;BOTTOM;LEFT;TOP;RIGHT;INSIDE_CIRCLE;</movementSequence>",
    # B
    "        <movementSequence>INSIDE_CIRCLE;TOP;RIGHT;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",
    # P
    "        <movementSequence>INSIDE_CIRCLE;TOP;RIGHT;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;LEFT;INSIDE_CIRCLE;</movementSequence>",
    # Q
    "        <movementSequence>INSIDE_CIRCLE;TOP;RIGHT;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;LEFT;TOP;INSIDE_CIRCLE;</movementSequence>",

    # A
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;TOP;LEFT;BOTTOM;RIGHT;TOP;INSIDE_CIRCLE;</movementSequence>",
    # R
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;TOP;LEFT;BOTTOM;RIGHT;TOP;LEFT;INSIDE_CIRCLE;</movementSequence>",
    # X
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;TOP;LEFT;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",
    # *
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;TOP;LEFT;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;RIGHT;INSIDE_CIRCLE;</movementSequence>",

    # N
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",
    # M
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;LEFT;INSIDE_CIRCLE;</movementSequence>",
    # F
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;LEFT;TOP;INSIDE_CIRCLE;</movementSequence>",
    # !
    "        <movementSequence>INSIDE_CIRCLE;RIGHT;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;LEFT;TOP;RIGHT;INSIDE_CIRCLE;</movementSequence>",

    # O
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;RIGHT;INSIDE_CIRCLE;</movementSequence>",
    # U
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;RIGHT;TOP;INSIDE_CIRCLE;</movementSequence>",
    # V
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;RIGHT;TOP;LEFT;INSIDE_CIRCLE;</movementSequence>",
    # W
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",

    # E
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;LEFT;INSIDE_CIRCLE;</movementSequence>",
    # L
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;LEFT;TOP;INSIDE_CIRCLE;</movementSequence>",
    # K
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;LEFT;TOP;RIGHT;INSIDE_CIRCLE;</movementSequence>",
    # @
    "        <movementSequence>INSIDE_CIRCLE;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",

    # I
    "        <movementSequence>INSIDE_CIRCLE;LEFT;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",
    # H
    "        <movementSequence>INSIDE_CIRCLE;LEFT;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;RIGHT;INSIDE_CIRCLE;</movementSequence>",
    # J
    "        <movementSequence>INSIDE_CIRCLE;LEFT;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;RIGHT;TOP;INSIDE_CIRCLE;</movementSequence>",
    # _
    "        <movementSequence>INSIDE_CIRCLE;LEFT;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;RIGHT;TOP;LEFT;INSIDE_CIRCLE;</movementSequence>",

    # T
    "        <movementSequence>INSIDE_CIRCLE;LEFT;TOP;RIGHT;BOTTOM;LEFT;TOP;INSIDE_CIRCLE;</movementSequence>",
    # C
    "        <movementSequence>INSIDE_CIRCLE;LEFT;TOP;RIGHT;BOTTOM;LEFT;TOP;RIGHT;INSIDE_CIRCLE;</movementSequence>",
    # Z
    "        <movementSequence>INSIDE_CIRCLE;LEFT;TOP;RIGHT;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",
    # -
    "        <movementSequence>INSIDE_CIRCLE;LEFT;TOP;RIGHT;BOTTOM;LEFT;TOP;RIGHT;BOTTOM;LEFT;INSIDE_CIRCLE;</movementSequence>",

    # S
    "        <movementSequence>INSIDE_CIRCLE;TOP;LEFT;BOTTOM;RIGHT;TOP;LEFT;INSIDE_CIRCLE;</movementSequence>",
    # D
    "        <movementSequence>INSIDE_CIRCLE;TOP;LEFT;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;INSIDE_CIRCLE;</movementSequence>",
    # G
    "        <movementSequence>INSIDE_CIRCLE;TOP;LEFT;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;RIGHT;INSIDE_CIRCLE;</movementSequence>",
    # "
    "        <movementSequence>INSIDE_CIRCLE;TOP;LEFT;BOTTOM;RIGHT;TOP;LEFT;BOTTOM;RIGHT;TOP;INSIDE_CIRCLE;</movementSequence>"
]



# 8VIM uses this string format to display the letters on the keyboard.
original_layout_string_lower = "nomufv!weilhkj@,tscdzg.'yabrpxq?"
original_layout_string_upper = "NOMUFV!WEILHKJ@_TSCDZG-\"YABRPXQ*"

new_layout_string_lower = ""
new_layout_string_upper = ""

# Convert from our nice layout string to the ugly one that 8vim uses.
for i in range(10, 14):
    new_layout_string_lower += new_layout_lower[i]
    new_layout_string_lower += new_layout_lower[i+5]
    new_layout_string_upper += new_layout_upper[i]
    new_layout_string_upper += new_layout_upper[i+5]

for i in range(20, 24):
    new_layout_string_lower += new_layout_lower[i]
    new_layout_string_lower += new_layout_lower[i+5]
    new_layout_string_upper += new_layout_upper[i]
    new_layout_string_upper += new_layout_upper[i+5]

for i in range(30, 34):
    new_layout_string_lower += new_layout_lower[i]
    new_layout_string_lower += new_layout_lower[i+5]
    new_layout_string_upper += new_layout_upper[i]
    new_layout_string_upper += new_layout_upper[i+5]

for i in range(0, 4):
    new_layout_string_lower += new_layout_lower[i]
    new_layout_string_lower += new_layout_lower[i+5]
    new_layout_string_upper += new_layout_upper[i]
    new_layout_string_upper += new_layout_upper[i+5]

print("Edit this file")
print("8vim/src/main/java/inc/flide/vim8/views/mainKeyboard/XpadView.java)")
print("Change these variables to this:")
print( "String characterSetSmall = \"" + new_layout_string_lower.replace("\"", "\\\"") + "\";" )
print( "String characterSetCaps  = \"" + new_layout_string_upper.replace("\"", "\\\"") + "\";" )
print("")
print("The new keyboard layout has been saved to:")
print("keyboard_actions.xml")
print("Move it to here:")
print("8vim/src/main/res/raw/keyboard_actions.xml")
print("")
print("Rebuild 8vim and send the apk to your phone.")


###################################################
# Start buliding the keyboard_actions.xml output. #
###################################################

# Used with sending key code
INPUT_KEY_START = """
    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
"""
INPUT_KEY_END = """
    </keyboardAction>
"""

# This line is used with capital letters for some reason.
# It's used when we circle around the board to capitalize a letter.
FLAGS = """
        <flags>
            <flag>1</flag>
        </flags>"""

# Inputing text rather than a single keycode.
INPUT_TEXT_START = """
    <keyboardAction>
        <keyboardActionType>INPUT_TEXT</keyboardActionType>
"""

INPUT_TEXT_END = """
    </keyboardAction>
"""

# All lower keys stored in here.
final_output_lower = ""
# All upper keys stored in here.
final_output_upper = ""

new_layout_lower = new_layout_lower.replace(" ", "")
new_layout_upper = new_layout_upper.replace(" ", "")

for i in range( len( new_layout_lower ) ):
    output_lower = ""
    output_upper = ""
    if (new_layout_lower[i].lower() >= 'a') and (new_layout_lower[i].lower() <= 'z'):
        caps_lock = ""
        # Set the caps lock string if it's not an enlish a-z letter.
        if not( (new_layout_upper[i].lower() >= 'a') and (new_layout_upper[i].lower() <= 'z') ):
            caps_lock = "\n        <inputCapsLockString>" + new_layout_upper[i] + "</inputCapsLockString>"

        key = "        <inputKey>KEYCODE_" + new_layout_lower[i].upper() + "</inputKey>"
        output_lower = INPUT_KEY_START + movement_lower[i] + "\n" + key + caps_lock + INPUT_KEY_END
        output_upper = INPUT_KEY_START + movement_upper[i] + "\n" + key + FLAGS     + INPUT_KEY_END
    else:
        # It's a special character so just use the text entry method.
        if (new_layout_lower[i] == '@') and (new_layout_upper[i] == '@'):
            # Keep the special functionality to enter your email address.
            input_string_lower =   "        <inputString>"         + "@"                + "</inputString>"
            input_string_upper =   "        <inputString>"         + your_email_address + "</inputString>"
            caps_lock          = "\n        <inputCapsLockString>" + "@gmail.com"       + "</inputCapsLockString>"

        elif (new_layout_lower[i] == '!') and (new_layout_upper[i] == '!'):
            # Useful exclamation marks
            input_string_lower =   "        <inputString>"         + "!"                + "</inputString>"
            input_string_upper =   "        <inputString>"         + "!!!"              + "</inputString>"
            caps_lock          = "\n        <inputCapsLockString>" + "!"                + "</inputCapsLockString>"
        else:
            # It's just a normal special character so just add it.
            input_string_lower =   "        <inputString>"         + new_layout_lower[i] + "</inputString>"
            input_string_upper =   "        <inputString>"         + new_layout_upper[i] + "</inputString>"
            caps_lock          = "\n        <inputCapsLockString>" + new_layout_upper[i] + "</inputCapsLockString>"

        output_lower       = INPUT_TEXT_START + movement_lower[i] + "\n" + input_string_lower + caps_lock + INPUT_TEXT_END
        output_upper       = INPUT_TEXT_START + movement_upper[i] + "\n" + input_string_upper +             INPUT_TEXT_END

    final_output_lower += output_lower
    final_output_upper += output_upper



XML_START = """<keyboardActionMap>
    <!-- Keywords for defining the movements -->
    <!--{NO_TOUCH, INSIDE_CIRCLE, TOP, LEFT, BOTTOM, RIGHT}-->

    <!-- ~~~~~~~~~ -->
    <!-- Lowercase -->
    <!-- ~~~~~~~~~ -->"""

CAPITAL = """

    <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->
    <!-- Capital Characters by going all the way around the board -->
    <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->"""
final_output_upper

XML_END = """

    <!-- ~~~~~~~~~~~~~~~~~~~~ -->
    <!-- Esperanto Characters -->
    <!-- ~~~~~~~~~~~~~~~~~~~~ -->
    <keyboardAction>
        <keyboardActionType>INPUT_TEXT</keyboardActionType>
        <movementSequence>INSIDE_CIRCLE;LEFT;TOP;RIGHT;TOP;RIGHT;INSIDE_CIRCLE;</movementSequence>
        <inputString>ĉ</inputString>
        <inputCapsLockString>Ĉ</inputCapsLockString>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_TEXT</keyboardActionType>
        <movementSequence>INSIDE_CIRCLE;TOP;LEFT;BOTTOM;RIGHT;BOTTOM;RIGHT;INSIDE_CIRCLE;</movementSequence>
        <inputString>ĝ</inputString>
        <inputCapsLockString>Ĝ</inputCapsLockString>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_TEXT</keyboardActionType>
        <movementSequence>INSIDE_CIRCLE;LEFT;BOTTOM;RIGHT;BOTTOM;RIGHT;INSIDE_CIRCLE;</movementSequence>
        <inputString>ĥ</inputString>
        <inputCapsLockString>Ĥ</inputCapsLockString>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_TEXT</keyboardActionType>
        <movementSequence>INSIDE_CIRCLE;LEFT;BOTTOM;RIGHT;TOP;RIGHT;TOP;INSIDE_CIRCLE;</movementSequence>
        <inputString>ĵ</inputString>
        <inputCapsLockString>Ĵ</inputCapsLockString>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_TEXT</keyboardActionType>
        <movementSequence>INSIDE_CIRCLE;TOP;LEFT;TOP;LEFT;INSIDE_CIRCLE;</movementSequence>
        <inputString>ŝ</inputString>
        <inputCapsLockString>Ŝ</inputCapsLockString>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_TEXT</keyboardActionType>
        <movementSequence>INSIDE_CIRCLE;BOTTOM;RIGHT;TOP;RIGHT;TOP;INSIDE_CIRCLE;</movementSequence>
        <inputString>ŭ</inputString>
        <inputCapsLockString>Ŭ</inputCapsLockString>
    </keyboardAction>


    <!--Paste Sequence-->
    <keyboardAction>
        <keyboardActionType>INPUT_SPECIAL</keyboardActionType>
        <movementSequence>LEFT;INSIDE_CIRCLE;NO_TOUCH;</movementSequence>
        <inputString>PASTE</inputString>
    </keyboardAction>


    <!--Shift-->
    <keyboardAction>
        <keyboardActionType>INPUT_SPECIAL</keyboardActionType>
        <movementSequence>TOP;NO_TOUCH;</movementSequence>
        <inputString>SHIFT_TOOGLE</inputString>
    </keyboardAction>


    <!-- Switch to Numpad-->
    <keyboardAction>
        <keyboardActionType>INPUT_SPECIAL</keyboardActionType>
        <movementSequence>LEFT;NO_TOUCH;</movementSequence>
        <inputString>SWITCH_TO_NUMBER_PAD</inputString>
    </keyboardAction>


    <!--Selection Mode Sequence-->
    <keyboardAction>
        <keyboardActionType>INPUT_SPECIAL</keyboardActionType>
        <movementSequence>RIGHT;INSIDE_CIRCLE;NO_TOUCH;</movementSequence>
        <inputString>SELECTION_START</inputString>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_SPECIAL</keyboardActionType>
        <movementSequence>RIGHT;INSIDE_CIRCLE;LONG_PRESS;</movementSequence>
        <inputString>SELECTION_START</inputString>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_SPECIAL</keyboardActionType>
        <movementSequence>RIGHT;INSIDE_CIRCLE;LONG_PRESS_END;</movementSequence>
        <inputString>SWITCH_TO_SELECTION_KEYBOARD</inputString>
    </keyboardAction>


    <!--Space-->
    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
        <movementSequence>INSIDE_CIRCLE;NO_TOUCH;</movementSequence>
        <inputKey>KEYCODE_SPACE</inputKey>
    </keyboardAction>


    <!-- Enter and Delete -->
    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
        <movementSequence>BOTTOM;NO_TOUCH;</movementSequence>
        <inputKey>KEYCODE_ENTER</inputKey>
    </keyboardAction>

    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
        <movementSequence>BOTTOM;LONG_PRESS;</movementSequence>
        <inputKey>KEYCODE_ENTER</inputKey>
    </keyboardAction>

    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
        <movementSequence>RIGHT;NO_TOUCH;</movementSequence>
        <inputKey>KEYCODE_DEL</inputKey>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
        <movementSequence>RIGHT;LONG_PRESS;</movementSequence>
        <inputKey>KEYCODE_DEL</inputKey>
    </keyboardAction>


    <!--D_Pad key-->
    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
        <movementSequence>NO_TOUCH;INSIDE_CIRCLE;TOP;NO_TOUCH;</movementSequence>
        <inputKey>KEYCODE_DPAD_UP</inputKey>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
        <movementSequence>NO_TOUCH;INSIDE_CIRCLE;BOTTOM;NO_TOUCH;</movementSequence>
        <inputKey>KEYCODE_DPAD_DOWN</inputKey>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
        <movementSequence>NO_TOUCH;INSIDE_CIRCLE;LEFT;NO_TOUCH;</movementSequence>
        <inputKey>KEYCODE_DPAD_LEFT</inputKey>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
        <movementSequence>NO_TOUCH;INSIDE_CIRCLE;RIGHT;NO_TOUCH;</movementSequence>
        <inputKey>KEYCODE_DPAD_RIGHT</inputKey>
    </keyboardAction>


    <!--Long press configuration-->
    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
        <movementSequence>NO_TOUCH;INSIDE_CIRCLE;TOP;LONG_PRESS;</movementSequence>
        <inputKey>KEYCODE_DPAD_UP</inputKey>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
        <movementSequence>NO_TOUCH;INSIDE_CIRCLE;BOTTOM;LONG_PRESS;</movementSequence>
        <inputKey>KEYCODE_DPAD_DOWN</inputKey>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
        <movementSequence>NO_TOUCH;INSIDE_CIRCLE;LEFT;LONG_PRESS;</movementSequence>
        <inputKey>KEYCODE_DPAD_LEFT</inputKey>
    </keyboardAction>
    <keyboardAction>
        <keyboardActionType>INPUT_KEY</keyboardActionType>
        <movementSequence>NO_TOUCH;INSIDE_CIRCLE;RIGHT;LONG_PRESS;</movementSequence>
        <inputKey>KEYCODE_DPAD_RIGHT</inputKey>
    </keyboardAction>


    <!-- Hide keyboard -->
    <keyboardAction>
        <keyboardActionType>INPUT_SPECIAL</keyboardActionType>
        <movementSequence>TOP;INSIDE_CIRCLE;NO_TOUCH</movementSequence>
        <inputString>HIDE_KEYBOARD</inputString>
    </keyboardAction>

</keyboardActionMap>"""

output = XML_START + final_output_lower + CAPITAL + final_output_upper + XML_END
f = open("keyboard_actions.xml", "w")
f.write( output )
f.close()
