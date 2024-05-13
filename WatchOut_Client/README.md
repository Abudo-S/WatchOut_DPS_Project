### admin client ###
-REST: reads players' hr averages from admin server in order to stop the game (through its manager) if a player has anomalies in his HR.

-REST: asks the server the number of ready players; so if it satisfies the minimum, it starts the game (through its manager).

-MQTT: allows its manager to send custom messages (topic "custom").

-MQTT: publish game-start and game-end msgs.

-CLI: invoke and print from all admin server rest-per-client methods + invoke and print mqtt-custom method


