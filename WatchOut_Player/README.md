### player functionalities ###
-GRPC: coordination to elect the seeker (closest to home base & highest id)+ to select who enters in the home base if one or more player are ready to enter the H.B. (highest id).

-GRPC: informing other players on acquired permission to reach H.B. (all players with acquired permissions who reach the H.B. simultaneously need the reach a consensus).

-GRPC: informing other players on tagged or safe state for execlusion.

-REST: Ask for registration from admin server+

-GRPC: if the list of present players isn't empty, then inform all present players of the new process[endpoint + position].

-GRPC: receive game-start msg and other messeges on the same topic.

-REST: After successful registeration, it sends heart-rate (player id, list of HR avgs, timestamp in milliseconds) periodically after 10sec.

cont.
HR interface (Buffer) of this data structure that exposes two methods:

• void add(Measurement m).
• List <Measurement> readAllAndClean().

you must process sensor data through the sliding window technique; a buffer of 8 measurements, with an overlap factor of 50 % (a HR is acquired each [predefined frequency]).
When the dimension of the buffer is equal to 8 measurements, you must invoke realAllAndClean compute the average of these 8 measurements (averages to the Administrator Server).
