### admin client ###
-rest: reads players' hr averages from admin server in order to stop the game (through its manager) if a player has anomalies in his HR.

-rest: asks the server the number of ready players; so if it satisfies the minimum, it starts the game (through its manager).

-mqtt: allows its manager to send custom messages (topic "custom").

-mqtt: publish game-start and game-end msgs.
