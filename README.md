# ZoomieCars
![demo](https://github.com/ToyDragon/ZoomieCars/blob/master/demo.gif?raw=true)

ZoomieCars is a shitty driving game that runs right in your terminal, using [ANSI](https://en.wikipedia.org/wiki/ANSI_escape_code) and [xterm](https://www.xfree86.org/current/ctlseqs.html) escape sequences that are supported by [PuTTY](https://www.chiark.greenend.org.uk/~sgtatham/putty/). Many terminal emulators sadly don't support these escape sequences, such as the WSL terminal.

## How to play
The input method is constrained, we don't have the ability to use a familiar scheme like "press W to go fast." The session doesn't have keydown/keyup events like you'd get running a local application, it only has the characters sent through the SSH connection. Instead you must press a series of keys with each hand: A->S->D->A->S->D with your left hand, L->K->J->L->K->J with your right hand. By mashing with one hand faster than the other you can steer, which you'll probably do on accident.  
There is a computer controlled blue car that races along side you. She always moves at the same speed, never swerves, and has no collision. The track is a loop, but there's no lap counter or timers. This is really more of a tech demo than game.  

## Design goals
My main goal for this was to run in the environment I have available at my office, which is a shared AIX server. 

**[x] Must work with default PuTTY configuration.** It's super shocking to pull this up in someones SSH session who hasn't seen it before, it's surprising that PuTTY is capable of this. There are some cool xterm features that PuTTY doesn't support that this boxes us out of, like 24 bit color, the "repeat preceeding character X times" command, and [sixels](https://en.wikipedia.org/wiki/Sixel).  
**[x] Consume low enough compute to not bog down the server.** We actually use it for work, I don't want to be that guy. Without any optimizations it was up around 40%. Optimization was mosting removing as much dynamic memory allocation as I could. It's not a super hard metric but now my CPU usage on my machine stays rounded to 0%, and occasionally spikes to 0.5%.   
**[x] Run with reasonable graphics.** The medium of TTY was clearly not built for transmit and display of video streams. ZoomieCars is still able to achieve 20FPS play at 240x180 resolution by removing redundant data from the text stream. We obviously can't compress the text stream using conventional means because PuTTY doesn't support it, but we have some other tricks we can use. Most of the compression comes from the Cursor Position ANSI command which can skip around the screen to skip rewriting pixels that haven't changed. We can use an "Alternate Font" command to fake a double height display by using the "▀" character, where the foreground color controls the top have of the cell, and the background color controls the bottom half of the cell.  
**[ ] Consume low memory.** The overhead from using java means we use like 50MB minimum. This is on the border of what I consider acceptable. Soon I'll reimplement in C++, I don't see any reason that this can't have <1MB of memory usage.
