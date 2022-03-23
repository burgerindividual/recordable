# Recordable

A (currently 1.18.x) lightweight fabric mod which adds user-recordable music discs with no external setup or file management.

User recordable disks are made out of copper, and are written to directly via a Recorder. A custom Record Player block is used to play them, but support for the original Jukebox may be added at some point.

There are more ideas and thoughts in [the planning file](https://github.com/burgerguy/recordable/blob/1.18/planning.txt).

## Technical Stuff
On the server, Recorders are registered which collect all sounds broadcased nearby. The recorded sounds, their volumes, and the ticks they were played on are all formatted in a custom binary format to take up the least space, and the entries are stored in a LMDB database on the server.

Each "score" (fancy musical term for a list of notes) has its own ID represented as a long, and each Copper Record that isn't blank will have one of these in its NBT.

The server has Broadcasters, which will strategically send out play packets to nearby players, allowing the client to take most of the control in the actual playing process, avoiding potential lag. If a client doesn't have the score data for a particular score ID when requested, it can follow up with the server to query its database, and then send back to the client so it can keep it in its cache.

The client also has Players, which play back the scores locally in the correct positions in relation to the position to keep playback smooth and accurate.

## Setup

For setup instructions please see the [fabric wiki page](https://fabricmc.net/wiki/tutorial:setup) that relates to the IDE that you are using.
