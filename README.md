1- Project Description

WatchOut is a real-life game that merges modern technology with the classic
thrill of hide-and-seek 1. Each player is provided with a smartwatch containing
an application (Java process) specifically designed for the game. Players’
smartwatches coordinate to choose the hiders and the seeker (Distributed election). Moreover, since
WatchOut is a game that requires a certain intense physical activity, the smartwatches
are equipped with a photoplethysmography sensor, capable of detecting
the heart rate by low-intensity infrared light. Therefore, a game manager,
thanks to an administration client, can control the health status of the participants
and intervene if needed. Figure 1 shows the overall architecture of the
WatchOut game.
The goal of the project is to implement the Administration Server, the Administration
Client, and a peer-to-peer scalable system of players. The Players autonomously
organize themselves when they need to elect a seeker and when
they concurrently want to reach the home base (Distributed mutual exclusion). Moreover, they periodically
send heart-rate measurements to the Administration Server.

![Screenshot 2024-07-08 143021](https://github.com/Abudo-S/WatchOut_DPS_Project/assets/40835481/6ddcbda3-8307-4db6-935b-3870fdfef1db)


1.2- Game Phases

1.2.1- Preparation

When a new player enters the game (i.e. their process is launched), they registers
on the administration server, if the registration is successful the player’s process
receives from the server their initial position on the perimeter of the pitch,
together with the list of other players already present. When the game starts
each player receives a notification from the game manager, at which point phase
0 of the game begins.

1.2.2- Phase 0

During phase 0 the players’ processes coordinate to choose who is the seeker.
Once a consensus is reached on who the seeker is, phase 1, or the actual game,
begins.

1.2.3- Phase 1

During phase 1 the goal of the hiders is to reach the home base without getting
tagged by the seeker. Upon reaching home base a player must stay there for 10
seconds after which that player is considered safe. Only one player at a time can
wait in the home base. If the home base is free the hider can go there. Every
player knows the position of the others. At the beginning of phase 1, the seeker
starts trying to tag hiders, they do that by moving toward the player they are
closest to. When the seeker reaches the point toward which they were moving
there are two possibilities:
• The player they were looking for is still there. In this case, the player is
tagged and thus eliminated from the game (the process is till running).
• The player they were looking for acquired permission to go to the home
base and thus fled. In this case, that player is not considered tagged.

In any case, as the next step the seeker starts again trying to tag a new player,
moving toward the closest player still in the game.

1.2.4 End of the Game

When a player is tagged, they are eliminated from the game. When a player
remains for 10 seconds at the home base, they are safe. Once a player is safe
or eliminated, they communicates it in broadcast to all other players. When all
hiders are either safe or eliminated the game ends. It is the network of players
themselves who understand when the game is over.

1.2.5- New players

New players can enter the game at any phase. If they enter when the seeker
election has not yet taken place or is in progress, they must be able to participate
in the election, if they enter during phase 1, they are automatically hiders.

1.3- Internal Representation of the Pitch

The pitch of WatchOut is represented as a 10 × 10 grid (see Figure 2), each cell
of the grid represents a point with the respective coordinates. For simplicity,
assume that the value of the coordinate is expressed in tens of meters. The
home base is located in a squared area defined by points (4, 4), (4, 5), (5, 5) and
(5, 4) included. To move from one point to another, the players move along
the diagonal. Therefore, the distance between a point A(xa, ya) and a point
B(xb, yb) is defined, according to the Euclidean distance metric, as follows:
d(A,B) = sqrt((xa − xb)^2 + (ya − yb)^2)
The players move at a speed of 2 meters per second.

![Screenshot 2024-07-08 143247](https://github.com/Abudo-S/WatchOut_DPS_Project/assets/40835481/0618ed64-9039-41e1-a20f-e962175bda3c)
