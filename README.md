# CSC133_Project: RainMaker

## Overview
In this project/game, the objective for the player is to be able to use the helicopter to 'seed' the clouds by pressing SPACE. Upon 'seeding' the cloud(s), the pond(s) that are near the cloud(s) will begin to increase it's capacity. Once all three ponds have an average of 80% capacity, the player can make its way back to the landing pad to land the helicopter and the player will be presented with the win message! However, be aware that if the helicopter runs out of fuel before then, the player will automatically lose.

The game will randomly spawn clouds once a cloud reaches the right side of the screen. Once there, the game has a random chance of spawning 1 - 3 clouds if there are less than 2 clouds on the screen. Additionally, it also have a 50/50 chance of spawning an extra cloud onto the screen no matter the amount of cloud on screen. As the game is running, the player will notice a blimp that is flying across the screen. The blimp is a game object and only one is able to exist at a time. The purpose of the blimp is to act as a refueling station for the player's helicopter where the player needs to be close to matching the blimp and making contact for a split second in order to refuel.

**Note**
This game has other difficulties that may pose as a challenge to the player such making the helicopter needing to go through different states until the player is allow to move the helicopter or turn off the helicopter as well as the changing wind speed of the clouds and blimp on screen. The player must manage their fuel wisely in order to successfully fill the ponds and make it safely back to the landing to shut down.

## Controls
**Helicopter control**
* UP ARROW - Increase helicopter speed
* DOWN ARROW - Decrease helicopter speed
* LEFT ARROW - Rotate helicopter left
* RIGHT ARROW - Rotate helicopter right
* SPACE - 'Seeding' cloud
* I - Turn on / Turn off Helicopter

**Game**
* R - Reset game
* B - Show bounding boxes of objects
