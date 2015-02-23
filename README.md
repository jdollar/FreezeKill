# FreezeKill
A new bukkit plugin requested by user PhillyCheezsteak on the Bukkit request plugin forum

The intent of this plugin is to freeze a player for a specified amount of time.
In this time they are required to kill and entity or player in order to be set free. If they fail
then they will die and respawn.

The intent is also to try and make mobs hostile to this player as well.

# List Of Permissions:
* freezeKill.freezekill = Allows a player to use the freezekill command and freeze a player in place
* freezeKill.unfreeze   = Allows a player to save a frozen player and remove him from the timer and allow them to move

# Commands
* /freezekill [playerName] [timelimit] = Use the player name and the time limit in seconds to freeze a player for a specified number of seconds. They will be required to kill a hostile mob before the time runs out or else they will die
* /unfreeze [playerName]               = Use the player name of a frozen player to remove them from the previous condition.
 

#Configuration Values
Example Configuration File
```
  hostileMobSettings:
    doMobAttackWhenFrozen: true
    areaAroundPlayer:
      x:
        - 50
      y:
        - 50
      z:
        - 50
```
* doMobAttackWhenFrozen = Makes hostile mobs hostile to the frozen player in a radius of the values specified by areaAroundPlayer
* areaAroundPlayer      = Values specified for the area around the player where mobs will be pulled in to attack the player
  * Example: Based off the example file above mobs will be pulled in from 50  blocks away from the x, y and z position of the player
