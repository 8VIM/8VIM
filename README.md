# 8Vim

[![Project Dashboard](https://sourcespy.com/shield.svg)](https://sourcespy.com/github/flide8vim/)

The application was started essentially as a clone of 8pen, which for some reason they have stopped distributing. (I can no longer install it, even though I paid for it!)

To exactly understand this keyboard, please watch the [video](https://www.youtube.com/watch?v=q3OuCR0EpGo)

| Stable [![Stable](https://img.shields.io/github/v/release/8VIM/8VIM)](https://github.com/8VIM/8VIM/releases/latest) ![F-Droid](https://img.shields.io/f-droid/v/inc.flide.vi8) | Beta [![Beta](https://img.shields.io/github/v/release/8VIM/8VIM?include_prereleases)](https://github.com/8VIM/8VIM/releases) ![F-Droid](https://img.shields.io/badge/dynamic/yaml?url=https%3A%2F%2Fraw.githubusercontent.com%2F8VIM%2Ffdroid%2Fmain%2Fbadges.yaml&query=%24%5B'8vim_rc'%5D&label=Private%20F-droid%20RC&color=orange) | Debug ![F-Droid](https://img.shields.io/badge/dynamic/yaml?url=https%3A%2F%2Fraw.githubusercontent.com%2F8VIM%2Ffdroid%2Fmain%2Fbadges.yaml&query=%24%5B'8vim_debug'%5D&label=Private%20F-droid%20Debug&color=orange)
| :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: |:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:----:|
| [<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80" />](https://f-droid.org/packages/inc.flide.vi8/) [<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="80" />](https://play.google.com/store/apps/details?id=inc.flide.vi8) | [<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80" />](https://github.com/8vim/repo) [<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="80" />](https://play.google.com/store/apps/details?id=inc.flide.vi8) | [<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80" />](https://github.com/8vim/repo) |

**Manual**: Download and install the APK from the release page.

## Become a Beta Tester

To become a beta tester please click [here](https://play.google.com/apps/testing/inc.flide.vi8)

## Usability Guide

So, what capablities does 8Vim have?

##### Basic Stuff

- right sector acts as a Backspace key.
- bottom sector acts as an Enter key.
- top sector acts as a combo of Shift and Caps Lock key (i.e., press once to activate Shift. Press again for Caps Lock. Press once more and everything is back to normal.)
- left sector acts as a button that takes you to the number pad.

##### Cursor Movements

If you move your finger from within the circle to any sector, the cursor will move one character in the direction of the sector. For example, if you swipe from the circle to the right sector and then raise your finger, the cursor will move to the right once. If you don't lift up your finger but instead keep pressing on the sector, the cursor will move as long as the sector is pressed.

##### Selection

There is a selection mode built into the keyboard. If you move your finger from the right sector to the circle and hold, the cursor will start moving left while selecting everything in its path, until you lift your finger. Once you release, the selection mode keyboard will open and show different actions that can then be applied to your selection. (i.e. right-sector->circle->hold->lift-finger)

##### Paste Functionality

Moving your finger from the left sector to the circle center will paste your clipboard content. (i.e. left-sector->circle->lift-finger)

###### Handwriting Reinvented

The 8vim draws inspiration from handwriting. By mimicking the way our hand moves when constrained to a square canvas, we are able to create a natural and fun writing experience, akin to doodling on a piece of paper.

###### Designed for Speed

The layout is optimised for fast writing, by allowing for the most common letter sequences to be produced with swift, fluid gestures. Once familiar with the layout, you can reach speeds of over 40 words per minute!

###### Typos, goodbye

The simplicity of the gestures used to enter words, allow for true blind typing and virtually eliminate typos. Regardless of whether you have small or big fingers. Word suggestions become an optimisation, not a dependency.

## Extended Idea

The thing that bugs me is that most editing done on mobile phones is within those quirky message boxes and there is JUST NO WAY to edit stuff there.
Also, it is beyond frustrating to try and create a document on a mobile phone with more characters than a tweet. I feel like 'text editors' for mobile phones are a joke. They have tons of toolbars and stuff which you rarely open up to "edit" something, but the problem remains: You just don't want to type on a mobile phone. It's tedious. It's time consuming. It's frustrating.

Then, there came along 8pen, the keyboard on which you actually can type without looking, and I thought to myself: Now, Now, NOW, FINALLY!! Editing and creating a document/text can be practical enough on a mobile device! In the frenzy, I turned to the plethora of text editors available on various app stores. Yeah, they get the job done, but you have to keep both of your hands engaged and tapping on the various toolbars and buttons… I was missing [Vim](http://www.vim.org/).

I am a huge fan of Vim - the editor. It is so cool to be able to edit without leaving the keyboard. The idea that, when you are editing, you add less and modify more is so comfortable and incredibly efficient. It works on desktop, because you have a keyboard attached that you can use with - hopefully - all of your fingers. On a touch screen however, it's a very stupid idea to try to use Vim.

I hear you say: "Come on! Don't dismiss the idea without trying it first!". Believe me, I did try. Try it yourself, it's called [vimtouch](https://github.com/momodalo/vimtouch). Vimtouch, the port of Vim for android, although being a big deal technologically, it's utterly unusable in a practical setting. Why? Because at best you've got two thumbs to do the job of ten fingers. It's highly inefficient.

No matter what editor you use, it has one basic issue: It only works when you open it. Confused? Let's try a practical, common, frequently occuring scenario. Your boss has sent you an email and you are ANGRY! You want to reply and pour your heart out. You press the reply button and suddenly realize: "This is the perfect job for an editor!" So you search through the millions of apps you've installed, looking for that editor and only then start editing. It takes you an hour to compose 5 lines. Afterwards you copy-paste the stuff into your email reply only then you are done. What a hassle!!

And then it hit me… Why the hell do you need text editors? Why can't your keyboard act as the editor? It's always there when you actually want to input/edit something and it's software only, so there's no hardware to worry about. You can hack the shit out of it. I mean, WHY NOT??

And this inspiration (rant) became the idea for VIII - an Editor-in-Keyboard.

TO BE CONTINUED...
