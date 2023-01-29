# Fishing Twitch Bot

FishingTwitchBot is an offline chat bot that enables users to have fun in offline chat via fishing.
It is completely developed in Koltin using the following libraries.

- Twitch4J
- Exposed(JetBrains)


Currently being hosted on JVLIA's offline chat (http://twitch.tv/JVLIA)
## Commands
Users can type to following commands:

- !fish -> Just shows the user to list of commands
- !fish top -> Displays the top 3 catches in a the current chat room
- !fish count -> Displays the number of total fish caught in the current chat room
- !opensource -> Displays a link to this github repository
- fishing - Attempts to fish

## Installation

To use this bot in your own channel and to host it yourself, you will need to create a new enum file or somewhere to store the following
- Client_ID -> See Twitch Dev Docs to obtain this
- Client_Secret -> Again see Twitch Dev Docs to obtain this
- OAUTH -> you oauth key using the scopes chat:read and chat:edit
- Database IP -> the IP of your database of choice (this is currently using a postgres database
- Database User -> the user doing all the operations in the database
- Database PWD -> the password of said user

The following lines reference those keys:
- Main.kt -> Lines 59, 68, 69
- OnMessage -> Lines 24

You will also want to change the channel the bot will join.
- Main.kt -> Line 15

For the cleanest and best look in chat please enable the folowing emotes from 7TV:
- Fishing -> https://7tv.app/emotes/63951ebf144f167f6a725f37
- Sadge -> https://7tv.app/emotes/603cac391cd55c0014d989be
- PogU -> https://7tv.app/emotes/60ae2376b2ecb015058f4aa7

Build via Gradle into jar file.

## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

Please make sure to update tests as appropriate.

## Contact Me

If you have any questions or concerns, want to know how it works, want me to explain something to you, PLEASE contact me on discord. TheRedSRT4#9652
or via email admin@theredsrt4.com